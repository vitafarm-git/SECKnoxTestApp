# AGENTS.md

## Project
Android diagnostic app for comparing behavior between normal Samsung phone environment and Secure Folder / Knox environment.

## Goal
Build a safe, internal-use Android app that measures:
- network state
- VPN-related signals
- routing and DNS
- device time and timezone
- optional location snapshot
- package visibility for a small predefined list
- app-specific storage accessibility
- exportable diagnostic reports

## Non-goals
Do not implement:
- VPN concealment or anti-detection features
- root-only tricks
- hidden APIs / reflection hacks / non-SDK APIs
- accessibility abuse
- harvesting unrelated personal data
- broad installed-app surveillance unless explicitly required

## Tech constraints
- Kotlin
- Jetpack Compose
- Material 3
- Android Studio stable
- Gradle Kotlin DSL
- No Hilt in MVP unless clearly needed
- Keep architecture simple and readable
- Prefer official Android APIs
- Physical Samsung device is the main target, not emulator

## Product constraints
- Internal / sideloaded utility app
- Must work without backend
- No secrets in repo
- No analytics SDKs
- No crash reporting SDKs in MVP

## Output requirements
- Clean project structure
- README with setup and testing steps
- Clear inline comments only where needed
- JSON export for diagnostic snapshot
- User-facing explanation text for ambiguous signals
- Graceful handling of missing permissions

## Code style
- Small focused classes
- Avoid overengineering
- Prefer immutable UI state
- Prefer one responsibility per file
- Use coroutines for async work
- Do not add unnecessary dependencies

## Review checklist
- Project builds
- No forbidden APIs
- No dangerous permissions without justification
- No feature that helps evade network/VPN detection
- Document every permission in README
