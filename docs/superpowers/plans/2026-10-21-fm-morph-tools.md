# Forbidden Magic: Reforged — морф-инструменты (слайс 3) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Портировать четыре хамелеоновых инструмента с тремя переключаемыми наборами зачарований, их исследования и рецепты, и закрыть три оставшихся зачарования (Cluster, Impact, Voidtouched) вместе с TT-стоимостью.

**Architecture:** Инструменты — обычные `Item` с `Registration.pickaxe/sword/shovel/axe`, поверх которых лежит кастомный компонент `MorphState` (фаза + три набора `ItemEnchantments` + три имени). Переключение — `use` с зажатым Shift: сохранить текущий набор в слот фазы, увеличить фазу, загрузить следующий набор, снять 5 прочности, проиграть `ModSounds.WANDFAIL`. Impact и Voidtouched живут в самом item-классе (как в оригинале), Cluster — в общем обработчике дропов.

**Tech Stack:** NeoForge 26.3.0.57-beta, Java 25, ModDevGradle, Thaumcraft 4: Reforged 1.2, GameTest.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md`.

## Граница слайса

Входит: `morph_pickaxe`, `morph_sword`, `morph_shovel`, `morph_axe`; фазы зачарований; Cluster, Impact, Voidtouched; исследование `MORPHTOOLS`; рецепты инструментов и трёх зачарований; TT-стоимость трёх зачарований (всего 8).

Не входит: цветной оверлей фазы (в этом слайсе оверлей статичный — отступление), морф-инструменты в worldgen/дропах, остальные предметы полного §5.1.

## Global Constraints

- Пакет `dev.visfox.forbiddenreforged`, modId `forbiddenmagic`; хост не редактируется, патчи не меняются.
- Числа и тексты — 1:1 из `../ForbiddenMagic`; отступления фиксируются в `docs/deviations.md`.
- Ресурсы — только `tools/gen_resources.py`; повторный прогон не меняет файлы.
- Конфиг `general.enchanting` гасит только зачарования; инструменты и их рецепты от него не зависят (как в оригинале, где `Config.enchanting` гейтит `DarkEnchantments.hex`, а не tools).
- Один Task — один коммит; после Task: `build`, `runGameTestServer`, `check_provenance.py`.

---

### Task 1: Компонент фаз и четыре инструмента

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmComponents.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/MorphState.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/MorphToolItem.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmItems.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/item/{chameleonpick,eyepick,chameleonaxe,eyeaxe,chameleonshovel,eyeshovel,chameleonsword,eyesword}.png`
- Modify: `tools/gen_resources.py`
- Create: `src/main/resources/data/minecraft/tags/item/{pickaxes,swords,axes,shovels}.json`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Consumes: `FmItems.ITEMS`, `FmEnchantments.level`.
- Produces: `FmComponents.MORPH_STATE`, `MorphState` (`phase()`, `sets()`, `names()`, `store(ItemEnchantments, String)`, `advance()`, `currentSet()`, `currentName()`, `EMPTY`), `FmItems.MORPH_PICKAXE/SWORD/SHOVEL/AXE`, `MorphToolItem`.

- [ ] **Step 1: Падающие тесты**

Добавить `"morph_tools_are_registered"` (26) и `"morph_phase_rotates_sets"` (27) в `TESTS`/`FUNCTIONS`, затем:

```java
private static void morphToolsAreRegistered(GameTestHelper helper) {
    for (var item : new net.neoforged.neoforge.registries.DeferredItem<?>[]{
        FmItems.MORPH_PICKAXE, FmItems.MORPH_SWORD, FmItems.MORPH_SHOVEL, FmItems.MORPH_AXE}) {
        ItemStack stack = new ItemStack(item.get());
        helper.assertTrue(stack.getMaxDamage() == 1500, "morph tools use elemental durability");
        helper.assertValueEqual(stack.getRarity(), net.minecraft.world.item.Rarity.EPIC, "morph tools are epic");
        helper.assertTrue(stack.get(DataComponents.REPAIRABLE) != null, "morph tools declare a repair item");
        helper.assertTrue(stack.get(DataComponents.REPAIRABLE).isValidRepairItem(new ItemStack(FmItems.DEADLY_SHARDS.get("envy").get())), "envy shards repair morph tools");
    }
    helper.succeed();
}

private static void morphPhaseRotatesSets(GameTestHelper helper) {
    MorphState state = MorphState.EMPTY.store(ItemEnchantments.EMPTY, "Sharpness").advance();
    helper.assertValueEqual(state.phase(), 1, "phase advances");
    helper.assertValueEqual(state.names().get(0), "Sharpness", "the old name is kept in slot zero");
    helper.assertValueEqual(state.currentName(), "", "the new slot starts empty");
    MorphState wrapped = state.store(ItemEnchantments.EMPTY, "Smite").advance().advance();
    helper.assertValueEqual(wrapped.phase(), 0, "three phases wrap back to zero");
    helper.assertValueEqual(wrapped.currentName(), "Sharpness", "the first slot name returns");
    helper.succeed();
}
```

Импорты теста: `net.minecraft.core.component.DataComponents`, `net.minecraft.world.item.enchantment.ItemEnchantments`, `dev.visfox.forbiddenreforged.MorphState`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `MorphState`/`FmItems.MORPH_PICKAXE`.

- [ ] **Step 3: `MorphState`**

