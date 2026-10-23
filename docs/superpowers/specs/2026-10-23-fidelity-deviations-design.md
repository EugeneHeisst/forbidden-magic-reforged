# Fidelity deviations — corrected plan (2026-10-23)

This file replaces the previous version, which was written from a stale deviations list and contained five
items that do not exist in the original. Everything below was checked against `../ForbiddenMagic` and the host's
`tools/id_map.json`, not against prose.

## A. Not deviations — do not implement

- **Corrupting pool "gluttony instead of taint".** `FMEventHandler.getSinShard()` draws `nextInt(7)` with
  case 2 → `gluttonyShard`, case 4 → lust (re-rolled under `noLust`), the rest → `deadlyShards` metas 0,1,3,5,6.
  There is no taint entry. The port's `SIN_POOL` is identical in content and order.
- **Wrath cage centre "should be arcane_stone".** The original passes `blockCosmeticSolid` meta 4;
  the host's `id_map.json` maps meta 4 → `thaumium_block` (arcane stone is meta 6). The port is correct.
- **"Tainted shards are not connected to tainted blocks".** No tainted block in the original drops deadly
  shards; `BlockLeavesTainted` drops taint fruit only. Also: `getSubItems` is creative-tab enumeration, never
  a drop path — do not put drop filters there.
- **"Orichalcum hammer".** There is no such item. Orichalcum is a **wand cap** whose item is the vanilla
  command block, cost 1000, zero vis modifier. Registered in `12ed1f2`; pinned by
  `ItemGameTests.orichalcum_cap_is_registered`.
- **Spork is a texture swap, not a new item.** `ItemDiabolistFork.registerIcons` picks `forbidden:spork`
  when `Config.spork` is on; the name change comes from `ForbiddenItems`. One icon branch, no new content.

## B. Open and real — with the exact original reference

1. **Spork icon.** `ItemDiabolistFork`: `icon = Config.spork ? forbidden:spork : forbidden:fork`.
   Port: name only. Needs the texture copied from the original and selected per config.
2. **Lust → despair under `no_lust`.** `ItemDeadlyShard.vices[4]` becomes `"despair"`, and `DarkAspects`
   registers meta 4 with **`Aspect.VOID`** instead of `LUST` (every other shard keeps
   `NETHER + sin + CRYSTAL`). Both halves are real; the VOID tag is not optional.
3. **Impact plane.** Original `ItemMorphPickaxe/Shovel.onBlockDestroyed` uses the stored `this.side` to pick the
   plane. The working tree already delegates to the host's `ToolHarvest.breakArea(...)`; keep it, but the test
   must prove the plane follows the struck **face**, not the look axis.
4. **Phase colour overlay.** Original `ItemMorph*.getColorFromItemStack` tints the tool by phase. Port has no
   item colour provider; add one keyed on `MorphState.phase()`.
5. **Tainted log side textures.** `BlockLogTainted` has `taint_log_side_gross` and `taint_log_side_gross2`,
   the second as a 25 % variant. Port blockstates/models carry only the plain side.
6. **TAINTSTONE.** Original: arcane recipe for 9 tainted stone from 8 stone + `resourceTaint`
   (`ENTROPY 2 / ORDER 1`), a crafting recipe for taint bricks, and a `TAINTSTONE` research entry plus pages.
   Port has a datapack-style craft and no research key.
7. **Taint coal burn time.** Original fuel handler returns `1 + randy.nextInt(2400)`. Port registers a
   constant 1200.
8. **Creative wand family (core, creative-only).** `neutronium` rod capacity 9001 and its staff 9002
   (item: bedrock, cost 1000, glowing), the `CreativeWandUpdate` that refills every primal each tick, and the
   pre-filled creative-tab wands (orichalcum cap + neutronium rod, 5000 vis per primal; iron cap + profane rod
   with 5000 vis). Port has the orichalcum cap only.

## C. Fix inside the in-flight work

- **`SkullAxeItem extends Item` is a regression.** The original is `ItemSkullAxe extends ItemSword` and overrides
  only `getDigSpeed`. Keep `getDestroySpeed() == 1.0F`, but inherit the sword class, or the axe loses sword
  behaviour (sweep, attack semantics) while also being a 1.0-speed tool.
- **Phase gate is fine as written.** `hasTags(stack)` (morph state / enchantments / custom name) is equivalent in
  practice to the original `itemstack.hasTagCompound()`: a fresh tool has neither, an enchanted one has both.
  Keep the check before the phase write; no further change.

## D. Already closed by the working tree (verify, then drop from fidelity)

- Essence cycling via `WrathCageBlockEntity.cycleMode()` from the diabolist fork.
- `HELLFIRE` research icon → `minecraft:fire`.
- Skull axe speed value 1.0 (modulo the class fix above).
- Impact area delegation to `ToolHarvest.breakArea`.
