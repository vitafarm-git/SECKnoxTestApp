# KnoxProbe (MVP)

KnoxProbe is an internal Android diagnostic app for comparing observable behavior between:
- a Samsung device's normal environment, and
- Samsung Secure Folder / Knox containerized environment.

It is **diagnostic-only** and uses **official Android SDK APIs**.

## What this app does
- Captures a diagnostic snapshot with:
  - Network state and visible transports (including VPN transport visibility)
  - Link properties (interface, MTU, DNS, routes, proxy when available)
  - External HTTPS probe results (endpoint/body snippet/latency/timestamp)
  - Device time, timezone, UTC offset, elapsed realtime
  - Optional location snapshot (after runtime permission)
  - Visibility checks for a small predefined package list
  - App-private / app-specific external storage marker checks
- Exports snapshot JSON through Storage Access Framework (user-selected destination).

## What this app does **not** do
- No VPN concealment or bypass behavior
- No anti-detection / evasion logic
- No hidden APIs / reflection hacks / non-SDK APIs
- No root tricks
- No analytics SDKs, trackers, ads, or backend integration

## Permissions and rationale
- `INTERNET`: external probe requests
- `ACCESS_NETWORK_STATE`: read connectivity/network capabilities/link properties
- `ACCESS_COARSE_LOCATION`: optional location snapshot (coarse)
- `ACCESS_FINE_LOCATION`: optional higher-precision location if granted

No broad file access or `QUERY_ALL_PACKAGES` is used.

## Package visibility approach
A small predefined list is probed. Manifest `<queries>` only includes those package names.
Results are still limited by Android package visibility rules.

## Build (Android Studio stable)
1. Open the repository in Android Studio.
2. Let Gradle sync.
3. Build and run the `app` module.

## Install on Samsung test device
1. Enable developer options + USB debugging.
2. Connect device and install from Android Studio (or sideload APK).
3. Run once in normal environment.
4. Run again inside Secure Folder (if app is available there).
5. Export snapshots and compare.

## Suggested test matrix
- Baseline network, no VPN, normal environment
- VPN enabled, normal environment
- Secure Folder environment, no VPN
- Secure Folder environment, VPN enabled (if policy allows)
- Compare exported snapshots with focus on strong vs circumstantial signals

## Notes on interpretation
Some artifacts are direct app-visible signals (strong), while others are indirect and circumstantial.
Do not overinterpret single weak signals without corroboration.
