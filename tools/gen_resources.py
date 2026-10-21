import json
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LANG_SOURCE = ROOT / "tools" / "lang"
ASSETS = ROOT / "src" / "generated" / "resources" / "assets" / "forbiddenmagic"
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "forbiddenmagic" / "textures" / "item"

ITEM_TEXTURES = {
    "wrath_shard": "wrathshard",
    "envy_shard": "envyshard",
    "taint_shard": "taintshard",
    "pride_shard": "prideshard",
    "lust_shard": "lustshard",
    "sloth_shard": "slothshard",
    "greed_shard": "greedshard",
    "gluttony_shard": "gluttonyshard",
    "skull_axe": "skullaxe",
}


def flatten(value: object, prefix: str = "") -> dict[str, str]:
    if isinstance(value, dict):
        result: dict[str, str] = {}
        for key, nested in value.items():
            result.update(flatten(nested, prefix + str(key) + "."))
        return result
    return {prefix.rstrip("."): str(value)}


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def generate_lang() -> None:
    for source in sorted(LANG_SOURCE.glob("*.toml")):
        data = tomllib.loads(source.read_text(encoding="utf-8"))
        entries = flatten(data)
        write_json(ASSETS / "lang" / (source.stem + ".json"), entries)
        print(f"lang/{source.stem}.json: {len(entries)} keys")


def generate_items() -> None:
    missing = [name for name in ITEM_TEXTURES if not (TEXTURES / f"{name}.png").is_file()]
    if missing:
        raise SystemExit(f"no texture for {missing}")
    for name in sorted(ITEM_TEXTURES):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"forbiddenmagic:item/{name}"},
        })
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"},
        })
    print(f"items: {len(ITEM_TEXTURES)} models and definitions")


def main() -> int:
    generate_lang()
    generate_items()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
