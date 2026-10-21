# Forbidden Magic: Reforged — материалы скверны (слайс 4) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Портировать пять статических блоков Forbidden Magic — бревно, доски, гладкий камень, каменные кирпичи и блок звезды Нижнего мира — с их тегами, моделями и рецептами.

**Architecture:** `FmBlocks` с `DeferredRegister.Blocks`; предметы блоков регистрируются в существующем `FmItems.ITEMS` через `registerSimpleBlockItem`. Блокстейты, модели и item-модели пишет `tools/gen_resources.py`. Теги — обычные JSON в `data/minecraft/tags/block`. Рецепты — ванильные crafting JSON в `data/forbiddenmagic/recipe`.

**Tech Stack:** NeoForge 26.3.0.57-beta, Java 25, ModDevGradle, Thaumcraft 4: Reforged 1.2, GameTest.

**Spec:** `docs/superpowers/specs/2026-10-20-forbidden-magic-reforged-design.md`.

## Граница слайса

Входит: `taint_log`, `taint_planks`, `taint_stone_smooth`, `taint_stone_brick`, `star_block`; теги инструментов и beacon base; рецепты «бревно → доски», «камень → кирпичи», «9 звёзд ↔ блок».

Не входит: листва, саженец, цветы, арканный торт, Клетка Гнева, плавка бревна в скверный уголь (предмет ещё не портирован), арканный рецепт камня (нужен research `TAINTSTONE`), распространение скверны.

## Global Constraints

- Пакет `dev.visfox.forbiddenreforged`; хост не редактируется.
- Числа — из оригинала: бревно и доски 2.0 прочности, доски сопротивление 5, камень 2.0/10, блок звезды 5.0; звуки wood/stone/metal.
- Ресурсы — только генератором; повторный прогон не меняет файлы.
- Один Task — один коммит; после Task: build, runGameTestServer, check_provenance.

---

### Task 1: Пять блоков, теги, модели

**Files:**
- Create: `src/main/java/dev/visfox/forbiddenreforged/FmBlocks.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/ForbiddenMagic.java`
- Modify: `src/main/java/dev/visfox/forbiddenreforged/FmCreativeTab.java`
- Create: `src/main/resources/assets/forbiddenmagic/textures/block/{taint_log_side,taint_log_bottom,taint_planks,taint_stone_smooth,taint_stone_brick,starblock}.png`
- Modify: `tools/gen_resources.py`
- Create: `src/main/resources/data/minecraft/tags/block/mineable/{axe,pickaxe}.json`
- Create: `src/main/resources/data/minecraft/tags/block/needs_stone_tool.json`
- Create: `src/main/resources/data/minecraft/tags/block/beacon_base_blocks.json`
- Modify: `tools/lang/en_us.toml`, `tools/lang/ru_ru.toml`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: `FmBlocks.TAINT_LOG/TAINT_PLANKS/TAINT_STONE_SMOOTH/TAINT_STONE_BRICK/STAR_BLOCK` (`DeferredBlock<?>`).

- [ ] **Step 1: Падающий тест**

Добавить `"taint_material_blocks_are_registered"` (34) и `"taint_blocks_carry_tool_tags"` (35):

