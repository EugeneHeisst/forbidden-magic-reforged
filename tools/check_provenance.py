import sys
import json
import tomllib
import zipfile
from pathlib import Path

FORBIDDEN_PREFIXES = ("thaumcraft/", "assets/thaumcraft/", "data/thaumcraft/", "assets/tc4modern/", "data/tc4modern/", "dev/eugheist/")


def main() -> int:
    jars = sorted(Path("build/libs").glob("forbidden-magic-*.jar"))
    if not jars:
        print("no artifact in build/libs")
        return 1
    violations = []
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            names = archive.namelist()
            if len(names) != len(set(names)):
                violations.append(f"{jar.name}: duplicate ZIP entries")
            metadata = tomllib.loads(archive.read("META-INF/neoforge.mods.toml").decode())
            dependencies = metadata["dependencies"]["forbiddenmagic"]
            minecraft = next(item for item in dependencies if item["modId"] == "minecraft")
            if minecraft["versionRange"] != "[1.21.1]":
                violations.append("Artifact must target Minecraft 1.21.1 only")
            if not any(item["modId"] == "tc4modern" and item["type"] == "required" for item in dependencies):
                violations.append("Missing TC4Modern host dependency")
            for name in names:
                if "CompatibilityTests" in name or "ClientCompatibilityAudit" in name:
                    violations.append(f"Audit class in release: {name}")
                if name.startswith("assets/forbiddenmagic/models/") and name.endswith(".json"):
                    model = json.loads(archive.read(name))
                    for texture in model.get("textures", {}).values():
                        if texture.startswith("forbiddenmagic:"):
                            path = "assets/forbiddenmagic/textures/" + texture.split(":", 1)[1] + ".png"
                            if path not in names:
                                violations.append(f"{name}: missing texture {texture}")
            for name in archive.namelist():
                if name in ("dev/eugheist/", "dev/eugheist/portupdates/") or name.startswith("dev/eugheist/portupdates/forbiddenmagic/"):
                    continue  # embedded shared port updater, not TC4Modern code
                if name.startswith(FORBIDDEN_PREFIXES):
                    violations.append(f"{jar.name}: {name}")
                elif name.endswith(".class") and not name.startswith("dev/visfox/forbiddenreforged/"):
                    violations.append(f"{jar.name}: {name}")
    for violation in violations:
        print(violation)
    if violations:
        return 1
    print(f"provenance ok: {len(jars)} jar(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
