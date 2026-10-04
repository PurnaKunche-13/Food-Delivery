# REST API

Base URL: `http://localhost:8080/api`. JSON except form login. Session cookie is HTTP-only. Passwords and Google subjects are never serialized.

## Authentication and CSRF

GET `/auth/csrf` with a cookie jar; use returned `headerName` and `token` in every POST/PUT/PATCH/DELETE. GET requests do not require a CSRF header. Fetch a new token after login/logout because Spring Security rotates it. Browser frontend does this automatically.

`POST /auth/login`: `application/x-www-form-urlencoded` fields `email`, `password`. JSON success or 401. Google login starts with a browser redirect to `/oauth2/authorization/google`, outside the API prefix.

| Method | Route | Access / purpose |
| --- | --- | --- |
| GET | /auth/csrf | Public, session CSRF token |
| GET | /auth/providers | Public, Google configuration status |
| GET | /auth/me | Public, authenticated flag and own user profile |
| POST | /auth/register | Public + CSRF, customer registration |
| POST | /auth/login | Public + CSRF, form login |
| POST | /auth/logout | CSRF, session invalidation |
| GET | /restaurants | Public, active restaurants |
| GET | /restaurants/{id}/menu | Public, restaurant and available menu items |
| GET | /cart | Signed-in user's cart |
| PUT | /cart | Signed-in user, replace entire cart |
| GET | /orders | Signed-in user's order history |
| POST | /orders | Signed-in user, checkout |
| POST | /orders/{id}/cancel | Owner, only PLACED |
| GET | /admin/stats | Admin, orders/customer count and delivered revenue |
| GET | /admin/orders | Admin, all orders |
| PATCH | /admin/orders/{id}/status | Admin, permitted status transition |
| GET/POST | /admin/restaurants | Admin, list/create |
| PUT | /admin/restaurants/{id} | Admin, edit/open/close |
| GET/POST | /admin/menu | Admin, list/create |
| PUT | /admin/menu/{id} | Admin, edit/availability |
| GET/POST | /admin/categories | Admin, list/create |
| PUT/DELETE | /admin/categories/{id} | Admin, rename/delete unused |
| GET | /admin/users | Admin, users without secrets |
| PATCH | /admin/users/{id}/enabled | Admin, enable/disable customers |

## Request examples

Register:
```json
{"name":"Purna","email":"purna@example.com","password":"MyPassword123!"}
```
Replace cart (empty array clears it):
```json
{"items":[{"menuItemId":1,"quantity":2}]}
```
Place order:
```json
{"address":"12 Main Road, Kakinada, Andhra Pradesh 533001","phone":"9876543210","notes":"Ring the bell","items":[{"menuItemId":1,"quantity":2}]}
```
The order endpoint validates the supplied selection and computes all prices. Browser-supplied totals are not used. Cart clearing and order persistence occur in the same transaction. A customer can also place an order directly through this API without a previously saved cart.

Change order status: `{"status":"CONFIRMED"}`.
Create category: `{"name":"Desserts"}`.
Create restaurant: `{"name":"Sweet Corner","cuisine":"Desserts","address":"Main Road","emoji":"🍰","active":true}`.
Create menu item:
```json
{"restaurantId":1,"name":"Paneer Biryani","description":"Rice with paneer and spices","price":199.00,"categoryId":1,"emoji":"🍛","vegetarian":true,"available":true}
```
Disable customer: `{"enabled":false}`. Administrators cannot be disabled through this endpoint.

## Errors and processing

Error payload: `{"message":"Human-readable reason"}`. 400 validation/business rule; 401 unauthenticated/bad password; 403 role/CSRF/disabled account; 404 missing or another customer's order; 409 duplicate/conflicting updates. Quantity 1–20 per item, maximum 30 distinct cart lines, one restaurant per order/cart. Price uses BigDecimal and database DECIMAL columns. Optimistic locking prevents concurrent order updates from silently overwriting each other.

Checkout disables repeat submission while the request is in flight. There is no cross-request idempotency key; after a network timeout, check history before retrying. Order refresh is manual, without WebSocket push. Disabled/unavailable items retained in an older cart are rejected at checkout; remove them or choose another item.
