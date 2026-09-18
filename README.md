# Nass — Second-Hand Clothing Marketplace

![Android CI](https://github.com/YOUR-USERNAME/YOUR-REPO/actions/workflows/build.yml/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09.00-4285F4?logo=jetpackcompose)
![License](https://img.shields.io/badge/license-academic-lightgrey)

An Android marketplace for buying and selling second-hand clothing. Built for
the OPSC6312 Portfolio of Evidence (Part 2 — Prototype) at The Independent
Institute of Education.

---

## Table of Contents

- [Overview](#overview)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Backend API](#backend-api)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Continuous Integration](#continuous-integration)
- [Building a Release](#building-a-release)
- [Project Structure](#project-structure)
- [Demo Video](#demo-video)
- [AI Tool Usage](#ai-tool-usage)
- [Author](#author)

---

## Overview

Nass is a three-role marketplace app:

| Role | What they can do |
|------|------------------|
| **Buyer** | Browse unsold items, add to cart, checkout, view purchase history |
| **Seller** | Track listing stats, post/edit/delete items, view personal listings |
| **Admin** | Verify pending users, change roles, reset passwords, delete users |

Every role also has a shared **Settings** screen for updating display name and
changing password.

---

## Screenshots

| Login | Seller — Stats | Seller — Listings |
|-------|----------------|-------------------|
| ![Login](docs/screenshots/01_login.jpg) | ![Stats](docs/screenshots/02_seller_stats.jpg) | ![Listings](docs/screenshots/03_seller_listings.jpg) |

| Buyer — Browse | Buyer — Cart | Buyer — History |
|----------------|--------------|-----------------|
| ![Browse](docs/screenshots/04_buyer_browse.jpg) | ![Cart](docs/screenshots/05_buyer_cart.jpg) | ![History](docs/screenshots/06_buyer_history.jpg) |

| Admin — Users | Settings | App Icon |
|---------------|----------|----------|
| ![Admin](docs/screenshots/07_admin_users.jpg) | ![Settings](docs/screenshots/08_settings.jpg) | ![Icon](docs/screenshots/09_icon.jpg) |

---

## Tech Stack

### Android Client
- **Language:** Kotlin `2.2.10`
- **UI:** Jetpack Compose (Material 3, BOM `2024.09.00`)
- **Architecture:** MVVM with `StateFlow`
- **Networking:** Retrofit `2.11.0` + OkHttp `4.12.0` + Gson
- **Concurrency:** Kotlin Coroutines `1.9.0`
- **Local storage:** DataStore Preferences
- **Image loading:** Coil `2.7.0`
- **Min SDK:** 26 · **Target SDK:** 35

### Backend
- **Runtime:** Node.js + Express
- **Database:** MySQL (XAMPP in development, tunneled via ngrok)
- **Auth:** JWT (`jsonwebtoken`) with bcrypt password hashing

---

## Architecture

```text
┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│ Compose UI   │ → │ ViewModel    │ → │ Repository   │ → │ Retrofit     │
│ (Screens)     │   │ (StateFlow)  │   │ (Resource)   │   │ + OkHttp     │
└──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
       │                    │                    │                    │
       └────────────────────┴────────────────────┴────────────────────┘
                                                                    │
                                                                    ▼
                                                          ┌──────────────┐
                                                          │ Node/Express │
                                                          │ REST API     │
                                                          └──────┬───────┘
                                                                 │
                                                                 ▼
                                                          ┌──────────────┐
                                                          │ MySQL        │
                                                          │ (pasthings)  │
                                                          └──────────────┘
```

**Key patterns:**
- `Resource<T>` sealed class (`Idle / Loading / Success / Error`) wraps every
  async operation so the UI reacts to one state object.
- `SessionManager` interface abstracts token storage — the real implementation
  uses DataStore, and unit tests inject an in-memory fake.
- `AuthInterceptor` reads the JWT from DataStore and attaches
  `Authorization: Bearer <token>` to every non-public request.
- Role-based routing sends each user to the correct dashboard after login.

---

## Features

### Authentication
- Register as buyer or seller
- Login with JWT (7-day expiry)
- Password hashing with bcrypt on the backend
- Session persistence via DataStore — reopening the app skips login
- Admin verification gate for new accounts

### Buyer Flow
- 2-column grid of all available (unsold) items
- Product detail sheet with image, description, seller username
- Cart with badge count, per-item remove, live total
- Checkout wrapped in a MySQL transaction — atomic and rollback-safe
- Purchase history grouped by transaction, expandable per order

### Seller Flow
- Live stats: total added, total sold, available
- My Listings with All / Available / Sold filters
- Add product with live image preview (Coil)
- Edit / delete with ownership + not-sold guards on backend
- Sold items are protected from edits in the UI

### Admin Flow
- User list with All / Pending / Active / Suspended filters
- Verify pending accounts in one tap
- Change user role (buyer / seller / admin)
- Reset user passwords
- Delete user (with self-delete protection)

### Shared
- Settings screen for all roles: display name + password change
- Proper logout that clears the DataStore session

---

## Backend API

Base URL: `https://upwind-defrost-jab.ngrok-free.dev/` *(dev — swapped for a
production host before final submission)*

### Auth — `/api/v1/auth`
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/register` | Public |
| POST | `/login` | Public |
| GET | `/me` | Any authenticated |
| PUT | `/me` | Any authenticated |
| PUT | `/me/password` | Any authenticated |

### Products — `/api/v1/products`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/` | Public (all) |
| GET | `/available` | Public (unsold only) |
| GET | `/:id` | Public |
| POST | `/` | Seller |
| PUT | `/:id` | Seller (own, unsold) |
| DELETE | `/:id` | Seller (own, unsold) |

### Seller — `/api/v1/seller`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/stats` | Seller |
| GET | `/products` | Seller (own) |

### Cart — `/api/v1/cart`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/` | Buyer |
| POST | `/` | Buyer |
| DELETE | `/:id` | Buyer |

### Checkout & History
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/v1/checkout` | Buyer |
| GET | `/api/v1/transactions/history` | Buyer |

### Admin — `/api/v1/admin`
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/users` | Admin |
| PUT | `/users/:id/verify` | Admin |
| PUT | `/users/:id` | Admin |
| DELETE | `/users/:id` | Admin |
| PUT | `/users/:id/reset-password` | Admin |

---

## Getting Started

### Prerequisites
- Android Studio (Ladybug or later)
- JDK 17
- Node.js 18+
- XAMPP (MySQL)

### Backend setup

```bash
git clone <repo> && cd server
npm install

inside env file

PORT=3000
JWT_SECRET=your-secret-here
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=
DB_NAME=pasthings_db
```

## Project Structure

```text
app/src/main/java/com/example/nass/
├── data/
│   ├── local/            DataStore + SessionManager abstraction
│   ├── model/            DTOs matching the backend JSON exactly
│   ├── remote/           Retrofit client, ApiService, AuthInterceptor
│   └── repository/       Auth, Seller, Product, Cart, Admin, Profile
├── ui/
│   ├── admin/            Admin dashboard + Users tab + UserDetailSheet
│   ├── auth/             Login + Register
│   ├── buyer/            Buyer dashboard, ProductDetailSheet, tabs/
│   ├── common/           SettingsScreen (shared across roles)
│   ├── seller/           Seller dashboard, tabs, EditProductSheet
│   ├── splash/           Splash + session routing
│   └── theme/            Material 3 theme
├── navigation/           NavGraph, Routes
├── util/                 Constants, Formatters, Logger, Resource
└── MainActivity.kt
```

## Demo Video

```text
//url for video
```

## AI Tool Usage

### AI Tool Usage Disclosure

Per the OPSC6312 POE brief, this document discloses how AI tools were used
during the development of the Nass Android application.

#### Tools Used

- **Anthropic Claude** — architectural guidance, code review, debugging, and
  boilerplate generation

#### What AI Was Used For

AI was used as a **senior developer on call**, not as a code generator. All
architectural decisions, testing strategy, and feature implementations were
reviewed and understood before being committed.

##### 1. Architectural scaffolding

AI suggested the repository pattern with a `Resource<T>` sealed wrapper for
async state, and the `SessionManager` interface abstraction that allowed me to
test repositories without an Android `Context`. These choices are documented in
the README under [Architecture](README.md#architecture).

##### 2. Boilerplate and API integration code

- Retrofit `ApiService` interfaces
- Data class definitions matching backend JSON (`@SerializedName` annotations)
- Compose UI components (screens, sheets, dialogs)

All generated code was **read line-by-line, modified to match the project's
naming and design language, and tested end-to-end** against a real MySQL
database on a physical phone.

##### 3. Debugging

AI assisted with:

- Diagnosing a Compose compiler plugin failure (`ComposableFunction0` errors)
  caused by a leftover `tokenManager` parameter in `AuthRepository`
- Resolving a keystore password mismatch during release signing
- Identifying a Windows file-lock issue during `./gradlew clean`

The root cause analysis in each case was explained and understood before
applying the fix.

### 4. Backend controller logic

AI helped design the checkout flow to use a MySQL `START TRANSACTION ...
COMMIT` block so that marking products as sold, creating transaction rows, and
clearing the cart all happen atomically. I tested the rollback behaviour
manually by killing ngrok mid-checkout.

#### What AI Was NOT Used For

- The database schema was written by me based on the POE brief
- The authentication middleware (`authMiddleware.js`, `adminMiddleware.js`)
  was written by me
- All Postman tests were written and executed by me
- The final selection of which features to prioritise was my decision

#### Verification

Every AI-generated code snippet was:

1. Read and understood before use
2. Adapted to the project's naming and design language
3. Tested against the live backend and physical device
4. Committed with descriptive messages in GitHub

No code was committed that I could not explain line-by-line in a code review.

---

## Author

Tshiamo Mbatha — ST10070515
OPSC6312 Portfolio of Evidence, 2026
The Independent Institute of Education

---

**Signed:** Tshiamo Mbatha (ST10070515)
**Date:** 16 September 2026
