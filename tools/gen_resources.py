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
    "emerald_fragment": "emerald_nugget",
}

MORPH_TEXTURES = {
    "morph_pickaxe": ("chameleonpick", "eyepick"),
    "morph_sword": ("chameleonsword", "eyesword"),
    "morph_shovel": ("chameleonshovel", "eyeshovel"),
    "morph_axe": ("chameleonaxe", "eyeaxe"),
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
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True) + "\n", encoding="utf-8", newline="\n")


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


def generate_morph_items() -> None:
    for name, (base, overlay) in sorted(MORPH_TEXTURES.items()):
        write_json(ASSETS / "models" / "item" / f"{name}.json", {
            "parent": "minecraft:item/generated",
            "textures": {
                "layer0": f"forbiddenmagic:item/{base}",
                "layer1": f"forbiddenmagic:item/{overlay}",
            },
        })
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:item/{name}"},
        })
    print(f"morph tools: {len(MORPH_TEXTURES)} two-layer models")



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


PLANT_MODELS = {
    "taint_leaves": ("cube_all", "taint_leaves"),
    "taint_sapling": ("cross", "sapling_taint"),
}


def generate_plants() -> None:
    for name, (parent, texture) in sorted(PLANT_MODELS.items()):
        write_json(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"forbiddenmagic:block/{name}"}}})
        model = {
            "parent": f"minecraft:block/{parent}",
            "render_type": "minecraft:cutout",
        }
        if parent == "cube_all":
            model["textures"] = {"all": f"forbiddenmagic:block/{texture}"}
        else:
            model["textures"] = {"cross": f"forbiddenmagic:block/{texture}"}
        write_json(ASSETS / "models" / "block" / f"{name}.json", model)
        write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"forbiddenmagic:block/{name}"})
        write_json(ASSETS / "items" / f"{name}.json", {
            "model": {"type": "minecraft:model", "model": f"forbiddenmagic:block/{name}"},
        })
    print(f"plants: {len(PLANT_MODELS)} models and blockstates")


def generate_umbral_plants() -> None:
    write_json(ASSETS / "blockstates" / "umbral_rose.json", {"variants": {"": {"model": "forbiddenmagic:block/umbral_rose"}}})
    write_json(ASSETS / "models" / "block" / "umbral_rose.json", {
        "parent": "minecraft:block/cross",
        "render_type": "minecraft:cutout",
        "textures": {"cross": "forbiddenmagic:block/flower_black"},
    })
    write_json(ASSETS / "models" / "item" / "umbral_rose.json", {"parent": "forbiddenmagic:block/umbral_rose"})
    write_json(ASSETS / "items" / "umbral_rose.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:block/umbral_rose"},
    })
    write_json(ASSETS / "blockstates" / "umbral_rose_bush.json", {
        "variants": {
            "half=lower": {"model": "forbiddenmagic:block/umbral_rose_bush_bottom"},
            "half=upper": {"model": "forbiddenmagic:block/umbral_rose_bush_top"},
        }
    })
    for half, texture in (("bottom", "umbral_rose_bottom"), ("top", "umbral_rose_top")):
        write_json(ASSETS / "models" / "block" / f"umbral_rose_bush_{half}.json", {
            "parent": "minecraft:block/cross",
            "render_type": "minecraft:cutout",
            "textures": {"cross": f"forbiddenmagic:block/{texture}"},
        })
    write_json(ASSETS / "models" / "item" / "umbral_rose_bush.json", {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": "forbiddenmagic:block/umbral_rose_top"},
    })
    write_json(ASSETS / "items" / "umbral_rose_bush.json", {
        "model": {"type": "minecraft:model", "model": "forbiddenmagic:item/umbral_rose_bush"},
    })
    print("umbral plants: 4 models and 2 blockstates")

def main() -> int:
    generate_lang()
    generate_items()
    generate_morph_items()
    generate_blocks()
    generate_plants()
    generate_umbral_plants()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
