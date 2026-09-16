# Release Notes

## v1.0 — Final PoE Release

**Released:** 16 September 2026

This is the final submission build for the OPSC6312 Portfolio of Evidence.
It builds on the Part 2 prototype with the following additions and improvements.

### Added Since Prototype (Part 2 → Final)

#### Buyer Flow (new)
- Browse grid of all available items with seller attribution
- Product detail sheet with Coil image loading
- Shopping cart with badge counter, per-item removal, and live total
- Checkout wrapped in an atomic MySQL transaction with double-sell protection
- Purchase history — grouped, expandable per-transaction view

#### Admin Flow (new)
- User list with All / Pending / Active / Suspended filter chips
- One-tap user verification
- Role management (buyer / seller / admin)
- Password reset for any user
- User deletion with self-delete protection

#### Shared Settings (new)
- Display name editing
- Change password with current-password verification
- Proper logout that clears the DataStore session

#### Infrastructure
- **Unit tests** — 15 tests across repositories, ViewModels, and formatters
- **GitHub Actions CI** — builds the debug APK and runs all unit tests on push
- **Signed release APK** — production-ready with adaptive app icon
- **Structured logging** via `Logger` wrapper (tagged `Nass.*` in Logcat)

### Changed
- All prices now formatted consistently via `Formatters.price()` (e.g. `R 349.99`)
- Session storage abstracted behind `SessionManager` interface for testability
- `AuthInterceptor` now reads from `SessionManager` instead of concrete
  `TokenManager`

### Fixed
- Logout no longer leaves a stale JWT in DataStore (previously required app
  reinstall to log out fully)
- Sold items can no longer be opened for edit — snackbar explains why
- `AuthRepository` duplicate constructor parameter removed

### Known Issues (deferred to post-PoE work)

The following POE Part 3 requirements are not yet implemented and will be added
in the next iteration:

- Single Sign-On (SSO) via Google / Facebook
- Offline mode with sync (Room / SQLite)
- Real-time push notifications (Firebase Cloud Messaging)
- Multi-language support (English + at least one additional SA language)
- In-app image upload (currently images are added via URL)

### Security
- Passwords hashed with bcrypt (10 rounds) on the backend
- JWT with 7-day expiry, verified on every protected request
- Role-based access enforced on both frontend (routing) and backend
  (middleware)
- Release keystore excluded from version control

### Technical
- **Package:** `com.example.nass`
- **Version:** `1.0` (versionCode `1`)
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)
- **APK size:** ~11.8 MB