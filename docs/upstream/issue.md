# Заголовок (вставить в поле Title)

```
Aspect: в TC4 класс с регистрацией аддон-аспектов, в порте enum (шов + 3 патча, проверки зелёные)
```

---

# Тело issue (всё, что ниже, — готовый текст)

## Summary

В TC4 4.2.3.5 `Aspect` — класс, а регистрация сторонних аспектов была публичным API Azanor'а: конструктор
сопровождается Javadoc «Use this constructor to register your own aspects.», повторный тег бросает
`IllegalArgumentException("<tag> already registered!")`. В порте `Aspect` — `enum` из 48 констант, и добавить
аспект из аддона нельзя. Диф возвращает оригинальное поведение и открывает один хук регистрации; публичный облик
не меняется: `Aspect.FIRE`, `values()`, `getBlend()`, `Codec` остаются как были.

## Key Changes

**0001 — `Aspect` enum → реестр** (3 файла, +173 / −96)

- `aspect/Aspect.java`: `public final class Aspect implements StringRepresentable`; 48 встроенных аспектов как
  `public static final` в исходном порядке, реестр по тегу, `register` / `values` / `byTag` / `byOrdinal` /
  `builtinCount`; `blend` (включая `771` у `ENTROPY`/`VOID`) сохранён.
- `wand/WandVis.java`: два `switch (aspect)` (`:32 get`, `:44 with`) переписаны — других switch по аспекту в
  `src/main/java` нет.
- `entity/AspectOrb.java`: `getAspect()` читает индекс через `byOrdinal` с фолбэком на `AIR`.

**0002 — хук для аддонов** (4 файла, +60 / −0)

- `aspect/AddonAspects.java`: `register(tag, color, components, blend, texture)` + `addRegistrar(...)`;
  идемпотентен, работает на клиенте и сервере; аддон обязан вызвать его из конструктора мода, чтобы клиент
  построил аспекты до декодирования пакетов.
- `aspect/AspectRegistrar.java`, `aspect/AspectRegistry.java`, `aspect/AspectSync.java`: вызов в `rebuild` после
  `ConfigAspects`, до `registerDerived` — там же, где уже живёт `addRegistrar`.

**0004 — сканеры имён аспектов в `tools/`** (3 файла, +9 / −5)

- `tools/gen_modern_aspects.py`, `tools/convert_research.py`, `tools/convert_recipes.py`: имена читаются из обеих
  форм объявления; без этого три ручных пересчёта дают пустой словарь имён и падают.

## Verification

Пин `69a47a1`, копия дерева с применёнными патчами; две последние строки продублированы на чистой копии `69a47a1`
как контроль.

| команда | результат |
|---|---|
| `python3 tools/check_seams.py` | `seams ok: 932 files checked` |
| `./gradlew build` | `BUILD SUCCESSFUL`, без предупреждений javac |
| `./gradlew runGameTestServer` | `All 256 required tests passed :)` |
| `python3 tools/gen_resources.py` | `generated 1783 files` |
| `python3 tools/inventory.py --check` | `check: ошибок нет`, побайтово совпадает с чистой копией |
| `python3 tools/validate_ids.py --mc 26.3 …` | `0 problem(s)` и на чистой копии `0 problem(s)` |

## Notes

- Аддон-аспект должен быть составным: праймальный попадает в `getPrimalAspects()`, который хост использует как
  шесть vis-слотов жезла, и роняет 14 тестов инфузии и жезла. Это и так записано у Azanor'а в Javadoc
  праймального конструктора.
- `0003` (локальная публикация в maven и `mod_version 1.2`) в апстрим не предлагается.
- Приложены `0001-aspect-registry.patch`, `0002-aspect-addon-init.patch`, `0004-tools-aspect-scan.patch`.
- Порт Forbidden Magic 0.574 на Reforged: <https://github.com/MxxFoxx2/forbidden-magic-reforged>. Работа сделана с
  участием ИИ; автор порта — MxxFoxx2.
