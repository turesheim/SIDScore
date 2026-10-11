# Changelog

## [Unreleased]

## [0.8.0] - 2026-10-11

### Added

- Inline `TEMPO` changes affecting all voices in a tune, with independent tempo maps per subtune.
- Local MIDI regeneration with embedded instruments and preserved tempo changes.
- The `play` launcher with tune selection and song information.
- Relocatable C64 modules with callable effects and shared voice ownership across tunes.

### Fixed

- Repeated MIDI notes retain their attacks instead of being merged into held notes.
- Tied notes retain their initial attack when extended across a tempo change.
- Beethoven examples have separate melody, middle and bass instruments with audible sustain.
- MIDI-derived examples include their instrument definitions directly.
- Mozart KV309 grace-note timing and the K.2 minuet triplet.
- Music resumes at its current position after embedded effects finish.
- Inactive SID notation becomes rests, with refreshed game conversion examples.

### Changed

- GitHub Actions builds and publishes only when a GitHub release is published.
- Release tags must match the POM version and have release notes.

