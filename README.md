# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) для Thaumcraft 4: Reforged на Minecraft 26.3. Перенесённый контент включает семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, материалы скверны, Mob Crystal и Wrath Cage.

## Статус

- Целевая версия — Minecraft 26.3 / Java 25. Полный паритет с оригиналом пока не подтверждён.
- Реализованные механики и отличия: [fidelity ledger](docs/fidelity.md). Оставшиеся задачи: [roadmap](docs/roadmap.md). Результаты проверок: [verification](docs/verification.md).
- Последний релиз — [0.1.1](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases/tag/0.1.1). CI успешно прошёл сборку и 94/94 GameTests в обоих режимах \u0060no_lust\u0060; визуальные проверки клиента и полноценные игровые проверки ещё нужны.
- Thaumcraft 4: Reforged 1.1.2+ содержит upstream API аспектов аддонов. Для регистрации портала Нижнего мира как отдельной цели сканирования блока нужен локальный [host patch](patches/0001-block-aspect-targets.patch).

## Установка

Скачайте мод из [Releases](https://github.com/MxxFoxx2/forbidden-magic-reforged/releases) и поместите JAR в \u0060mods/\u0060 вместе с Thaumcraft 4: Reforged 1.1.2+ для Minecraft 26.3.

Хост не включён в этот репозиторий из-за лицензии All Rights Reserved.

## Сборка

Нужны Git, Python 3 и JDK 25. Клонируйте хост рядом с этим репозиторием:

\u0060\u0060\u0060powershell
git clone https://github.com/notlikethiss/thaumcraft-4-reforged.git ../thaumcraft-4-reforged
python tools/prepare_host.py
Push-Location host
.\\gradlew.bat build --no-daemon
Pop-Location
.\\gradlew.bat build --no-daemon
.\\gradlew.bat runGameTestServer --no-daemon
\u0060\u0060\u0060

На Linux/macOS используйте \u0060./gradlew\u0060 и \u0060python3\u0060. Скрипт \u0060tools/prepare_host.py\u0060 создаёт закреплённую копию хоста в \u0060host/\u0060 и применяет патчи. Composite build использует её автоматически; отдельный \u0060publish\u0060 не требуется. Не редактируйте \u0060host/\u0060 вручную.

## Использование

В Thaumonomicon категория **FORBIDDEN** открывается после исследования «Сетевые осколки». Хамелеоновы инструменты переключают наборы зачарований по **Shift + ПКМ**.

Настройки в \u0060config/forbiddenmagic-startup.toml\u0060:

- \u0060no_lust = true\u0060 — отключает аспект Luxuria и связанные с Lust рецепты.
- \u0060silverfish_emeralds = false\u0060 — отключает выпадение изумрудных самородков с чешуйниц.
- \u0060emerald_transmutation = false\u0060 — отключает исследование и алхимическую трансмутацию самородков.

## Авторы и лицензии

- **SpitefulFox** — оригинальный Forbidden Magic 0.574 (WTFPL).
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved).
- **Azanor** — Thaumcraft 4.2.3.5.
- **MxxFoxx2** — порт (MIT).

Порт распространяется по MIT; лицензии оригинала и хоста остаются отдельными. Подробности — в \u0060LICENSE\u0060 и \u0060NOTICE\u0060.