```java
private static void taintMaterialBlocksAreRegistered(GameTestHelper helper) {
    helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_LOG.get()), Identifier.parse("forbiddenmagic:taint_log"), "taint log id");
    helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_PLANKS.get()), Identifier.parse("forbiddenmagic:taint_planks"), "taint planks id");
    helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_SMOOTH.get()), Identifier.parse("forbiddenmagic:taint_stone_smooth"), "smooth stone id");
    helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.TAINT_STONE_BRICK.get()), Identifier.parse("forbiddenmagic:taint_stone_brick"), "brick id");
    helper.assertValueEqual(BuiltInRegistries.BLOCK.getKey(FmBlocks.STAR_BLOCK.get()), Identifier.parse("forbiddenmagic:star_block"), "star block id");
    helper.assertTrue(!new ItemStack(FmBlocks.TAINT_LOG.get().asItem()).isEmpty(), "taint log has an item");
    helper.assertTrue(!new ItemStack(FmBlocks.STAR_BLOCK.get().asItem()).isEmpty(), "star block has an item");
    helper.succeed();
}

private static void taintBlocksCarryToolTags(GameTestHelper helper) {
    TagKey<Block> axe = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:mineable/axe"));
    TagKey<Block> pickaxe = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:mineable/pickaxe"));
    TagKey<Block> beacon = TagKey.create(Registries.BLOCK, Identifier.parse("minecraft:beacon_base_blocks"));
    helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_LOG.get()).is(axe), "taint log mines with an axe");
    helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_PLANKS.get()).is(axe), "taint planks mine with an axe");
    helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.TAINT_STONE_SMOOTH.get()).is(pickaxe), "taint stone mines with a pickaxe");
    helper.assertTrue(BuiltInRegistries.BLOCK.wrapAsHolder(FmBlocks.STAR_BLOCK.get()).is(beacon), "star block is a beacon base");
    helper.succeed();
}
```

Импорты: `dev.visfox.forbiddenreforged.FmBlocks`, `net.minecraft.world.level.block.Block`.

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: компиляция падает на `FmBlocks`.

- [ ] **Step 3: `FmBlocks`**

```java
package dev.visfox.forbiddenreforged;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FmBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ForbiddenMagic.MODID);
    public static final DeferredBlock<RotatedPillarBlock> TAINT_LOG;
    public static final DeferredBlock<Block> TAINT_PLANKS;
    public static final DeferredBlock<Block> TAINT_STONE_SMOOTH;
    public static final DeferredBlock<Block> TAINT_STONE_BRICK;
    public static final DeferredBlock<Block> STAR_BLOCK;

    static {
        TAINT_LOG = BLOCKS.registerBlock("taint_log", RotatedPillarBlock::new,
            properties -> wood(properties, 2.0F, 2.0F));
        TAINT_PLANKS = BLOCKS.registerBlock("taint_planks", Block::new,
            properties -> wood(properties, 2.0F, 5.0F));
        TAINT_STONE_SMOOTH = BLOCKS.registerBlock("taint_stone_smooth", Block::new,
            properties -> stone(properties));
        TAINT_STONE_BRICK = BLOCKS.registerBlock("taint_stone_brick", Block::new,
            properties -> stone(properties));
        STAR_BLOCK = BLOCKS.registerBlock("star_block", Block::new,
            properties -> properties.mapColor(MapColor.METAL).instrument(NoteBlockInstrument.PLING)
                .strength(5.0F, 6.0F).sound(SoundType.METAL).requiresCorrectToolForDrops());
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_LOG);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_PLANKS);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_STONE_SMOOTH);
        FmItems.ITEMS.registerSimpleBlockItem(TAINT_STONE_BRICK);
        FmItems.ITEMS.registerSimpleBlockItem(STAR_BLOCK);
    }

    private FmBlocks() {
    }

    private static BlockBehaviour.Properties wood(BlockBehaviour.Properties properties, float hardness, float resistance) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS)
            .strength(hardness, resistance).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties stone(BlockBehaviour.Properties properties) {
        return properties.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASEDRUM)
            .strength(2.0F, 10.0F).sound(SoundType.STONE).requiresCorrectToolForDrops();
    }
}
```

- [ ] **Step 4: Регистрация**

`ForbiddenMagic`, перед `FmItems.ITEMS.register`:

```java
        FmBlocks.BLOCKS.register(modEventBus);
```

`FmCreativeTab`, после инструментов:

```java
        event.accept(FmBlocks.TAINT_LOG.get().asItem());
        event.accept(FmBlocks.TAINT_PLANKS.get().asItem());
        event.accept(FmBlocks.TAINT_STONE_SMOOTH.get().asItem());
        event.accept(FmBlocks.TAINT_STONE_BRICK.get().asItem());
        event.accept(FmBlocks.STAR_BLOCK.get().asItem());
```

- [ ] **Step 5: Текстуры**

