# Forbidden Magic for Thaumcraft 4: Reforged — design

Status: approved in chat on 2026-10-20, pending spec review
Target: Minecraft 26.3 / NeoForge 26.3.0.57-beta / Java 25, add-on to Thaumcraft 4: Reforged `main`
Source of behaviour: Forbidden Magic 0.574 for Minecraft 1.7.10 (`ForbiddenMagic/`, WTFPL)

## 1. Goal

Port Forbidden Magic to run as an add-on on Thaumcraft 4: Reforged, keeping the original
mod's content, numbers, recipes, research text and assets as the reference, the same doctrine
the host itself applies to Thaumcraft 4.2.3.5. Content that requires a third-party mod which
does not exist for this loader and version is dropped, not invented.

The port is one project in one new directory. The host working tree stays byte-identical to
the upstream clone at all times; this is enforced by a gate, not by discipline.

## 2. Constraints

- **Host ownership and license.** Thaumcraft 4: Reforged belongs to `notlikethiss`. The repo has
  no `LICENSE` file, declares `mod_license=All Rights Reserved`, and states in `README.md:9`
  "Personal use only … not intended for redistribution". Modifications are therefore developed
  locally and proposed upstream; no rebuilt host jar is ever published by this project.
- **Host addon API is not a stability contract.** The addon-facing surface is documented in the
  host's `CLAUDE.md` section "API для аддонов", which is an instruction file for the host's own
  agents, not a published compatibility promise. Every host version is pinned by commit sha.
- **Original Forbidden Magic is WTFPL.** Its code and its 135 textures may be copied and modified.
  Thaumcraft assets and code are not copied into this project; where the original references
  `thaumcraft:` texture paths, those resolve at runtime from the host.
- **Parity.** Numbers, aspect costs, research text and recipe shapes are taken from the 1.7.10
  source unchanged. Any deviation is recorded in `docs/deviations.md` with the reason.
- **Attribution.** The port is published as Forbidden Magic by its original author, this
  repository as the port, Thaumcraft 4: Reforged and Azanor in credits, and with a statement of
  AI-assisted development.

## 3. Why the host must change at all

Forbidden Magic registers seven aspects of its own — `luxuria`, `infernus`, `superbia`, `gula`,
`invidia`, `desidia`, `ira` — built with `new Aspect(tag, color, components, texture, blend)`.
They carry 16 recipe definitions, 16 research entries, 17 references in the Wrath Cage and the
identity of the Deadly Shards.

In the host, `Aspect` is `public enum` of 48 constants (`thaumcraft/aspect/Aspect.java:17`) with
the texture path hard-wired to `thaumcraft:textures/aspects/<tag>.png`. A Java enum cannot gain a
constant from outside: the constructor is private, `$VALUES` is fixed, reflective creation throws.
`AspectList` is keyed `Map<Aspect, Integer>`, so a stand-in type is not an option either. There is
no `IClassTransformer` or coremod hook in NeoForge 26.3 sources; `accesstransformer.cfg` only
widens visibility, it cannot restructure a class.

Verified against the host source, so that the size of the ask is known:

| Measured | Value | Consequence |
|---|---|---|
| `Aspect.<CONSTANT>` references | 1827, of which 1379 in four generated files | generated files are untouched; field access on `static final` keeps the same syntax |
| `Aspect.values()` call sites | 17 in hand-written code plus 2 test files | a `static Aspect[] values()` method keeps every call site valid |
| `switch` on an `Aspect` | none | no exhaustive-switch rewrites |
| `Aspect.valueOf`, `isEnum`, `<E extends Enum>` | none | no hidden enum dependencies |
| `aspect.ordinal()` | 1 site, `entity/AspectOrb.java:41` | registry index must stay stable; covered by a game test |
| Enum-ness that must be replaced | `StringRepresentable.fromEnum`, `values()` loops in `combine`/`getPrimalAspects`/`getCompoundAspects` | localised inside `Aspect.java` |

