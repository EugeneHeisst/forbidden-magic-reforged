import json
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_SOURCE = ROOT / "tools" / "lang"
LANG_OUTPUT = ROOT / "src" / "generated" / "resources" / "assets" / "forbiddenmagic" / "lang"


def flatten(value: object, prefix: str = "") -> dict[str, str]:
    if isinstance(value, dict):
        result: dict[str, str] = {}
        for key, nested in value.items():
            result.update(flatten(nested, prefix + str(key) + "."))
        return result
    return {prefix.rstrip("."): str(value)}


def main() -> int:
    LANG_OUTPUT.mkdir(parents=True, exist_ok=True)
    for source in sorted(LANG_SOURCE.glob("*.toml")):
        data = tomllib.loads(source.read_text(encoding="utf-8"))
        entries = flatten(data)
        target = LANG_OUTPUT / (source.stem + ".json")
        target.write_text(json.dumps(entries, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        print(f"{target.relative_to(ROOT)}: {len(entries)} keys")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