```bash
mkdir -p src/main/resources/assets/forbiddenmagic/textures/block
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/taint_log_side.png src/main/resources/assets/forbiddenmagic/textures/block/taint_log_side.png
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/taint_log_bottom.png src/main/resources/assets/forbiddenmagic/textures/block/taint_log_bottom.png
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/taint_planks.png src/main/resources/assets/forbiddenmagic/textures/block/taint_planks.png
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/taint_stone_smooth.png src/main/resources/assets/forbiddenmagic/textures/block/taint_stone_smooth.png
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/taint_stone_brick.png src/main/resources/assets/forbiddenmagic/textures/block/taint_stone_brick.png
cp ../ForbiddenMagic/src/main/resources/assets/forbidden/textures/blocks/starblock.png src/main/resources/assets/forbiddenmagic/textures/block/starblock.png
```

- [ ] **Step 6: Генератор блоков**

В `tools/gen_resources.py` добавить:

```python
BLOCK_TEXTURES = {
    "taint_planks": ("taint_planks",),
    "taint_stone_smooth": ("taint_stone_smooth",),
    "taint_stone_brick": ("taint_stone_brick",),
    "star_block": ("starblock",),
}

def generate_blocks() -> None:
    for name, (texture,) in sorted(BLOCK_TEXTURES.items()):
        write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"forbiddenmagic:block/{name}"}}})
        write_json(ASSETS / "models" / "block" / f"{name}.json", {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"forbiddenmagic:block/{texture}"},
        })
        write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"forbiddenmagic:block/{name}"})
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:block/{name}"},
        })
    write_json(ASSETS / "blockstates" / "taint_log.json", {
        "variants": {
            "axis=x": {"model": "forbiddenmagic:block/taint_log_horizontal", "x": 90, "y": 90},
            "axis=y": {"model": "forbiddenmagic:block/taint_log"},
            "axis=z": {"model": "forbiddenmagic:block/taint_log_horizontal", "x": 90},
        }
    })
    write_json(ASSETS / "models" / "block" / "taint_log.json", {
        "parent": "minecraft:block/cube_column",
        "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": "forbiddenmagic:block/taint_log_side"},
    })
    write_json(ASSETS / "models" / "block" / "taint_log_horizontal.json", {
        "parent": "minecraft:block/cube_column_horizontal",
        "textures": {"end": "forbiddenmagic:block/taint_log_bottom", "side": "forbiddenmagic:block/taint_log_side"},
    })
    write_json(ASSETS / "models" / "item" / "taint_log.json", {"parent": "forbiddenmagic:block/taint_log"})
    write_json(ASSETS / "items" / "taint_log.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/taint_log"},
    })
    print(f"blocks: {len(BLOCK_TEXTURES) + 1} models and blockstates")
```

Вызвать `generate_blocks()` в `main()`.

- [ ] **Step 7: Теги**

`data/minecraft/tags/block/mineable/axe.json`:

```json
{"replace": false, "values": ["forbiddenmagic:taint_log", "forbiddenmagic:taint_planks"]}
```

`mineable/pickaxe.json`:

```json
{"replace": false, "values": ["forbiddenmagic:taint_stone_smooth", "forbiddenmagic:taint_stone_brick", "forbiddenmagic:star_block"]}
```

`needs_stone_tool.json`:

```json
{"replace": false, "values": ["forbiddenmagic:taint_stone_smooth", "forbiddenmagic:taint_stone_brick", "forbiddenmagic:star_block"]}
```

`beacon_base_blocks.json`:

```json
{"replace": false, "values": ["forbiddenmagic:star_block"]}
```

- [ ] **Step 8: Lang**

en:

```toml
[block.forbiddenmagic]
taint_log = "Tainted Log"
taint_planks = "Tainted Planks"
taint_stone_smooth = "Tainted Stone"
taint_stone_brick = "Tainted Stone Bricks"
star_block = "Nether Star Block"
```

ru:

```toml
[block.forbiddenmagic]
taint_log = "Скверное бревно"
taint_planks = "Скверные доски"
taint_stone_smooth = "Скверный камень"
taint_stone_brick = "Скверные каменные кирпичи"
star_block = "Блок звезды Нижнего мира"
```

