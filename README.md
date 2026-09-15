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
