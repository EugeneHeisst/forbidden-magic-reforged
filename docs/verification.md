# Verification status

## Последний успешный runtime CI

- Проверенный коммит: [`a5f0c015`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/a5f0c01531b3c90f80f00957d4832a1a0ec504a9).
- [Java CI with Gradle #40368491928](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40368491928) завершился успешно 26 октября 2026 года.
- Хост закреплён на upstream `5ea7ba3cb4753f06cbf93c7bb83bab9afbd99755` (1.1.2); host patch подготовлен через `tools/prepare_host.py`, сборка использует Gradle composite build вместо локального Maven publish.
- Сборка хоста, сборка аддона и проверка provenance завершились успешно.
- С `no_lust=false`: **94/94** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **94/94** required GameTests passed; `luxuria=null`.
- Опубликованный релиз: [0.1.1](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/0.1.1). Сборка и тесты headless не заменяют ручные проверки клиента и реального геймплея.

## Проверки вне CI

- `tools/gen_resources.py` запускался дважды с чистым diff после генерации в pre-release проверке.
- `runClient` ещё должен подтвердить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели и подсказки в клиенте не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity ledger](fidelity.md); полный parity интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
