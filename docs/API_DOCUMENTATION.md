# RESTful API Documentation

## Patel Samaj Community Management System (Backend)

The **Patel Samaj Backend REST API** is built using Spring Boot 4, Spring Security 6 with JWT (JSON Web Tokens), Spring Data JPA, and PostgreSQL. It delivers complete automation for user authentication, role-based administration, community event bookings with item snapshot pricing, automated PDF receipt rendering via headless Playwright, donor management, and financial expense tracking.

---

## 1. API Overview & Design Conventions

### 1.1. Base URL
- **Local Development:** `http://localhost:8080`
- **Context Path:** `/api`
- **Default Port:** `8080` (Configurable via `PORT` environment variable)

### 1.2. Authentication & Security
- **Type:** Stateless JSON Web Token (JWT)
- **Header Format:**
  ```http
  Authorization: Bearer <your_jwt_token_here>
  ```
- **Access Control Matrix:**
  - **Public Endpoints (`permitAll`):** `/api/auth/**`
  - **Admin Restricted (`ROLE_ADMIN`):**
    - `POST /api/donors`, `POST /api/expenses`, `POST /api/items/**`, `POST /api/events/close/**`, `POST /api/user/**`
    - `PUT /api/user/**`, `PUT /api/events/close/{eventId}`
    - `DELETE /api/items/{itemId}`, `DELETE /api/expenses/{expenseId}`, `DELETE /api/donors/{donorId}`
  - **Authenticated Users (`authenticated`):** All remaining endpoints (e.g., event bookings, reading catalog, profile retrieval).

### 1.3. Standard HTTP Status Codes
| Status Code | Meaning | Typical Usage |
| :--- | :--- | :--- |
| `200 OK` | Success | Successful GET, PUT, POST, or DELETE operation. |
| `400 BAD REQUEST` | Validation Failure | Bean validation constraint violated (`MethodArgumentNotValidException`). |
| `401 UNAUTHORIZED` | Missing/Invalid JWT | Authentication missing, invalid, or expired. |
| `403 FORBIDDEN` | Access Denied | Insufficient permissions (e.g. non-admin accessing admin routes). |
| `404 NOT FOUND` | Resource Absent | Requested entity ID does not exist (`ResourceNotFoundException`). |
| `409 CONFLICT` | Duplicate Resource | Resource with identical key exists (`DuplicateResourceFoundException`). |
| `500 INTERNAL ERROR`| Server Exception | Uncaught runtime error handled by `GlobalExceptionHandler`. |

### 1.4. Global Error Response Schemas

#### Bean Validation Error (`400 Bad Request`)
```json
{
  "mobileNumber": "Contact number must be exactly 10 digits",
  "familyHeadName": "Family head name is required",
  "startDate": "Start date must be greater than today's date"
}
```

#### Resource Not Found (`404 Not Found`)
```json
{
  "error": "Bill not found"
}
```

#### Conflict / Duplicate Resource (`409 Conflict`)
```text
User with same mobile number or email present
```

---

## 2. Authentication API (`/api/auth`)

Publicly accessible routes for session authentication and account password recovery.

### 2.1. User Login / Generate JWT
Authenticates a user via their 10-digit mobile number and password, returning a signed JWT token.

- **Method:** `POST`
- **Endpoint:** `/api/auth/login`
- **Security:** Public (`permitAll`)
- **Headers:** `Content-Type: application/json`

#### Request Body
| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `mobileNumber` | `String` | Yes | 10 digits (`^\d{10}$`) | Registered mobile number. |
| `password` | `String` | Yes | Non-blank | Plaintext user password. |

```json
{
  "mobileNumber": "9876543210",
  "password": "SecurePassword@123"
}
```

#### Response (`200 OK`)
Returns the signed JWT bearer token string.
```text
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI5ODc2NTQzMjEwIiwiZXhwIjoxNzA0MDY3MjAwfQ.s7dG...
```

---

### 2.2. User Logout
Invalidates the current client-side security session context.

- **Method:** `POST`
- **Endpoint:** `/api/auth/logout`
- **Security:** Public (`permitAll`)

#### Response (`200 OK`)
```text
Logged out successfully.
```

---

