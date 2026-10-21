# Forbidden Magic: Reforged — Клетка Гнева (M7b) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Портировать Клетку Гнева: блок с block entity, спавнер мобов на эссенции, вставку кристалла моба, рендерер моба внутри клетки, рецепт и исследование.

**Architecture:** `WrathCageBlock` + `WrathCageBlockEntity extends TcBlockEntity` (готовый хост-базовый класс с sync). BE хранит id моба, три вида топлива (`special`, `wrath`, `sloth`), режим, задержку и счётчик. Эссенция забирается из соседних `EssentiaTransport` в тике; спавн идёт по задержке 200–300 тиков, по 3 моба, лимит 6 рядом, только при игроке в 16 блоках и без редстоун-сигнала. Кристалл моба (M7a) вставляется/меняется ПКМ. Рендерер — `BlockEntityRenderer` нового образца с `EntityRenderDispatcher.extractEntity/submit`.

**Tech Stack:** NeoForge 26.3.0.57-beta, Java 25, ModDevGradle, Thaumcraft 4: Reforged 1.2, GameTest.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md`.

## Граница слайса

Входит: `wrath_cage` (блок + BE + спавнер + эссенция + красный камень), вставка кристалла, дроп кристалла при разрушении, рендерер моба, рецепт инфузии, crucible-рецепт кристалла, исследование `WRATHCAGE`, тексты.

Не входит: вилка дьяволиста и переключение режимов вилкой (вилка не портирована), босс-мобы в клетке, `wrathCrazy`-специфика за пределами разрешённых id.

## Global Constraints

- Хост не редактируется; патчи не меняются.
- NBT — только через `ValueInput`/`ValueOutput` (как в `TcBlockEntity`), не `CompoundTag`.
- Спавн — современные id из `FmSpawnerMobs`; моб без id в таблице не спавнится.
- Конфиг STARTUP с fallback: `wrathCage`, `wrathCost`, `wrathEff`, `wrathCrazy` (M7a).
- Один Task — один коммит; после Task: build, runGameTestServer, check_provenance.

---

### Task 1: Блок, block entity, спавнер и эссенция

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmBlockEntities.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/WrathCageBlock.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/WrathCageBlockEntity.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmBlocks.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/block/spirit_box.png`
- Modify: `tools/gen_resources.py`, `data/lang/*.toml`
- Create: `src/main/resources/data/forbiddenmagic/loot_table/blocks/wrath_cage.json`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `FmBlocks.WRATH_CAGE`, `FmBlockEntities.WRATH_CAGE`, `WrathCageBlockEntity.getMobId/setMobId/nextDelay/shouldSpawn`, `WrathCageBlockEntity.crystalForBreak()`.

- [ ] **Step 1: Падающие тесты**

Добавить `"wrath_cage_block_and_entity"` (43) и `"wrath_cage_crystal_and_spawn_rules"` (44):

```java
private static void wrathCageBlockAndEntity(GameTestHelper helper) {
    BlockPos pos = new BlockPos(5, 4, 5);
    helper.getLevel().setBlockAndUpdate(pos, FmBlocks.WRATH_CAGE.get().defaultBlockState());
    helper.assertTrue(helper.getLevel().getBlockEntity(pos) instanceof WrathCageBlockEntity, "cage creates its block entity");
    WrathCageBlockEntity cage = (WrathCageBlockEntity) helper.getLevel().getBlockEntity(pos);
    helper.assertTrue(cage != null && cage.getMobId() == null, "a fresh cage is unattuned");
    cage.setMobId("minecraft:zombie");
    helper.assertValueEqual(cage.getMobId(), "minecraft:zombie", "the cage keeps the mob id");
    ItemStack crystal = WrathCageBlockEntity.crystalForBreak(cage);
    helper.assertValueEqual(MobCrystalItem.mob(crystal), "minecraft:zombie", "breaking the cage yields the attuned crystal");
    helper.succeed();
}

private static void wrathCageCrystalAndSpawnRules(GameTestHelper helper) {
    helper.assertFalse(WrathCageBlockEntity.shouldSpawn(true, true, 10, 5, 0), "a powered cage waits");
    helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 10, 5, 0), "an attuned fuelled cage spawns");
    helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 4), "no fuel and too little essentia waits");
    helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 5, 5), "enough essentia starts a fuel cycle");
    helper.assertFalse(WrathCageBlockEntity.shouldSpawn(false, false, 10, 5, 0), "an unattuned cage waits");
    helper.assertTrue(WrathCageBlockEntity.shouldSpawn(false, true, 0, 0, 0), "cost 0 needs no fuel");
    helper.succeed();
}
```

