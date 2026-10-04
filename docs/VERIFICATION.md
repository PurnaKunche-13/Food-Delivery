# Verification performed

- Java 17 compilation succeeded; Spring Boot executable JAR built with Maven.
- 10 Spring Boot / MockMvc integration tests passed: 0 failures, 0 errors, 0 skipped.
- Tests cover public browsing, login/registration, customer role enforcement, password hiding, price recalculation, delivery threshold, order snapshots, cart clearing, CSRF, admin authorization, invalid/mixed/unavailable items, order ownership, status transitions, revenue, category rename/deletion restrictions, disabled-user login and verified Google account provisioning.
- `node --check` passed for the frontend JavaScript.
- The packaged JAR started successfully with H2 and served real HTTP requests.
- A jsdom interaction check against the running JAR passed restaurant browsing, guest cart, customer login, saved cart, checkout (₹249 + ₹35 = ₹284), order history, logout, admin login, all delivery transitions, collected revenue, category creation and user-control rendering.
- Google profile startup and OAuth authorization redirect were checked using dummy credentials. Live Google consent, code exchange and token verification against Google's servers require your real OAuth credentials and were not exercised.

Limitations of these checks: frontend interactions used a DOM simulator, not a visual browser layout check. MySQL/Docker configuration and Windows batch scripts were supplied but not executed in this Linux environment. The default H2 executable was tested. No real payment or courier service is integrated.

Run the included repeatable Java tests with `mvn clean verify`. The DOM smoke check was a delivery-time check and is not a required runtime dependency.
