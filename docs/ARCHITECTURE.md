# KnoxProbe MVP Architecture

## Module layout
Single app module (`:app`) for MVP simplicity.

## Package layout
- `ui`: Compose screens, navigation, ViewModel, theme
- `domain`: snapshot models
- `diagnostics`: feature-specific probes (network/time-location/packages/storage)
- `export`: JSON serialization + SAF write helper
- `util`: session ID + small utility helpers
- `data`: reserved for future data sources (kept minimal in MVP)

## Flow
1. `KnoxProbeViewModel` orchestrates diagnostics.
2. User taps **Run full snapshot**.
3. Diagnostics run via official APIs.
4. Snapshot object is assembled in memory.
5. User taps **Export snapshot** and picks destination via SAF.

## Key design principles
- Readable and explicit code over abstraction-heavy patterns.
- No hidden APIs or evasive logic.
- Clear UI copy for ambiguous/circumstantial signals.
- Minimal permission set.