`shouldSpawn(powered, mobSet, fuel, cost, availableEssentia)` возвращает `!powered && mobSet && (cost == 0 || fuel > 0 || availableEssentia >= cost)`.

- [ ] **Step 2: Красный прогон**, затем `FmBlockEntities`:

```java
public final class FmBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ForbiddenMagic.MODID);
    public static final Supplier<BlockEntityType<WrathCageBlockEntity>> WRATH_CAGE =
        BLOCK_ENTITIES.register("wrath_cage", () -> new BlockEntityType<>(
            WrathCageBlockEntity::new, FmBlocks.WRATH_CAGE.get()));

    private FmBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
```

- [ ] **Step 3: `WrathCageBlock`**

```java
public class WrathCageBlock extends Block {
    public WrathCageBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WrathCageBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != FmBlockEntities.WRATH_CAGE.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> WrathCageBlockEntity.tick(tickLevel, pos, tickState, (WrathCageBlockEntity) entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WrathCageBlockEntity cage) || level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getMainHandItem();
        String incoming = MobCrystalItem.mob(held);
        if (incoming == null || !FmSpawnerMobs.ids().contains(incoming)) {
            return InteractionResult.PASS;
        }
        String previous = cage.getMobId();
        cage.setMobId(incoming);
        if (previous == null) {
            held.remove(FmComponents.MOB_ID.get());
        } else {
            MobCrystalItem.setMob(held, previous);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(ServerLevel level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        if (blockEntity instanceof WrathCageBlockEntity cage && cage.getMobId() != null) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                WrathCageBlockEntity.crystalForBreak(cage));
            level.addFreshEntity(drop);
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }
}
```

Блок регистрируется в `FmBlocks` с `properties.mapColor(MapColor.METAL).strength(5.0F, 2000.0F).sound(SoundType.METAL).noOcclusion()` и `FmItems.ITEMS.registerSimpleBlockItem(WRATH_CAGE)`; вкладка — `event.accept(FmBlocks.WRATH_CAGE.get().asItem())`. `FmBlockEntities.register(modEventBus)` в конструкторе до `FmItems.ITEMS.register`.

- [ ] **Step 4: `WrathCageBlockEntity`**

Поля: `@Nullable String mobId; int special, wrath, sloth; int fuel; int spawnDelay = 20; boolean slothful; int mode;`

NBT:

```java
@Override
protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (mobId != null) {
        output.putString("MobId", mobId);
    }
    output.putInt("Special", special);
    output.putInt("Wrath", wrath);
    output.putInt("Sloth", sloth);
    output.putInt("Fuel", fuel);
    output.putInt("Delay", spawnDelay);
    output.putBoolean("Slothful", slothful);
    output.putInt("Mode", mode);
}

@Override
protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    mobId = input.getString("MobId").orElse(null);
    special = input.getIntOr("Special", 0);
    wrath = input.getIntOr("Wrath", 0);
    sloth = input.getIntOr("Sloth", 0);
    fuel = input.getIntOr("Fuel", 0);
    spawnDelay = input.getIntOr("Delay", 20);
    slothful = input.getBooleanOr("Slothful", false);
    mode = input.getIntOr("Mode", 0);
}
```

(Точные имена методов `ValueOutput.putString/putInt/putBoolean` и `ValueInput.getString/getBooleanOr` сверить с хостом: в `JarBlockEntity` используются `output.store(...)` и `input.getIntOr`; для строки использовать `output.store("MobId", Codec.STRING, mobId)` и `input.read("MobId", Codec.STRING).orElse(null)`.)

