# Forbidden Magic: Reforged — зачарования (слайс 2) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Портировать пять зачарований Forbidden Magic, которые не зависят от морф-инструментов (Wrath, Capitalist/Greedy, Consuming, Educational, Corrupting), вместе с предметом «Изумрудный фрагмент», исследованиями, infusion-рецептами и опциональной регистрацией стоимости в Thaumic Tinkerer.

**Architecture:** Зачарования в 26.3 — датапак-записи (`Registries.ENCHANTMENT`), а поведение — обработчики событий NeoForge, как у хостовых `haste`/`repair`. Add-on не использует datagen: JSON-файлы зачарований, тегов и lang лежат в `src/main/resources`, lang собирается `tools/gen_resources.py`. Поведение выносится в `FmEnchantmentEffects`, чистые функции проверяются GameTest, события подключаются к `NeoForge.EVENT_BUS`.

**Tech Stack:** NeoForge 26.3.0.57-beta, Java 25, ModDevGradle, Thaumcraft 4: Reforged 1.2 (локальный патченный хост), GameTest.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md` (§5.1, конфиг, лицензия).

## Граница слайса

В слайс входят пять зачарований, у которых в оригинале нет зависимости от морф-инструментов:

| Зачарование | Уровни | Оригинальный класс | Где эффект |
|---|---|---|---|
| Wrath | 5 | `EnchantmentWrath` | `FMEventHandler`, урон |
| Capitalist (`greedy`) | 1 | `EnchantmentGreedy` | `FMEventHandler`, дропы |
| Consuming | 1 | `EnchantmentConsuming` | `FMEventHandler`, дропы блоков |
| Educational | 5 | `EnchantmentEducational` | `FMEventHandler`, опыт |
| Corrupting | 1 | `EnchantmentCorrupting` | `FMEventHandler`, дропы блоков |

**Вне слайса:** Cluster, Impact, Voidtouched. Их эффекты в оригинале живут внутри `ItemMorphPickaxe/Axe/Shovel/Sword`, а исследования требуют родителя `MORPHTOOLS` и хостовых `ELEMENTALPICK/ELEMENTALSHOVEL/VOIDMETAL`. Они переезжают в слайс морф-инструментов; TT-стоимость для этих трёх регистрируется там же.

## Global Constraints

- Пакет `dev.visfox.forbiddenreforged`, modId `forbiddenmagic`, версия `0.574-26.3-port.0.1.0`.
- Хост не редактируется: изменения только через `patches/`; в этом слайсе патчи не меняются.
- Числа, аспекты, тексты и текстуры — 1:1 из `../ForbiddenMagic`; никаких новых балансов.
- Ресурсы — только генератором `tools/gen_resources.py`; повторный прогон не меняет файлы.
- Коммиты на английском, `feat:`/`fix:`/`docs:`/`chore:`; один Task — один коммит, без push.
- Гейт после каждого Task: `cmd //c gradlew.bat build`, `cmd //c gradlew.bat runGameTestServer`, `python tools/check_provenance.py`.
- Конфиги только STARTUP; новый рубильник `enchanting` читается через fallback на default, как `no_lust`.

---

### Task 1: Реестр зачарований, JSON, теги и рубильник

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantments.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmConfig.java`
- Create: `src/main/resources/data/forbiddenmagic/enchantment/wrath.json`
- Create: `src/main/resources/data/forbiddenmagic/enchantment/greedy.json`
- Create: `src/main/resources/data/forbiddenmagic/enchantment/consuming.json`
- Create: `src/main/resources/data/forbiddenmagic/enchantment/educational.json`
- Create: `src/main/resources/data/forbiddenmagic/enchantment/corrupting.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/item/enchantable/wrath.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/item/enchantable/weapon.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/item/enchantable/digger.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/item/enchantable/pickaxe.json`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: ничего.
- Produces: `FmEnchantments.WRATH/GREEDY/CONSUMING/EDUCATIONAL/CORRUPTING` (`ResourceKey<Enchantment>`), `FmEnchantments.level(RegistryAccess, ItemStack, ResourceKey<Enchantment>) → int`, `FmConfig.enchantmentsEnabled() → boolean`.

- [ ] **Step 1: Написать падающий тест**

В `FmGameTests.java` добавить `"enchantments_resolve_with_expected_shape"` в конец массива `TESTS` (индекс 16) и в `static`-блок:

```java
FUNCTIONS.register(TESTS[16], () -> FmGameTests::enchantmentsResolveWithExpectedShape);
```

И сам тест:

```java
private static void enchantmentsResolveWithExpectedShape(GameTestHelper helper) {
    var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.WRATH).value().getMaxLevel(), 5, "wrath max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.GREEDY).value().getMaxLevel(), 1, "greedy max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.EDUCATIONAL).value().getMaxLevel(), 5, "educational max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CONSUMING).value().getMaxLevel(), 1, "consuming max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CORRUPTING).value().getMaxLevel(), 1, "corrupting max level");
    helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_SWORD)), "wrath applies to swords");
    helper.assertTrue(registry.getOrThrow(FmEnchantments.WRATH).value().isSupportedItem(new ItemStack(Items.DIAMOND_AXE)), "wrath applies to axes");
    helper.assertTrue(registry.getOrThrow(FmEnchantments.CONSUMING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "consuming applies to shovels");
    helper.assertFalse(registry.getOrThrow(FmEnchantments.CORRUPTING).value().isSupportedItem(new ItemStack(Items.IRON_SHOVEL)), "corrupting is pickaxe only");
    helper.succeed();
}
```

Добавить импорты в тест: `net.minecraft.core.registries.Registries`, `net.minecraft.world.item.ItemStack`, `net.minecraft.world.item.Items`, `dev.visfox.forbiddenreforged.FmEnchantments`.

- [ ] **Step 2: Убедиться, что тест красный**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `FmEnchantments`/`FmConfig.enchantmentsEnabled`.

- [ ] **Step 3: Добавить рубильник в конфиг**

В `FmConfig.java` добавить поле и геттер (рядом с `gluttony`):

```java
public static ModConfigSpec.BooleanValue enchanting;

