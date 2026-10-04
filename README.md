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

## Google login setup

Google login is implemented but disabled by default so the app works before credentials are configured.

1. In https://console.cloud.google.com/ create/select a project.
2. Configure Google Auth Platform branding/consent. Choose an audience; add your Google account as a test user if the app is in testing mode.
3. Create an OAuth client of type **Web application**.
4. Add authorized JavaScript origin: `http://localhost:8080`.
5. Add authorized redirect URI, **exactly**: `http://localhost:8080/login/oauth2/code/google`.
6. In Command Prompt, set your actual values, then start the Google profile:

```bat
set "GOOGLE_CLIENT_ID=YOUR_CLIENT_ID.apps.googleusercontent.com"
set "GOOGLE_CLIENT_SECRET=YOUR_CLIENT_SECRET"
run-google.bat
```

Linux/macOS:
```bash
export GOOGLE_CLIENT_ID='YOUR_CLIENT_ID.apps.googleusercontent.com'
export GOOGLE_CLIENT_SECRET='YOUR_CLIENT_SECRET'
bash run.sh --spring.profiles.active=google
```

The login dialog will now show **Continue with Google**. The frontend redirects through `/oauth2/authorization/google`; the server handles the callback, token verification and session creation. Client secrets never go in browser JavaScript.
Google's verified email links to an existing local account with that email. Subsequent Google logins also check the stored Google subject. A new Google account receives `CUSTOMER`, never automatic admin privileges.
If you use another port, register its exact origin and redirect URI. For MySQL plus Google use `--spring.profiles.active=mysql,google` with both sets of environment variables.
Google console labels may change; see official setup documentation: https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html .

## MySQL setup (optional)

The default H2 file database needs no separate installation. To switch:

1. Install/start MySQL 8+, or run `docker compose up -d db`.
2. Run the statements in `database/mysql-setup.sql` as a MySQL administrator, replacing the example password first.
3. Configure the connection in Command Prompt:

```bat
set "DB_URL=jdbc:mysql://localhost:3306/foodexpress"
set "DB_USERNAME=foodexpress_app"
set "DB_PASSWORD=YOUR_DATABASE_PASSWORD"
run-mysql.bat
```

Linux/macOS: set the same variables with `export`, then `bash run.sh --spring.profiles.active=mysql`.
Hibernate creates the tables. MySQL and H2 have separate data; switching profiles does not migrate old orders.
For a local classroom/demo project this uses Hibernate `ddl-auto=update`. Use managed migrations, backups, HTTPS and deployment-specific credentials before public deployment.

## Build and edit the source

Requirements: JDK 17+, Maven 3.6.3+, internet access on the first dependency download.
Open the root `pom.xml` in IntelliJ IDEA, Eclipse/STS or VS Code with Java support.

```bash
mvn clean verify
mvn spring-boot:run
```

Windows: `build.bat` runs the tests, builds and refreshes `dist/food-delivery.jar`.
Linux/macOS: `bash build.sh` does the same.
There is no Node/npm build and no external image/font CDN. All frontend assets are in `src/main/resources/static/`.

## Docker (optional)

1. Copy `.env.example` to `.env` and choose your own passwords.
2. `docker compose up --build -d`
3. Open http://localhost:8080 .
4. `docker compose down` stops services and preserves the database volume. Deleting the volume deletes data.

The full compose deployment uses MySQL. The provided Dockerfile builds/tests the source and runs as a non-root user.
Google can be enabled by setting `SPRING_PROFILES_ACTIVE=mysql,google`, `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` in `.env`. Restart/recreate the app after edits.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| PORT | 8080 | HTTP port |
| DEMO_ENABLED | true | Seed demo customer/restaurants when empty |
| ADMIN_EMAIL | admin@foodexpress.com | Initial admin email |
| ADMIN_PASSWORD | Admin@12345 | Initial admin password |
| H2_PASSWORD | empty | Local H2 database password |
| DB_URL / DB_USERNAME / DB_PASSWORD | See MySQL profile | MySQL credentials |
| GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET | none | Google profile credentials |
| SPRING_PROFILES_ACTIVE | none | `mysql`, `google` or `mysql,google` |

Admin initialization is create-if-missing: changing environment variables does not reset an existing account's password. Set your admin values **before the first startup** of a new database. Disable demo seeding and replace default admin credentials for any deployment beyond a local demo. Session timeout is 30 minutes.

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
