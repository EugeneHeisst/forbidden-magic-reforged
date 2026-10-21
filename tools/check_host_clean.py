import subprocess
import sys
from pathlib import Path

HOST = Path(__file__).resolve().parents[2] / "thaumcraft-4-reforged"


def main() -> int:
    result = subprocess.run(
        ["git", "-C", str(HOST), "status", "--porcelain", "--untracked-files=no"],
        capture_output=True,
        text=True,
        check=True,
    )
    if result.stdout.strip():
        print("host working tree is dirty:")
        print(result.stdout)
        return 1
    print("host clean")
    return 0


if __name__ == "__main__":
    sys.exit(main())
