import sys
import zipfile
from pathlib import Path

FORBIDDEN_PREFIXES = ("thaumcraft/", "assets/thaumcraft/", "data/thaumcraft/", "dev/")


def main() -> int:
    jars = sorted(Path("build/libs").glob("forbidden-magic-*.jar"))
    if not jars:
        print("no artifact in build/libs")
        return 1
    violations = []
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            for name in archive.namelist():
                if name.startswith(FORBIDDEN_PREFIXES):
                    violations.append(f"{jar.name}: {name}")
                elif name.endswith(".class") and not name.startswith("fox/"):
                    violations.append(f"{jar.name}: {name}")
    for violation in violations:
        print(violation)
    if violations:
        return 1
    print(f"provenance ok: {len(jars)} jar(s)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
