# Forbidden Magic — TC4Modern fork

This fork targets Minecraft 1.21.1 / NeoForge 21.1.256 / Java 21 only. Use branch `1.21.1`; do not merge upstream `main` (26.3).
Read `/home/eugheist/Project/CLAUDE.md` when it exists for shared port workflow and release rules.

The upstream Reforged port and its attribution remain in history. Its prior agent instructions are archived in `docs/upstream-AGENTS.md`; references to the other host, Windows, 26.3, or host patches do not apply to this fork.

Required host: EugeneHeisst/thaumcraft-port, modId tc4modern, minimum 0.17.191. Do not bundle any host classes/assets or edit the host repository. Bridges live under dev.visfox.forbiddenreforged.compat and targeted mixins under .mixin. Keep original Forbidden Magic mechanics and resource attribution; document necessary integration deviations in docs/tc4modern-compatibility.md.

Build: `python tools/prepare_dependencies.py`; `./gradlew build`. All downloads are pinned and SHA-256 checked in data/compat-dependencies.json. Optional -Ptc4Jar overrides the host artifact.

Integration tests: `python tools/prepare_dependencies.py --test-pack`; `./gradlew -PcompatTest -PcompatPack=deps/test-pack runGameTestServer`. Require the native "All ... required tests passed" log marker; Gradle can exit successfully after a game startup failure. Test sources only compile with -PcompatTest and must not ship in the release JAR; use `clean build` without the property for release. Hidden, muted isolated client audits are permitted, never use the user's live game. Run one Gradle process per project.

Java: four spaces. Commits: feat:/fix:/docs:/chore:, English, no coauthor trailer. Preserve MIT/WTFPL attribution and NOTICE. This fork does not support installing over the upstream Reforged version; both use forbiddenmagic modId.
