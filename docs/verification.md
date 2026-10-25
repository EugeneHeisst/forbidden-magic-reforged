# Verification status

## Последний успешный CI

- Кодовый срез: [`4d043b0`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/4d043b0ce2442e9ed976f218dd38de7b430dd7d4).
- [GitHub Actions run #153](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40184394296) завершился успешно.
- CI подготовил закреплённый хост, выполнил host `build publish`, проверил локальный Maven, собрал аддон, выполнил `tools/check_provenance.py` (`provenance ok: 1 jar(s)`) и загрузил JAR.
- С `no_lust=false`: 89/89 required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: 89/89 required GameTests passed; `luxuria=null`.

## Проверки вне CI

- `tools/gen_resources.py` два раза подряд с чистым diff после генерации остаётся pre-release проверкой.
- `runClient` не headless: нужно проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Wrath Cage render, исследования и рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.

## Статус паритета

Паритет 26.3 не объявлен завершённым. Два API-зависимых расхождения и критерии закрытия — в [roadmap](roadmap.md); подтверждённые отличия и приближения — в [fidelity](fidelity.md). Исторические журналы отдельных этапов удалены: их результаты относятся к старым HEAD.