enchanting = BUILDER.comment("Enable Forbidden Magic enchantments.").define("general.enchanting", true);

public static boolean enchantmentsEnabled() {
    return SPEC.isLoaded() ? enchanting.get() : enchanting.getDefault();
}
```

- [ ] **Step 4: Создать `FmEnchantments`**

```java
package dev.visfox.forbiddenreforged;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class FmEnchantments {
    public static final ResourceKey<Enchantment> WRATH = key("wrath");
    public static final ResourceKey<Enchantment> GREEDY = key("greedy");
    public static final ResourceKey<Enchantment> CONSUMING = key("consuming");
    public static final ResourceKey<Enchantment> EDUCATIONAL = key("educational");
    public static final ResourceKey<Enchantment> CORRUPTING = key("corrupting");

    private FmEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, ForbiddenMagic.id(name));
    }

    public static int level(RegistryAccess access, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return access.lookupOrThrow(Registries.ENCHANTMENT)
            .get(key)
            .map(holder -> stack.getEnchantmentLevel(holder))
            .orElse(0);
    }

    public static boolean enabled() {
        return FmConfig.enchantmentsEnabled();
    }
}
```

- [ ] **Step 5: Написать пять JSON зачарований**

`src/main/resources/data/forbiddenmagic/enchantment/wrath.json`:

```json
{
  "anvil_cost": 4,
  "description": {
    "translate": "enchantment.forbiddenmagic.wrath"
  },
  "effects": {},
  "max_cost": {
    "base": 25,
    "per_level_above_first": 11
  },
  "max_level": 5,
  "min_cost": {
    "base": 5,
    "per_level_above_first": 11
  },
  "slots": [
    "mainhand"
  ],
  "supported_items": "#forbiddenmagic:enchantable/wrath",
  "weight": 1
}
```

`greedy.json`:

```json
{
  "anvil_cost": 4,
  "description": {
    "translate": "enchantment.forbiddenmagic.greedy"
  },
  "effects": {},
  "max_cost": {
    "base": 70,
    "per_level_above_first": 0
  },
  "max_level": 1,
  "min_cost": {
    "base": 20,
    "per_level_above_first": 0
  },
  "slots": [
    "mainhand"
  ],
  "supported_items": "#forbiddenmagic:enchantable/weapon",
  "weight": 1
}
```

`consuming.json`:

```json
{
  "anvil_cost": 4,
  "description": {
    "translate": "enchantment.forbiddenmagic.consuming"
  },
  "effects": {},
  "max_cost": {
    "base": 51,
    "per_level_above_first": 10
  },
  "max_level": 1,
  "min_cost": {
    "base": 1,
    "per_level_above_first": 10
  },
  "slots": [
    "mainhand"
  ],
  "supported_items": "#forbiddenmagic:enchantable/digger",
  "weight": 5
}
```

`educational.json`:

```json
{
  "anvil_cost": 4,
  "description": {
    "translate": "enchantment.forbiddenmagic.educational"
  },
  "effects": {},
  "max_cost": {
    "base": 51,
    "per_level_above_first": 10
  },
  "max_level": 5,
  "min_cost": {
    "base": 1,
    "per_level_above_first": 10
  },
  "slots": [
    "mainhand"
  ],
  "supported_items": "#forbiddenmagic:enchantable/weapon",
  "weight": 5
}
```

`corrupting.json`:

```json
{
  "anvil_cost": 4,
  "description": {
    "translate": "enchantment.forbiddenmagic.corrupting"
  },
  "effects": {},
  "max_cost": {
    "base": 70,
    "per_level_above_first": 0
  },
  "max_level": 1,
  "min_cost": {
    "base": 20,
    "per_level_above_first": 0
  },
  "slots": [
    "mainhand"
  ],
  "supported_items": "#forbiddenmagic:enchantable/pickaxe",
  "weight": 1
}
```

- [ ] **Step 6: Написать теги поддерживаемых предметов**

`.../tags/item/enchantable/wrath.json`:

```json
{
  "replace": false,
  "values": [
    "#minecraft:swords",
    "#minecraft:axes"
  ]
}
```

`.../enchantable/weapon.json`:

```json
{
  "replace": false,
  "values": [
    "#minecraft:swords"
  ]
}
```

`.../enchantable/digger.json`:

```json
{
  "replace": false,
  "values": [
    "#minecraft:pickaxes",
    "#minecraft:shovels",
    "thaumcraft:primal_crusher"
  ]
}
```

`.../enchantable/pickaxe.json`:

```json
{
  "replace": false,
  "values": [
    "#minecraft:pickaxes",
    "thaumcraft:primal_crusher"
  ]
}
```

- [ ] **Step 7: Добавить имена в lang**

В `tools/lang/en_us.toml`:

```toml
[enchantment.forbiddenmagic]
wrath = "Wrath"
greedy = "Capitalist"
consuming = "Consuming"
educational = "Educational"
corrupting = "Corrupting"
```

В `tools/lang/ru_ru.toml`:

```toml
[enchantment.forbiddenmagic]
wrath = "Гнев"
greedy = "Капиталист"
consuming = "Потребление"
educational = "Образование"
corrupting = "Искажение"
```

- [ ] **Step 8: Сгенерировать lang и прогнать тест**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 17 required tests passed`.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmEnchantments.java src/main/java/dev/visfox/forbiddenreforged/FmConfig.java src/main/resources/data/forbiddenmagic tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java tools/lang src/generated
git commit -m "feat: register the five standalone FM enchantments"
```

---

### Task 2: Изумрудный фрагмент

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmItems.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/item/emerald_fragment.png` (копия `../ForbiddenMagic/src/main/resources/assets/forbidden/textures/items/emerald_nugget.png`)
- Modify: `tools/gen_resources.py` (добавить `"emerald_fragment": "emerald_nugget"` в `ITEM_TEXTURES`)
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: ничего.
- Produces: `FmItems.EMERALD_FRAGMENT` (`DeferredItem<Item>`), id `forbiddenmagic:emerald_fragment`.