- [ ] **Step 9: Зелёный прогон**

Run: `python tools/gen_resources.py && cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 36 required tests passed`.

- [ ] **Step 10: Commit**

```bash
git add src/main/java/dev/visfox/forbiddenreforged src/main/resources/assets src/main/resources/data/minecraft tools/gen_resources.py tools/lang src/generated tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add the tainted material blocks"
```

---

### Task 2: Рецепты

**Files:**
- Create: `src/main/resources/data/forbiddenmagic/recipe/{taint_planks,taint_stone_brick,star_block,star_from_block}.json`
- Test: `tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java`

**Interfaces:**
- Produces: рецепты `forbiddenmagic:taint_planks`, `taint_stone_brick`, `star_block`, `star_from_block`.

- [ ] **Step 1: Падающий тест**

`"materials_have_crafting_recipes"` (36):

```java
private static void materialsHaveCraftingRecipes(GameTestHelper helper) {
    var manager = helper.getLevel().getRecipeManager();
    for (String name : new String[]{"taint_planks", "taint_stone_brick", "star_block", "star_from_block"}) {
        var key = net.minecraft.resources.ResourceKey.create(Registries.RECIPE, Identifier.parse("forbiddenmagic:" + name));
        helper.assertTrue(manager.byKey(key).isPresent(), name + " recipe must load");
    }
    helper.succeed();
}
```

- [ ] **Step 2: Красный прогон**

Run: `cmd //c gradlew.bat runGameTestServer`
Expected: `taint_planks recipe must load`.

- [ ] **Step 3: JSON**

`taint_planks.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "key": {"#": "forbiddenmagic:taint_log"},
  "pattern": ["#"],
  "result": {"id": "forbiddenmagic:taint_planks", "count": 4}
}
```

`taint_stone_brick.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "key": {"#": "forbiddenmagic:taint_stone_smooth"},
  "pattern": ["##", "##"],
  "result": {"id": "forbiddenmagic:taint_stone_brick", "count": 4}
}
```

`star_block.json`:

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "building",
  "key": {"#": "minecraft:nether_star"},
  "pattern": ["###", "###", "###"],
  "result": {"id": "forbiddenmagic:star_block", "count": 1}
}
```

`star_from_block.json`:

```json
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": ["forbiddenmagic:star_block"],
  "result": {"id": "minecraft:nether_star", "count": 9}
}
```

- [ ] **Step 4: Зелёный прогон**

Run: `cmd //c gradlew.bat build && cmd //c gradlew.bat runGameTestServer`
Expected: `All 37 required tests passed`.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/data/forbiddenmagic/recipe tests/java/dev/visfox/forbiddenreforged/test/FmGameTests.java
git commit -m "feat: add tainted material recipes"
```

---

### Task 3: Документация и гейт

- [ ] **Step 1: Отступления**

```markdown
| Скверный камень | один блок с метой 0/1 | два блока `taint_stone_smooth` и `taint_stone_brick` | в 26.3 нет метапредметов |
| Боковые текстуры бревна | 75% чистых, 25% «gross» по позиции | всегда чистый `taint_log_side` | позиционный выбор текстуры не выражается blockstate |
| Арканный рецепт скверного камня | `addArcaneCraftingRecipe` с research `TAINTSTONE` | отложен до слайса исследований скверны | нужен research-ключ и предмет `resourceTaint` |
| Плавка бревна | `taint_charcoal` | отложена | предмет ещё не портирован |
```

- [ ] **Step 2: Roadmap/README/verification** — отметить материалы, обновить счёт тестов, коммит и тег `m4-rc1`.

- [ ] **Step 3: Гейт** — build, tests, два прогона `no_lust`, генератор ×2, provenance, host verify.

---

## Self-Review

Пять блоков, теги инструментов и beacon base, четыре рецепта, генератор моделей, тексты и отступления. Отложенные вещи перечислены явно. `FmItems.ITEMS` регистрируется после `FmBlocks.BLOCKS`, но block items попадают в `ITEMS`, поэтому порядок конструктора — `FmBlocks.BLOCKS.register`, затем `FmItems.ITEMS.register`.