The change is one file plus a hook, and it does not touch `platform/` seams, so the host's
`tools/check_seams.py` gate is unaffected.

## 4. Architecture

### 4.1 Repository layout

```
forbidden-magic-reforged/
  host/                     pristine clone at the pinned sha, patched          (gitignored)
  patches/                  series .patch files, tracked, the temporary crutch
  maven/                    local maven repo the patched host is published to   (gitignored)
  src/main/java/fox/spiteful/forbidden/     the port
  src/main/resources/                        FM's own assets
  src/main/java/fox/spiteful/forbidden/test/ game tests
  src/generated/resources/                   written by tools/, never by hand
  tools/                    prepare_host.py, gen_resources.py, id_map.json, checks
  reference/ForbiddenMagic/ the 1.7.10 source, source of truth for behaviour    (gitignored)
  docs/                     this spec, deviations.md, dropped.md, upstream.md
```

`patches/` is tracked because it is also the payload of the upstream proposal. Nothing published
by this project contains a rebuilt host class file.

### 4.2 Host patch series

| Patch | Content | Scope |
|---|---|---|
| `0001-aspect-registry` | `Aspect`: enum to `final class` with a registry, `static final` constants in the original order, `static Aspect[] values()` over builtins then addon registrations, `CODEC`/`STREAM_CODEC` via the by-tag map, `image` accepts a full `Identifier`, public `Aspect.register(...)` | `aspect/Aspect.java`, ~200 lines |
| `0002-aspect-addon-init` | a point where an addon registers aspects before `AspectSync.rebuild` runs | ~20 lines |
| `0003-host-publish` | `maven-publish` producing `thaumcraft:thaumcraft:1.2`, jar name carrying the pinned sha | ~15 lines |

The patched host reports mod version `1.2`, not `1.1` and not a suffixed variant: a pre-release
qualifier such as `1.2-fm.1` sorts below `1.2` and would fail the version range the port declares.
`0001`, `0002` and `0003` are exactly the files a future upstream merge makes this project delete.

`0004-taint-block-tag` is held back. The host hard-codes eight taint blocks in
`taint/TaintSpread.java:88`, but the port can satisfy Forbidden Magic's requirement — taint wood
and taint stone are immune to fibrous taint — without it, because taint spread only converts air,
replaceable blocks and vegetation. If increment 8 shows the exemption is not achievable cleanly,
the patch is added then rather than assumed now.

`tools/prepare_host.py` clones the local host at the sha recorded in `host.sha`, applies the
series, and fails loudly on the first hunk that does not land.

### 4.3 Addon integration, per host API

| Forbidden Magic needs | Host mechanism |
|---|---|
| new aspects | `Aspect.register` from patch `0001` |
| item and entity aspect tags, including retro-tagging vanilla mobs | subclass `thaumcraft.aspect.AspectRegistrar` (already `public abstract`, resolves ids and item tags, `copy`/`fill`/`complex` give merge semantics) fed through `AspectSync.addRegistrar` |
| research category, 34 entries, 35 pages | `ResearchCategories.registerCategory`, `ResearchItem` builder, `ResearchPage` sealed records |
| arcane, crucible, infusion, infusion-enchantment recipes | `TcRecipes.register(TcRecipe)`; recipes are records `(key, research, result, AspectList, ingredients)` |
| dynamic wand recipe behaviour | `ArcaneCrafting.registerDynamic(DynamicArcaneRecipe)` |
| wand caps and rods with per-rod update logic | `WandRegistry.registerCap`/`registerRod` with explicit `item` and `texture` identifiers and a `RodUpdate` lambda; research keys `CAP_<tag>`/`ROD_<tag>`, lang supplied by the addon |
| custom focus (Blink) | subclass the open `thaumcraft.wand.focus.FocusItem` |
| baubles (Ring of Nutrition) | `thaumcraft.platform.Bauble`; four slots exist, no new slot types are needed |
| warp from gear | `Warp.registerItem`, `WarpingGear` interface |
| stack-carrying data in infusion results | `TcStack.registerDataHandler` / `registerDataMatcher` |
| enchantments | enchantment JSON in the port's own datapack plus behaviour handlers; the host resolves infusion-valid enchantments from registered `InfusionEnchantmentRecipe` entries, so adding one is enough |
| taint-immune blocks, world generation, villager trades | the port's own `DeferredRegister`, NeoForge features and data-driven trades; public helpers `VisNet`, `TaintBiomes`, `GolemActions`, `Transfer`, `Teleport` are reused rather than re-implemented |

