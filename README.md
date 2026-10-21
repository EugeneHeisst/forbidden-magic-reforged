# Forbidden Magic (Reforged Port)

Неофициальный порт **Forbidden Magic 0.574** (Minecraft 1.7.10, SpitefulFox) на **Thaumcraft 4: Reforged**.
Проект не связан с оригинальным автором; оригинал распространяется под WTFPL, порт — под MIT
(см. `LICENSE` и `NOTICE`).

## Поддерживается

- Thaumcraft 4: Reforged `1.2` и новее на Minecraft `26.3`.
- Семь аспектов: `luxuria`, `infernus`, `superbia`, `gula`, `invidia`, `desidia`, `ira`.
- Таблица меток `DarkAspects` для существующих целей, Осколки грехов (семь id) и Осколок Чревоугодия.
- Дропы осколков по правилам оригинала, категория `FORBIDDEN`, записи `NETHERSHARDS` / `HELLFIRE` /
  `SKULLAXE`, рецепт топора Череподрать.
- `no_lust` как STARTUP-конфиг; значения на клиенте и сервере должны совпадать.
- Опциональная интеграция с Thaumic Tinkerer Reforged: стоимость восьми FM-зачарований в энчантере,
  если TT установлен.

## Чего нет в этой вехе

- Остальной контент полного объёма: зачарования, жезлы и фокусы, Клетка Гнева, скруное дерево,
  житель-еретик, морф-инструменты и прочее — см. `docs/roadmap.md`.
- Blood Magic, Botania, Ars Magica 2, CoFH не поддерживаются: под NeoForge 26.3 этих портов нет.
- Minecraft 1.21.1 — второй таргет, после M1.
- `plans.txt` оригинального автора не реализуется: это незавершённый список идей.

## Установка

Нужен Thaumcraft 4: Reforged `1.2` с аддон-аспект-API. Если официального релиза хоста с этим API
ещё нет, используй патчер: применить `patches/` к своему клону хоста, собрать его и положить рядом
собранный jar порта. Сам jar хоста проект не распространяет. Подробности — `docs/upstream.md`.

## Сборка

```bash
python tools/prepare_host.py
cd host && cmd //c gradlew.bat publish && cd ..
cmd //c gradlew.bat build
```

На Linux/macOS замени `cmd //c gradlew.bat` на `./gradlew`. Готовый артефакт —
`build/libs/forbidden-magic-0.574-26.3-port.0.1.0.jar`.

Тесты:

```bash
cmd //c gradlew.bat runGameTestServer
```

## Лицензия, авторы и темп

- Код порта — MIT; оригинальный Forbidden Magic — WTFPL, хост — All Rights Reserved; всё это
  зафиксировано в `NOTICE`.
- Авторы: SpitefulFox (оригинал), notlikethiss (хост), Azanor (Thaumcraft 4.2.3.5),
  MxxFoxx2 (порт).
- Разработка велась с участием ИИ; ответственность за содержимое остаётся за автором порта.
- Личная инициатива: регулярных обновлений не обещаем, автор может время от времени возвращаться
  к проекту. Баг-репорты полезны; исправления — по возможности.

См. `docs/roadmap.md`, `docs/deviations.md`, `docs/upstream.md`.
