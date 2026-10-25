# Verification status

## Последний успешный runtime CI

- Проверенный кодовый срез: [`b670267`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/b670267fe430ad634eae67687c3eb6c14d4d6577).
- [GitHub Actions run #160](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40185916750) завершился успешно на этом SHA.
- CI подготовил закреплённый хост и применил все локальные патчи, выполнил host `build publish`, проверил локальный Maven, собрал аддон, прошёл `tools/check_provenance.py` (`provenance ok: 1 jar(s)`) и загрузил JAR.
- С `no_lust=false`: **91/91** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **91/91** required GameTests passed; `luxuria=null`.
- В run #160 проверен block-aspect host patch: GameTest подтверждает прямое сканирование `minecraft:nether_portal` с NETHER 4, вместо прокси через обсидиан.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации — pre-release проверка.
- `runClient` не headless: проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Wand Cap и Equivalent Wand Rod пока отсутствуют; после реализации требуются тесты параметров/создания wand и ручная проверка моделей/рецептов.

## Статус паритета

Core parity 26.3 **не завершён**. Остались два набора core-разрывов: интеграция FM-блоков с host taint system (после сверки исходного поведения) и Alchemical Wand Cap / Equivalent Wand Rod. Также не завершены client-side и полноценные игровые проверки. Полный parity всех внешних интеграций не заявляется.
