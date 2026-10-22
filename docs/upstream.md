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

Payload: `docs/upstream/issue.md` (заголовок для поля Title + тело, готовое к вставке). Разбор, почему текст
выглядит именно так: `docs/upstream/issue-draft.md`. Отправляет пользователь, не проект.

В запросе: измеренный блокер (`Aspect` — закрытый enum, тогда как в TC4 4.2.3.5 это класс с публичной регистрацией
и Javadoc Azanor'а «Use this constructor to register your own aspects»), серия патчей файлами, прогоны
`check_seams.py` / `build` / `runGameTestServer` цифрами, собственная находка о том, что диф ломает три скрейпера
`Aspect.java` в `tools/`, участие ИИ и прецедент Thaumcraft 4 Reconstructed с data-driven аддон-аспектами.

Вопросов в тексте нет ни одного: по замеру автор не оставил в своём трекере ни одного комментария за 31 ишью, и
вопросительная форма означает молчание, а не отказ. Вопрос «не портирует ли он FM сам» — в Telegram
(`t.me/overseascode`, поле homepage репо), туда же, где он отвечал живьём.

Форма — issue с приложенными `.patch`, а не PR: CI хоста триггерится только на `push` в `main`/`mc/**` (события
`pull_request` нет), поэтому внешний PR приходит без единой проверки; единственный такой (#30) стоит нетронутым.
PR — только по его просьбе. Отдельного текста под PR нет: то же тело `docs/upstream/issue.md` идёт описанием, если
он попросит открыть draft PR.

## Четвёртый патч серии

`patches/0004-tools-aspect-scan.patch` (3 файла, +9 / −5) — правка трёх скрейперов `Aspect.java` в `tools/`
(`gen_modern_aspects.py`, `convert_research.py`, `convert_recipes.py`): на `final class` они не находили ни одного
имени аспекта. Это standalone-прогоны — `gen_resources.py` (1783 файла, exit 0) и CI их не зовут, — поэтому они не
ломали сборку, но ломали следующий ручной пересчёт конфигов. Проверено до/после: три падения → три exit 0.

## Проверки хоста

Все шесть из раздела «Проверки» в его `CLAUDE.md` прогнаны на копии с четырьмя патчами, и две из них — ещё раз на
чистой копии `69a47a1` как контроль:

| проверка | пропатченный хост | чистый хост |
|---|---|---|
| `./gradlew build` | BUILD SUCCESSFUL | — |
| `./gradlew runGameTestServer` | 256/256 | — |
| `check_seams.py` | 932 файла | — |
| `gen_resources.py` | 1783 файла, exit 0 | — |
| `inventory.py --check` | `check: ошибок нет` | вывод совпадает побайтово |
| `validate_ids.py --mc 26.3` | `0 problem(s)` | `0 problem(s)` |

Что для этого пришлось развернуть: `tools/fetch_1710.py` (MC 1.7.10 + libraries, Forge userdev/universal,
MCP stable-12 + SRG, SpecialSource, Baubles), `tools/decompile.py tc4 mc1710` (Vineflower 1.12 — наш вендоренный
`tools/vineflower-1.12.0.jar`, тот же мажор, что у него; ремап 757/859, 297 остаточных SRG), modern-исходники в
`reference/mc` из `build/moddev/artifacts/minecraft-patched-*-sources.jar`, отчёты ванильного data generator для
`validate_ids`. Признаться: в тексте обращения я сначала написал «эти две требуют твоей машины» — не требуют,
требуют времени, и я это исправил.

Два известных окруженческих артефакта, не связанных с дифом: `decompile.py` и `validate_ids.py` захардкодили
пути `/opt/homebrew/opt/openjdk@25` (на Windows — обход вызовом своего JDK; у нас Temurin 25.0.4.1 из
`~/.gradle/jdks`), а генераторы пишут через `Path.write_text` и потому на Windows выдают CRLF — отсюда «изменённые»
1797 файлов `src/generated` при побайтовом совпадении содержимого.

## Если API принят

Удалить `0001`/`0002`, зависеть от релизного хоста с API. `0003` остаётся только локальной сборкой.

## Если API не принят или молчание

Патчер: пользователь применяет `patches/0001`-`0002` к своему клону хоста на пине, собирает хост
локально, публикует в свой maven и ставит собранный jar порта. Это персональная сборка; jar хоста
никуда не выкладывается. Порт против стокового хоста `1.1` не стартует по версии и по отсутствию API.