```java
package dev.visfox.forbiddenreforged;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public record MorphState(int phase, List<ItemEnchantments> sets, List<String> names) {
    public static final int PHASES = 3;
    public static final MorphState EMPTY = new MorphState(
        0,
        List.of(ItemEnchantments.EMPTY, ItemEnchantments.EMPTY, ItemEnchantments.EMPTY),
        List.of("", "", "")
    );
    public static final Codec<MorphState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.intRange(0, PHASES - 1).fieldOf("phase").forGetter(MorphState::phase),
        ItemEnchantments.CODEC.listOf(PHASES, PHASES).fieldOf("sets").forGetter(MorphState::sets),
        Codec.STRING.listOf(PHASES, PHASES).fieldOf("names").forGetter(MorphState::names)
    ).apply(instance, MorphState::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MorphState> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        MorphState::phase,
        ItemEnchantments.STREAM_CODEC.apply(ByteBufCodecs.list(PHASES)),
        MorphState::sets,
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(PHASES)),
        MorphState::names,
        MorphState::new
    );

    public MorphState {
        sets = List.copyOf(sets);
        names = List.copyOf(names);
        if (sets.size() != PHASES || names.size() != PHASES || phase < 0 || phase >= PHASES) {
            throw new IllegalArgumentException("morph state needs three slots and a phase in range");
        }
    }

    public MorphState store(ItemEnchantments set, String name) {
        ItemEnchantments[] nextSets = sets.toArray(ItemEnchantments[]::new);
        String[] nextNames = names.toArray(String[]::new);
        nextSets[phase] = set;
        nextNames[phase] = name;
        return new MorphState(phase, List.of(nextSets), List.of(nextNames));
    }

    public MorphState advance() {
        return new MorphState((phase + 1) % PHASES, sets, names);
    }

    public ItemEnchantments currentSet() {
        return sets.get(phase);
    }

    public String currentName() {
        return names.get(phase);
    }
}
```

- [ ] **Step 4: `FmComponents`**

```java
package dev.visfox.forbiddenreforged;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmComponents {
    private static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ForbiddenMagic.MODID);
    public static final Supplier<DataComponentType<MorphState>> MORPH_STATE =
        COMPONENTS.registerComponentType("morph_state", builder -> builder
            .persistent(MorphState.CODEC)
            .networkSynchronized(MorphState.STREAM_CODEC));

    private FmComponents() {
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
```

- [ ] **Step 5: `MorphToolItem`**

```java
package dev.visfox.forbiddenreforged;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import thaumcraft.item.gear.WarpingGear;
import thaumcraft.registry.ModSounds;

public class MorphToolItem extends Item implements WarpingGear {
    public MorphToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getMaxDamage() - stack.getDamageValue() <= 5) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            MorphState state = stack.getOrDefault(FmComponents.MORPH_STATE.get(), MorphState.EMPTY);
            String current = stack.has(DataComponents.CUSTOM_NAME) ? stack.getHoverName().getString() : "";
            MorphState next = state.store(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY), current).advance();
            stack.set(FmComponents.MORPH_STATE.get(), next);
            stack.set(DataComponents.ENCHANTMENTS, next.currentSet());
            if (next.currentName().isEmpty()) {
                stack.remove(DataComponents.CUSTOM_NAME);
            } else {
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(next.currentName()).withStyle(ChatFormatting.ITALIC));
            }
            stack.hurtAndBreak(5, player, EquipmentSlot.MAINHAND);
            level.playSound(null, player.blockPosition(), ModSounds.WANDFAIL.get(), SoundSource.PLAYERS, 0.2F, 0.2F + player.getRandom().nextFloat() * 0.2F);
        }
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, EquipmentSlot slot) {
        if (!(owner instanceof LivingEntity living) || !stack.isDamaged() || owner.tickCount % 10 != 0) {
            return;
        }
        if (FmEnchantments.level(level.registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 0;
    }
}
```

Варп остаётся нулевым, пока в Task 3 не появится ключ `VOIDTOUCHED`; тест Task 2 проверяет именно 0.

- [ ] **Step 6: Регистрация четырёх предметов**

В `FmItems` добавить поля и регистрацию:

```java
    public static final DeferredItem<Item> MORPH_PICKAXE;
    public static final DeferredItem<Item> MORPH_SWORD;
    public static final DeferredItem<Item> MORPH_SHOVEL;
    public static final DeferredItem<Item> MORPH_AXE;

    MORPH_PICKAXE = ITEMS.registerItem("morph_pickaxe", MorphToolItem::new,
        properties -> Registration.pickaxe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 2.0F, -2.8F));
    MORPH_SWORD = ITEMS.registerItem("morph_sword", MorphToolItem::new,
        properties -> Registration.sword(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 4.0F, -2.4F));
    MORPH_SHOVEL = ITEMS.registerItem("morph_shovel", MorphToolItem::new,
        properties -> Registration.shovel(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 1.0F, -3.0F));
    MORPH_AXE = ITEMS.registerItem("morph_axe", MorphToolItem::new,
        properties -> Registration.axe(properties.rarity(Rarity.EPIC), TcMaterials.ELEMENTAL_TOOL, 3.0F, -3.0F));
```

Импорты: `thaumcraft.item.gear.TcMaterials`, `thaumcraft.platform.Registration`.

Ремонт энви-осколком — в каждом properties:

```java
    private static Item.Properties repairable(Item.Properties properties) {
        return properties.repairable(DEADLY_SHARDS.get("envy").get());
    }
```

и обернуть: `repairable(Registration.pickaxe(...))`.

- [ ] **Step 7: Вкладка, lang, теги, текстуры, модели**

`FmCreativeTab.addItems`, после `EMERALD_FRAGMENT`:

```java
        event.accept(FmItems.MORPH_PICKAXE.get());
        event.accept(FmItems.MORPH_SWORD.get());
        event.accept(FmItems.MORPH_SHOVEL.get());
        event.accept(FmItems.MORPH_AXE.get());
```

Lang (en):

```toml
morph_pickaxe = "Chameleon Pickaxe"
morph_sword = "Chameleon Sword"
morph_shovel = "Chameleon Shovel"
morph_axe = "Chameleon Axe"
```

Lang (ru):

```toml
morph_pickaxe = "Хамелеоновая кирка"
morph_sword = "Хамелеоновый меч"
morph_shovel = "Хамелеоновая лопата"
morph_axe = "Хамелеоновый топор"
```

Теги `data/minecraft/tags/item/pickaxes.json` и аналогичные:

```json
{
  "replace": false,
  "values": [
    "forbiddenmagic:morph_pickaxe"
  ]
}
```

(для `swords` — `morph_sword`, `axes` — `morph_axe`, `shovels` — `morph_shovel`).

Текстуры:

```bash
for name in chameleonpick eyepick chameleonaxe eyeaxe chameleonshovel eyeshovel chameleonsword eyesword; do
  cp "../ForbiddenMagic/src/main/resources/assets/forbidden/textures/items/$name.png" "src/main/resources/assets/forbiddenmagic/textures/item/$name.png"
done
```

`tools/gen_resources.py`: добавить карту и генерацию двухслойных моделей:

```python
MORPH_TEXTURES = {
    "morph_pickaxe": ("chameleonpick", "eyepick"),
    "morph_sword": ("chameleonsword", "eyesword"),
    "morph_shovel": ("chameleonshovel", "eyeshovel"),
    "morph_axe": ("chameleonaxe", "eyeaxe"),
}

def generate_morph_items() -> None:
    for name, (base, overlay) in sorted(MORPH_TEXTURES.items()):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"forbiddenmagic:item/{base}",
                "layer1": f"forbiddenmagic:item/{overlay}",
            },
        })
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"},
        })
    print(f"morph tools: {len(MORPH_TEXTURES)} two-layer models")
```

Вызвать `generate_morph_items()` в `main()`.

- [ ] **Step 8: Зарегистрировать компоненты в конструкторе**

В `ForbiddenMagic`, перед `FmItems.ITEMS.register(modEventBus)`:

```java
        FmComponents.register(modEventBus);
```

- [ ] **Step 9: Зелёный прогон**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: build success, `All 28 required tests passed`.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged src/main/resources tools/gen_resources.py tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the chameleon tools with three enchantment phases"
```

---

### Task 2: Impact, Voidtouched и варп

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/MorphToolItem.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `MorphToolItem.impactArea(Direction, BlockPos) → List<BlockPos>`, `MorphToolItem.dominantDirection(Vec3) → Direction`, варп 1 при `VOIDTOUCHED`.

- [ ] **Step 1: Падающие тесты**

Добавить `"impact_covers_a_three_by_three_plane"` (28) и `"voidtouched_repairs_and_warps"` (29):

```java
private static void impactCoversAThreeByThreePlane(GameTestHelper helper) {
    java.util.List<BlockPos> area = MorphToolItem.impactArea(Direction.UP, BlockPos.ZERO);
    helper.assertValueEqual(area.size(), 9, "impact covers nine blocks");
    helper.assertTrue(area.contains(new BlockPos(-1, 0, 1)) && area.contains(new BlockPos(1, 0, -1)), "impact stays in the horizontal plane");
    helper.assertTrue(MorphToolItem.impactArea(Direction.EAST, BlockPos.ZERO).contains(new BlockPos(0, 1, 1)), "east impact uses the vertical plane");
    helper.assertValueEqual(MorphToolItem.dominantDirection(new net.minecraft.world.phys.Vec3(0.2, -3.0, 0.1)), Direction.DOWN, "look direction maps to a face");
    helper.succeed();
}