### 4.4 Everything else stays host-independent

The single class touching the patched API is `FmAspects`. The dependency is declared as
`thaumcraft` version range `[1.2,)`, which is the version the upstream proposal asks for. Switching
from the local patch to an upstream release is therefore: delete `patches/0001`, `patches/0002`,
`patches/0003`, drop `host/` from `prepare_host.py`, repoint the repository at the published jar.
No port file changes. This is the exit condition that keeps the project from locking onto a fork.

### 4.5 Resources and conventions

Inherited from the host, because diverging would make a backport and a future upstream merge harder:

- resources are produced by Python generators in `tools/`; no datagen, no hand-edited
  `src/generated/resources`
- meta variants of 1.7.10 items and blocks become distinct identifiers plus blockstate properties,
  keyed by an `id_map.json` the way the host does it
- lang: `en_us` and `ru_ru` only, original Forbidden Magic wording preserved; research keys follow
  `tc.research_name.<KEY>` / `tc.research_text.<KEY>`
- Java: four spaces, no comments; Python: four spaces, double quotes, `pathlib`, no comments
- commits in English, `feat:` / `fix:`, no co-author lines
- regenerating resources must leave `git status` empty

## 5. Content inventory

### 5.1 Ported

- blocks: Arcane Cake, Wrath Cage, Ink Flower, Umbral Bush, Star Block, taint stone, taint log,
  taint planks, taint leaves, taint sapling
- items: Deadly Shards (wrath, envy, taint, pride, lust, sloth, greed), Gluttony Shard, taint coal,
  taint fruit, morph pickaxe/sword/shovel/axe, taint pickaxe/shovel, skull axe, diabolist fork and
  spork, riding crop, arcane cake item, divine orb, scribes (bloodwell, boundwell, crystalwell,
  primewell), ring of nutrition, FM resource items minus third-party variants, wand caps and cores
  minus third-party variants, Blink focus
- seven aspects and the full item and entity tagging table from `DarkAspects`
- research: one custom category and 34 entries with 35 pages
- recipes: arcane, crucible, infusion and infusion-enchantment sets from `ForbiddenRecipes`
- enchantments: Cluster, Consuming, Corrupting, Educational, Greedy, Impact, Void, Wrath
- Wrath Cage block entity, its spawner logic and its renderer, with geometry computed in the
  port's equivalent of `client/geom`
- taint tree world generation in taint biomes, heretic villager and its trades
- config, including the original `no_lust` kill switch, kept as a synced config

### 5.2 Dropped, with the reason recorded in `docs/dropped.md`

- `compat/` in full: Blood Magic, Botania, Ars Magica 2, CoFH, Thaumic Tinkerer detection — none
  of these exist for NeoForge 26.3
- `ItemBloodRapier`, `DarkPotions` and both potion effects — Blood Magic API
- four Botania `SubTile` implementations (bloodthorn, euclidaisy, tainthistle, whisperweed)
- `ItemSubCollar` and `ItemMobCrystal` — Thaumic Tinkerer
- blood, mana and staff wand updates that read Blood Magic or Botania resources
- third-party meta variants inside `ItemResource`, `ItemWandCaps`, `ItemWandCores`. The classes
  survive, the variants are cut. The exact list is written into `docs/dropped.md` in increment 2,
  once every variant is traced to the mod that supplied its item — 27 names are registered and the
  mod of origin is not recorded in the 1.7.10 source, so guessing them now would put an unverified
  list in the spec
- `plans.txt` is never implemented: it is the original author's unfinished idea list, including
  demon binding and sinful bees

