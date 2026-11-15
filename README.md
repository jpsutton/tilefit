# Tilefit

A single-player block-fitting puzzle for Android. Drag three pieces at a time onto a 9x9 board; fill a row, a
column or a 3x3 box to clear it. Clear several at once and chain clears for a streak bonus. The game ends when no
piece in your hand fits.

The rules and scoring are in [docs/RULES.md](docs/RULES.md). Tilefit is an independent game, not affiliated with or
endorsed by any publisher.

An unfinished game is saved and can be resumed; the best score is kept. No ads, accounts or network access.

## Install

Tilefit runs on Android 8.0 or newer. The easiest way to install and update it is Tilefit's F-Droid repository,
which works in F-Droid, Droid-ify and Neo Store:

1. On the phone, open [jpsutton.github.io/tilefit/fdroid](https://jpsutton.github.io/tilefit/fdroid/) and tap
   **Add to F-Droid**.
2. Install **Tilefit** from that repository. New releases then show up as regular app updates.

To add the repository by hand, use the address `https://jpsutton.github.io/tilefit/fdroid/repo` and check that its
fingerprint is:

```
FEAAF9F0566D396B06FD05519D704D3FDA9FF9CF5E74370898BBC1D6A1D7D538
```

You can also download `tilefit-<version>.apk` from the
[Releases page](https://github.com/jpsutton/tilefit/releases/latest) and install it directly. Android asks you to
allow installs from your browser the first time.

## Build

Needs JDK 21 and Android SDK platform 37, or Docker:

```bash
./gradlew test lint assembleRelease
scripts/docker-gradle.sh test lint assembleRelease   # same, in a container; no SDK needed
```

## Layout

- `app/src/main/java/io/github/jpsutton/tilefit/game/`: the rules, with no Android dependencies (`Board`, `Piece`,
  `Game`, `GameState`, `GameCodec`). JVM unit tests are in `app/src/test`.
- `.../ui/`: Jetpack Compose screens and the `GameViewModel`; `.../data/Store.kt`: saved game and best score.
- `packaging/fdroid/`: F-Droid metadata and icon. `scripts/ci/`: release scripts.

## Branches and releases

Changes go feature branch -> pull request into `staging` (static checks only, no build) -> pull request
`staging` -> `release`. The `release` pull request runs the tests, lint and release build (`build.yml`). Merge it with
"Create a merge commit" after bumping `VERSION_NAME` in `gradle.properties`; `release.yml` then signs that build's
APK without rebuilding, tags `v<version>`, creates a GitHub release and updates the F-Droid repository on the
`gh-pages` branch.

### Release keys

`release.yml` needs these repository secrets (base64 PKCS12 keystores and their passwords):

- `TILEFIT_APK_KEYSTORE`, `TILEFIT_APK_KEYSTORE_PASS`: signs the APK. Android refuses an update signed by a
  different key, so this key must never change or be lost.
- `TILEFIT_FDROID_KEYSTORE`, `TILEFIT_FDROID_KEYSTORE_PASS`: signs the F-Droid index; its fingerprint is what
  clients pin. Alias `tilefit-fdroid`.

## License

MIT, see [LICENSE](LICENSE).
