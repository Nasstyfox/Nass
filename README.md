# Nass — Second-Hand Clothing Marketplace

![Android CI](https://github.com/YOUR-USERNAME/YOUR-REPO/actions/workflows/build.yml/badge.svg)

An Android marketplace for buying and selling second-hand clothing, built with
Jetpack Compose, Retrofit, and a custom Node.js/Express + MySQL backend.

## Tech Stack

- **Client:** Kotlin, Jetpack Compose (Material 3), MVVM, Retrofit, Coroutines
- **Backend:** Node.js + Express, MySQL (hosted via ngrok in dev)
- **CI:** GitHub Actions (builds + runs unit tests on every push)

## Features (Seller)

- Register / log in with JWT authentication (bcrypt hashed passwords)
- View live stats: total added, total sold, available for sale
- Browse personal listings with image previews and sold/available filters
- Add, edit, and delete product listings
- Session persistence via DataStore
- Role-based routing (seller / buyer / admin dashboards)

## Architecture

- **AuthInterceptor** attaches `Bearer <jwt>` to every protected request
- **SessionManager** abstracts token storage (real impl: DataStore; tests use in-memory fake)
- **Resource<T>** wraps async results as Idle / Loading / Success / Error

## Testing

Unit tests run via `./gradlew testDebugUnitTest`:

- `FormattersTest` — price/plural formatting
- `AuthRepositoryTest` — login/register success + error paths (MockWebServer)
- `SellerRepositoryTest` — stats and listings parsing
- `SellerViewModelTest` — ViewModel state machine with mocked HTTP

## Continuous Integration

Every push to `main` / `develop` triggers GitHub Actions to:
1. Check out the code
2. Set up JDK 17
3. Run all unit tests
4. Build a debug APK
5. Upload test reports + APK as artifacts

See [`.github/workflows/build.yml`](.github/workflows/build.yml).

## Local Development

```bash
# 1. Start the backend
cd server && npm start

# 2. Expose it publicly
ngrok http 3000

# 3. Update app/src/main/java/com/example/nass/util/Constants.kt with the ngrok URL

# 4. Build + install
./gradlew :app:installDebug
