# Upstream PR: add-on aspect registration

## Intent

Prepare a focused pull request to `notlikethiss/thaumcraft-4-reforged` that lets add-ons register additional aspects and merge their aspect tags with the host's built-in item and entity data. The pull request must make Forbidden Magic Reforged usable with a stock host release instead of requiring the local compatibility patch series.

## Goals

- Add a public host API for registering additional aspects.
- Preserve the behavior, tags, codecs, and ordinals of all built-in aspects.
- Merge add-on entity aspect tags with matching built-in tags instead of replacing them.
- Cover registration, lookup, entity merging, and tag-based payload decoding with host GameTests.
- Document the new API in the host's existing add-on API section.
- Keep the host's aspect scanners compatible with the non-enum `Aspect` implementation.
- Produce a reviewable pull request from `MxxFoxx2/thaumcraft-4-reforged` to `notlikethiss/thaumcraft-4-reforged`.

## Non-goals

- Do not include the local Maven publication patch or bump the host mod version.
- Do not change Forbidden Magic gameplay or copy its code or assets into the host.
- Do not refactor unrelated host systems.
- Do not modify generated resources unless their generating source changes.
- Do not change the upstream host working tree directly.

## Constraints

- The upstream repository has no pull-request CI trigger; local verification results must be stated in the pull request description.
- The upstream host uses Java 25, Minecraft 26.3, NeoForge 26.3, four-space Java formatting, and no Java comments.
- The host's `CLAUDE.md` is the source of truth for add-on APIs, architecture seams, generated files, and verification commands.
- Built-in aspects must retain their existing registry indexes because aspect ordinals are serialized.
- The add-on API must work on a dedicated server.
- The add-on must register the same aspects in the same order on the client and dedicated server before an aspect payload is decoded.
- The upstream fork branch starts from upstream commit `69a47a1b238e3ac15fa5d4b10e61ccb6a4a80dbf`.

## Approaches considered

### Public registration surface

#### Public constructor — selected

Restore the original TC4 registration contract with a public constructor:

```java
public Aspect(String tag, int color, Aspect[] components, Identifier image, int blend)
```

This matches TC4, whose constructor documentation explicitly says that add-ons use it to register their own aspects. `components` and `image` are required, and an add-on aspect must be compound. The host creates its six primal aspects through private factories. It keeps the API small and avoids adding a second registration abstraction beside `AspectSync.addRegistrar`.

#### Static factory

Use `Aspect.register(...)` with the same parameters. This avoids exposing construction directly but departs from the original TC4 API and still requires add-ons to retain the returned aspect.

#### Separate add-on facade

Use an `AddonAspects` or `AspectRegistrar` facade to register aspects. This gives the host more control over initialization but enlarges the API and introduces a second registry abstraction. The local patch used this shape, but it is not required to support Forbidden Magic.

### Pull request delivery

#### Direct focused pull request — selected

Rebase and refine the local runtime patches in a dedicated branch on our fork, add upstream-owned tests and documentation, then open a pull request directly to upstream. This matches the user's request and the upstream repository's existing pattern of small API, test, and documentation commits. It avoids maintaining a second discussion in an issue.

### Issue first, pull request after agreement

Open an issue describing the API and attach the patch series, then create the pull request after the maintainer responds. This is socially safer but unnecessarily delays an already requested contribution because no existing issue covers this API.

### Issue and pull request simultaneously

Open both at once. This provides two places for maintainer feedback but duplicates the same technical discussion.

## Public API contract

### Aspect registration

`Aspect` becomes a final registry-backed class. Its public constructor has this shape:

```java
public Aspect(String tag, int color, Aspect[] components, Identifier image, int blend)
```

The constructor registers the aspect immediately and returns the aspect instance for assignment to an add-on field. Contract:

- `tag` is the serialized key, lookup key, and Latin display-name source.
- `components` contains exactly two non-null aspects. The public constructor rejects `null` and arrays of any other length because add-ons cannot safely add primals; the host creates its six primals through private factories.
- `image` is the required add-on-owned texture identifier.
- `blend` is the GL blend mode used for node rendering, normally `1` or `771`.
- A duplicate `tag` throws `IllegalArgumentException`; it does not silently return an existing aspect.
- The built-in aspects retain registry indexes `0` through `47`. Add-on aspects receive indexes after the built-ins in deterministic registration order.
- The same aspects must be registered in the same order on the client and dedicated server. `Aspect` keeps tag-based codecs for aspect lists and payload transport; the stable registry index remains used by entity aspect-orb data.
- The registry-backed class retains `Aspect.values()` and `Aspect.ordinal()` so current host call sites and existing enum-style add-on code continue to compile. `Aspect.getAspect(String)` remains the primary lookup method and returns `null` for an unknown tag; the existing codec/stream codec behavior is unchanged. A `valueOf(String)` compatibility method is not added because tag lookup already existed and enum `valueOf` semantics were based on constant names rather than serialized tags.

