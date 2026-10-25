# Verification status

## Последний успешный runtime CI

- Проверенный кодовый срез: [`256f189`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/256f189ae3f300aeb7f78dfc3757106c0bf7b603).
- [GitHub Actions run #163](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40187253872) завершился успешно.
- CI подготовил закреплённый хост и применил все локальные патчи, выполнил host `build publish`, проверил локальный Maven, собрал аддон, выполнил `tools/check_provenance.py` (`provenance ok: 1 jar(s)`) и загрузил JAR.
- С `no_lust=false`: **92/92** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **92/92** required GameTests passed; `luxuria=null`.
- GameTest `alchemical_cap_and_equivalent_rod_match_original` проверяет параметры обоих wand parts, оригинальные текстуры и Equivalent creative wand. GameTest `nether_portal_has_direct_block_aspect` проверяет прямое сканирование `minecraft:nether_portal` с NETHER 4 вместо прокси через обсидиан.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации — pre-release проверка.
- `runClient` не headless: проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели/подсказки в клиенте пока не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks рассматривается как незавершённая сверка ожидаемого поведения, а не как подтверждённый API-блокер. Отклонения и приближения перечислены в [fidelity](fidelity.md); полный parity внешних интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