- [ ] **Step 1: Падающий тест**

Добавить `"emerald_fragment_is_registered"` в конец `TESTS` (индекс 17) и `FUNCTIONS.register(TESTS[17], () -> FmGameTests::emeraldFragmentIsRegistered);`. Метод:

```java
private static void emeraldFragmentIsRegistered(GameTestHelper helper) {
    ItemStack stack = new ItemStack(FmItems.EMERALD_FRAGMENT.get());
    helper.assertFalse(stack.isEmpty(), "emerald fragment must exist");
    helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(stack.getItem()), Identifier.parse("forbiddenmagic:emerald_fragment"), "emerald fragment id");
    helper.succeed();
}
```

Импорты: `net.minecraft.core.registries.BuiltInRegistries`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: ошибка компиляции на `FmItems.EMERALD_FRAGMENT`.

- [ ] **Step 3: Реализация**

`FmItems`:

```java
public static final DeferredItem<Item> EMERALD_FRAGMENT;

EMERALD_FRAGMENT = ITEMS.registerSimpleItem("emerald_fragment");
```

`FmCreativeTab.addItems`, после `SKULL_AXE`:

```java
event.accept(FmItems.EMERALD_FRAGMENT.get());
```

Lang (en):

```toml
emerald_fragment = "Emerald Fragment"
```

Lang (ru):

```toml
emerald_fragment = "Изумрудный фрагмент"
```

- [ ] **Step 4: Скопировать текстуру и сгенерировать модели**

```bash
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/items/emerald_nugget.png src/main/resources/assets/forbiddenmagic/textures/item/emerald_fragment.png
python tools/gen_resources.py
```

- [ ] **Step 5: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 18 required tests passed`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmItems.java src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java src/main/resources/assets/forbiddenmagic/textures/item/emerald_fragment.png tools/gen_resources.py tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the emerald fragment item"
```

---

### Task 3: Consuming и Corrupting

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java`
- Create: `src/main/resources/data/forbiddenmagic/tags/block/infused_stones.json`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: `FmEnchantments.level/ enabled`, `FmItems.DEADLY_SHARDS`, `FmItems.GLUTTONY_SHARD`.
- Produces: `FmEnchantmentEffects.register()`, `isGarbage(ItemStack) → boolean`, `sinShard(RandomSource) → ItemStack`, `corruptRoll(RandomSource) → boolean` (1 из 3, как `nextInt(3) == 1`).

- [ ] **Step 1: Падающие тесты**

Добавить `"consuming_destroys_garbage"` (18) и `"corrupting_yields_sin_shards"` (19) в `TESTS`, затем:

```java
FUNCTIONS.register(TESTS[18], () -> FmGameTests::consumingDestroysGarbage);
FUNCTIONS.register(TESTS[19], () -> FmGameTests::corruptingYieldsSinShards);
```

Методы:

```java
private static void consumingDestroysGarbage(GameTestHelper helper) {
    helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIRT)), "dirt is garbage");
    helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.COBBLESTONE)), "cobblestone is garbage");
    helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.SAND)), "sand is garbage");
    helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.GRAVEL)), "gravel is garbage");
    helper.assertTrue(FmEnchantmentEffects.isGarbage(new ItemStack(Items.NETHERRACK)), "netherrack is garbage");
    helper.assertFalse(FmEnchantmentEffects.isGarbage(new ItemStack(Items.DIAMOND)), "diamond is not garbage");
    helper.succeed();
}

