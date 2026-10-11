import hashlib
from pathlib import Path
import tempfile
import unittest
import zipfile

import release


class ReleaseTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        (self.root / release.POM).parent.mkdir(parents=True)
        (self.root / release.POM).write_text('''<project xmlns="http://maven.apache.org/POM/4.0.0">
  <version>0.7.3</version>
  <dependencies><dependency><version>0.7.3</version></dependency></dependencies>
</project>
''')
        (self.root / "CHANGELOG.md").write_text("# Changelog\n\n## [Unreleased]\n\n- Inline tempo.\n")

    def test_prepare_retains_dependency_versions_and_moves_notes(self):
        release.prepare_release(self.root, "0.8.0")
        self.assertEqual("0.8.0", release.project_version(self.root))
        self.assertIn("<dependency><version>0.7.3</version>", (self.root / release.POM).read_text())
        self.assertEqual("- Inline tempo.\n", release.release_notes(self.root, "0.8.0"))
        self.assertEqual("0.8.0", release.check_release(self.root, "v0.8.0"))
        with self.assertRaises(ValueError):
            release.prepare_release(self.root, "0.8.0")

    def test_tag_mismatch_and_missing_notes_fail(self):
        for tag in ["v0.8.0", "0.7.3", "v0.7.3-SNAPSHOT", "v0.7.3"]:
            with self.assertRaises(ValueError):
                release.check_release(self.root, tag)

    def test_empty_notes_do_not_partially_update_version(self):
        (self.root / "CHANGELOG.md").write_text("# Changelog\n\n## [Unreleased]\n")
        with self.assertRaises(ValueError):
            release.prepare_release(self.root, "0.8.0")
        self.assertEqual("0.7.3", release.project_version(self.root))

    def test_release_candidate_versions_are_supported(self):
        release.prepare_release(self.root, "0.8.0-rc.1")
        self.assertEqual("0.8.0-rc.1", release.check_release(self.root, "v0.8.0-rc.1"))

    def test_artifacts_require_shaded_jar_and_include_checksum_and_licenses(self):
        release.prepare_release(self.root, "0.8.0")
        jar_path = self.root / "net.resheim.sidscore/bin/sidscore-cli-0.8.0.jar"
        jar_path.parent.mkdir()
        with zipfile.ZipFile(jar_path, "w") as jar:
            jar.writestr("net/resheim/sidscore/SIDScoreCLI.class", b"main")
        output = self.root / "assets"
        with self.assertRaises(ValueError):
            release.package_artifacts(self.root, output)
        self.assertFalse(output.exists())
        with zipfile.ZipFile(jar_path, "a") as jar:
            jar.writestr("org/antlr/v4/runtime/Lexer.class", b"runtime")
        (self.root / "LICENSE").write_text("EPL-2.0")
        (self.root / "net.resheim.sidscore/lib").mkdir()
        (self.root / "net.resheim.sidscore/lib/LICENSE.txt").write_text("ANTLR license")
        release.package_artifacts(self.root, output)
        self.assertEqual(jar_path.read_bytes(), (output / jar_path.name).read_bytes())
        expected = hashlib.sha256(jar_path.read_bytes()).hexdigest() + "  " + jar_path.name + "\n"
        self.assertEqual(expected, (output / "SHA256SUMS").read_text())
        self.assertEqual("ANTLR license", (output / "ANTLR-LICENSE.txt").read_text())


if __name__ == "__main__":
    unittest.main()