private static void voidtouchedRepairsAndWarps(GameTestHelper helper) {
    ItemStack stack = new ItemStack(FmItems.MORPH_PICKAXE.get());
    ServerPlayer player = mockPlayer(helper);
    helper.assertValueEqual(MorphToolItem.getWarp(stack, player), 0, "plain morph tool carries no warp");
    helper.succeed();
}
```

Импорты: `net.minecraft.core.BlockPos`, `net.minecraft.core.Direction`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `impactArea`.

- [ ] **Step 3: Реализация в `MorphToolItem`**

```java
    public static List<BlockPos> impactArea(Direction facing, BlockPos origin) {
        Direction.Axis axis = facing.getAxis();
        List<BlockPos> area = new ArrayList<>(9);
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                area.add(switch (axis) {
                    case Y -> origin.offset(first, 0, second);
                    case X -> origin.offset(0, first, second);
                    case Z -> origin.offset(first, second, 0);
                });
            }
        }
        return area;
    }

    public static Direction dominantDirection(Vec3 look) {
        double x = Math.abs(look.x);
        double y = Math.abs(look.y);
        double z = Math.abs(look.z);
        if (y >= x && y >= z) {
            return look.y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (x >= z) {
            return look.x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return look.z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
```

```java
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity owner) {
        if (!level.isClientSide()
            && FmEnchantments.level(level.registryAccess(), stack, FmEnchantments.IMPACT) > 0
            && stack.isCorrectToolForDrops(state)) {
            Direction facing = dominantDirection(owner.getLookAngle());
            for (BlockPos target : impactArea(facing, pos)) {
                if (target.equals(pos)) {
                    continue;
                }
                BlockState neighbour = level.getBlockState(target);
                if (neighbour.isAir() || !stack.isCorrectToolForDrops(neighbour)
                    || neighbour.getDestroySpeed(level, target) < 0.0F) {
                    continue;
                }
                stack.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
                level.destroyBlock(target, true, owner, 512);
            }
        }
        return super.mineBlock(stack, level, state, pos, owner);
    }
```

Варп в этом Task остаётся нулевым (перегрузка `getWarp(ItemStack, Player)` возвращает 0): ключ `VOIDTOUCHED` появляется только в Task 3.

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 29 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/MorphToolItem.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: port impact mining and voidtouched repair"
```

---

### Task 3: JSON трёх зачарований, теги и эксклюзивность

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantments.java` (дополнить ключами)
- Create: `src/main/resources/data/forbiddenmagic/enchantment/{cluster,impact,voidtouched}.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/item/enchantable/{morph_pickaxe,morph_digger,morph_tools}.json`
- Create: `src/main/resources/data/forbiddenmagic/tags/enchantment/exclusive_set/{cluster,consuming,trade}.json`
- Modify: `src/main/resources/data/forbiddenmagic/enchantment/{consuming,educational,greedy}.json`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `FmEnchantments.CLUSTER/IMPACT/VOIDTOUCHED`, `MorphToolItem.getWarp(ItemStack, Player)` с реальной проверкой.

- [ ] **Step 1: Падающий тест**

Добавить `"morph_enchantments_and_exclusivity"` (30):

```java
private static void morphEnchantmentsAndExclusivity(GameTestHelper helper) {
    var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.CLUSTER).value().getMaxLevel(), 4, "cluster max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.IMPACT).value().getMaxLevel(), 1, "impact max level");
    helper.assertValueEqual(registry.getOrThrow(FmEnchantments.VOIDTOUCHED).value().getMaxLevel(), 1, "voidtouched max level");
    helper.assertTrue(registry.getOrThrow(FmEnchantments.CLUSTER).value().isSupportedItem(new ItemStack(FmItems.MORPH_PICKAXE.get())), "cluster fits the chameleon pickaxe");
    helper.assertFalse(registry.getOrThrow(FmEnchantments.IMPACT).value().isSupportedItem(new ItemStack(FmItems.MORPH_SWORD.get())), "impact does not fit the sword");
    helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
        registry.getOrThrow(FmEnchantments.CLUSTER), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FORTUNE)),
        "cluster excludes fortune");
    helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
        registry.getOrThrow(FmEnchantments.CONSUMING), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH)),
        "consuming excludes silk touch");
    helper.assertFalse(net.minecraft.world.item.enchantment.Enchantment.areCompatible(
        registry.getOrThrow(FmEnchantments.GREEDY), registry.getOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING)),
        "greedy excludes looting");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `FmEnchantments.CLUSTER`.

- [ ] **Step 3: Ключи и JSON**

В `FmEnchantments`:

```java
    public static final ResourceKey<Enchantment> CLUSTER = key("cluster");
    public static final ResourceKey<Enchantment> IMPACT = key("impact");
    public static final ResourceKey<Enchantment> VOIDTOUCHED = key("voidtouched");
```

`cluster.json`:

```json
{
  "anvil_cost": 4,
  "description": {"translate": "enchantment.forbiddenmagic.cluster"},
  "effects": {},
  "exclusive_set": "#forbiddenmagic:exclusive_set/cluster",
  "max_cost": {"base": 51, "per_level_above_first": 10},
  "max_level": 4,
  "min_cost": {"base": 1, "per_level_above_first": 10},
  "slots": ["mainhand"],
  "supported_items": "#forbiddenmagic:enchantable/morph_pickaxe",
  "weight": 1
}
```

`impact.json`:

```json
{
  "anvil_cost": 4,
  "description": {"translate": "enchantment.forbiddenmagic.impact"},
  "effects": {},
  "max_cost": {"base": 40, "per_level_above_first": 0},
  "max_level": 1,
  "min_cost": {"base": 30, "per_level_above_first": 0},
  "slots": ["mainhand"],
  "supported_items": "#forbiddenmagic:enchantable/morph_digger",
  "weight": 1
}
```

`voidtouched.json`:

```json
{
  "anvil_cost": 4,
  "description": {"translate": "enchantment.forbiddenmagic.voidtouched"},
  "effects": {},
  "max_cost": {"base": 60, "per_level_above_first": 0},
  "max_level": 1,
  "min_cost": {"base": 30, "per_level_above_first": 0},
  "slots": ["mainhand"],
  "supported_items": "#forbiddenmagic:enchantable/morph_tools",
  "weight": 1
}
```

- [ ] **Step 4: Теги**

`tags/item/enchantable/morph_pickaxe.json`:

```json
{"replace": false, "values": ["forbiddenmagic:morph_pickaxe"]}
```

`morph_digger.json`:

```json
{"replace": false, "values": ["forbiddenmagic:morph_pickaxe", "forbiddenmagic:morph_shovel"]}
```

`morph_tools.json`:

```json
{"replace": false, "values": ["forbiddenmagic:morph_pickaxe", "forbiddenmagic:morph_sword", "forbiddenmagic:morph_shovel", "forbiddenmagic:morph_axe"]}
```

`tags/enchantment/exclusive_set/cluster.json`:

```json
{"replace": false, "values": ["minecraft:fortune", "forbiddenmagic:cluster"]}
```

`exclusive_set/consuming.json`:

```json
{"replace": false, "values": ["minecraft:silk_touch", "forbiddenmagic:consuming"]}
```

`exclusive_set/trade.json`:

```json
{"replace": false, "values": ["minecraft:looting", "forbiddenmagic:greedy", "forbiddenmagic:educational"]}
```

В `consuming.json` добавить `"exclusive_set": "#forbiddenmagic:exclusive_set/consuming",`, в `educational.json` и `greedy.json` — `"exclusive_set": "#forbiddenmagic:exclusive_set/trade",`.

- [ ] **Step 5: Варп**

В `MorphToolItem` заменить заглушку варпа на проверку зачарования:

```java
    @Override
    public int getWarp(ItemStack stack, Player player) {
        return FmEnchantments.level(player.level().registryAccess(), stack, FmEnchantments.VOIDTOUCHED) > 0 ? 1 : 0;
    }
```

Тест из Task 2 переписать так, чтобы он зачаровывал стек и вызывал перегрузку с игроком:

```java
private static void voidtouchedRepairsAndWarps(GameTestHelper helper) {
    ItemStack stack = new ItemStack(FmItems.MORPH_PICKAXE.get());
    ServerPlayer player = mockPlayer(helper);
    stack.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FmEnchantments.VOIDTOUCHED), 1);
    helper.assertValueEqual(MorphToolItem.getWarp(stack, player), 1, "voidtouched morph tool carries warp 1");
    helper.succeed();
}
```

- [ ] **Step 6: Lang**

```toml
cluster = "Fiery Core"
impact = "Impact"
voidtouched = "Voidtouched"
```

ru:

```toml
cluster = "Огненное ядро"
impact = "Влияние"
voidtouched = "Прикосновение пустоты"
```

- [ ] **Step 7: Зелёный прогон**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 30 required tests passed`.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged src/main/resources tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add cluster, impact and voidtouched enchantments"
```

---

### Task 4: Эффект Cluster

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `clusterChance(int level) → float`.

- [ ] **Step 1: Падающий тест**

Добавить `"cluster_replaces_drops_with_clusters"` (31):

```java
private static void clusterReplacesDropsWithClusters(GameTestHelper helper) {
    helper.assertValueEqual(FmEnchantmentEffects.clusterChance(1), 0.275F, "level 1 chance");
    helper.assertValueEqual(FmEnchantmentEffects.clusterChance(4), 0.5F, "level 4 chance");
    ItemStack replaced = thaumcraft.item.tool.SpecialMining.find(
        net.minecraft.world.level.block.Blocks.IRON_ORE.defaultBlockState(),
        new ItemStack(Items.RAW_IRON),
        1.0F,
        RandomSource.create(1L));
    helper.assertValueEqual(BuiltInRegistries.ITEM.getKey(replaced.getItem()), Identifier.parse("thaumcraft:native_iron_cluster"), "iron ore yields a native cluster");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `clusterChance`.

- [ ] **Step 3: Реализация**

```java
    public static float clusterChance(int level) {
        return 0.2F + 0.075F * level;
    }
```

В `onBlockDrops`, после блока consuming:

```java
        int cluster = FmEnchantments.level(player.level().registryAccess(), tool, FmEnchantments.CLUSTER);
        if (cluster > 0 && FmEnchantments.level(player.level().registryAccess(), tool, Enchantments.FORTUNE) == 0) {
            RandomSource random = event.getLevel().getRandom();
            float chance = clusterChance(cluster);
            for (ItemEntity drop : event.getDrops()) {
                drop.setItem(thaumcraft.item.tool.SpecialMining.find(event.getState(), drop.getItem(), chance, random));
            }
        }
```

Тест `clusterChance(4)` даёт 0.2+0.3=0.5 — float-точность: 0.075F*4 = 0.3F, сумма 0.5F, сравнение точное.

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 31 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmEnchantmentEffects.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: port the cluster enchantment effect"
```

---

