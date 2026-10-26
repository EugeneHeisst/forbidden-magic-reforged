# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) на Thaumcraft 4: Reforged 1.1.2 для Minecraft 26.3. Переносит семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

## Статус

- Основной таргет — Minecraft 26.3 / Java 25. `main` остаётся канонической веткой; полный паритет пока не объявлен.
- Проверенные отличия записываются в [fidelity ledger](docs/fidelity.md), оставшиеся задачи — в [roadmap](docs/roadmap.md).
- Опубликован релиз [0.1.1](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/0.1.1). До закрытия паритета остаются ручные проверки клиента и реального геймплея.
- Публичный API аспектов аддонов влит в апстрим ([Thaumcraft PR #37](https://github.com/notlikethiss/thaumcraft-4-reforged/pull/37), коммит [d75a946](https://github.com/notlikethiss/thaumcraft-4-reforged/commit/d75a94602100595f48bf31debab0f26c5ad521e2), релиз хоста 1.1.2). Реестр и lifecycle-хук аспектов больше не требуют локальных патчей.
- Для локальной проверки прямого аспекта блока портала нужен один дополнительный host patch: [0001-block-aspect-targets.patch](patches/0001-block-aspect-targets.patch). Без него аддон работает со стоковым хостом 1.1.2+, но портал не получает собственную block-цель сканирования.
- Ветка Minecraft 1.21.1 переведена на upstream API и Gradle composite build в [PR #44](https://github.com/MxxFoxx2/forbidden-magic-reforged/pull/44), слитом коммитом [88e6329](https://github.com/MxxFoxx2/forbidden-magic-reforged/commit/88e6329f44ff15824b68bfd9b7481e2c0c2aacf8). Это миграция инфраструктуры/API, а не заявление о завершённом content parity для 1.21.1.

## Установка

Для регистрации аспектов аддонов требуется Thaumcraft 4: Reforged 1.1.2 или новее с upstream API. Из-за лицензии хоста All Rights Reserved этот репозиторий не распространяет его JAR. Отдельная поддержка прямой block-target для портала пока остаётся локальной разработческой модификацией хоста.

## Сборка разработчика

Нужны Git, Python 3, JDK 25 и доступ к репозиториям. Клонируй хост рядом с этим репозиторием, как ожидает `tools/prepare_host.py`:

```powershell
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
Push-Location host
.\gradlew.bat build --no-daemon
Pop-Location
.\gradlew.bat build --no-daemon
.\gradlew.bat runGameTestServer --no-daemon
```

На Linux/macOS используй `./gradlew` и `python3`. Скрипт подготавливает pinned-копию хоста и применяет `patches/*.patch`. Gradle использует composite build и подставляет подготовленный `host/` вместо Maven-артефакта — отдельная команда `publish` не нужна. Не редактируй `host/` вручную.

CI собирает подготовленный хост и аддон, проверяет происхождение JAR и запускает GameTests с включённым и выключенным `no_lust`. Визуальные проверки требуют `runClient`.

Артефакт локальной сборки: `build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`.

## Использование

Открой Thaumonomicon — категория **FORBIDDEN** открывается с исследованием «Сетевые осколки». Хамелеоновы инструменты переключают наборы зачарований по Shift + ПКМ.

Конфиг `config/forbiddenmagic-startup.toml`: `no_lust = true` отключает Luxuria и связанные с Lust рецепты; `silverfish_emeralds = false` отключает изумрудные самородки с чешуйниц; `emerald_transmutation = false` отключает исследование и алхимическую трансмутацию самородков.

## Структура

- `src/` — код и ресурсы.
- `tests/` — GameTest.
- `data/` и `tools/` — источники данных и генераторы.
- `patches/` — только необходимые локальные патчи хоста.
- `docs/fidelity.md`, `docs/roadmap.md`, `docs/verification.md` — паритет, задачи и результаты проверок.

## Авторы и лицензии

- **SpitefulFox** — оригинальный Forbidden Magic 0.574 (WTFPL).
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved).
- **Azanor** — Thaumcraft 4.2.3.5.
- **MxxFoxx2** — порт (MIT).

Код порта распространяется по MIT; оригинал и хост имеют собственные лицензии. Подробности — в `LICENSE` и `NOTICE`.
