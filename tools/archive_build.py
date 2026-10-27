import hashlib
import json
from pathlib import Path
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
properties = dict(line.split("=", 1) for line in (ROOT / "gradle.properties").read_text().splitlines() if "=" in line and not line.startswith("#"))
version = properties["mod_version"]
jar = ROOT / "build/libs" / f"forbidden-magic-{version}.jar"
with zipfile.ZipFile(jar) as archive:
    assert not any("CompatibilityTests" in name or "ClientCompatibilityAudit" in name for name in archive.namelist())
    metadata = archive.read("META-INF/neoforge.mods.toml").decode()
destination = ROOT / "versions" / version
destination.mkdir(parents=True, exist_ok=True)
shutil.copy2(jar, destination / jar.name)
digest = hashlib.sha256(jar.read_bytes()).hexdigest()
(destination / "SHA256SUMS").write_text(f"{digest}  {jar.name}\n")
(destination / "package-audit.json").write_text(json.dumps({"version": version, "minecraft": "1.21.1", "jar": jar.name, "sha256": digest, "size": jar.stat().st_size, "test_classes": False, "host_bundled": False, "metadata": metadata}, indent=2) + "\n")
print("Archived", destination, digest)