### 5.3 Out of scope for now

Backport to `mc/1.21.1`. It is a real target — the host publishes a `1.21.1` release — but the
upstream proposal has to land in `main` first, and the host's own backport chain moves from
`main` outward, so a `1.21.1` port before that means hand-porting the patch twice.

## 6. Increments

Every increment ends with: `gradlew build` green against the patched host,
`gradlew runGameTestServer` green, `git -C ../thaumcraft-4-reforged status --porcelain` empty for
tracked files, and a resource regeneration that changes nothing.

| # | Slice | Gate |
|---|---|---|
| 0 | `prepare_host.py`, patch series, local maven, empty mod with `neoforge.mods.toml` requiring `thaumcraft` | patched host builds; the port loads in `runClient` beside it; a game test asserts seven aspects exist and survive a client sync |
| 1 | aspects, item and entity tag tables, textures, lang | `combine` on new aspects, `AspectOrb` index stability, discovery in the Thaumonomicon, JEI aspect strips render |
| 2 | blocks and items, models via `tools/`, creative tab | registration and id-map coverage tests; **decision point: send the upstream proposal, and whether the port publishes or stays personal-use** |
| 3 | research category and 34 entries | unlock by parents, aspect cost, hidden and warp flags |
| 4 | recipes | one game test per crafting type, costs equal to the 1.7.10 values |
| 5 | eight enchantments | application, behaviour of `Corrupting` and `Void` drops |
| 6 | caps, rods, rod updates, Blink focus | wand crafted through the host's dynamic wand recipe, vis update tick, focus activation |
| 7 | Wrath Cage and baubles | spawner logic game test; renderer verified by eye by the user |
| 8 | taint wood set, world generation, heretic trades | taint immunity assertions; decides whether `0004-taint-block-tag` is needed |

Increments 1 through 8 are the port; increment 0 is its infrastructure. Nothing in a later
increment is started while an earlier gate is red.

## 7. Risks

- **Upstream answer is not ours to schedule.** The host's maintainer closes issues within a day and
  merges agent-authored pull requests, but an outside pull request that has been open since 19
  October has no review comments yet. Mitigation: the issue and the pull request are prepared from
  the working patch series, and the fallback form (mapping the seven aspects onto existing ones) is
  costed, not dismissed.
- **`ordinal()` in entity data.** `AspectOrb` stores an aspect as an int. Appending addon aspects
  after the 48 builtins in registration order keeps existing values valid; a game test pins it.
- **Beta loader.** NeoForge `26.3.0.57-beta` and the host both move. Mitigation: pinned sha,
  `prepare_host.py` failing loudly, and the whole host rebuild taking about seven minutes on this
  machine.
- **Client and server registration order.** An aspect must exist on both sides before an
  `AspectList` decodes. Mitigation: registration happens in the mod constructor, and a test loads
  a dedicated server plus a client.
- **Behaviour that has no 1.7.10 analogue.** `WandTriggerRegistry`, mutating
  `ThaumcraftApi.scanEntities`, `IScribeTools` are replaced by host seams, so parity is by
  behaviour, not by API. Each substitution is recorded in `docs/deviations.md`.
- **Visual correctness is not covered by tests.** Models, the Wrath Cage renderer and aspect icons
  are checked by the user in `runClient`, following the host's own convention.

## 8. Distribution plan

1. Develop against `patches/` locally.
2. At the increment 2 gate, send the upstream proposal: an issue in the host tracker (Russian, the
   language the whole tracker and `CLAUDE.md` use) describing the addon aspect need, with the
   working patch series and green check runs attached, stating that the work was AI-assisted.
   Drafting the text is this project's job; sending it is the user's.
3. If the proposal is taken, drop `patches/0001` through `0003` and depend on the released host.
   If it is refused or stalls, either keep the port personal-use with local patches, or switch the
   aspect layer to the substitution form; that switch is a decision, not a default, and it is taken
   with the numbers from increments 0 and 1 in hand.
