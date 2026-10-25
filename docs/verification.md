# Verification status

## Последний успешный runtime CI

- Проверенный кодовый срез: [`8c920a7`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/8c920a7a2a430a04dc89060172244ae1fd784ff9).
- [GitHub Actions run #157](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40185338238) завершился успешно на этом SHA.
- CI подготовил закреплённый хост, выполнил host `build publish`, проверил локальный Maven, собрал аддон, выполнил `tools/check_provenance.py` (`provenance ok: 1 jar(s)`) и загрузил JAR.
- С `no_lust=false`: **91/91** required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: **91/91** required GameTests passed; `luxuria=null`.
- Последующие документальные изменения описывают этот успешный кодовый срез; после изменения runtime-кода CI нужно запускать заново.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации — pre-release проверка.
- `runClient` не headless: проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Taint Sapling stages/canopy/leaf support, Wrath Cage render и исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.

## Статус паритета

Core parity 26.3 **не завершён**. Три группы открытых расхождений и критерии закрытия — в [roadmap](roadmap.md); подтверждённые отличия и приближения — в [fidelity](fidelity.md). Успешные GameTest не доказывают визуальную эквивалентность и не закрывают API-разрывы.