private static void corruptingYieldsSinShards(GameTestHelper helper) {
    RandomSource random = RandomSource.create(1234L);
    java.util.Set<Item> pool = java.util.Set.of(
        FmItems.DEADLY_SHARDS.get("wrath").get(),
        FmItems.DEADLY_SHARDS.get("envy").get(),
        FmItems.DEADLY_SHARDS.get("pride").get(),
        FmItems.DEADLY_SHARDS.get("sloth").get(),
        FmItems.DEADLY_SHARDS.get("greed").get(),
        FmItems.GLUTTONY_SHARD.get()
    );
    for (int roll = 0; roll < 200; roll++) {
        ItemStack shard = FmEnchantmentEffects.sinShard(random);
        helper.assertTrue(pool.contains(shard.getItem()), "sin shard must come from the pool");
    }
    helper.assertValueEqual(FmEnchantmentEffects.sinShard(RandomSource.create(7L)).isEmpty(), false, "sin shard is never empty");
    helper.succeed();
}
```

Импорты: `net.minecraft.util.RandomSource`, `net.minecraft.world.item.Item`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `FmEnchantmentEffects`.

- [ ] **Step 3: Реализация**

`FmEnchantmentEffects`:

```java
package dev.visfox.forbiddenreforged;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public final class FmEnchantmentEffects {
    private static final Set<Item> GARBAGE = Set.of(Items.DIRT, Items.SAND, Items.GRAVEL, Items.COBBLESTONE, Items.NETHERRACK);
    private static final TagKey<Block> INFUSED_STONES = TagKey.create(Registries.BLOCK, ForbiddenMagic.id("infused_stones"));
    private static final List<String> SIN_POOL = List.of("wrath", "envy", "gluttony", "pride", "lust", "sloth", "greed");

    private FmEnchantmentEffects() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onBlockDrops);
    }

    public static boolean isGarbage(ItemStack stack) {
        return GARBAGE.contains(stack.getItem());
    }

    public static ItemStack sinShard(RandomSource random) {
        while (true) {
            String vice = SIN_POOL.get(random.nextInt(SIN_POOL.size()));
            if (vice.equals("lust") && FmConfig.lustDisabled()) {
                continue;
            }
            if (vice.equals("gluttony")) {
                return new ItemStack(FmItems.GLUTTONY_SHARD.get());
            }
            return new ItemStack(FmItems.DEADLY_SHARDS.get(vice).get());
        }
    }

    public static boolean corruptRoll(RandomSource random) {
        return random.nextInt(3) == 1;
    }

    private static void onBlockDrops(BlockDropsEvent event) {
        if (!FmEnchantments.enabled()) {
            return;
        }
        Entity breaker = event.getBreaker();
        if (!(breaker instanceof ServerPlayer player) || breaker instanceof FakePlayer) {
            return;
        }
        ItemStack tool = event.getTool();
        int consuming = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CONSUMING);
        if (consuming > 0) {
            event.getDrops().removeIf(drop -> isGarbage(drop.getItem()));
        }
        int corrupting = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CORRUPTING);
        if (corrupting > 0 && event.getState().is(INFUSED_STONES)) {
            RandomSource random = event.getLevel().getRandom();
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (corruptRoll(random) && BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("thaumcraft")) {
                    drop.setItem(sinShard(random).copyWithCount(stack.getCount()));
                }
            }
        }
    }
}
```

`copyWithCount` существует в 26.3? Проверить: `ItemStack.copyWithCount(int)` — да, с 1.20.5. Если компилятор не найдёт — заменить на `new ItemStack(item, stack.getCount())`.

Тег `data/forbiddenmagic/tags/block/infused_stones.json`:

```json
{
  "replace": false,
  "values": [
    "thaumcraft:air_infused_stone",
    "thaumcraft:fire_infused_stone",
    "thaumcraft:water_infused_stone",
    "thaumcraft:earth_infused_stone",
    "thaumcraft:order_infused_stone",
    "thaumcraft:entropy_infused_stone"
  ]
}
```

Подключить в конструкторе `ForbiddenMagic`, после `FmDrops.register()`:

```java
FmEnchantmentEffects.register();
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 20 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged src/main/resources/data/forbiddenmagic/tags/block tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: port consuming and corrupting behaviour"
```

---

### Task 4: Greedy и Educational

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: `FmItems.EMERALD_FRAGMENT`, `FmEnchantments.level`.
- Produces: `greedyDrop(boolean villager, int roll) → ItemStack`, `educationalExperience(int original, int level) → int`.

- [ ] **Step 1: Падающие тесты**

Добавить `"greedy_drops_match_kills"` (20) и `"educational_multiplies_experience"` (21) в `TESTS`, затем:

```java
FUNCTIONS.register(TESTS[20], () -> FmGameTests::greedyDropsMatchKills);
FUNCTIONS.register(TESTS[21], () -> FmGameTests::educationalMultipliesExperience);
```

Методы:

```java
private static void greedyDropsMatchKills(GameTestHelper helper) {
    helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(true, 99).getItem(), Items.EMERALD, "villager drops an emerald");
    helper.assertValueEqual(FmEnchantmentEffects.greedyDrop(false, 2).getItem(), FmItems.EMERALD_FRAGMENT.get(), "hostile mob drops a fragment on a low roll");
    helper.assertTrue(FmEnchantmentEffects.greedyDrop(false, 3).isEmpty(), "hostile mob drops nothing on a high roll");
    helper.succeed();
}

private static void educationalMultipliesExperience(GameTestHelper helper) {
    helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 1), 40, "level 1 adds three times the base xp");
    helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(10, 5), 160, "level 5 adds fifteen times the base xp");
    helper.assertValueEqual(FmEnchantmentEffects.educationalExperience(0, 5), 0, "no base xp stays zero");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на отсутствующих методах.

- [ ] **Step 3: Реализация**

Добавить в `FmEnchantmentEffects`:

```java
public static ItemStack greedyDrop(boolean villager, int roll) {
    if (villager) {
        return new ItemStack(Items.EMERALD);
    }
    return roll < 3 ? new ItemStack(FmItems.EMERALD_FRAGMENT.get()) : ItemStack.EMPTY;
}

public static int educationalExperience(int original, int level) {
    return original + 3 * original * level;
}
```

Добавить обработчики и их регистрацию:

```java
NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onLivingDrops);
NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onExperienceDrop);
```

```java
private static void onLivingDrops(LivingDropsEvent event) {
    if (!FmEnchantments.enabled() || !event.isRecentlyHit()) {
        return;
    }
    Entity attacker = event.getSource().getDirectEntity();
    if (!(attacker instanceof ServerPlayer player) || attacker instanceof FakePlayer) {
        return;
    }
    ItemStack weapon = player.getMainHandItem();
    if (FmEnchantments.level(player.level().registryAccess(), weapon, FmEnchantments.GREEDY) <= 0) {
        return;
    }
    if (FmEnchantments.level(player.level().registryAccess(), weapon, Enchantments.LOOTING) > 0) {
        return;
    }
    boolean villager = event.getEntity() instanceof Villager;
    boolean hostile = event.getEntity() instanceof Enemy;
    if (!villager && !hostile) {
        return;
    }
    ItemStack drop = greedyDrop(villager, player.getRandom().nextInt(35));
    if (!drop.isEmpty()) {
        addDrop(event, drop);
    }
}

private static void onExperienceDrop(LivingExperienceDropEvent event) {
    if (!FmEnchantments.enabled() || event.getAttackingPlayer() == null) {
        return;
    }
    ServerPlayer player = (ServerPlayer) event.getAttackingPlayer();
    ItemStack weapon = player.getMainHandItem();
    int level = FmEnchantments.level(player.level().registryAccess(), weapon, FmEnchantments.EDUCATIONAL);
    if (level <= 0) {
        return;
    }
    if (FmEnchantments.level(player.level().registryAccess(), weapon, Enchantments.LOOTING) > 0) {
        return;
    }
    event.setDroppedExperience(educationalExperience(event.getOriginalExperience(), level));
}

private static void addDrop(LivingDropsEvent event, ItemStack stack) {
    LivingEntity entity = event.getEntity();
    ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
    drop.setPickUpDelay(10);
    event.getDrops().add(drop);
}
```

