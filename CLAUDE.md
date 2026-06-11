# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build Android debug APK
./gradlew :androidApp:assembleDebug

# Run all Android host tests
./gradlew :sharedLogic:testAndroidHostTest

# Run a single test class
./gradlew :sharedLogic:testAndroidHostTest --tests "com.eightbitstack.toolbox.ToolboxRepositoryTest"

# Run iOS simulator tests (requires macOS + Xcode)
./gradlew :sharedLogic:iosSimulatorArm64Test
```

## Architecture

This is a Kotlin Multiplatform (KMP) project with two modules:

- **`androidApp`** — Android-only UI. All screens are Jetpack Compose. No ViewModel layer; state flows directly from `ToolboxRepository` via a listener callback (`onStateChanged`).
- **`sharedLogic`** — KMP library shared between Android and iOS. Contains models, business logic, storage abstraction, and date utilities.

### State management

`ToolboxRepository` (`sharedLogic`) owns the single source of truth (`ToolboxState`). It follows a reducer-like pattern: every mutation goes through an action method that replaces `state` via `copy()`, which triggers `saveState()` and notifies all registered `Listener` instances.

`App.kt` registers a listener via `DisposableEffect` and drives the whole UI from a single `appState` variable. Screens receive slices of state as parameters and report actions back via lambdas — screens have no direct repository access.

### Persistence

State is serialized to a custom pipe-delimited text format (not JSON/protobuf) and stored under a single key (`toolbox-state-v1`). The `StorageProvider` interface has `expect/actual` implementations:
- Android: `SharedPreferences` via `KeyValueStorage` in `sharedLogic/src/androidMain`
- iOS: implementation lives in `sharedLogic/src/iosMain`

If the stored string is missing or fails to parse, `getSeedState()` provides realistic seed data.

### Theming

`Theme.kt` (androidApp) defines a bespoke design system — **not** Material3 theming:
- `ToolboxTheme` — singleton object with `@Composable` color tokens that switch on `LocalDarkMode`
- `BrandPalette` / `BrandPalettes` — four accent themes (indigo, forest, cyber, sunset) selectable at runtime
- `LocalBrandPalette`, `LocalShowFlourishes`, `LocalDarkMode` — `CompositionLocal` values set in `App.kt` and consumed anywhere in the tree

Use `ToolboxTheme.*` tokens (e.g. `ToolboxTheme.surface`, `ToolboxTheme.ink`) rather than hardcoded colors so dark mode and accent changes propagate automatically.

### Navigation

There is no navigation library. `App.kt` holds an `activeTab` string and renders the active screen with a `when` expression. The bottom tab bar is also rendered inside `App.kt`.