### Task 5: Исследования MORPHTOOLS и трёх зачарований

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmResearch.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/misc/{lucrative,impact,voidtouched}.png`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: исследования `MORPHTOOLS`, `CLUSTER`, `IMPACT`, `VOIDTOUCHED`.

- [ ] **Step 1: Падающий тест**

Добавить `"morph_research_entries_present"` (32):

```java
private static void morphResearchEntriesPresent(GameTestHelper helper) {
    ResearchItem morph = ResearchCategories.getResearch("MORPHTOOLS");
    helper.assertTrue(morph != null, "morph research must exist");
    helper.assertValueEqual(morph.warp(), 4, "morph research carries warp 4");
    helper.assertValueEqual(morph.pages().size(), 5, "morph research has text and four recipes");
    helper.assertTrue(ResearchCategories.getResearch("CLUSTER") != null, "cluster research exists");
    helper.assertTrue(ResearchCategories.getResearch("IMPACT") != null, "impact research exists");
    ResearchItem voidtouched = ResearchCategories.getResearch("VOIDTOUCHED");
    helper.assertTrue(voidtouched != null, "voidtouched research exists");
    helper.assertValueEqual(voidtouched.warp(), 3, "voidtouched research carries warp 3");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: `morph research must exist`.

- [ ] **Step 3: Иконки**

```bash
for name in lucrative impact voidtouched; do
  cp "../ForbiddenMagic/src/main/resources/assets/forbidden/textures/misc/$name.png" "src/main/resources/assets/forbiddenmagic/textures/misc/$name.png"
done
```

- [ ] **Step 4: Регистрация исследований**

Добавить в `FmResearch.register()` отдельным блоком (после `SKULLAXE`, до `if (FmConfig.enchantmentsEnabled())`):

```java
        new ResearchItem("MORPHTOOLS", "FORBIDDEN", new AspectList().add(Aspect.TOOL, 2).add(FmAspects.ENVY, 3).add(Aspect.EXCHANGE, 2), -9, -3, 3, TcStack.of("forbiddenmagic:morph_pickaxe", 1))
            .setPages(
                ResearchPage.text("forbiddenmagic.research_page.MORPHTOOLS.1"),
                ResearchPage.infusion("MORPHPICK"),
                ResearchPage.infusion("MORPHSWORD"),
                ResearchPage.infusion("MORPHSHOVEL"),
                ResearchPage.infusion("MORPHAXE")
            )
            .setParents("NETHERSHARDS")
            .setParentsHidden("THAUMIUM", "INFUSIONENCHANTMENT")
            .setConcealed()
            .setWarp(4)
            .registerResearchItem();
        if (FmConfig.enchantmentsEnabled()) {
            new ResearchItem("CLUSTER", "FORBIDDEN", new AspectList().add(Aspect.METAL, 1).add(Aspect.FIRE, 4).add(FmAspects.ENVY, 3), -10, -1, 3, ForbiddenMagic.id("textures/misc/lucrative.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.CLUSTER.1"), ResearchPage.infusionEnchantment("CLUSTER"))
                .setParents("MORPHTOOLS", "ELEMENTALPICK")
                .setSecondary()
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("IMPACT", "FORBIDDEN", new AspectList().add(Aspect.ENTROPY, 8).add(Aspect.TOOL, 10).add(Aspect.MINE, 16).add(FmAspects.ENVY, 10), -9, -1, 3, ForbiddenMagic.id("textures/misc/impact.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.IMPACT.1"), ResearchPage.infusionEnchantment("IMPACT"))
                .setParents("MORPHTOOLS", "ELEMENTALSHOVEL")
                .setConcealed()
                .registerResearchItem();
            new ResearchItem("VOIDTOUCHED", "FORBIDDEN", new AspectList().add(Aspect.ELDRITCH, 16).add(Aspect.TOOL, 10).add(Aspect.CRAFT, 8).add(FmAspects.ENVY, 32), -8, -1, 3, ForbiddenMagic.id("textures/misc/voidtouched.png"))
                .setPages(ResearchPage.text("forbiddenmagic.research_page.VOIDTOUCHED.1"), ResearchPage.infusionEnchantment("VOIDTOUCHED"))
                .setParents("MORPHTOOLS", "VOIDMETAL")
                .setConcealed()
                .setWarp(3)
                .registerResearchItem();
        }
```

- [ ] **Step 5: Тексты**

en:

```toml
[forbiddenmagic.research_page.MORPHTOOLS]
1 = "Chameleon Tools are like three tools in one. When first enchanted, right-clicking while sneaking with the tool in your hand changes it to a blank new enchanting slate, allowing you to enchant it with a completely different set of enchantments. Each tool can hold up to three separate enchantment sets which can be alternated between by right-clicking while sneaking.<BR><BR>The tools have no innate way to remove enchantments once fully enchanted, so make each enchantment count!"

[forbiddenmagic.research_page.CLUSTER]
1 = "You've discovered an enchantment to allow the Chameleon Pickaxe to imitate the effects of the Pickaxe of the Core.  A Chameleon Pickaxe with the Fiery Core enchantment has a chance of dropping clusters when mining certain ores.  Clusters are a lot purer than raw ore and can be smelted for two of the ore's normal smelting result.  Higher levels of Fiery Core increase the chance of dropping clusters."

[forbiddenmagic.research_page.IMPACT]
1 = "The Shovel of the Earthmover mines larger areas than most tools. With the Impact enchantment, so can your Chameleon Pickaxe and Shovel.<BR><BR>A Chameleon Tool with the Impact enchantment mines a 3 x 3 area of blocks instead of just one, making exploratory mining and flattening areas much easier."

[forbiddenmagic.research_page.VOIDTOUCHED]
1 = "Voidmetal's ability to flow back into place after being damaged is fascinating. You sure wish your Chameleon Tools had the same properties...<BR><BR>The Voidtouched enchant gives a Chameleon Tool the same regenerating quality as Void Tools.<BR><BR>Chameleon Tools affected by the enchantment appear to be made of living scaled flesh which morphs back into shape when hurt. Just looking at it is disturbing for your sanity."
```

ru:

```toml
[forbiddenmagic.research_page.MORPHTOOLS]
1 = "Хамелеоновые инструменты - это как три инструмента в одном. При первом зачаровании, нажатие ПКМ, сидя с инструментом в руке, изменяет его на чистое покрытие для зачарования, позволяя Вам зачаровать его с совершенно другим набором зачарований. Каждый инструмент может содержать три отдельных набора зачарований, которые можно чередовать между собой, нажимая ПКМ, сидя.<BR><BR>Инструменты не имеют природного способа удаления наборов зачарований после полного зачарования, так что считайте каждое зачарование!"

[forbiddenmagic.research_page.CLUSTER]
1 = "Вы обнаружили зачарование, которое позволит хамелеоновой кирке имитировать эффекты кирки Огня.  Хамелеоновая кирка с зачарованием: Огненное ядро имеет шанс получения кластеров определённых руд.  Кластеры намного чище, чем сырая руда, а также они могут быть переплавлены в два слитка руды.  Чем выше уровень зачарования, тем выше шанс получения кластеров."

[forbiddenmagic.research_page.IMPACT]
1 = "Некоторые инструменты имеют гораздо большую область добычи. Зачарование: Влияние применяется на хамелеоновую кирку и лопату.<BR><BR>Хамелеоновый инструмент с зачарованием: Влияние добывает территорию блоков 3 x 3 вместо одного, делая разведочную добычу и уплощение территории гораздо проще."

[forbiddenmagic.research_page.VOIDTOUCHED]
1 = "Способность пустотного металла регенерировать повреждения завораживает. Вы хотите, чтобы Ваши хамелеоновые инструменты имели теже свойства...<BR><BR>Зачарование: Прикосновение пустоты даёт хамелеоновому инструменту тоже качество регенерации, что и у пустотных инструментов.<BR><BR>Хамелеоновые инструменты после зачарования похожи на живую плоть, которая превращается обратно в нужную форму при нанесении урона. Даже когда Вы просто смотрите на них Ваш рассудок начинает пошатываться."
```

Плюс имена:

```toml
[research_name] — ключи MORPHTOOLS/CLUSTER/IMPACT/VOIDTOUCHED в [tc.research_name];
[tc.research_text] — «Chameleon Tools», «Chameleon of the Core», «Someday that wall is gonna fall.», «Colour Out of Space» и их русские пары.
```

Ru-имена: «Хамелеоновые инструменты», «Хамелеон ядра», «Когда-нибудь эта стена упадёт.», «Цвет из Космоса».

- [ ] **Step 6: Зелёный прогон**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 32 required tests passed`.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmResearch.java src/main/resources/assets/forbiddenmagic/textures/misc tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the chameleon tool research"
```

---

### Task 6: Рецепты инструментов и трёх зачарований

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: рецепты `MORPHPICK`, `MORPHSWORD`, `MORPHSHOVEL`, `MORPHAXE`, `CLUSTER`, `IMPACT`, `VOIDTOUCHED`.

- [ ] **Step 1: Падающий тест**

Добавить `"morph_recipes_match_the_original"` (33):

```java
private static void morphRecipesMatchTheOriginal(GameTestHelper helper) {
    for (String key : new String[]{"MORPHPICK", "MORPHSWORD", "MORPHSHOVEL", "MORPHAXE"}) {
        InfusionRecipe recipe = (InfusionRecipe) TcRecipes.get(key);
        helper.assertTrue(recipe != null, key + " recipe must exist");
        helper.assertValueEqual(recipe.instability(), 6, key + " instability");
        helper.assertValueEqual(recipe.aspects().getAmount(FmAspects.ENVY), 16, key + " envy cost");
    }
    for (String key : new String[]{"CLUSTER", "IMPACT", "VOIDTOUCHED"}) {
        helper.assertTrue(TcRecipes.get(key) instanceof InfusionEnchantmentRecipe, key + " must be an enchantment recipe");
    }
    InfusionEnchantmentRecipe voidtouched = (InfusionEnchantmentRecipe) TcRecipes.get("VOIDTOUCHED");
    helper.assertValueEqual(voidtouched.aspects().getAmount(FmAspects.ENVY), 24, "voidtouched envy cost");
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: `MORPHPICK recipe must exist`.

- [ ] **Step 3: Реализация**

В `OWN_KEYS` добавить `MORPHPICK`, `MORPHSWORD`, `MORPHSHOVEL`, `MORPHAXE`, `CLUSTER`, `IMPACT`, `VOIDTOUCHED`.

Блок инструментов (вне `enchanting`):

```java
        TcRecipes.register(new InfusionRecipe("MORPHPICK", "MORPHTOOLS",
            TcStack.of("forbiddenmagic:morph_pickaxe", 1), 6,
            new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.TOOL, 16),
            TcIngredient.item("thaumcraft:thaumium_pickaxe"),
            List.of(
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("thaumcraft:enchanted_fabric"),
                TcIngredient.item("thaumcraft:silverwood_log")
            )));
        TcRecipes.register(new InfusionRecipe("MORPHSWORD", "MORPHTOOLS",
            TcStack.of("forbiddenmagic:morph_sword", 1), 6,
            new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.WEAPON, 16),
            TcIngredient.item("thaumcraft:thaumium_sword"),
            List.of(
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("thaumcraft:enchanted_fabric"),
                TcIngredient.item("thaumcraft:silverwood_log")
            )));
        TcRecipes.register(new InfusionRecipe("MORPHSHOVEL", "MORPHTOOLS",
            TcStack.of("forbiddenmagic:morph_shovel", 1), 6,
            new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.TOOL, 16),
            TcIngredient.item("thaumcraft:thaumium_shovel"),
            List.of(
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("thaumcraft:enchanted_fabric"),
                TcIngredient.item("thaumcraft:silverwood_log")
            )));
        TcRecipes.register(new InfusionRecipe("MORPHAXE", "MORPHTOOLS",
            TcStack.of("forbiddenmagic:morph_axe", 1), 6,
            new AspectList().add(Aspect.EXCHANGE, 32).add(FmAspects.ENVY, 16).add(Aspect.TOOL, 16),
            TcIngredient.item("thaumcraft:thaumium_axe"),
            List.of(
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item(FmItems.id("envy").toString()),
                TcIngredient.item("minecraft:diamond"),
                TcIngredient.item("thaumcraft:enchanted_fabric"),
                TcIngredient.item("thaumcraft:silverwood_log")
            )));