Импорты: `net.minecraft.world.entity.LivingEntity`, `net.minecraft.world.entity.npc.villager.Villager`, `net.minecraft.world.entity.monster.Enemy`, `net.minecraft.world.item.enchantment.Enchantments`, `net.neoforged.neoforge.event.entity.living.LivingDropsEvent`, `net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent`.

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 22 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: port greedy and educational behaviour"
```

---

### Task 5: Wrath

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `wrathDamage(int level) → float`.

- [ ] **Step 1: Падающий тест**

Добавить `"wrath_adds_damage"` (22) в `TESTS` и `FUNCTIONS.register(TESTS[22], () -> FmGameTests::wrathAddsDamage);`. Метод:

```java
private static void wrathAddsDamage(GameTestHelper helper) {
    helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(1), 1.25F, "level 1 adds 1.25 damage");
    helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(5), 6.25F, "level 5 adds 6.25 damage");
    helper.assertValueEqual(FmEnchantmentEffects.wrathDamage(0), 0.0F, "level 0 adds nothing");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `wrathDamage`.

- [ ] **Step 3: Реализация**

```java
public static float wrathDamage(int level) {
    return 1.25F * level;
}
```

```java
NeoForge.EVENT_BUS.addListener(FmEnchantmentEffects::onIncomingDamage);
```

```java
private static void onIncomingDamage(LivingIncomingDamageEvent event) {
    if (!FmEnchantments.enabled()) {
        return;
    }
    Entity direct = event.getSource().getDirectEntity();
    if (!(direct instanceof ServerPlayer player) || direct instanceof FakePlayer) {
        return;
    }
    int level = FmEnchantments.level(player.level().registryAccess(), player.getMainHandItem(), FmEnchantments.WRATH);
    if (level > 0) {
        event.setAmount(event.getAmount() + wrathDamage(level));
    }
}
```

Импорт: `net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent`.

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 23 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: port the wrath enchantment"
```

---

### Task 6: Исследования и тексты

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmResearch.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/wrath.png`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/greedy.png`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/consuming.png`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/educational.png`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/corrupting.png`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: `FmEnchantments` keys, `FmAspects`.
- Produces: исследования `WRATH`, `GREEDY`, `CONSUMING`, `EDUCATIONAL`, `CORRUPTING` в категории `FORBIDDEN`.

- [ ] **Step 1: Падающий тест**

Добавить `"enchantment_research_present"` (23) в `TESTS` и `FUNCTIONS.register(TESTS[23], () -> FmGameTests::enchantmentResearchPresent);`. Метод:

```java
private static void enchantmentResearchPresent(GameTestHelper helper) {
    ResearchItem wrath = ResearchCategories.getResearch("WRATH");
    helper.assertTrue(wrath != null, "wrath research must exist");
    helper.assertValueEqual(wrath.warp(), 3, "wrath research carries warp 3");
    helper.assertTrue(wrath.parents().contains("NETHERSHARDS"), "wrath descends from nether shards");
    helper.assertTrue(wrath.parents().contains("INFUSIONENCHANTMENT"), "wrath needs infusion enchantment");
    helper.assertValueEqual(wrath.pages().size(), 2, "wrath has text and recipe pages");
    for (String key : new String[]{"GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
        helper.assertTrue(ResearchCategories.getResearch(key) != null, key + " research must exist");
    }
    helper.succeed();
}
```

Импорты: `thaumcraft.research.ResearchCategories`, `thaumcraft.research.ResearchItem`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: тест падает на `wrath research must exist`.

- [ ] **Step 3: Скопировать иконки**

```bash
for name in wrath greedy consuming educational corrupting; do
  cp "../ForbiddenMagic/src/main/resources/assets/forbidden/textures/misc/$name.png" "src/main/resources/assets/forbiddenmagic/textures/misc/$name.png"
done
```

- [ ] **Step 4: Зарегистрировать исследования**

В конец `FmResearch.register()`, под `if (FmConfig.enchantmentsEnabled()) { ... }`:

```java
new ResearchItem("WRATH", "FORBIDDEN", new AspectList().add(FmAspects.WRATH, 16).add(Aspect.WEAPON, 20).add(Aspect.MAGIC, 10), -6, -3, 3, ForbiddenMagic.id("textures/misc/wrath.png"))
    .setPages(ResearchPage.text("forbiddenmagic.research_page.WRATH.1"), ResearchPage.infusionEnchantment("WRATH"))
    .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
    .setConcealed()
    .setWarp(3)
    .registerResearchItem();
new ResearchItem("GREEDY", "FORBIDDEN", new AspectList().add(Aspect.MAGIC, 2).add(Aspect.WEAPON, 1).add(Aspect.GREED, 3), -6, -5, 2, ForbiddenMagic.id("textures/misc/greedy.png"))
    .setPages(ResearchPage.text("forbiddenmagic.research_page.GREEDY.1"), ResearchPage.infusionEnchantment("GREEDY"))
    .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
    .setConcealed()
    .registerResearchItem();
new ResearchItem("CORRUPTING", "FORBIDDEN", new AspectList().add(FmAspects.NETHER, 5).add(Aspect.CRYSTAL, 2).add(Aspect.EXCHANGE, 1), -6, -4, 1, ForbiddenMagic.id("textures/misc/corrupting.png"))
    .setPages(ResearchPage.text("forbiddenmagic.research_page.CORRUPTING.1"), ResearchPage.infusionEnchantment("CORRUPTING"))
    .setParents("NETHERSHARDS", "INFUSIONENCHANTMENT")
    .setConcealed()
    .registerResearchItem();
new ResearchItem("CONSUMING", "FORBIDDEN", new AspectList().add(Aspect.VOID, 4).add(Aspect.ENTROPY, 3).add(Aspect.MAGIC, 2), -4, -8, 1, ForbiddenMagic.id("textures/misc/consuming.png"))
    .setPages(ResearchPage.text("forbiddenmagic.research_page.CONSUMING.1"), ResearchPage.infusionEnchantment("CONSUMING"))
    .setParents("INFUSIONENCHANTMENT")
    .setSecondary()
    .setConcealed()
    .registerResearchItem();
new ResearchItem("EDUCATIONAL", "FORBIDDEN", new AspectList().add(Aspect.MIND, 5).add(Aspect.WEAPON, 1).add(Aspect.MAGIC, 3), -4, -7, 2, ForbiddenMagic.id("textures/misc/educational.png"))
    .setPages(ResearchPage.text("forbiddenmagic.research_page.EDUCATIONAL.1"), ResearchPage.infusionEnchantment("EDUCATIONAL"))
    .setParents("INFUSIONENCHANTMENT")
    .setSecondary()
    .setConcealed()
    .registerResearchItem();
```