`tick`:

```java
public static void tick(Level level, BlockPos pos, BlockState state, WrathCageBlockEntity cage) {
    if (level.isClientSide()) {
        return;
    }
    boolean powered = level.hasNeighborSignal(pos);
    if (powered || cage.mobId == null) {
        return;
    }
    cage.drawEssentia(level, pos);
    if (cage.fuel <= 0 && FmConfig.wrathCost() > 0) {
        if (cage.special >= FmConfig.wrathCost()) {
            cage.special -= FmConfig.wrathCost();
            cage.fuel = FmConfig.wrathEff();
            cage.slothful = false;
            cage.sync();
        } else if (cage.wrath >= FmConfig.wrathCost()) {
            cage.wrath -= FmConfig.wrathCost();
            cage.fuel = FmConfig.wrathEff();
            cage.slothful = false;
            cage.sync();
        } else if (cage.sloth >= FmConfig.wrathCost()) {
            cage.sloth -= FmConfig.wrathCost();
            cage.fuel = FmConfig.wrathEff();
            cage.slothful = true;
            cage.sync();
        }
    }
    if (cage.fuel <= 0 && FmConfig.wrathCost() > 0) {
        return;
    }
    if (cage.spawnDelay > 0) {
        cage.spawnDelay--;
        return;
    }
    cage.spawnRound(level, pos);
}
```

`spawnRound`: проверка игрока в 16 блоках (`level.getNearestPlayer(x,y,z,16,false)`), лимит 6 мобов в боксе `pos ± 4 по X/Z и ±1 по Y`, три попытки: `EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(mobId))`, `Entity entity = type.create(level, EntitySpawnReason.SPAWNER)`, позиция со случайным смещением, `level.noCollision(entity)` и `!level.containsAnyLiquid(entity.getBoundingBox())`, `level.addFreshEntity(entity)`, `fuel--`. После раунда — `spawnDelay = nextDelay(level.getRandom(), slothful)`.

```java
public static int nextDelay(RandomSource random, boolean slothful) {
    int delay = 200 + random.nextInt(101);
    return slothful ? delay + 200 : delay;
}
```

`drawEssentia`: для каждого `Direction`, `level.getBlockEntity(pos.relative(dir)) instanceof EssentiaTransport transport`, `transport.canOutputTo(dir.getOpposite())`, взять `transport.takeEssentia(aspect, 1, dir.getOpposite()) == 1` → `special++` (лимит 64), иначе wrath/sloth по тем же правилам, что оригинал.

`AspectContainer`/`EssentiaTransport`: реализовать по `JarBlockEntity`; `getAspects` — один аспект по режиму, `addEssentia` — принимать `mobAspect`/wrath/sloth, `takeEssentia` — 0, `getSuctionAmount` — 128, `getMinimumSuction` — 0.

`crystalForBreak`:

```java
public static ItemStack crystalForBreak(WrathCageBlockEntity cage) {
    ItemStack stack = new ItemStack(FmItems.MOB_CRYSTAL.get());
    if (cage.mobId != null) {
        MobCrystalItem.setMob(stack, cage.mobId);
    }
    return stack;
}
```

- [ ] **Step 5: Ресурсы**

Скопировать `spirit_box.png`; сгенерировать `cube_all` модель с `render_type: cutout`, blockstate, item-модель; loot table `wrath_cage` — сам блок. Lang: `wrath_cage = "Wrath Cage"` / «Клетка гнева».

- [ ] **Step 6: Зелёный прогон и коммит**

`python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer` → `All 45 required tests passed`.

```bash
git add src/main/java src/main/resources tools/gen_resources.py data/lang src/generated tests
git commit -m "feat: add the wrath cage block and spawner"
```

---

