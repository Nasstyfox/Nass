# Release Notes

## v2.0 — Final PoE Release (Part 3)

**Released:** 22 October 2026

This is the final submission build for the OPSC6312 Portfolio of Evidence (Part 3).
It builds on the Part 2 prototype with the following major innovative features.

### Added Since Part 2

#### 1. Single Sign-On (SSO)
- Users can now register and log in using their Google accounts.
- Google ID tokens are verified on the Node.js backend, and users are automatically created in the MySQL database with a default 'buyer' role.
- Existing buyers can upgrade their role to 'seller' directly from the Settings screen.

#### 2. NoSQL Database (Cloud Firestore)
- Implemented a real-time "Wishlist/Favorites" feature.
- Buyers can tap the heart icon on any product to save it to Firebase Firestore (NoSQL).
- Favorites persist across app restarts and are synced immediately.

#### 3. Real-Time Push Notifications (Firebase Cloud Messaging)
- Sellers now receive an instant push notification on their physical device when a buyer purchases one of their listed items.
- FCM tokens are stored in the MySQL database during login.
- The Node.js backend triggers notifications via Firebase Admin SDK during the checkout transaction.

#### 4. Offline Mode with Sync (RoomDB)
- Implemented local caching using Android Room Database.
- If the device loses internet connection, the app seamlessly falls back to displaying cached products and cart items.
- A visual "Offline Mode" banner appears on the Browse screen to notify the user.
- Once the connection is restored, the app automatically syncs and overwrites the local cache with the latest API data.

#### 5. Multi-Language Support
- The entire UI is now fully localizable.
- Added support for English (Default), Afrikaans (af), and isiZulu (zu).
- Users can switch languages dynamically from the Settings screen without restarting their phone.

### Security & Infrastructure
- Migrated backend to use `firebase-admin` for secure server-to-server FCM communication.
- Added RoomDB migrations for local data integrity.
- Extended the MySQL `users` table to store `fcm_token`.

### Known Limitations
- Offline mode currently caches the Browse and Cart views; checkout requires an active internet connection.