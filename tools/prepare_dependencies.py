import argparse
import hashlib
import json
import os
import time
from pathlib import Path
import urllib.request

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--test-pack", action="store_true")
    args = parser.parse_args()
    for entry in json.loads((ROOT / "data/compat-dependencies.json").read_text()):
        if not entry["host"] and not args.test_pack:
            continue
        destination = ROOT / "deps" / ("" if entry["host"] else "test-pack") / entry["name"]
        destination.parent.mkdir(parents=True, exist_ok=True)
        if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() == entry["sha256"]:
            print("Verified", destination.name)
            continue
        request = urllib.request.Request(entry["url"], headers={"User-Agent": "ForbiddenMagic-TC4Modern/0.1.0"})
        for attempt in range(4):
            try:
                with urllib.request.urlopen(request, timeout=120) as response:
                    data = response.read()
                break
            except OSError:
                if attempt == 3:
                    raise
                time.sleep(2 ** attempt)
        if hashlib.sha256(data).hexdigest() != entry["sha256"]:
            raise RuntimeError("Dependency digest mismatch: " + entry["name"])
        temporary = destination.with_suffix(".download")
        temporary.write_bytes(data)
        os.replace(temporary, destination)
        print("Downloaded and verified", destination.name)


if __name__ == "__main__":
    main()
