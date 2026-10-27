# Forbidden Magic UNOFFICIAL PORT

Форк [MxxFoxx2/forbidden-magic-reforged](https://github.com/MxxFoxx2/forbidden-magic-reforged/tree/1.21.1) для **Minecraft 1.21.1 / NeoForge 21.1.256+ / Java 21**. Поддерживается только ветка `1.21.1`.

Аддон предназначен для [Thaumcraft UNOFFICIAL PORT от EugeneHeisst](https://github.com/EugeneHeisst/thaumcraft-port), **0.17.189 или новее** (`tc4modern`). Чужой Thaumcraft 4: Reforged (`thaumcraft`) этой сборке не подходит. Одновременно ставить два Forbidden Magic нельзя.

## Установка

Скачайте один JAR из [Releases](https://github.com/EugeneHeisst/forbidden-magic-reforged/releases) и положите в `mods` клиента и сервера вместе с указанным Thaumcraft. Дополнительный патчер и отдельный API не нужны. Перед заменой другой реализации аддона сделайте резервную копию мира: автоматическая миграция данных из Reforged не предусмотрена.

## Совместимость

- Thaumcraft: аспекты грехов, сканирование, исследования в отдельной вкладке, тигель, магический верстак, наполнение и зачарование наполнением, жезлы и фокус, Baubles, трубы эссенции и клетка гнева.
- [Thaumic Tinkerer](https://github.com/EugeneHeisst/thaumic-tinkerer-port): восемь зачарований Forbidden Magic на столе зачарования. Если вариантов больше 16, страницы переключаются колесом мыши. Аддон работает и без Tinkerer.
- IndustrialCraft 2, Thaumic Energistics, Advanced Solar Panels, Electro-Magic Tools, Gravitation Suite: совместный запуск проверяется вместе с остальной сборкой. Новых преобразований EU/FE или интеграций с отсутствующими модами этот форк не добавляет.

Sodium и Iris не поддерживаются: с ними возможны визуальные баги.

## Сборка

```sh
python tools/prepare_dependencies.py
./gradlew clean build
python tools/check_provenance.py
```

Зависимости закреплены версиями и SHA-256. Существующая локальная сборка `../thaumcraft4-modern/versions/0.17.189` используется автоматически; можно указать `-Ptc4Jar=/path/to/tc4modern.jar`.

```sh
python tools/prepare_dependencies.py --test-pack
./gradlew -PcompatTest -PcompatPack=deps/test-pack runGameTestServer
```

Описание адаптации и ограничений: [docs/tc4modern-compatibility.md](docs/tc4modern-compatibility.md). Старые документы Reforged в `docs/superpowers`, `patches` и прежние тесты в `tests/java` сохранены для истории; актуальные тесты этого форка находятся в `tests/compat`.

Оригинальный Forbidden Magic: SpitefulFox (WTFPL). Порт Reforged: MxxFoxx2 (MIT). Адаптация TC4Modern: EugeneHeisst. Thaumcraft: Azanor. Подробная атрибуция в [NOTICE](NOTICE).