The host keeps its existing `AspectSync.addRegistrar(Consumer<AspectRegistry>)` hook for item and entity tags. Registration of aspects themselves occurs during add-on common setup, before `AspectSync.rebuild` runs. A late registrar cannot safely introduce a new aspect because clients may already have decoded payloads containing that aspect.

### Entity aspect merging

The host adds a public merge operation to `AspectRegistry`:

```java
public void mergeEntity(EntityType<?> type, AspectList additions, Object... nbt)
```

And a protected convenience method to `AspectRegistrar`:

```java
protected void entityCopy(String target, AspectList additions, Object... nbt)
```

Merging rules:

- Conditions are normalized by `EntityAspects.of` and compared as an ordered list of `(name, value)` pairs.
- A candidate with exactly equal conditions is merged with the add-on amounts by summing each aspect amount.
- If no candidate has exactly equal conditions, the add-on entry is appended as a new candidate.
- This reproduces TC4/FM behavior where FM adds aspects to an existing entity registration with the same NBT conditions, such as the ordinary and powered creeper entries.
- Existing host registrations and `registerEntity` behavior are unchanged.

## Client and server behavior

The aspect registry is local to each physical side and is not sent as a separate network registry. The existing `AspectTagsPayload` transports aspect tags by string. Therefore:

- Add-ons register aspects during their common setup on both client and dedicated server.
- The host's `AspectSync.rebuild` runs after common setup and before aspect payloads are sent.
- Client/server registration order must match so serialized aspect-orb indexes agree.
- Add-ons must not gate aspect registration differently on client and server.
- The upstream GameTest performs an `AspectList.STREAM_CODEC` round trip with a registered add-on aspect. It verifies tag-based payload decoding without claiming to execute a client-only handler in the dedicated-server GameTest environment.
- The Forbidden Magic GameTest suite provides the add-on integration check for registry lookup, entity merging, and aspect codec behavior. A real client/server visual session remains a manual user check.

## Localization

`Aspect.getDescription()` continues to use `tc.aspect.<tag>`. An add-on owns that translation key in its own `en_us.json` and `ru_ru.json`; the host does not generate add-on translations or resources.

## Implementation shape

Use four atomic commits:

1. `feat: support add-on aspect registration`
   - Replace the closed aspect enum with a stable registry-backed aspect type.
   - Preserve built-in constants, tags, colors, components, codecs, and serialization indexes.
   - Add the public constructor with a required add-on-owned texture identifier.
   - Add entity-tag merging support that preserves matching built-in tags.

2. `feat: cover add-on aspect registration with a game test`
   - Register a test aspect.
   - Verify registration, duplicate rejection, lookup, codec/stream codec behavior, stable index behavior, entity-tag merging, and client/server payload decoding.
   - Keep the test independent of Forbidden Magic.

3. `docs: document add-on aspect registration`
   - Add the public constructor, registration timing, entity merge method, and localization rules to the host's existing add-on API section.

4. `fix: update aspect scanners for non-enum Aspect class`
   - Make the three host scanners recognize both enum and registry-backed aspect declarations.

The local `0003-host-publish.patch` remains outside the pull request. The local patch series and Forbidden Magic source are then migrated to consume the upstream API directly: `FmAspects` uses `new Aspect(...)`, and the old local-only `AddonAspects` compatibility layer is removed. This migration is local follow-up work and is not included in the upstream pull request.

## Pull request presentation

- Base: `notlikethiss/thaumcraft-4-reforged:main`.
- Head: `MxxFoxx2/thaumcraft-4-reforged:feat/addon-aspect-registration`.
- English title describing the public API.
- English body with sections: Summary, API, Compatibility, Testing, and Notes.
- Include exact local command results and the Forbidden Magic Reforged GameTest result.
- Mention that upstream CI does not run on pull requests and that equivalent checks were run locally.
- Disclose that the change was prepared with OpenCode using the `nvidia_nim/minimaxai/minimax-m3.1-flash` model and state that the human partner reviewed the complete diff before submission.
- Do not claim a human-authored summary unless the human partner supplies or approves the exact summary wording.

## Verification

Before opening the pull request:

- Run the host `build` task.
- Run all host GameTests and record the required-test count.
- Run `python3 tools/check_seams.py`.
- Run the relevant resource generators and confirm no unexpected tracked diff.
- Run the Forbidden Magic Reforged gate: `build` and `runGameTestServer`.
- Run `python3 tools/check_provenance.py` for Forbidden Magic Reforged.
- Confirm the upstream host working tree remains unchanged.
- Inspect the final diff and commit history against upstream `main`.

## Remaining implementation detail

The test will be placed in the host's existing aspect/game-test area and will use host-owned test data only. The implementation must additionally account for every former enum switch over `Aspect`; the local patch already identified `AspectOrb` and `WandVis`, but the implementation phase must search the complete upstream tree and verify every affected call site.
