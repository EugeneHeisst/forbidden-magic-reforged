# Host compatibility

Хост: [`notlikethiss/thaumcraft-4-reforged`](https://github.com/notlikethiss/thaumcraft-4-reforged). Исходный коммит закреплён в `host.sha`; лицензия хоста — All Rights Reserved. Не включай host-код или ассеты в JAR аддона и не распространяй собранный host JAR.

## Локальные патчи

- `patches/0001-aspect-registry.patch` — превращает закрытый `Aspect` enum в registry-backed class с сохранением встроенных ordinal.
- `patches/0002-aspect-addon-init.patch` — включает регистрацию аспектов аддонов и `entityCopy` для объединения entity aspects.
- `patches/0003-host-publish.patch` — только локальная публикация `thaumcraft:1.2` в Maven, необходимая для сборки аддона.
- `patches/0004-tools-aspect-scan.patch` — адаптирует генераторы хоста к `Aspect` как классу.

CI сначала подготавливает закреплённый host из этих патчей, собирает/публикует его в локальный Maven и только затем собирает аддон. Успешный CI не доказывает совместимость со стоковым опубликованным host JAR, пока нужный API не появится в релизе хоста.

## Запрос публичного API

[Host PR #37 — `feat: add public API for add-on aspect registration`](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37) смержен 2026-10-26 (`d75a946`), API выпущен в `1.1.2`. Он добавляет регистрацию аспектов аддонов и слияние аспектов сущностей.

Этот API сам по себе не закрывает два текущих parity gap: распознавание блоков Forbidden Magic системой скверны и аспектовую цель `minecraft:portal`. Они требуют отдельных публичных API хоста и интеграционных GameTest; см. [roadmap](roadmap.md).

Если upstream примет изменения, сначала закрепи новый host commit, проверь, какие части `0001`/`0002` заменены upstream, затем удали только дублирующиеся патчи и повтори полный CI. `0003` нужен лишь пока local Maven publish остаётся частью workflow; `0004` нужен, пока upstream-генераторы не поддержат текущую форму `Aspect`.