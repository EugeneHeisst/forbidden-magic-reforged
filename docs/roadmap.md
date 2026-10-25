# Roadmap

Основной таргет — Minecraft 26.3. `main` — каноническая ветка. Подтверждённая реализация описывается в [fidelity ledger](fidelity.md), актуальный результат CI — в [verification](verification.md). Здесь остаются только незакрытые требования и критерии завершения.

## Неразрешённый вопрос аудита (не подтверждённый parity gap)

**Участие FM-taint blocks в host taint graph** пока нельзя объявлять доказанным расхождением. В оригинальном исходнике [`BlockLogTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLogTainted.java) получает `Config.taintMaterial`, а [`BlockLeavesTainted`](https://github.com/SpitefulFox/ForbiddenMagic/blob/master/src/main/java/fox/spiteful/forbidden/blocks/BlockLeavesTainted.java) реализует собственный decay-алгоритм, который распознаёт `taintLog` как опору; отдельной регистрации этих FM-блоков как источников скверны в исходнике не найдено. Современный `TaintSpread.isTaintBlock` действительно проверяет фиксированный список блоков Thaumcraft, но одного этого недостаточно, чтобы заключить, что оригинал ожидал иной результат. Не добавлять новый host hook, пока сравнительная проверка конкретного исходного поведения не докажет пропуск. Существующий GameTest подтверждает только то, что проверенные материалы FM не заменяются в протестированных путях.

На текущий момент этого аудита не осталось подтверждённых source-level core-функциональных блокеров. Завершение core parity всё ещё зависит от ручных проверок в клиенте и полноценного gameplay-гейта ниже.

## Уже устранено и проверено

- **Aspect scan Nether Portal:** локальный host patch [`0005-block-aspect-targets.patch`](../patches/0005-block-aspect-targets.patch) добавляет регистрацию аспектов на блоки и синхронизацию block targets. `FmAspectTags` назначает NETHER 4 современному блоку `minecraft:nether_portal`; GameTest проверяет фактическую цель `block:minecraft:nether_portal` и количество NETHER 4. Обсидиан больше не используется как прокси. [CI run #165](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40188998514) прошёл после обновления pinned host до `cf0c421`.
- **Alchemical Wand Cap и Equivalent Wand Rod:** восстановлены исходные параметры (discount 0.9, WATER discount 0.8, cost 7; capacity 100, cost 12), отдельные компоненты/текстуры и Equivalent creative wand с void cap. [CI run #163](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40187253872) прошёл сборку, provenance и 92 GameTest в обоих режимах.
- Скверное дерево/саженец восстановлены по оригинальному генератору; headless-тесты покрывают стадии, форму кроны и поддержку листвы. Визуальная проверка в клиенте остаётся открытой.
- **Upstream обновлён:** `host.sha` передвинут с `69a47a1` на актуальный `cf0c421` (11 upstream-коммитов). В частности, автор закрыл [issue #38](https://github.com/notlikethiss/thaumcraft-4-reforged/issues/38) коммитом [`cf0c421`](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/cf0c42199d01fdfcd99ee167224f7a1b4a4427a5): tainted-land worldgen больше не размещает деревья над водой; добавлены три host GameTest. Это исправление **мировой генерации деревьев Thaumcraft**, не логики нашего FM-саженца. На обновлённом host наш `0005` применяется, host и addon собираются, 92/92 addon GameTest проходят в обоих режимах; см. [CI #165](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40188998514).

[Host PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) на момент проверки остаётся открытым **Draft**. Он добавляет регистрацию аспектов аддонов и слияние аспектов сущностей; taint recognition API в нём не заявлен. Block aspect targets пока поставляются нашим локальным патчем `0005`; при обновлении upstream нужно проверять его применимость и сохранение теста портала.

## Незакрытые ручные проверки

- `runClient`: tint Morph Tools; Creative Wand family — модели, подсказки, creative tab и пополнение vis.
- `runClient`: Tainted Log во всех трёх ориентациях; Taint Sapling обеих стадий, форма кроны и decay support листьев; Wrath Cage render и обновление моба после смены кристалла.
- Реальная игровая проверка hooks урона жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Проверить в клиенте Alchemical Cap / Equivalent Rod: предметные и wand-модели, подсказки и Equivalent creative wand.

Headless CI не заменяет визуальный и игровой гейт.

## Ограничения, не выдаваемые за баги

- Старый Extreme Hills сопоставлен с `minecraft:windswept_hills`; точного аналога нет.
- Mob Crystal хранит современный namespaced entity ID; миграция миров 1.7.10 не заявлена.
- Blood Magic, Botania, Ars Magica 2 и CoFH не поддерживаются на 26.3 из-за отсутствия согласованных совместимых зависимостей. Без них нельзя заявлять полный паритет интеграций оригинала; это отдельный scope вне core parity.
- 1.21.1 начинается только после закрытия основного гейта 26.3.

## Гейт завершения core parity 26.3

- Нет неподтверждённых предположений, выдаваемых за баги; оставшиеся функциональные отличия доказаны исходным кодом и отражены в `docs/fidelity.md`.
- Текущий CI зелёный: host prepare/build/publish, сборка аддона, provenance и GameTests с `no_lust` в обоих режимах.
- Завершён `runClient` и записаны результаты всех ручных пунктов выше.
- Все реальные отклонения core поведения отражены в единственном [fidelity ledger](fidelity.md); отсутствующие внешние интеграции не выдаются за полный parity.
