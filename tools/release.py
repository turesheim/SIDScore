#!/usr/bin/env python3
"""Prepare release metadata and validate/package a reviewed SIDScore release."""
import argparse
from datetime import date
import hashlib
from pathlib import Path
import re
import shutil
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
POM = Path("net.resheim.sidscore/pom.xml")
NS = {"m": "http://maven.apache.org/POM/4.0.0"}
VERSION = re.compile(r"(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)\.(?:0|[1-9]\d*)(?:-(?:alpha|beta|rc)\.[1-9]\d*)?")


def validate_version(version):
    if not VERSION.fullmatch(version):
        raise ValueError("Use a release version such as 0.8.0 or 0.8.0-rc.1; SNAPSHOT is not a release")
    return version


def project_version(root):
    return ET.parse(root / POM).findtext("m:version", namespaces=NS)


def release_notes(root, version):
    changelog = (root / "CHANGELOG.md").read_text(encoding="utf-8")
    match = re.search(r"^## \[" + re.escape(version) + r"\][^\n]*\n(.*?)(?=^## |\Z)",
                      changelog, re.MULTILINE | re.DOTALL)
    if not match or not match[1].strip():
        raise ValueError(f"Missing release notes for {version} in CHANGELOG.md")
    return match[1].strip() + "\n"


def check_release(root, tag):
    if not tag.startswith("v"):
        raise ValueError("Release tags must start with v")
    version = validate_version(tag[1:])
    current = project_version(root)
    if current != version:
        raise ValueError(f"Tag {tag} does not match POM version {current}")
    release_notes(root, version)
    return version


def prepare_release(root, version):
    validate_version(version)
    changelog_path = root / "CHANGELOG.md"
    changelog = changelog_path.read_text(encoding="utf-8")
    if re.search(r"^## \[" + re.escape(version) + r"\]", changelog, re.MULTILINE):
        raise ValueError(f"Release {version} is already prepared")
    unreleased = re.search(r"^## \[Unreleased\]\n(.*?)(?=^## |\Z)",
                           changelog, re.MULTILINE | re.DOTALL)
    if not unreleased or not unreleased[1].strip():
        raise ValueError("Add notes under ## [Unreleased] before preparing a release")
    pom_path = root / POM
    pom = pom_path.read_text(encoding="utf-8")
    current = project_version(root)
    pom, count = re.subn(r"<version>" + re.escape(current) + r"</version>",
                        f"<version>{version}</version>", pom, count=1)
    if count != 1:
        raise ValueError("Could not update the project version")
    section = f"## [Unreleased]\n\n## [{version}] - {date.today().isoformat()}\n\n{unreleased[1].strip()}\n\n"
    changelog = changelog[:unreleased.start()] + section + changelog[unreleased.end():]
    pom_path.write_text(pom, encoding="utf-8")
    changelog_path.write_text(changelog, encoding="utf-8")


def package_artifacts(root, output):
    version = check_release(root, "v" + project_version(root))
    jar_name = f"sidscore-cli-{version}.jar"
    source = root / "net.resheim.sidscore/bin" / jar_name
    if not source.is_file():
        raise ValueError(f"Build the release first: missing {source}")
    # Maven's original (unshaded) JAR is deliberately excluded.
    with zipfile.ZipFile(source) as jar:
        for required in ["net/resheim/sidscore/SIDScoreCLI.class", "org/antlr/v4/runtime/Lexer.class"]:
            if required not in jar.namelist():
                raise ValueError(f"Release JAR is missing {required}")
    output.mkdir(parents=True, exist_ok=True)
    destination = output / jar_name
    shutil.copyfile(source, destination)
    (output / "SHA256SUMS").write_text(
        f"{hashlib.sha256(destination.read_bytes()).hexdigest()}  {jar_name}\n", encoding="utf-8")
    shutil.copyfile(root / "LICENSE", output / "LICENSE")
    shutil.copyfile(root / "net.resheim.sidscore/lib/LICENSE.txt", output / "ANTLR-LICENSE.txt")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("prepare").add_argument("version")
    commands.add_parser("check").add_argument("tag")
    commands.add_parser("notes").add_argument("version")
    commands.add_parser("artifacts").add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    try:
        if args.command == "prepare":
            prepare_release(ROOT, args.version)
            print(f"Prepared {args.version}. Review and commit POM/CHANGELOG before creating the release.")
        elif args.command == "check":
            print(f"Release {check_release(ROOT, args.tag)} validated")
        elif args.command == "notes":
            print(release_notes(ROOT, validate_version(args.version)), end="")
        else:
            package_artifacts(ROOT, args.output)
            print(f"Release assets written to {args.output}")
    except (ValueError, OSError, ET.ParseError, zipfile.BadZipFile) as error:
        parser.exit(1, f"Release error: {error}\n")


if __name__ == "__main__":
    main()
