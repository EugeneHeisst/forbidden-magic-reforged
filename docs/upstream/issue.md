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

**0004 — чтобы твои скрипты продолжали находить имена аспектов** (3 файла, +9 / −5)

- `tools/gen_modern_aspects.py`, `tools/convert_research.py`, `tools/convert_recipes.py` — чтение имён аспектов из
  обеих форм объявления. Подробности и прогон до/после ниже.

Если форма удобнее другая — данные в `data/<ns>/thaumcraft/aspects/*.json` вместо кода — готов обсудить; приложенный
диф — это то, что уже собрано и прогнано.

## Ограничение, которое стоит зафиксировать в доке

Аддон-аспект должен быть **составным**. Праймальный аспект попадает в `Aspect.getPrimalAspects()`, которое хост
использует как шесть vis-слотов жезла; проверено — такой аспект роняет 14 тестов инфузии и жезла. Это совпадает с
Javadoc оригинала про праймальный конструктор («making your own primal aspects will break all the things»), так что
правило стоит записать словами, а не оставлять на уровне падения.

## Verification

Пин `69a47a1` (HEAD `main`, тег `26.3`). Все шесть проверок из раздела «Проверки» в `CLAUDE.md` прогнаны на
копии дерева с применёнными патчами; `reference/` собран той же цепочкой, что и у тебя.

| команда | результат |
|---|---|
| `python3 tools/check_seams.py` | `seams ok: 932 files checked` |
| `./gradlew build` | `BUILD SUCCESSFUL`, ни одного предупреждения javac |
| `./gradlew runGameTestServer` | `All 256 required tests passed :)` |
| `python3 tools/gen_resources.py` | `generated 1783 files`, `phase 5 recipes: 37 written, 0 waiting for items` |
| `python3 tools/inventory.py --check` | `check: ошибок нет` (blocks 207/с id 200/skip 7, items 261/257, lang 411/411) |
| `python3 tools/validate_ids.py --mc 26.3 --reports-dir … --server-jar … --client-jar …` | `26.3: 0 problem(s)` |

Контроль: `inventory.py --check` и `validate_ids.py` прогнаны ещё и на **чистой** копии `69a47a1` — вывод
`inventory.py` совпадает побайтово, `validate_ids` даёт `0 problem(s)` там же. То есть диф не двигает ни инвентарь,
ни валидацию id.

Как развёрнуто `reference/`: `tools/fetch_1710.py` (MC 1.7.10 + libraries, Forge 10.13.4.1614 userdev/universal,
MCP stable-12 + SRG, SpecialSource, Baubles), затем `tools/decompile.py tc4 mc1710` (Vineflower 1.12, ремап
757/859 файлов, 297 остаточных SRG-имен — как в твоём `decompiled-unmapped.txt`), modern-исходники
`reference/mc` распакованы из `build/moddev/artifacts/minecraft-patched-26.3.0.57-beta-sources.jar`, отчёты для
`validate_ids` сняты `net.minecraft.data.Main --reports` на ванильных `server.jar`/`client.jar`.
Признак, что дерево воспроизведено верно: перегенерированные `ModernAspects.java`, `ConfigResearch.java`,
`ConfigRecipes.java` и `tc_recipes.json` не отличаются от закоммиченных по содержимому — расходятся только
переносы строк.

Про переносы строк отдельно: гейт «повторная генерация не должна менять файлы» на Windows не выполняется сам по
себе — генераторы пишут через `Path.write_text`, а это платформенный CRLF. К дифу это отношения не имеет.

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

## Что диф ломает, и как это чинит 0004

Три отдельных скрипта узнают имена аспектов, скрейпя `Aspect.java` регуляркой, и на `final class` не находят ничего.
Они **не** в графе `gen_resources.py` и не в CI — это ручные maintenance-прогоны (перевыгрузка оригинальных
конфигов и генерация аспектов для современного контента), поэтому сборка и тесты зелёные, а прогон любого из трёх
упирается в исключение:

| место | чем меряно | до | после (патч 0004) |
|---|---|---|---|
| `tools/gen_modern_aspects.py:12` | `^    ([A-Z_]+)\(` | `SystemExit: blocks.json: minecraft:deepslate: unknown aspect EARTH` | exit 0, `465 items, 90 entities -> ModernAspects.java` |
| `tools/convert_research.py:586` | `^    ([A-Z_]+)\(` | `ValueError: unknown aspect AURA` | exit 0 |
| `tools/convert_recipes.py:77-80` | якорь `text.index("public enum Aspect")` | `ValueError: substring not found` | exit 0, `unmapped: []` |

Правка — читать имена из обеих форм объявления, по две строки на файл:

```python
names = re.findall(r"^    ([A-Z_]+)\(", text, re.M) or re.findall(r"^    public static final Aspect ([A-Z_]+) = ", text, re.M)
```

а в `convert_recipes.py` вместо жёсткого якоря — ветка на класс. Это оформлено как `0004-tools-aspect-scan.patch`
и лежит рядом с 0001–0003; прогнано на собранном из четырёх патчей дереве, вывод перегенерированных
`ModernAspects.java` / `ConfigResearch.java` / `ConfigRecipes.java` / `tc_recipes.json` совпадает с закоммиченным по
содержимому.

Сразу оговорка, потому что я сам на этом попался: к сборке это не относится. `gen_resources.py` эти три скрипта не
импортирует и CI их не вызывает, поэтому `build` и `runGameTestServer` были зелёные и с слепыми скрейперами.

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

Патч-файлы приложены: `0001-aspect-registry.patch`, `0002-aspect-addon-init.patch`,
`0004-tools-aspect-scan.patch`.

Работа сделана с участием ИИ (агент), автор порта — MxxFoxx2; Forbidden Magic — SpitefulFox, Thaumcraft 4 — Azanor.
Если удобнее получить это веткой или PR — скажи, оформлю.
