# Verification status

## Последний подтверждённый CI

- Кодовый срез: [`e12002a`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/e12002acbdc1b8a76b13e86cd455a92bab72e823).
- [GitHub Actions run #151](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40183873648) прошёл: подготовка закреплённого хоста, host build/publish, проверка локального Maven, сборка аддона и оба прогона GameTest.
- С `no_lust=false`: 89/89 required GameTests passed; `luxuria=luxuria`.
- С `no_lust=true`: 89/89 required GameTests passed; `luxuria=null`.

## Проверки, требующие отдельного подтверждения

- Коммит [`995bc5e`](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/995bc5eb1e8b047add1cadfaa3d5efb1cc593fa4) добавил `tools/check_provenance.py` в CI. Для него запущен [run #152](https://github.com/MxxFoxx2/forbidden-magic-reforged/actions/runs/40184089634); результат нужно считать подтверждённым только после завершения job.
- `tools/gen_resources.py` дважды подряд и чистый diff после генерации пока не являются шагом CI; это остаётся pre-release проверкой.
- `runClient` не headless: нужно проверить Morph Tools tint, Creative Wand family, Tainted Log во всех ориентациях, Wrath Cage render, исследования/рецепты.
- Полноценная игровая проверка всё ещё нужна для боевых hooks жезлов, дропов осколков, Collar of Pain и Riding Crop.

## Статус паритета

Паритет 26.3 не объявлен завершённым. Текущие два API-зависимых расхождения и критерии закрытия — в [roadmap](roadmap.md); подтверждённые отличия и приближения — в [fidelity](fidelity.md). Исторические журналы по отдельным этапам удалены, потому что их результаты относятся к старым HEAD.