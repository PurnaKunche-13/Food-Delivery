# FoodExpress — Food Delivery Management System

A complete full-stack Java project: Spring Boot REST backend, Hibernate/JPA persistence, Spring Security authentication and a responsive HTML/CSS/JavaScript frontend served by the same application.

## Start on Windows (easiest)

1. Install **JDK 17 or later**. Check `java -version` in Command Prompt.
2. Extract this entire ZIP. Open the `food-delivery` folder.
3. Double-click `run.bat`, or open Command Prompt in this folder and run `run.bat`.
4. Keep that terminal open. After you see **Started FoodDeliveryApplication**, open **http://localhost:8080**.
5. Stop the application with **Ctrl+C**.

A prebuilt executable is provided in `dist/food-delivery.jar`. Maven and an internet connection are not needed to run that JAR. Google sign-in needs internet access and your own credentials. Java remains required.

Linux/macOS: `bash run.sh`.
Direct command: `java -jar dist/food-delivery.jar`.
If 8080 is already occupied: `run.bat --server.port=8081` (then browse to http://localhost:8081).
Run one copy of the app per database directory.

## Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | admin@foodexpress.com | Admin@12345 |
| Customer | customer@foodexpress.com | Customer@123 |

You can also register a new customer. Public registration cannot create administrators.
The first startup seeds four restaurants, 18 menu items, categories and demo accounts. Data persists under `data/` between restarts; seed data is not duplicated.

## Included features

- Email/password registration and login, BCrypt hashing and session-based authentication.
- Google OAuth 2.0 / OpenID Connect login with verified email checks and customer provisioning.
- Restaurant and cuisine search, menu browsing and vegetarian filter.
- Guest cart in browser storage; authenticated carts saved per customer in the database.
- A cart contains items from one restaurant. Adding food from another restaurant asks to replace it.
- Delivery address, 10-digit phone number, notes and **cash-on-delivery** checkout.
- Server-side price/availability validation, immutable order line price/name snapshots.
- Delivery fee ₹35; free delivery from ₹499 subtotal. INR throughout.
- Order history, manual status refresh, progress display and cancellation while `PLACED`.
- Admin dashboard, restaurant editing/open-close controls, menu editing/availability controls.
- Category creation/renaming; deletion only when no menu item uses the category.
- Admin customer list, account enable/disable, and session access checks for disabled accounts.
- Order processing and collected revenue tracking after delivery.
- CSRF protection, input validation, role-based API access and ownership checks.

Order lifecycle:
`PLACED → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED`.
Customers can cancel only `PLACED`; admins can cancel `PLACED` or `CONFIRMED`.
Delivered cash-on-delivery orders are marked `COLLECTED`; this records an admin assertion, not a payment-provider confirmation.


## Structure

- `src/main/java/com/foodexpress/` — entities, repositories, services, controllers and security.
- `src/main/resources/static/` — complete responsive frontend.
- `src/main/resources/application*.properties` — H2, MySQL and Google configurations.
- `src/test/java/com/foodexpress/` — integration/security/business-rule tests.
- `dist/food-delivery.jar` — prebuilt executable.
- `docs/API.md` — API routes, authentication and examples.
- `docs/DATABASE.md` — schema and relationships.
- `docs/VERIFICATION.md` — performed checks and test scope.
- `database/mysql-setup.sql` — database and user setup.
- `run*.bat`, `run.sh`, `build.*` — launch/build scripts.

## Troubleshooting and boundaries

- `java is not recognized`: install JDK 17+ and reopen Command Prompt with Java on PATH.
- `Port 8080 was already in use`: stop your previous server using Ctrl+C or choose a new port.
- Google `redirect_uri_mismatch`: compare the configured URI exactly, including port and path.
- Google access denied: check consent-screen testing users and OAuth credentials; disabled customer accounts cannot log in.
- `Please log in` / CSRF error: refresh the page and sign in again.
- Build dependency errors: check internet/proxy access to Maven Central.
- H2 file lock: another app instance may already be using the same `data/` directory.

This is a runnable local/demo food ordering system. Cash on delivery is the only payment method. It does not include a real courier service, live GPS tracking, Google Maps, restaurant vendor accounts, OTP/SMS, email delivery, refunds or an online payment gateway. Status changes are performed by the admin. Google credentials and a live Google consent flow cannot be supplied or tested on your behalf.
"# Food-Delivery" 
