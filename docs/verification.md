# Verification status

## Последний успешный runtime CI

- Проверенный кодовый срез: [`05815c4`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/05815c43d1b190f7f9384a60832f0964a4fd5bf1).
- [GitHub Actions run #165](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40188998514) завершился успешно после обновления pinned host.
- `host.sha` закреплён на upstream `cf0c42199d01fdfcd99ee167224f7a1b4a4427a5` вместо прежнего `69a47a1`. Сборка скачивает этот upstream snapshot и применяет локальные patch-файлы; `0005-block-aspect-targets.patch` применился чисто.
- CI успешно выполнил host `build publish`, проверку локального Maven, сборку аддона и `tools/check_provenance.py` (`provenance ok: 1 jar(s)`).
- С `no_lust=false`: **92/92** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **92/92** required GameTests passed; `luxuria=null`.
- GameTest `alchemical_cap_and_equivalent_rod_match_original` проверяет параметры двух wand parts, их модельные текстуры и Equivalent creative wand. `nether_portal_has_direct_block_aspect` проверяет сканирование `minecraft:nether_portal` с NETHER 4.
- Обновление upstream включает 11 коммитов со времени прежнего pinned SHA. [Issue #38](https://github.com/notlikethiss/thaumcraft-4-reforged/issues/38) закрыт upstream-коммитом [`cf0c421`](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/cf0c42199d01fdfcd99ee167224f7a1b4a4427a5), который исправляет worldgen деревьев на воде и добавляет host-тесты.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации — pre-release проверка.
- `runClient` не headless: проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели/подсказки в клиенте пока не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity](fidelity.md); полный parity внешних интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
