# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Оставшиеся реальные разрывы паритета

1. **Блоки Forbidden Magic в системе скверны.** `TaintSpread.isTaintBlock` использует фиксированный список Thaumcraft и не предоставляет публичного API для добавления блоков аддона. GameTest подтверждает, что FM-taint материалы не заменяются в проверенных путях, но это не доказывает, что хост распознаёт эти блоки как taint blocks во всех механиках. Перед изменениями нужно окончательно сверить ожидаемое поведение по оригиналу; если FM-блоки должны участвовать в host taint graph, добавить API/tag и интеграционные GameTest.
2. **Alchemical Wand Cap и Equivalent Wand Rod.** Оригинал регистрирует их без зависимости от внешних модов: alchemical cap с базовым discount 0.9, WATER special discount 0.8 и craft cost 7; equivalent rod capacity 100 и craft cost 12, используется для creative wand с void cap. В текущем `FmWands` нет этих записей, в порте нет отдельного alchemical cap component item и эквивалентного creative wand. Нужны современные item IDs, модели/текстуры, регистрация wand parts, корректные рецепты/исследования и GameTest параметров и сборки жезла. Это core-parity gap, не исключённая интеграция.

## Уже устранено и проверено

- **Aspect scan Nether Portal:** локальный host patch [`0005-block-aspect-targets.patch`](../patches/0005-block-aspect-targets.patch) добавил регистрацию аспектов на блоки и синхронизацию block targets. `FmAspectTags` назначает NETHER 4 современному блоку `minecraft:nether_portal`; GameTest вызывает `ScanManager.resolveBlock` и проверяет цель `block:minecraft:nether_portal` и количество NETHER 4. Обсидиан больше не используется как прокси. [CI run #160](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40185916750) прошёл на кодовом срезе `b670267`.
- Скверное дерево/саженец восстановлены по оригинальному генератору; headless-тесты покрывают стадии, форму кроны и поддержку листвы. Визуальная проверка в клиенте остаётся открытой.

[Host PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) открыт и готов к ревью (не Draft). Он добавляет регистрацию аспектов аддонов и слияние аспектов сущностей; taint recognition API в нём не заявлен. Block aspect targets сейчас реализованы локальным патчем `0005`; при последующем обновлении pinned host необходимо проверить совместимость патча с upstream.

## Незакрытые ручные проверки

- `runClient`: tint Morph Tools; Creative Wand family — модели, подсказки, creative tab и пополнение vis.
- `runClient`: Tainted Log во всех трёх ориентациях; Taint Sapling обеих стадий, форма кроны и decay support листьев; Wrath Cage render и обновление моба после смены кристалла.
- Реальная игровая проверка hooks урона жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Проверить в игре новые Alchemical Cap / Equivalent Rod после реализации: рецепты, исследования, компоненты и creative wand.

Headless CI не заменяет визуальный и игровой гейт.

## Ограничения, не выдаваемые за баги

- Старый Extreme Hills сопоставлен с `minecraft:windswept_hills`; точного аналога нет.
- Mob Crystal хранит современный namespaced entity ID; миграция миров 1.7.10 не заявлена.
- Blood Magic, Botania, Ars Magica 2 и CoFH не поддерживаются на 26.3 из-за отсутствия согласованных совместимых зависимостей. Без них нельзя заявлять полный паритет интеграций оригинала; это отдельный scope вне core parity.
- 1.21.1 начинается только после закрытия основного гейта 26.3.

## Гейт завершения core parity 26.3

- Оба оставшихся core-разрыва выше устранены либо каждое отклонение явно одобрено и описано как компромисс в `docs/fidelity.md`.
- Текущий CI зелёный: host prepare/build/publish, сборка аддона, provenance и GameTests с `no_lust` в обоих режимах.
- Завершён `runClient` и записаны результаты всех ручных пунктов выше.
- Все реальные отклонения core поведения отражены в единственном [fidelity ledger](fidelity.md); отсутствующие внешние интеграции не выдаются за полный parity.