- [ ] **Step 5: Добавить тексты страниц**

В `tools/lang/en_us.toml` добавить секции с текстами оригинала (скопировать дословно из `../ForbiddenMagic/src/main/resources/assets/forbidden/lang/en_US.lang`, ключи `forbidden.research_page.<KEY>.1` → `forbiddenmagic.research_page.<KEY>.1`):

```toml
[forbiddenmagic.research_page.WRATH]
1 = "Ugh. You need more excessively violent weapons to kill things with. Enchanting is a good start, but there's only so much Sharpness you can squeeze into a typical sword before it starts to liquefy and sprout daisies instead. Your weapons just aren't killing things hard enough.<BR><BR>To amend this, you've created a special enchantment to force even more violence and death into a weapon. The Wrath enchantment increases the offensive power of a weapon, but is still compatible with Sharpness, Bane of Arthropods, and Smite."

[forbiddenmagic.research_page.GREEDY]
1 = "You've discovered a weapon enchantment that allows you to profit from taking lives. Every time a Capitalist weapon slays a hostile creature, there's a chance to drop an Emerald Fragment. If a Capitalist Weapon is used to kill a villager, it will instead drop one of the villager's emeralds."

[forbiddenmagic.research_page.CORRUPTING]
1 = "The Nether is great and all, but it's also kind of a... Hell Hole.<BR><BR>The Corrupting enchant allows you to harvest Nether Shards without having to be in the Nether.  A pickaxe enchanted with Corrupting will randomly corrupt harvested elemental shards, changing them into nether shards. The shards only seem susceptible while they are encased in infused stone, so corrupting a shard cluster will have no effect."

[forbiddenmagic.research_page.CONSUMING]
1 = "Sick of having to empty your pack of worthless junk, you've devised an enchantment that will automatically destroy common, unwanted materials.  Any Cobblestone, Dirt, Gravel, or Netherrack mined by a Consuming tool will be destroyed instead of falling to the ground."

[forbiddenmagic.research_page.EDUCATIONAL]
1 = "You've created an enchantment to infuse a weapon with the very essence of knowledge and learning. Hammering such learning into monsters' heads will result in them dropping more experience than normal, depending on the level of enchantment."
```

В `ru_ru.toml` — русские тексты из `ru_RU.lang` с теми же ключами:

```toml
[forbiddenmagic.research_page.WRATH]
1 = "Ах! Вам нужно больше чрезмерно жестокого оружия для убийства существ. Зачарование - хорошее начало, только так можно заточить меч прежде чем он начнёт превращаться в жидкость. Ваш меч просто убивает недостаточно эффективно.<BR><BR>Чтобы исправить это, Вы создали специальное зачарование, которое внедряет ещё больше силы и смерти в Ваш меч. Зачарование: Гнев увеличивает наступательную силу меча, оно также совместимо с зачарованиями острота, бич членистоногих и небесная кара."

[forbiddenmagic.research_page.GREEDY]
1 = "Вы обнаружили зачарование для оружия, которое позволяет получать выгоду, забирая жизни. При убийстве оружием капиталиста враждебных существ, существует шанс получения изумрудного осколка. Если оружие капиталиста используется для убийства жителя деревни, то существует шанс получения его изумруда."

[forbiddenmagic.research_page.CORRUPTING]
1 = "Нижний мир великолепен, но он также что-то вроде.. адской дыры.<BR><BR>Зачарование: Искажение позволит Вам собирать осколки Нижнего мира, при этом не находясь в Нижнем мире.  Кирка зачарованная искажением будет случайно искажать собранные осколки стихий, превращая их в осколки Нижнего мира. Осколки кажутся восприимчивыми только тогда, когда они помещены в наполненный камень, это значит что искажение кластера не будет иметь никого эффекта."

[forbiddenmagic.research_page.CONSUMING]
1 = "Из-за боли от опустошения Вашего инвентаря от ненужного мусора, Вы разработали зачарование, которое будет автоматически уничтожать распространённые, ненужные материалы.  Любой булыжник, земля, гравий или камень Нижнего мира, добытый инструментом потребления, будет уничтожен."

[forbiddenmagic.research_page.EDUCATIONAL]
1 = "Вы создали зачарование для наполнения оружия эссенцией знаний и изучения. Уничтожение голов монстров учёбой даст больше опыта чем обычно, в зависимости от уровня зачарования."
```

- [ ] **Step 6: Сгенерировать и прогнать**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 24 required tests passed`.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmResearch.java src/main/resources/assets/forbiddenmagic/textures/misc tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the enchantment research pages"
```

---

### Task 7: Infusion-рецепты

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: `FmEnchantments` keys, `FmItems.DEADLY_SHARDS`.
- Produces: рецепты с ключами `WRATH`, `GREEDY`, `CONSUMING`, `EDUCATIONAL`, `CORRUPTING`.

- [ ] **Step 1: Падающий тест**

Добавить `"enchantment_recipes_cover_five"` (24) в `TESTS` и `FUNCTIONS.register(TESTS[24], () -> FmGameTests::enchantmentRecipesCoverFive);`. Метод:

```java
private static void enchantmentRecipesCoverFive(GameTestHelper helper) {
    for (String key : new String[]{"WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING"}) {
        TcRecipe recipe = TcRecipes.get(key);
        helper.assertTrue(recipe instanceof InfusionEnchantmentRecipe, key + " must be an infusion enchantment recipe");
        InfusionEnchantmentRecipe infusion = (InfusionEnchantmentRecipe) recipe;
        helper.assertValueEqual(infusion.enchantment(), Identifier.parse("forbiddenmagic:" + key.toLowerCase(java.util.Locale.ROOT)), key + " enchantment id");
    }
    InfusionEnchantmentRecipe wrath = (InfusionEnchantmentRecipe) TcRecipes.get("WRATH");
    helper.assertValueEqual(wrath.instability(), 8, "wrath instability");
    helper.assertValueEqual(wrath.aspects().getAmount(FmAspects.WRATH), 16, "wrath aspect cost");
    helper.succeed();
}
```

Импорты: `thaumcraft.crafting.InfusionEnchantmentRecipe`, `thaumcraft.crafting.TcRecipe`, `thaumcraft.crafting.TcRecipes`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: `WRATH must be an infusion enchantment recipe`.

- [ ] **Step 3: Реализация**

В `FmRecipes` расширить `OWN_KEYS`:

```java
public static final Set<String> OWN_KEYS = Set.of("SKULLAXE", "WRATH", "GREEDY", "CONSUMING", "EDUCATIONAL", "CORRUPTING");
```

И в `register()`, под `if (FmConfig.enchantmentsEnabled()) { ... }`, добавить пять рецептов:

```java
TcRecipes.register(new InfusionEnchantmentRecipe("WRATH", "WRATH", ForbiddenMagic.id("wrath"), 8,
    new AspectList().add(FmAspects.WRATH, 16).add(Aspect.WEAPON, 16).add(FmAspects.NETHER, 8),
    List.of(
        TcIngredient.item("thaumcraft:salis_mundus"),
        TcIngredient.item("minecraft:diamond_sword"),
        TcIngredient.item(FmItems.id("wrath").toString()),
        TcIngredient.item(FmItems.id("wrath").toString()),
        TcIngredient.item(FmItems.id("wrath").toString())
    )));
TcRecipes.register(new InfusionEnchantmentRecipe("GREEDY", "GREEDY", ForbiddenMagic.id("greedy"), 4,
    new AspectList().add(FmAspects.NETHER, 16).add(Aspect.WEAPON, 8).add(Aspect.GREED, 16),
    List.of(
        TcIngredient.item("minecraft:golden_sword"),
        TcIngredient.item("minecraft:diamond"),
        TcIngredient.item(FmItems.id("greed").toString()),
        TcIngredient.item(FmItems.id("greed").toString()),
        TcIngredient.item("thaumcraft:salis_mundus")
    )));
TcRecipes.register(new InfusionEnchantmentRecipe("CONSUMING", "CONSUMING", ForbiddenMagic.id("consuming"), 3,
    new AspectList().add(Aspect.VOID, 8).add(Aspect.TOOL, 8).add(Aspect.HUNGER, 8),
    List.of(
        TcIngredient.item("minecraft:iron_pickaxe"),
        TcIngredient.item("minecraft:lava_bucket"),
        TcIngredient.item("thaumcraft:salis_mundus")
    )));
TcRecipes.register(new InfusionEnchantmentRecipe("EDUCATIONAL", "EDUCATIONAL", ForbiddenMagic.id("educational"), 3,
    new AspectList().add(Aspect.MAGIC, 4).add(Aspect.WEAPON, 4).add(Aspect.MIND, 8),
    List.of(
        TcIngredient.item("thaumcraft:zombie_brain"),
        TcIngredient.item("minecraft:book"),
        TcIngredient.item("thaumcraft:salis_mundus")
    )));
TcRecipes.register(new InfusionEnchantmentRecipe("CORRUPTING", "CORRUPTING", ForbiddenMagic.id("corrupting"), 4,
    new AspectList().add(FmAspects.NETHER, 16).add(Aspect.EXCHANGE, 16).add(Aspect.CRYSTAL, 8),
    List.of(
        TcIngredient.item("minecraft:nether_wart"),
        TcIngredient.item("minecraft:soul_sand"),
        TcIngredient.item("thaumcraft:salis_mundus")
    )));
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 25 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the five enchantment infusion recipes"
```

---

### Task 8: Thaumic Tinkerer (опционально)

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmTinkerer.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmSetup.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

TT не входит в compile classpath (All Rights Reserved, отдельная сборка), поэтому интеграция вызывает публичный API через reflection, как и оригинал. Сборка порта от TT не зависит.

**Interfaces:**
- Produces: `FmTinkerer.register()`, `FmTinkerer.registeredCount() → int`.

- [ ] **Step 1: Падающий тест**

Добавить `"tinkerer_integration_is_optional"` (25) в `TESTS` и `FUNCTIONS.register(TESTS[25], () -> FmGameTests::tinkererIntegrationIsOptional);`. Метод:

```java
private static void tinkererIntegrationIsOptional(GameTestHelper helper) {
    boolean loaded = net.neoforged.fml.ModList.get().isLoaded("thaumictinkerer");
    if (!loaded) {
        helper.assertValueEqual(FmTinkerer.registeredCount(), 0, "no TT means no registered costs");
    } else {
        helper.assertValueEqual(FmTinkerer.registeredCount(), 5, "TT receives five enchantment costs");
    }
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `FmTinkerer`.

- [ ] **Step 3: Реализация**

`FmTinkerer.java`:

```java
package dev.visfox.forbiddenreforged;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.fml.ModList;
import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class FmTinkerer {
    private static int registered;

    private FmTinkerer() {
    }

    public static void register() {
        if (!FmConfig.enchantmentsEnabled() || !ModList.get().isLoaded("thaumictinkerer")) {
            return;
        }
        try {
            Class<?> manager = Class.forName("thaumictinkerer.enchanting.EnchantmentManager");
            Method data = manager.getMethod("data");
            Map<Object, Object> costs = (Map<Object, Object>) data.invoke(null);
            Class<?> datum = Class.forName("thaumictinkerer.enchanting.EnchantmentManager$Data");
            Constructor<?> constructor = datum.getConstructor(ResourceKey.class, net.minecraft.resources.Identifier.class, boolean.class, AspectList.class, String.class);
            add(costs, constructor, FmEnchantments.WRATH, "textures/misc/wrath.png", new AspectList().add(Aspect.ENTROPY, 6).add(Aspect.FIRE, 6), "WRATH");
            add(costs, constructor, FmEnchantments.GREEDY, "textures/misc/greedy.png", new AspectList().add(Aspect.ENTROPY, 50).add(Aspect.EARTH, 30).add(Aspect.ORDER, 20), "GREEDY");
            add(costs, constructor, FmEnchantments.EDUCATIONAL, "textures/misc/educational.png", new AspectList().add(Aspect.AIR, 10).add(Aspect.FIRE, 10).add(Aspect.WATER, 10).add(Aspect.EARTH, 10).add(Aspect.ORDER, 15).add(Aspect.ENTROPY, 15), "EDUCATIONAL");
            add(costs, constructor, FmEnchantments.CONSUMING, "textures/misc/consuming.png", new AspectList().add(Aspect.ENTROPY, 10), "CONSUMING");
            add(costs, constructor, FmEnchantments.CORRUPTING, "textures/misc/corrupting.png", new AspectList().add(Aspect.ENTROPY, 20).add(Aspect.FIRE, 15).add(Aspect.EARTH, 10), "CORRUPTING");
            registered = 5;
        } catch (ReflectiveOperationException | LinkageError error) {
            ForbiddenMagic.LOGGER.warn("Thaumic Tinkerer is present but its enchantment API is missing", error);
        }
    }

    public static int registeredCount() {
        return registered;
    }

    private static void add(Map<Object, Object> costs, Constructor<?> constructor, ResourceKey<Enchantment> key, String texture, AspectList aspects, String research) throws ReflectiveOperationException {
        Object datum = constructor.newInstance(key, ForbiddenMagic.id(texture), false, aspects, research);
        costs.put(key, datum);
    }
}
```

В `FmSetup.commonSetup`, после `FmResearch.register()`:

```java
FmTinkerer.register();
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 26 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmTinkerer.java src/main/java/dev/visfox/forbiddenreforged/FmSetup.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: register enchantment costs with Thaumic Tinkerer"
```

---

### Task 9: Документация, отступления и гейт

**Files:**
- Modify: `docs/deviations.md`
- Modify: `docs/roadmap.md`
- Modify: `docs/verification.md`
- Modify: `README.md`

- [ ] **Step 1: Записать отступления**

В `docs/deviations.md` добавить строки:

```markdown
| Corrupting | блок infused stone, заменяет осколки стихий | то же; в 26.3 infused stone сам дропает `thaumcraft:*_shard`, поэтому цель — тег `forbiddenmagic:infused_stones` |
| Стоимость зачарований | min/max enchantability 1.7.10 | `min_cost`/`max_cost` в JSON; стол зачарований не предлагает их ни в одной версии |
| `enchanting = false` | зачарования не регистрируются | JSON остаётся в реестре, но эффекты, исследования, рецепты и TT-стоимость отключены |
| TT-стоимость Cluster/Impact/Voidtouched | в этом слайсе | регистрируется вместе с морф-инструментами |
```

- [ ] **Step 2: Обновить roadmap и README**

`docs/roadmap.md`, в «После M1 — полный объём §5.1», заменить первую строку:

```markdown
- зачарования: Wrath, Capitalist, Consuming, Educational, Corrupting — готовы; Cluster, Impact, Voidtouched — вместе с морф-инструментами, там же их TT-стоимость;
```

`README.md`, в «Поддерживается», заменить строку про TT и добавить строку о пяти зачарованиях:

```markdown
- Пять зачарований: Wrath, Capitalist, Consuming, Educational, Corrupting; Cluster, Impact и Voidtouched появятся вместе с морф-инструментами.
- Опциональная интеграция с Thaumic Tinkerer Reforged для этих пяти зачарований, если TT установлен.
```

- [ ] **Step 3: Полный гейт**

```bash
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
python tools/gen_resources.py && python tools/gen_resources.py
git status --porcelain src/generated
python tools/check_provenance.py
python tools/prepare_host.py --verify
```

Expected: build success, `All 26 required tests passed`, второй прогон генератора не меняет `src/generated`, `provenance ok`, `host matches sha + 3 patch commit(s)`.

- [ ] **Step 4: Записать замеры**

В `docs/verification.md` заменить верхнюю строку на новый HEAD и обновить таблицу аддона: число тестов 26, артефакт нового размера. Прогнать `runClient` не требуется для этого слайса, но отметить, что текстуры зачарований проверяются в Thaumonomicon.

- [ ] **Step 5: Commit и метка**

```bash
git add docs README.md
git commit -m "docs: record the enchantment slice"
git tag -a m2-rc1 -m "M2 first slice: five standalone enchantments"
```

---

## Self-Review

**Покрытие спеки:** слайс закрывает пять зачарований, их исследования, рецепты, TT-стоимость и рубильник `enchanting`. Три оставшихся зачарования явно вынесены в слайс морф-инструментов с причиной (эффекты в классах инструментов, родитель `MORPHTOOLS`).

**Плейсхолдеры:** все шаги содержат код или точную команду; тексты страниц вставлены дословно из оригинала.

**Согласованность типов:** `FmEnchantments.level` принимает `RegistryAccess`, `ItemStack`, `ResourceKey`; все обработчики вызывают его одинаково. `FmEnchantmentEffects.register()` вызывается один раз в конструкторе. `FmTinkerer.registeredCount()` возвращает 0, если TT нет или зачарования отключены.