### 2.3. Forgot Password / Reset via Email
Triggers a random 8-character password generation, updates the account hash, and sends the temporary password to the registered email address.

- **Method:** `POST`
- **Endpoint:** `/api/auth/forgot-password`
- **Security:** Public (`permitAll`)

#### Request Body
| Field | Type | Required | Constraints | Description |
| :--- | :--- | :--- | :--- | :--- |
| `email` | `String` | Yes | Valid email format | User's registered email address. |

```json
{
  "email": "user@example.com"
}
```

#### Response (`200 OK`)
```text
If that email exists in our system, a new password has been sent.
```

---

## 3. User & Administrator Management API (`/api/user`)

Endpoints for managing user profiles, invitations, password changes, and administrator activations.

### 3.1. Get Authenticated Profile
Retrieves profile details for the currently logged-in user derived from the JWT context.

- **Method:** `GET`
- **Endpoint:** `/api/user`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
{
  "userId": 5,
  "fullName": "Ramesh Patel",
  "mobileNumber": "9876543210",
  "email": "ramesh.patel@example.com",
  "isActive": true,
  "roleName": "ADMIN"
}
```

---

### 3.2. Update Current Profile
Updates personal information (name, contact number, email) for the active user.

- **Method:** `PUT`
- **Endpoint:** `/api/user/profile`
- **Security:** Authenticated

#### Request Body
```json
{
  "fullName": "Rameshchandra Patel",
  "mobileNumber": "9876543210",
  "email": "ramesh.updated@example.com"
}
```

#### Response (`200 OK`)
```text
Profile updated successfully.
```

---

### 3.3. Get Paginated Administrator List
Fetches a paginated and filterable list of all registered administrative users.

- **Method:** `POST`
- **Endpoint:** `/api/user/list`
- **Security:** Authenticated (POST restricted to `ROLE_ADMIN`)

#### Request Body (`PaginationDTO`)
```json
{
  "search": "Ramesh",
  "page": 0,
  "size": 10,
  "direction": "DESC"
}
```

#### Response (`200 OK`)
```json
{
  "content": [
    {
      "userId": 5,
      "fullName": "Ramesh Patel",
      "mobileNumber": "9876543210",
      "email": "ramesh.patel@example.com",
      "isActive": true,
      "roleName": "ADMIN"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### 3.4. Invite New Administrator
Allows an existing administrator to register a new admin member. Generates a secure random 8-character password and dispatches an automated HTML welcome email with login credentials.

- **Method:** `POST`
- **Endpoint:** `/api/user/inviteAdmin`
- **Security:** Requires `ROLE_ADMIN`

#### Request Body
```json
{
  "fullName": "Kiran Patel",
  "mobileNumber": "9123456789",
  "email": "kiran.patel@example.com"
}
```

#### Response (`200 OK`)
```text
Admin invited successfully.
```

---

### 3.5. Toggle Administrator Activation
Toggles an administrator's active state (`true` $\leftrightarrow$ `false`). Inactivated admins cannot log in.

- **Method:** `PUT`
- **Endpoint:** `/api/user/toggle/{userId}`
- **Security:** Requires `ROLE_ADMIN`

#### Path Parameters
- `userId` (`Integer`, required): Target user identifier.

#### Response (`200 OK`)
```text
Admin Activated
```
*(or `"Admin Inactivated"`)*

---

### 3.6. Change Password
Allows the authenticated user to change their account password. Validates existing password before applying the new hash.

- **Method:** `POST`
- **Endpoint:** `/api/user/change-password`
- **Security:** Authenticated

#### Request Body
```json
{
  "currentPassword": "OldPassword@123",
  "newPassword": "NewStrongPassword@2026"
}
```

#### Response (`200 OK`)
```text
Password changed successfully
```

---

## 4. Event & Booking Management API (`/api/events`)

Comprehensive booking and billing pipeline for community functions (weddings, meetings, religious functions). Handles snapshot pricing, locking, and Playwright-powered PDF generation.

### 4.1. List Events by Status (Paginated & Filterable)
Lists bookings filtered by status ID and optional search string (searches `familyHeadName`).

- **Method:** `POST`
- **Endpoint:** `/api/events/list`
- **Security:** Authenticated
- **Query Parameters:**
  - `statusId` (`Integer`, required):
    - `1`: Draft / Initial Booking
    - `2` / `3`: Confirmed / Active
    - `4`: Closed / Completed
- **Request Body:** `PaginationDTO`

```json
{
  "search": "Patel",
  "page": 0,
  "size": 10,
  "direction": "DESC"
}
```

#### Response (`200 OK`)
```json
{
  "content": [
    {
      "eventId": 101,
      "familyHeadName": "Kishorbhai Patel",
      "contactNumber": "9825012345",
      "email": "kishor@example.com",
      "eventType": "Wedding",
      "startDate": "2026-11-15T09:00:00",
      "endDate": "2026-11-16T18:00:00",
      "totalAmount": 45000,
      "isClosed": false,
      "statusId": 2,
      "items": [
        {
          "itemId": 1,
          "quantity": 1,
          "itemName": "Main Community Hall",
          "priceAtBooking": 25000
        },
        {
          "itemId": 4,
          "quantity": 200,
          "itemName": "Dining Chairs",
          "priceAtBooking": 20
        }
      ]
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 4.2. Get Event Details by ID
Retrieves full details of a specific event bill, including all booked line items.

- **Method:** `GET`
- **Endpoint:** `/api/events/{eventId}`
- **Security:** Authenticated
- **Path Parameters:**
  - `eventId` (`Integer`, required)

#### Response (`200 OK`)
```json
{
  "eventId": 101,
  "familyHeadName": "Kishorbhai Patel",
  "contactNumber": "9825012345",
  "email": "kishor@example.com",
  "eventType": "Wedding",
  "startDate": "2026-11-15T09:00:00",
  "endDate": "2026-11-16T18:00:00",
  "totalAmount": 45000,
  "isClosed": false,
  "statusId": 2,
  "items": [
    {
      "itemId": 1,
      "quantity": 1,
      "itemName": "Main Community Hall",
      "priceAtBooking": 25000
    }
  ]
}
```

---

### 4.3. Create New Event Booking
Registers a new event booking. Calculates total amount by fetching current prices from master items and locking them as `priceAtBooking`.

- **Method:** `POST`
- **Endpoint:** `/api/events`
- **Security:** Authenticated

#### Request Body
```json
{
  "familyHeadName": "Kishorbhai Patel",
  "contactNumber": "9825012345",
  "email": "kishor@example.com",
  "eventType": "Wedding",
  "startDate": "2026-11-15T09:00:00",
  "endDate": "2026-11-16T18:00:00",
  "items": [
    {
      "itemId": 1,
      "quantity": 1
    },
    {
      "itemId": 4,
      "quantity": 200
    }
  ]
}
```

#### Response (`200 OK`)
Returns the persisted `EventDTO` with generated `eventId` and calculated `totalAmount`.

---

### 4.4. Update Existing Event Booking
Modifies event metadata and recalculates item snapshot costs. Fails if the bill has been finalized and closed.

- **Method:** `PUT`
- **Endpoint:** `/api/events`
- **Security:** Authenticated

#### Request Body
Same schema as `POST /api/events`, with required `eventId`:
```json
{
  "eventId": 101,
  "familyHeadName": "Kishorbhai Patel",
  "contactNumber": "9825012345",
  "email": "kishor@example.com",
  "eventType": "Wedding Ceremony",
  "startDate": "2026-11-15T09:00:00",
  "endDate": "2026-11-16T21:00:00",
  "items": [
    {
      "itemId": 1,
      "quantity": 1
    }
  ]
}
```

#### Response (`200 OK`)
Returns updated `EventDTO`.

---

### 4.5. Finalize and Close Event Bill
Locks the booking bill, preventing any subsequent updates or alterations. Sets `isClosed = true` and `statusId = 4`.

- **Method:** `PUT`
- **Endpoint:** `/api/events/{eventId}/close`
- **Security:** Requires `ROLE_ADMIN`

#### Response (`200 OK`)
```json
{
  "eventId": 101,
  "familyHeadName": "Kishorbhai Patel",
  "isClosed": true,
  "statusId": 4,
  "totalAmount": 45000
}
```

---

### 4.6. Generate Dynamic Bill PDF & Email Receipt
Compiles an official Gujarati-localized A4 invoice bill. Uses a headless Chromium browser instance via Playwright to render the Thymeleaf HTML template, embeds Gujarati fonts in Base64, converts amounts into Gujarati words and numerals, dispatches an email attachment to the family head, and streams the binary PDF to the client.

- **Method:** `GET`
- **Endpoint:** `/api/events/{eventId}/pdf`
- **Security:** Authenticated
- **Response Headers:**
  - `Content-Type: application/pdf`
  - `Content-Disposition: attachment; filename="<FamilyHeadName>_<EventType>.pdf"`

#### Response (`200 OK`)
Binary PDF stream (`byte[]`).

---

### 4.7. Get Calendar Event Counts
Computes daily booking density for calendar views by aggregating multi-day bookings across date ranges.

- **Method:** `GET`
- **Endpoint:** `/api/events/calendar-counts`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
{
  "2026-11-15": 2,
  "2026-11-16": 2,
  "2026-11-20": 1,
  "2026-11-21": 3
}
```

---

## 5. Master Inventory Catalog API (`/api/items`)

Master catalog of rental amenities, halls, and equipment.

### 5.1. List All Active Catalog Items
Retrieves all active items (where `is_deleted = false`) sorted alphabetically by name.

- **Method:** `GET`
- **Endpoint:** `/api/items`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
[
  {
    "itemId": 1,
    "itemName": "Main Community Hall",
    "currentPrice": 25000,
    "description": "Central AC hall with stage and lighting"
  },
  {
    "itemId": 2,
    "itemName": "Dining Hall",
    "currentPrice": 10000,
    "description": "Ground floor dining hall with kitchen access"
  },
  {
    "itemId": 3,
    "itemName": "PA Sound System",
    "currentPrice": 3500,
    "description": "Full audio setup with 4 microphones and amplifier"
  }
]
```

---

### 5.2. Get Catalog Item by ID
- **Method:** `GET`
- **Endpoint:** `/api/items/{itemId}`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
{
  "itemId": 1,
  "itemName": "Main Community Hall",
  "currentPrice": 25000,
  "description": "Central AC hall with stage and lighting"
}
```

---

### 5.3. Create or Update Catalog Item (Upsert)
Creates a new catalog item or updates an existing item's pricing and description.

- **Method:** `POST`
- **Endpoint:** `/api/items`
- **Security:** Requires `ROLE_ADMIN`

#### Request Body
```json
{
  "itemId": 0,
  "itemName": "Deluxe Guest Room",
  "currentPrice": 1200,
  "description": "Air-conditioned double bedroom for visiting guests"
}
```

#### Response (`200 OK`)
Returns created/updated `ItemDTO`.

---

### 5.4. Soft Delete Catalog Item
Marks the item as deleted (`is_deleted = true`), removing it from active catalogs while maintaining relational integrity with past invoices.

- **Method:** `DELETE`
- **Endpoint:** `/api/items/{itemId}`
- **Security:** Requires `ROLE_ADMIN`

#### Response (`200 OK`)
```text
Item moved to trash successfully.
```

---

## 6. Philanthropic Donors API (`/api/donors`)

Manages charitable contributions and donor records.

### 6.1. Create or Update Donor (Upsert)
- **Method:** `POST`
- **Endpoint:** `/api/donors`
- **Security:** Requires `ROLE_ADMIN`

#### Request Body
```json
{
  "donorId": 0,
  "fullName": "Prafulbhai Patel",
  "amount": 51000,
  "remarks": "Contribution towards Community Library Construction"
}
```

#### Response (`200 OK`)
Returns created `DonorDTO` with assigned `donorId` and `donationDate`.

---

### 6.2. Get Donor by ID
- **Method:** `GET`
- **Endpoint:** `/api/donors/{donorId}`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
{
  "donorId": 12,
  "fullName": "Prafulbhai Patel",
  "amount": 51000,
  "donationDate": "2026-10-10T14:30:00",
  "remarks": "Contribution towards Community Library Construction"
}
```

---

### 6.3. List Donors (Paginated, Sorted by Amount)
Fetches a paginated list of donors automatically sorted in descending order of donation amount.

- **Method:** `POST`
- **Endpoint:** `/api/donors/list`
- **Security:** Authenticated

#### Request Body
```json
{
  "search": "Praful",
  "page": 0,
  "size": 10,
  "direction": "DESC"
}
```

#### Response (`200 OK`)
```json
{
  "content": [
    {
      "donorId": 12,
      "fullName": "Prafulbhai Patel",
      "amount": 51000,
      "donationDate": "2026-10-10T14:30:00",
      "remarks": "Contribution towards Community Library Construction"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 6.4. Delete Donor Record
Permanently removes a donor record.

- **Method:** `DELETE`
- **Endpoint:** `/api/donors/{donorId}`
- **Security:** Requires `ROLE_ADMIN`

#### Response (`200 OK`)
```text
Donor deleted successfully.
```

---

## 7. Expense Tracking & Financial Analytics API (`/api/expenses`)

Tracks facility operational expenses, maintenance, utilities, and provides time-series aggregations for financial reporting.

### 7.1. List Expenses (Paginated & Filterable)
Returns paginated expense records sorted by date descending. Filters across `description` and `category`.

- **Method:** `POST`
- **Endpoint:** `/api/expenses/list`
- **Security:** Authenticated

#### Request Body
```json
{
  "search": "Electricity",
  "page": 0,
  "size": 10,
  "direction": "DESC"
}
```

#### Response (`200 OK`)
```json
{
  "content": [
    {
      "expenseId": 45,
      "description": "Monthly Electricity Bill - Main Hall",
      "amount": 14200,
      "expenseDate": "2026-10-05",
      "category": "Utilities"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 7.2. Get Expense by ID
- **Method:** `GET`
- **Endpoint:** `/api/expenses/{expenseId}`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
{
  "expenseId": 45,
  "description": "Monthly Electricity Bill - Main Hall",
  "amount": 14200,
  "expenseDate": "2026-10-05",
  "category": "Utilities"
}
```

---

### 7.3. Create or Update Expense (Upsert)
- **Method:** `POST`
- **Endpoint:** `/api/expenses`
- **Security:** Requires `ROLE_ADMIN`

#### Request Body
```json
{
  "expenseId": 0,
  "description": "Generator Diesel & Servicing",
  "amount": 4500,
  "expenseDate": "2026-10-11",
  "category": "Maintenance"
}
```

#### Response (`200 OK`)
Returns created `ExpenseDTO`.

---

### 7.4. Daily Expense Summary Analytics
Aggregates total expenditures grouped by calendar date (`GROUP BY expenseDate ORDER BY expenseDate DESC`). Ideal for rendering expenditure charts and time-series trends on admin dashboards.

- **Method:** `GET`
- **Endpoint:** `/api/expenses/daily-summary`
- **Security:** Authenticated

#### Response (`200 OK`)
```json
[
  {
    "date": "2026-10-11",
    "totalSpent": 4500
  },
  {
    "date": "2026-10-05",
    "totalSpent": 14200
  },
  {
    "date": "2026-10-01",
    "totalSpent": 8750
  }
]
```

---

### 7.5. Delete Expense Record
- **Method:** `DELETE`
- **Endpoint:** `/api/expenses/{expenseId}`
- **Security:** Requires `ROLE_ADMIN`

#### Response (`200 OK`)
```text
Expense deleted successfully.
```

---

## 8. Quick cURL Testing Examples

### 1. Authenticate and Receive Token
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"mobileNumber":"9876543210","password":"AdminPassword@123"}'
```

### 2. Fetch Active Inventory Items
```bash
curl -X GET "http://localhost:8080/api/items" \
     -H "Authorization: Bearer <TOKEN>"
```

### 3. Create an Event Booking
```bash
curl -X POST "http://localhost:8080/api/events" \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "familyHeadName": "Jay Patel",
       "contactNumber": "9898989898",
       "email": "jay@example.com",
       "eventType": "Reception",
       "startDate": "2026-12-01T10:00:00",
       "items": [{"itemId": 1, "quantity": 1}]
     }'
```

### 4. Download Generated PDF Bill
```bash
curl -X GET "http://localhost:8080/api/events/101/pdf" \
     -H "Authorization: Bearer <TOKEN>" \
     -o "Bill_101.pdf"
```
