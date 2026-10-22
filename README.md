# Forbidden Magic: Reforged

Порт Forbidden Magic 0.574 (Minecraft 1.7.10) на Thaumcraft 4: Reforged 1.2 (MC 26.3). Переносит семь аспектов грехов, осколки, зачарования, хамелеоновы инструменты, скверные материалы, кристалл моба и Клетку Гнева — с теми же числами, рецептами и текстами, что в оригинале.

## Установка

### Для игроков

1. Установи **Thaumcraft 4: Reforged 1.2** для Minecraft 26.3
2. Скачай `forbidden-magic-0.574-26.3-port.x.x.x.jar` из релизов
3. Положи jar в папку `mods/`

### Для разработчиков

```bash
git clone <thaumcraft-4-reforged> ../thaumcraft-4-reforged
python tools/prepare_host.py
./gradlew build
```

Артефакт: `build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`

Тесты: `./gradlew runGameTestServer` (48/48).

## Использование

Открой Thaumonomicon — категория **FORBIDDEN** открывается с исследования «Сетевые осколки». Осколки грехов падают с мобов по правилам оригинала: гнев за убийства, гордость с боссов, лень за смерть без игрока. Хамелеоновы инструменты переключают наборы зачарований по Shift + ПКМ.

Конфиг `config/forbiddenmagic-startup.toml`: `no_lust = true` убирает аспект luxuria и связанные рецепты.

## Структура проекта

```
src/          Исходный код и ресурсы
tests/        GameTest-тесты
docs/         Документация (roadmap, deviations, verification)
data/         Исходные данные (lang, id_map)
tools/        Скрипты сборки и генерации
patches/      Патчи к хосту
```

## Авторы

- **SpitefulFox** — оригинальный Forbidden Magic (WTFPL)
- **notlikethiss** — Thaumcraft 4: Reforged (All Rights Reserved)
- **Azanor** — Thaumcraft 4.2.3.5
- **MxxFoxx2** — порт (MIT)

Разработка велась с участием ИИ; ответственность за содержимое остаётся за автором порта.

**Лицензия:** код порта — MIT; оригинал — WTFPL; хост — All Rights Reserved. Подробности в `LICENSE` и `NOTICE`.

Регулярных обновлений не обещаем. Баг-репорты полезны; исправления — по возможности.

---

Полный список реализованного и отложенного — в `docs/roadmap.md`. Отступления от оригинала — в `docs/deviations.md`.