### Task 2: Рендерер, исследование, рецепты, гейт

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/client/WrathCageRenderer.java`
- Create: `src/main/java/dev/visfox/forbiddenreforged/client/FmClientEvents.java`
- Modify: `FmResearch.java`, `FmRecipes.java`
- Modify: `data/lang/*.toml`, `docs/*`, `README.md`
- Test: `tests/.../FmGameTests.java`

- [ ] **Step 1: Рендерер**

```java
public class WrathCageRenderer implements BlockEntityRenderer<WrathCageBlockEntity, WrathCageRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public @Nullable EntityRenderState mob;
        public float spin;
    }

    private final EntityRenderDispatcher dispatcher;

    public WrathCageRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.entityRenderer();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WrathCageBlockEntity cage, State state, float partialTicks, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(cage, state, partialTicks, camera, overlay);
        state.mob = null;
        EntityType<?> type = cage.entityType();
        if (type != null && cage.getLevel() != null) {
            Entity entity = type.create(cage.getLevel(), EntitySpawnReason.TRIGGERED);
            if (entity != null) {
                entity.setPos(0.0, 0.0, 0.0);
                state.mob = dispatcher.extractEntity(entity, partialTicks);
                state.spin = cage.isPowered() ? 0.0F : (cage.getLevel().getGameTime() % 360L) * 3.0F;
            }
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.mob == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 0.4, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(state.spin));
        pose.scale(0.4375F, 0.4375F, 0.4375F);
        dispatcher.submit(state.mob, camera, 0.0, 0.0, 0.0, pose, collector);
        pose.popPose();
    }
}
```

`FmClientEvents` — `@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)` с `registerRenderers(EntityRenderersEvent.RegisterRenderers event) → event.registerBlockEntityRenderer(FmBlockEntities.WRATH_CAGE.get(), WrathCageRenderer::new)`.

- [ ] **Step 2: Исследование и рецепты**

Исследование `WRATHCAGE`: WRATH 5, MECHANISM 3, BEAST 2; -6, -9; сложность 3; parents hidden `INFUSION`, `THAUMIUM`, `DISTILESSENTIA`; concealed; warp 3; страницы: text1, infusion `WRATHCAGE`, text2, text3, crucible `MOBCRYSTAL`.

Инфузия `WRATHCAGE`: центр `thaumcraft:arcane_stone`, instability 10, WRATH32 MAGIC32 BEAST32 MECHANISM16, компоненты: 4× wrath shard, 4× diamond, 3× `thaumcraft:warded_jar` (в хосте нет пустой банки — отступление).

Crucible `MOBCRYSTAL`: катализатор `minecraft:diamond`, MIND10 ENERGY10, результат `forbiddenmagic:mob_crystal`.

Тексты страниц WRATHCAGE 1–3 и research name/text — дословно из оригинала (en/ru), ключи `forbiddenmagic.research_page.WRATHCAGE.*`.

- [ ] **Step 2b: Тест исследования и рецептов**

Добавить `"wrath_cage_research_and_recipes"` (45): исследование `WRATHCAGE` существует, warp 3, 5 страниц; `TcRecipes.get("WRATHCAGE")` — `InfusionRecipe` с WRATH 32, `TcRecipes.get("MOBCRYSTAL")` — `CrucibleRecipe` с MIND 10.

- [ ] **Step 3: Гейт, документация, тег**

Добавить deviations: вилка и режимы отложены; вставка кристалла через компонент; банки заменены на `warded_jar`; рендерер использует современный extract/submit. Прогнать build, 46 тестов дважды, генератор ×2, provenance, host verify. Обновить roadmap/README/verification. Коммит `feat: add the wrath cage renderer and research`, тег `m7b-rc1`.

---

## Self-Review

Task 1 закрывает блок, BE, NBT, кристалл, дроп, спавнер и эссенцию; Task 2 — рендерер, исследование, два рецепта и гейт. Вилка и режимы явно отложены. Все хостовые сигнатуры взяты из `host/src/main/java` (`TcBlockEntity`, `JarBlockEntity`, `EssentiaTransport`, `AdvancedFurnaceRenderer`, `DeviceMirrorClient`); MC-сигнатуры (`EntityType.create`, `EntityRenderDispatcher.extractEntity/submit`, `ValueInput/ValueOutput`) сверены по декомпилированным исходникам.
