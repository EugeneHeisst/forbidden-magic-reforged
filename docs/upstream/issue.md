# Заголовок (вставить в поле Title)

```
Aspect: в TC4 класс с регистрацией аддон-аспектов, в порте enum (шов + 2 патча, build/seams/GameTest зелёные)
```

---

# Тело issue (всё, что ниже, — готовый текст)

## Summary

В TC4 4.2.3.5 класс `Aspect` — расширяемый, и регистрация сторонних аспектов была публичным API Azanor'а, а не
хаком: конструктор снабжён Javadoc «Use this constructor to register your own aspects.», а повторяющийся тег
бросает `IllegalArgumentException("<tag> already registered!")`.

В порте `Aspect` — `enum` из 48 констант. Добавить аспект из аддона нельзя: Java не позволяет добавить константу
enum извне. Прошу вернуться к оригинальному поведению — превратить `Aspect` в `final class` со статическими
константами и реестром, и открыть один хук регистрации для аддонов.

Публичный облик не меняется: `Aspect.FIRE` остаётся статическим полем, `values()`, `byTag`, `getBlend()`,
`getChatcolor()`, `Codec`/`StringRepresentable` — как были.

## Key Changes

**0001 — `Aspect` enum → реестр** (3 файла, +173 / −96)

- `aspect/Aspect.java`: `public final class Aspect implements StringRepresentable`, 48 встроенных аспектов как
  `public static final`, реестр `LinkedHashMap` по тегу, `register` / `values` / `byTag` / `byOrdinal` /
  `builtinCount`, `CODEC` по тегу. Имена, порядок и значения `blend` (включая `771` у `ENTROPY`/`VOID`) сохранены.
- `wand/WandVis.java`: два `switch (aspect)` (`:32 get`, `:44 with`) переписаны — это единственные switch по аспекту
  в `src/main/java` (`EntityAspects.java:56` — по байту, `TcRecipeAspects.java:83` — по типу рецепта).
- `entity/AspectOrb.java`: `getAspect()` читает индекс через `byOrdinal` с фолбэком на `AIR`.

**0002 — хук для аддонов** (4 файла, +60 / −0)

- `aspect/AddonAspects.java` — новый класс, ровно в духе раздела «API для аддонов» в `CLAUDE.md`:
  `register(tag, color, components, blend, texture)` + `addRegistrar(...)`; идемпотентен, работает на клиенте и
  на сервере.
- `aspect/AspectRegistrar.java`, `aspect/AspectRegistry.java`, `aspect/AspectSync.java` — вызов в `rebuild` после
  `ConfigAspects`, до `registerDerived` (там же, где уже живёт `addRegistrar`).

Если форма удобнее другая — данные в `data/<ns>/thaumcraft/aspects/*.json` вместо кода — готов обсудить; приложенный
диф — это то, что уже собрано и прогнано.

## Ограничение, которое стоит зафиксировать в доке

Аддон-аспект должен быть **составным**. Праймальный аспект попадает в `Aspect.getPrimalAspects()`, которое хост
использует как шесть vis-слотов жезла; проверено — такой аспект роняет 14 тестов инфузии и жезла. Это совпадает с
Javadoc оригинала про праймальный конструктор («making your own primal aspects will break all the things»), так что
правило стоит записать словами, а не оставлять на уровне падения.

## Verification

Пин `69a47a1` (HEAD `main`, тег `26.3`), патчи 0001+0002 применены к копии дерева:

| команда | результат |
|---|---|
| `python3 tools/check_seams.py` | `seams ok: 932 files checked` |
| `./gradlew build` | `BUILD SUCCESSFUL`, ни одного предупреждения javac |
| `./gradlew runGameTestServer` | `All 256 required tests passed :)` |

**Не прогонял** (у меня нет `reference/decompiled` и отчётов ванильного datagen, а тулчейн — Windows):
`validate_ids.py --mc 26.3`, `inventory.py --check`. Прогони, пожалуйста, у себя — диф ни `tools/`, ни id не трогает,
но гарантировать их по своей машине не могу.

А `gen_resources.py` я прогнать мог — и он **падает**. Отдельный раздел ниже, потому что это единственное, что
диф ломает по-настоящему.

Что сверено статически по всей `src/main/java`: **1830 строк, содержащих 4118 обращений** вида
`Aspect.<КОНСТАНТА>`, не меняют текста (`Aspect.FIRE` остаётся валидным статическим полем `final class`); 19 мест
`Aspect.values()` работают через сохранённый метод; `Aspect.valueOf`, `isEnum`, границ вида `<E extends Enum<...>>`
для аспекта нет; `AspectList` ключует аспекты в `LinkedHashMap` (по ссылке, не по ординалу).

