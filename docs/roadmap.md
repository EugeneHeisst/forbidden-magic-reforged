# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Оставшиеся реальные разрывы паритета

1. **Блоки Forbidden Magic в системе скверны.** `TaintSpread.isTaintBlock` использует фиксированный список Thaumcraft и не предоставляет публичного API для добавления блоков аддона. GameTest доказывает, что материалы FM не заменяются в проверенных путях, но не доказывает, что host распознаёт их как taint blocks во всех механиках. Нужно расширяемое API/tag в хосте и интеграционные GameTest для распространения и преобразования.
2. **Aspect scan Nether Portal.** Оригинал назначает NETHER 4 блоку `minecraft:portal`; текущий API принимает item targets, поэтому обсидиан используется как прокси. Нужно публичное API регистрации block/state aspect targets, назначение аспекта порталу и GameTest сканирования.
3. **Alchemical Wand Cap и Equivalent Wand Rod.** Оригинал регистрирует их без зависимости от внешних модов: alchemical cap с базовым discount 0.9, WATER special discount 0.8 и craft cost 7; equivalent rod capacity 100 и craft cost 12, используется для creative wand с void cap. В текущем `FmWands` нет этих записей, в порте нет отдельного alchemical cap component item, а соответствующий equivalent rod/creative wand тоже отсутствует. Нужны современные item IDs, модели/текстуры, рецепты/исследования где применимо, регистрация wand parts и GameTest на параметры и создание жезла. Это core-parity gap, не исключённая интеграция.

[Host PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) открыт и готов к ревью (не Draft). Он добавляет регистрацию аспектов аддонов и слияние аспектов сущностей, но не добавляет block/state aspect targets и не расширяет распознавание taint blocks. Даже после его слияния два API-разрыва выше останутся незакрытыми без дополнительных изменений хоста.

## Незакрытые ручные проверки

- `runClient`: tint Morph Tools; Creative Wand family — модели, подсказки, creative tab и пополнение vis.
- `runClient`: Tainted Log во всех трёх ориентациях; Taint Sapling обеих стадий, форма кроны и decay support листьев; Wrath Cage render и обновление моба после смены кристалла.
- Реальная игровая проверка hooks урона жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Проверить, что исследования/рецепты вновь добавленных wand parts открываются в правильной последовательности и показываются в игре.

Headless CI не заменяет визуальный и игровой гейт.

## Ограничения, не выдаваемые за баги

- Старый Extreme Hills сопоставлен с `minecraft:windswept_hills`; точного аналога нет.
- Mob Crystal хранит современный namespaced entity ID; миграция миров 1.7.10 не заявлена.
- Blood Magic, Botania, Ars Magica 2 и CoFH не поддерживаются на 26.3 из-за отсутствия согласованных совместимых зависимостей. Без них нельзя заявлять полный паритет интеграций оригинала; это отдельный scope вне core parity.
- 1.21.1 начинается только после закрытия основного гейта 26.3.

## Гейт завершения core parity 26.3

- Все три core-разрыва выше устранены либо каждое оставшееся отличие явно одобрено и описано как компромисс в `docs/fidelity.md`.
- Текущий CI зелёный: host prepare/build/publish, сборка аддона, provenance и GameTests с `no_lust` в обоих режимах.
- Завершён `runClient` и записаны результаты всех ручных пунктов выше.
- Все реальные отклонения core поведения от оригинала отражены в единственном [fidelity ledger](fidelity.md); отсутствующие внешние интеграции не выдаются за полный parity.
