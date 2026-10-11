# Releasing SIDScore

GitHub Actions builds and publishes only when a GitHub release is published.
Pushes to branches, pull requests, tag pushes and draft releases do not run a build.
Run checks locally before merging changes or preparing a release.

## Prepare the version

Add changes under `[Unreleased]` in `CHANGELOG.md`, then prepare the next version:

```sh
python3 tools/release.py prepare 0.8.0
```

The command updates the project version in `net.resheim.sidscore/pom.xml` and
moves the notes into a dated release section. It does not commit, tag or publish.
Review the changes and merge them through a PR before proceeding.

## Verify the release locally

Use JDK 21. On macOS, select it with `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`.

```sh
python3 tools/release.py check v0.8.0
mvn -B -f net.resheim.sidscore/pom.xml clean package
python3 -m unittest discover -s tools -p 'test_*.py'
python3 tools/release.py artifacts --output /tmp/sidscore-release-assets
```

The release JAR includes the ANTLR runtime. Assets include the JAR, `SHA256SUMS`
and the SIDScore/ANTLR licenses. The original unshaded Maven JAR is excluded.
Smoke-test the packaged CLI against an example with `--no-play` and an output option.
PRG/SID assembly requires KickAssembler; use the repository's copy explicitly:

```sh
java -Dsidscore.kickass.jar=net.resheim.sidscore/lib/KickAss.jar \
  -jar net.resheim.sidscore/bin/sidscore-cli-0.8.0.jar \
  examples/tempo_changes.sidscore --no-play --sid /tmp/tempo-changes.sid
```

## Create and publish the release

After the preparation PR is merged, update `main` and create an annotated tag
on the reviewed commit. The version must match the POM and changelog exactly:

```sh
git switch main
git pull --ff-only origin main
python3 tools/release.py check v0.8.0
git tag -a v0.8.0 -m "SIDScore 0.8.0"
git push origin v0.8.0
python3 tools/release.py notes 0.8.0 > /tmp/sidscore-release-notes.md
gh release create v0.8.0 --verify-tag --draft --title "SIDScore 0.8.0" \
  --notes-file /tmp/sidscore-release-notes.md
```

Review the draft on GitHub. Publishing the draft starts the release workflow:

1. Check out the exact tag and validate its version and release notes.
2. Build with JDK 21 and run the Java and Python tests.
3. Attach the shaded JAR, checksums and licenses to the GitHub release.
4. Publish the Maven package to GitHub Packages using the repository's `GITHUB_TOKEN`.

The token needs `contents: write` and `packages: write`; the workflow declares both.
Check that the workflow succeeds and that both the release assets and Maven package
are present before announcing the release. Published Maven versions cannot be reused;
`0.7.3` is already published. Keep release tags on their original commits.

For a release candidate, use a version such as `0.8.0-rc.1` throughout and pass
`--prerelease` when creating the draft. SNAPSHOT versions are not accepted as releases.
