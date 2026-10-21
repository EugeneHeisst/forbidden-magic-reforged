# Upstream: патчи и установка без официального API

- Хост: `notlikethiss/thaumcraft-4-reforged`, ветка `main`, пин `69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf`
  (`host.sha`). Хост под All Rights Reserved; его jar проект не публикует.
- `patches/0001-aspect-registry.patch` — `Aspect` из enum в `final class` с реестром, `WandVis` switch →
  if, чтение индекса в `AspectOrb` через `byOrdinal`.
- `patches/0002-aspect-addon-init.patch` — `AddonAspects`, вызов в `AspectSync.rebuild`, `entityCopy`
  для слияния меток сущностей с базовыми.
- `patches/0003-host-publish.patch` — `maven-publish` и версия `1.2`; локальная инфраструктура сборки,
  в upstream не предлагается.

## Запрос автору

Черновик: `docs/upstream/issue-draft.md`. Отправляет пользователь, не проект. В запросе: измеренный
блокер (`Aspect` — закрытый enum), серия патчей, логи прогонов, участие ИИ, вопрос о том, не портирует
ли автор FM сам, и прецедент Thaumcraft 4 Reconstructed с data-driven аддон-аспектами.

## Если API принят

Удалить `0001`/`0002`, зависеть от релизного хоста с API. `0003` остаётся только локальной сборкой.

## Если API не принят или молчание

Патчер: пользователь применяет `patches/0001`-`0002` к своему клону хоста на пине, собирает хост
локально, публикует в свой maven и ставит собранный jar порта. Это персональная сборка; jar хоста
никуда не выкладывается. Порт против стокового хоста `1.1` не стартует по версии и по отсутствию API.
