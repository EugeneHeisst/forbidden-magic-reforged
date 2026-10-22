# Forbidden Magic: Reforged — кристалл моба (M7a) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Портировать `ItemMobCrystal`, карту «моб → аспект» и импринт кристалла при убийстве — фундамент для Клетки Гнева (M7b).

**Architecture:** Новый компонент `MOB_ID` (`String`) хранит id сущности в стеке. `FmSpawnerMobs` — статическая карта id → аспект и правило импринта. Импринт ищет первый пустой кристалл в инвентаре убийцы в существующем обработчике `LivingDropsEvent`. Модель предмета выбирается по наличию компонента через `minecraft:has_component`.

**Tech Stack:** NeoForge 26.3.0.57-beta, Java 25, ModDevGradle, Thaumcraft 4: Reforged 1.2, GameTest.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md`.

## Граница слайса

Входит: `mob_crystal`, карта аспектов, конфиг `wrathCage`/`wrathCost`/`wrathEff`/`wrathCrazy`, импринт, lang, модели, тесты.

Не входит: блок Клетки Гнева, block entity, спавнер, рендерер, рецепт и исследование — это M7b.

## Global Constraints

- Хост не редактируется; патчи не меняются.
- id сущностей — современные: `minecraft:zombie`, `thaumcraft:brainy_zombie` и т.д.
- Импринт работает только при `wrathCage = true`; `wrathCrazy` разрешает любого не-босса.
- Один Task — один коммит; после Task: build, runGameTestServer, check_provenance.

---

### Task 1: Кристалл, карта аспектов и импринт

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmConfig.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmComponents.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmSpawnerMobs.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/MobCrystalItem.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmItems.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/item/{emptycrystal,mobcrystal}.png`
- Modify: `tools/gen_resources.py`, `data/lang/*.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `FmComponents.MOB_ID`, `MobCrystalItem.mob(ItemStack)`, `MobCrystalItem.setMob(ItemStack, String)`, `FmSpawnerMobs.aspect(String)`, `FmSpawnerMobs.canImprint(EntityType<?>)`, `FmSpawnerMobs.ids()`, `FmConfig.wrathCage()/wrathCost()/wrathEff()/wrathCrazy()`, `FmItems.MOB_CRYSTAL`.

- [ ] **Step 1: Падающий тест**

Добавить `"mob_crystal_and_spawn_table"` (42):

```java
private static void mobCrystalAndSpawnTable(GameTestHelper helper) {
    helper.assertTrue(FmSpawnerMobs.aspect("minecraft:zombie") == Aspect.FLESH, "zombie carries flesh");
    helper.assertTrue(FmSpawnerMobs.aspect("thaumcraft:brainy_zombie") == Aspect.MIND, "brainy zombie carries mind");
    helper.assertTrue(FmSpawnerMobs.aspect("minecraft:pig") == Aspect.BEAST, "pig carries beast");
    helper.assertTrue(FmSpawnerMobs.ids().size() >= 40, "the table keeps the original mobs");
    ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
    helper.assertValueEqual(MobCrystalItem.mob(stack), null, "a fresh crystal is empty");
    MobCrystalItem.setMob(stack, "minecraft:zombie");
    helper.assertValueEqual(MobCrystalItem.mob(stack), "minecraft:zombie", "the crystal keeps the mob id");
    helper.assertTrue(FmSpawnerMobs.canImprint(EntityType.ZOMBIE), "zombies can be imprinted");
    helper.assertFalse(FmSpawnerMobs.canImprint(EntityType.ENDER_DRAGON), "bosses cannot be imprinted");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

`cmd //c gradlew.bat runGameTestServer` → компиляция падает на `FmSpawnerMobs`.

- [ ] **Step 3: Конфиг**

В `FmConfig`:

```java
public static ModConfigSpec.BooleanValue wrathCage;
public static ModConfigSpec.IntValue wrathCost;
public static ModConfigSpec.IntValue wrathEff;
public static ModConfigSpec.BooleanValue wrathCrazy;
```

```java
wrathCage = BUILDER.comment("Enable the Wrath Cage.").define("general.wrath_cage", true);
wrathCost = BUILDER.comment("Essentia cost per spawn round.").defineInRange("general.wrath_cost", 5, 0, 64);
wrathEff = BUILDER.comment("Spawns granted per essentia cost.").defineInRange("general.wrath_eff", 4, 1, 64);
wrathCrazy = BUILDER.comment("Let the cage imprint on any non-boss mob.").define("general.wrath_crazy", false);
```

Геттеры с fallback на default, как `lustDisabled()`.

- [ ] **Step 4: Компонент**

В `FmComponents`:

```java
public static final Supplier<DataComponentType<String>> MOB_ID =
    COMPONENTS.registerComponentType("mob_id", builder -> builder
        .persistent(Codec.STRING)
        .networkSynchronized(ByteBufCodecs.STRING_UTF8));
```

Импорты `com.mojang.serialization.Codec`, `net.minecraft.network.codec.ByteBufCodecs`.

- [ ] **Step 5: `FmSpawnerMobs`**

Карта из оригинала с современными id:

```java
public final class FmSpawnerMobs {
    private static final Map<String, Aspect> MOBS = Map.ofEntries(
        Map.entry("minecraft:zombie", Aspect.FLESH),
        Map.entry("minecraft:skeleton", Aspect.DEATH),
        Map.entry("minecraft:creeper", Aspect.FIRE),
        Map.entry("minecraft:horse", Aspect.BEAST),
        Map.entry("minecraft:pig", Aspect.BEAST),
        Map.entry("minecraft:sheep", Aspect.CLOTH),
        Map.entry("minecraft:cow", Aspect.BEAST),
        Map.entry("minecraft:mooshroom", Aspect.PLANT),
        Map.entry("minecraft:ocelot", Aspect.BEAST),
        Map.entry("minecraft:chicken", Aspect.FLIGHT),
        Map.entry("minecraft:squid", Aspect.SENSES),
        Map.entry("minecraft:wolf", Aspect.BEAST),
        Map.entry("minecraft:bat", Aspect.FLIGHT),
        Map.entry("minecraft:spider", Aspect.CLOTH),
        Map.entry("minecraft:slime", Aspect.SLIME),
        Map.entry("minecraft:ghast", FmAspects.NETHER),
        Map.entry("minecraft:zombified_piglin", Aspect.GREED),
        Map.entry("minecraft:enderman", Aspect.ELDRITCH),
        Map.entry("minecraft:cave_spider", Aspect.POISON),
        Map.entry("minecraft:silverfish", Aspect.GREED),
        Map.entry("minecraft:blaze", Aspect.FIRE),
        Map.entry("minecraft:magma_cube", Aspect.FIRE),
        Map.entry("minecraft:witch", Aspect.MAGIC),
        Map.entry("minecraft:villager", Aspect.GREED),
        Map.entry("thaumcraft:firebat", Aspect.FIRE),
        Map.entry("thaumcraft:wisp", Aspect.AURA),
        Map.entry("thaumcraft:thaumic_slime", Aspect.TAINT),
        Map.entry("thaumcraft:brainy_zombie", Aspect.MIND),
        Map.entry("thaumcraft:giant_brainy_zombie", Aspect.MIND),
        Map.entry("thaumcraft:taint_spider", Aspect.TAINT),
        Map.entry("thaumcraft:taint_swarm", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_pig", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_sheep", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_cow", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_chicken", Aspect.TAINT),
        Map.entry("thaumcraft:tainted_villager", Aspect.TAINT),
        Map.entry("thaumcraft:cultist_knight", Aspect.ELDRITCH),
        Map.entry("thaumcraft:cultist_cleric", Aspect.ELDRITCH),
        Map.entry("thaumcraft:eldritch_crab", Aspect.ELDRITCH),
        Map.entry("thaumcraft:inhabited_zombie", Aspect.ELDRITCH),
        Map.entry("thaumcraft:pech", Aspect.GREED),
        Map.entry("thaumcraft:eldritch_guardian", Aspect.ELDRITCH)
    );

    public static @Nullable Aspect aspect(@Nullable String id) {
        return id == null ? null : MOBS.get(id);
    }

    public static List<String> ids() {
        return List.copyOf(MOBS.keySet());
    }

    public static boolean canImprint(EntityType<?> type) {
        if (!FmConfig.wrathCage()) {
            return false;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        return MOBS.containsKey(id) || (FmConfig.wrathCrazy() && !isBoss(type));
    }

    private static boolean isBoss(EntityType<?> type) {
        Class<? extends Entity> base = type.getBaseClass();
        return WitherBoss.class.isAssignableFrom(base)
            || EnderDragon.class.isAssignableFrom(base)
            || ThaumcraftBoss.class.isAssignableFrom(base);
    }
}
```

Проверку боссов реализовать через `type.getBaseClass()`: `WitherBoss.class.isAssignableFrom(...)`, `EnderDragon.class.isAssignableFrom(...)`, `ThaumcraftBoss.class.isAssignableFrom(type.getBaseClass())` — без создания сущности.

- [ ] **Step 6: `MobCrystalItem`**

```java
public class MobCrystalItem extends Item {
    public MobCrystalItem(Properties properties) {
        super(properties);
    }

    public static @Nullable String mob(ItemStack stack) {
        return stack.get(FmComponents.MOB_ID.get());
    }

    public static void setMob(ItemStack stack, String id) {
        stack.set(FmComponents.MOB_ID.get(), id);
    }

    @Override
    public Component getName(ItemStack stack) {
        String id = mob(stack);
        if (id == null) {
            return Component.translatable("item.forbiddenmagic.mob_crystal.empty");
        }
        return Component.translatable("item.forbiddenmagic.mob_crystal.filled",
            Component.translatable(Util.makeDescriptionId("entity", Identifier.parse(id))));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Aspect aspect = FmSpawnerMobs.aspect(mob(stack));
        if (aspect != null) {
            tooltip.accept(aspect.getDescription());
        }
    }
}
```

- [ ] **Step 7: Регистрация и вкладка**

`FmItems`: `public static final DeferredItem<Item> MOB_CRYSTAL;` и

```java
MOB_CRYSTAL = ITEMS.registerItem("mob_crystal", MobCrystalItem::new,
    properties -> properties.stacksTo(1).rarity(Rarity.UNCOMMON));
```

`FmCreativeTab`: принять пустой кристалл и по одному заполненному на каждый id из `FmSpawnerMobs.ids()`:

```java
event.accept(FmItems.MOB_CRYSTAL.get());
for (String id : FmSpawnerMobs.ids()) {
    ItemStack filled = new ItemStack(FmItems.MOB_CRYSTAL.get());
    MobCrystalItem.setMob(filled, id);
    event.accept(filled);
}
```

- [ ] **Step 8: Импринт**

В `FmEnchantmentEffects.onLivingDrops`, после существующих дропов:

```java
        if (FmSpawnerMobs.canImprint(event.getEntity().getType())) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                if (candidate.is(FmItems.MOB_CRYSTAL.get()) && MobCrystalItem.mob(candidate) == null) {
                    MobCrystalItem.setMob(candidate, id);
                    break;
                }
            }
        }
```

- [ ] **Step 9: Ресурсы**

Скопировать `emptycrystal.png` и `mobcrystal.png` из `../ForbiddenMagic/src/main/resources/assets/forbidden/textures/items/`. В `gen_resources.py` добавить item-модели:

```python
write_json(ASSETS / "models" / "item" / "mob_crystal_empty.json", {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "forbiddenmagic:item/emptycrystal"},
})
write_json(ASSETS / "models" / "item" / "mob_crystal_filled.json", {
    "parent": "minecraft:item/generated",
    "textures": {"layer0": "forbiddenmagic:item/mobcrystal"},
})
write_json(ASSETS / "items" / "mob_crystal.json", {
    "model": {
        "type": "minecraft:condition",
        "property": "minecraft:has_component",
        "component": "forbiddenmagic:mob_id",
        "on_true": {"type": "minecraft:model", "model": "forbiddenmagic:item/mob_crystal_filled"},
        "on_false": {"type": "minecraft:model", "model": "forbiddenmagic:item/mob_crystal_empty"},
    },
})
```

Lang en: `mob_crystal.empty = "Mob Crystal"`, `mob_crystal.filled = "Mob Crystal (%s)"`; ru: «Кристалл моба», «Кристалл моба (%s)».

- [ ] **Step 10: Зелёный прогон и коммит**

`python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer` → `All 43 required tests passed`.

```bash
git add src/main/java src/main/resources tools/gen_resources.py data/lang src/generated tests
git commit -m "feat: add the mob crystal and spawn table"
```

---

### Task 2: Документация и гейт

- [ ] **Step 1: Отступления**

```markdown
| Кристалл моба: варианты | мета 0/1 в одном предмете | компонент `forbiddenmagic:mob_id`, модель через `has_component` | в 26.3 нет метапредметов |
| Импринт | любый моб из `Config.spawnerMobs` + `wrathCrazy` | карта современных id в `FmSpawnerMobs` | имена 1.7.10 заменены на id 26.3 |
```

- [ ] **Step 2: Roadmap/README/verification** — отметить M7a, 43 теста, артефакт, коммит и тег `m7a-rc1`.

- [ ] **Step 3: Гейт** — build, тесты, два прогона `no_lust`, генератор ×2, provenance, host verify.

---

## Self-Review

Карта покрывает все 42 моба оригинала (24 vanilla + 18 Thaumcraft), импринт ограничен конфигом, боссы исключены. Кристалл использует компонент и условную модель. Импринт встроен в существующий обработчик дропов и не меняет его поведение. M7b (клетка) строится на этих интерфейсах.