`ordinal()` уходит за сеть в пяти местах. В `AspectOrb` — четыре строки: `:41` и `:132` пишут `aspect.ordinal()`,
`:54` задаёт дефолт синк-данных `Aspect.AIR.ordinal()`, `:60` читает `values()[id]` (единственная из четырёх,
где диф меняет код — на `byOrdinal` с фолбэком на `AIR`). Плюс стол деконструкции:
`menu/DeconstructionTableMenu.java:55` отдаёт в `ContainerData` значение `ordinal() + 1`, а `:87` на клиенте
раскодует его как `Aspect.values()[value - 1]`. Все пять работают через сохранённый API. Из них следует требование
к порядку: аддон обязан зарегистрировать свои аспекты из конструктора мода, чтобы клиент построил их до
декодирования пакета; вызов `AddonAspects.init()` в `rebuild` — это порядок на сервере, а не замена конструктору.
Если хочешь, могу вынести это в Javadoc `AddonAspects`.

## Что диф ломает, и это надо починить в нём же

Три питоновских генератора узнают имена аспектов, скрейпя `Aspect.java` регуляркой, и на `final class` они
не находят ничего:

| место | чем меряно | стоковый enum | пропатченный класс |
|---|---|---|---|
| `tools/gen_modern_aspects.py:12` | `^    ([A-Z_]+)\(` | 48 имён | **0**, и следом `SystemExit: blocks.json: minecraft:deepslate: unknown aspect EARTH` |
| `tools/convert_research.py:586` | `^    ([A-Z_]+)\(` | 48 имён | **0** |
| `tools/convert_recipes.py:77-80` | `text.index("public enum Aspect")` | 48 имён | **`ValueError: substring not found`** |

То есть `build`, `check_seams.py` и `runGameTestServer` зелёные — потому что CI не вызывает генераторы, — а
твоя же цепочка «после правки lang: `convert_assets.py`, затем `gen_resources.py`» падает на первом шаге. Я это
проглядел, пока не прогнал `gen_modern_aspects.py` на пропатченной копии.

Правка на несколько строк в каждом из трёх файлов: читать имена из обеих форм объявления, например

```python
names = re.findall(r"^    ([A-Z_]+)\(", text, re.M) or re.findall(r"^    public static final Aspect ([A-Z_]+) = ", text, re.M)
```

плюс в `convert_recipes.py` заменить якорь `"public enum Aspect"` на tolerant-поиск. Скажи, форма приемлемая —
и я приложу это четвёртым патчем к тем же двум, с прогоном `gen_modern_aspects.py`, `convert_research.py` и
`convert_recipes.py` до/после.

## Стоимость для тебя

- Регистрируемых аспектов хоста диф не добавляет: 48 как были. Меняется только возможность зарегистрировать новый
  извне.
- Бэкпорт по твоей же процедуре (`tools/backport/rewrite.py --target 1.21.1`), ничего отдельно для `mc/1.21.1` и
  `mc/1.19.2` не прошу.
- В `CLAUDE.md` это одна строка в существующем разделе «API для аддонов».
- Стиль соблюдён: 4 пробела, комментарии в коде отсутствуют (проверено по патчам), коммит `feat:` на английском, без
  соавторств.

## Что не входит в предложение

Локальная публикация в maven и bump `mod_version` до `1.2` — plumbing моей сборки, в апстрим не предлагаю. Номер
следующего релиза из `gradle.properties` ты выставишь сам.

## Контекст

Порт Forbidden Magic 0.574 на Reforged: <https://github.com/MxxFoxx2/forbidden-magic-reforged>. Аддон уже зависит от
`thaumcraft >= 1.2`, поэтому твой следующий релиз сам его разблокирует; пока API нет, аддон отказывается грузиться с
сообщением «requires Thaumcraft 4: Reforged 1.2 or newer with the add-on aspect API», а не падает в
`NoSuchMethodError`. Семь аспектов FM — `luxuria`, `infernus`, `superbia`, `gula`, `invidia`, `desidia`, `ira` — все
составные, создаются ровно тем же вызовом, что и в 1.7.10 (`DarkAspects.java`).

Соседний порт, Thaumcraft 4 Reconstructed, возит аддон-аспекты как данные, и на этой точке расширения стоит линейка
из 32 аддонов — спрос за пределами этого репо уже измерен.

Патч-файлы приложены: `0001-aspect-registry.patch`, `0002-aspect-addon-init.patch`.

Работа сделана с участием ИИ (агент), автор порта — MxxFoxx2; Forbidden Magic — SpitefulFox, Thaumcraft 4 — Azanor.
Если удобнее получить это веткой или PR — скажи, оформлю.
