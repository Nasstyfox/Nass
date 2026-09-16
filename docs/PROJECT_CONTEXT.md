# PROJECT_CONTEXT.md

## 1. Executive Summary & Tech Stack

**Nass** is a mobile/web marketplace application built with an
Android client and a custom Node.js/Express REST API backed by a MySQL
database running on XAMPP.

### App / Frontend Client

-   **Language & Version:** Kotlin `v2.2.10`
-   **IDE:** Android Studio / IntelliJ
-   **Build System:** Gradle
-   **UI Toolkit:** Jetpack Compose (BOM `2024.09.00`)
-   **Design System:** Material Design 3
-   **Architecture Pattern:** MVVM (Model-View-ViewModel)
-   **Networking & Concurrency:** Retrofit with Kotlin Coroutines
    (`suspend` functions)
-   **Serialization:** Gson / Kotlinx Serialization

### Backend Service

-   **Runtime & Environment:** Node.js + Express
-   **Database:** MySQL via XAMPP (`pasthings_db`)
-   **Authentication:** JSON Web Tokens (JWT) with password encryption

------------------------------------------------------------------------

## 2. Complete Database Schema

``` sql
CREATE DATABASE IF NOT EXISTS pasthings_db;
USE pasthings_db;

-- 1. Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'buyer', 'seller') DEFAULT 'buyer',
    status ENUM('pending', 'active', 'suspended') DEFAULT 'pending',
    display_name VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Products Table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(50) DEFAULT 'Clothing',
    image_url TEXT NULL,
    is_sold TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Shopping Cart Table
CREATE TABLE cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 4. Transactions Table
CREATE TABLE transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Transaction Items Table
CREATE TABLE transaction_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    product_id INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);
```

------------------------------------------------------------------------

## 3. Comprehensive API Specification

### Authentication & Users --- `/api/v1/auth`

-   **`POST /api/v1/auth/register`** --- Public
    -   **Body:**
        `{ "username", "email", "password", "confirmPassword", "role" }`
    -   Creates user with `pending` status.
-   **`POST /api/v1/auth/login`** --- Public
    -   **Body:** `{ "username", "password" }`
    -   Returns JWT and user role (`admin`, `buyer`, `seller`).
    -   Redirects the user to their specific dashboard based on role.
-   **`GET /api/v1/auth/me`** --- Authenticated
-   **`PUT /api/v1/auth/me/password`** --- Authenticated

### Admin Operations --- `/api/v1/admin`

-   **`GET /api/v1/admin/users`** --- Admin
    -   Fetches a list of all users.
-   **`PUT /api/v1/admin/users/:id/verify`** --- Admin
    -   Updates user status to `active`.
-   **`PUT /api/v1/admin/users/:id`** --- Admin
    -   Updates user details or changes the role (`admin`, `buyer`,
        `seller`).
-   **`DELETE /api/v1/admin/users/:id`** --- Admin
    -   Removes a user.
-   **`PUT /api/v1/admin/users/:id/reset-password`** --- Admin
    -   Performs an admin-side user password reset.

### Buyer Flow --- `/api/v1/buyer`

-   **`GET /api/v1/products/available`** --- Buyer
    -   Retrieves clothing items where `is_sold = 0`, excluding sold
        items.
-   **`GET /api/v1/cart`** --- Buyer
    -   Gets items in the buyer's cart.
-   **`POST /api/v1/cart`** --- Buyer
    -   **Body:** `{ "product_id", "quantity" }`
-   **`POST /api/v1/checkout`** --- Buyer
    -   Converts cart items into a completed transaction record, marks
        involved products as `is_sold = 1`, and clears the cart.
-   **`GET /api/v1/transactions/history`** --- Buyer
    -   Retrieves past purchase transaction history.

### Seller Flow --- `/api/v1/seller`

-   **`GET /api/v1/seller/stats`** --- Seller
    -   Returns counts of added items and sold items.
-   **`POST /api/v1/products`** --- Seller
    -   Creates a new clothing product listing.
-   **`PUT /api/v1/products/:id`** --- Seller
    -   Updates the item description and price.

------------------------------------------------------------------------

## 4. Feature Requirements & Dashboard Roles

### Launch & Authentication

-   Application launches to a **Login Screen** on the web/mobile
    interface.
-   A redirect option is provided for **Registration**.
-   Authentication redirects users directly to role-specific dashboards
    based on JWT details (`admin`, `buyer`, `seller`).

### Admin Dashboard

-   Verify registered users (`pending` → `active`).
-   Perform full CRUD actions on user accounts.
-   Trigger password resets for users.

### Buyer Dashboard

-   Browse available clothing items (`is_sold = 0` condition enforced).
-   Add items to the cart and complete checkout transactions.
-   View past purchase transaction history.

### Seller Dashboard

-   Analytics view showing the count of added items versus total sold
    items.
-   Post new clothing items for sale.
-   Edit the description and price for posted listings.

------------------------------------------------------------------------

## 5. Development Roadmap & Execution Checklist

-   [ ] Sync the MySQL database schema via XAMPP with the new tables:
    -   `products`
    -   `cart_items`
    -   `transactions`
    -   `transaction_items`
-   [ ] Implement backend JWT authentication with role-authorization
    middleware (`admin`, `buyer`, `seller`).
-   [ ] Complete Admin CRUD and Password Reset API endpoints.
-   [ ] Implement Buyer endpoints:
    -   `GET /api/v1/products/available`
    -   `POST /api/v1/checkout`
    -   `GET /api/v1/transactions/history`
-   [ ] Implement Seller endpoints:
    -   `GET /api/v1/seller/stats`
    -   `PUT /api/v1/products/:id`
-   [ ] Construct Jetpack Compose / Web UI screens for:
    -   Login
    -   Registration
    -   Admin Dashboard
    -   Buyer Dashboard
    -   Seller Dashboard
