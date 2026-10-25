# Verification status

## Последний успешный runtime CI

- Проверенный кодовый срез: [`13e088e`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/13e088efe1478cc241d50121ef4684d2b1bc1c5c).
- [Beta release workflow #5](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40205532603) завершился успешно и опубликовал [`0.1.0-beta.1`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/0.1.0-beta.1) как prerelease.
- `host.sha` закреплён на upstream `cf0c42199d01fdfcd99ee167224f7a1b4a4427a5`; host patches применены через `tools/prepare_host.py`.
- CI успешно выполнил host `build publish`, проверку локального Maven, сборку аддона и `tools/check_provenance.py`.
- С `no_lust=false`: **92/92** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **92/92** required GameTests passed; `luxuria=null`.
- JAR: [`forbidden-magic-0.574-26.3-port.0.1.0-beta.1.jar`](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/download/0.1.0-beta.1/forbidden-magic-0.574-26.3-port.0.1.0-beta.1.jar).
- Это экспериментальная beta: headless CI не заменяет ручные проверки клиента и реального геймплея; публичный JAR не совместим со стоковым host, пока требуемые API/patches не появятся в upstream-релизе.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации — pre-release проверка.
- `runClient` не headless: проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.
- Alchemical Cap и Equivalent Rod прошли headless-проверку, но визуальные модели/подсказки в клиенте пока не подтверждены.

## Статус паритета

Core parity 26.3 **не объявлен завершённым**: остаются ручные визуальные и игровые проверки. Вопрос о распознавании FM-taint blocks остаётся предметом сверки оригинального поведения, а не подтверждённым API-блокером. Отклонения и приближения перечислены в [fidelity](fidelity.md); полный parity внешних интеграций Blood Magic/Botania/Ars Magica 2/CoFH не заявляется.