```

Внутри `if (FmConfig.enchantmentsEnabled())`, после существующих пяти:

```java
            TcRecipes.register(new InfusionEnchantmentRecipe("CLUSTER", "CLUSTER", ForbiddenMagic.id("cluster"), 3,
                new AspectList().add(Aspect.FIRE, 4).add(Aspect.METAL, 4).add(Aspect.GREED, 4),
                List.of(
                    TcIngredient.item("thaumcraft:elemental_pickaxe"),
                    TcIngredient.item("thaumcraft:salis_mundus")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("IMPACT", "IMPACT", ForbiddenMagic.id("impact"), 4,
                new AspectList().add(Aspect.ENTROPY, 16).add(Aspect.MINE, 16),
                List.of(
                    TcIngredient.item("thaumcraft:salis_mundus"),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item("thaumcraft:elemental_shovel")
                )));
            TcRecipes.register(new InfusionEnchantmentRecipe("VOIDTOUCHED", "VOIDTOUCHED", ForbiddenMagic.id("voidtouched"), 8,
                new AspectList().add(Aspect.VOID, 16).add(Aspect.DARKNESS, 16).add(FmAspects.ENVY, 24).add(Aspect.ELDRITCH, 16),
                List.of(
                    TcIngredient.item("thaumcraft:salis_mundus"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item("thaumcraft:void_seed"),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString()),
                    TcIngredient.item(FmItems.id("envy").toString())
                )));
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 33 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmRecipes.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add chameleon tool and enchantment recipes"
```

---

### Task 7: TT-стоимость трёх зачарований

**Files:**
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmTinkerer.java`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

- [ ] **Step 1: Обновить тест**

В `tinkererIntegrationIsOptional` заменить 5 на 8:

```java
            helper.assertValueEqual(FmTinkerer.registeredCount(), 8, "TT receives eight enchantment costs");
```

- [ ] **Step 2: Красный прогон (при установленном TT)**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: в тестовом окружении TT нет, тест проходит по ветке «0»; красного прогона нет. Это ожидаемо, поэтому шаг фиксируется как ручная проверка кода, а не TDD.

- [ ] **Step 3: Реализация**

В `FmTinkerer.register()` после пяти `add(...)`:

```java
            add(costs, constructor, FmEnchantments.CLUSTER, "textures/misc/lucrative.png", new AspectList().add(Aspect.FIRE, 5).add(Aspect.EARTH, 5).add(Aspect.ORDER, 5), "CLUSTER");
            add(costs, constructor, FmEnchantments.IMPACT, "textures/misc/impact.png", new AspectList().add(Aspect.ENTROPY, 30).add(Aspect.EARTH, 30), "IMPACT");
            add(costs, constructor, FmEnchantments.VOIDTOUCHED, "textures/misc/voidtouched.png", new AspectList().add(Aspect.ENTROPY, 70), "VOIDTOUCHED");
            registered = 8;
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 33 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged/FmTinkerer.java tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: register the remaining enchantment costs with Thaumic Tinkerer"
```

---

### Task 8: Документация и гейт

**Files:**
- Modify: `docs/deviations.md`, `docs/roadmap.md`, `docs/verification.md`, `README.md`

- [ ] **Step 1: Отступления**

Добавить:

```markdown
| Цвет фазы морф-инструмента | оверлей `eye*` красится в цвет фазы (красный/синий/жёлтый) | оверлей статичный | в этом слайсе нет item tint source; фаза видна по имени и набору зачарований |
| Переключение фаз | требует наличия NBT (первое зачарование) | работает всегда | компоненты существуют у любого стека |
| Impact: выбор плоскости | сторона удара из рейкаста | доминирующая ось взгляда | у `mineBlock` нет стороны; приближение записано в тест |
| Repairable | энви-осколок через `getIsRepairable` | компонент `REPAIRABLE` с осколком зависти | современный механизм наковальни |
| Рецепты инструментов | `itemResource` мета 3 и `blockMagicalLog` мета 1 | `thaumcraft:enchanted_fabric`, `thaumcraft:silverwood_log` | соответствие мет TC4 предметам хоста |
| Рецепт Voidtouched | `itemResource` мета 16 | `thaumcraft:void_seed` | единственный предмет хоста, соответствующий мете |
```

- [ ] **Step 2: Roadmap и README**

Roadmap: в списке остатка убрать три зачарования и морф-инструменты, добавить строку о слайсе M3.

README: в «Поддерживается» добавить хамелеоновы инструменты и три зачарования; в «Чего нет» убрать их.

- [ ] **Step 3: Полный гейт**

```bash
cmd //c gradlew.bat build
cmd //c gradlew.bat runGameTestServer
# no_lust=true, повторный прогон, вернуть false
python tools/gen_resources.py && python tools/gen_resources.py
git status --porcelain src/generated
python tools/check_provenance.py
python tools/prepare_host.py --verify
```

- [ ] **Step 4: Записать замеры в `docs/verification.md`, коммит и тег `m3-rc1`.**

```bash
git add docs README.md
git commit -m "docs: record the chameleon tool slice"
git tag -a m3-rc1 -m "M3: chameleon tools and the remaining enchantments"
```

---

## Self-Review

**Покрытие:** четыре инструмента, фазы, Impact, Voidtouched, Cluster, исследование `MORPHTOOLS`, семь рецептов, TT-стоимость (8), тексты, отступления. Оставшиеся пункты §5.1 (блоки, жезлы, Клетка Гнева, скруное дерево, житель) не входят.

**Плейсхолдеры:** каждый шаг содержит код или команду; тексты вставлены дословно.

**Согласованность:** `MorphState` — единственный источник фаз; `FmEnchantments.level` используется во всех обработчиках; ключи рецептов совпадают с ключами страниц исследований.
