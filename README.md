# Reward Points App

A Spring Boot REST API that calculates and tracks reward points earned by customers based on their purchase transactions.

---

## How Points Are Calculated

- Purchases **at or below $50** — no points
- Purchases **between $50 and $100** — 1 point per dollar above $50
- Purchases **above $100** — 2 points per dollar above $100, plus 1 point per dollar between $50 and $100

**Example:** A $120 purchase earns `(120 - 100) × 2 + 50 × 1 = 90 points`

---

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2 (in-memory database)
- Lombok
- JUnit 5 + Mockito (testing)

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run the application

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`.  
On startup, the database is seeded automatically with 5 sample customers and their transactions across January–March 2025.

### Run the tests

```bash
./mvnw test
```

---

## API Endpoints

All endpoints are prefixed with `/api/v1/rewards`.

Dates must be in **ISO format: `yyyy-MM-dd`**

### Get rewards for a single customer (custom date range)

```
GET /api/v1/rewards/customers/{customerId}?startDate=2025-01-01&endDate=2025-03-31
```

### Get rewards for all customers (custom date range)

```
GET /api/v1/rewards/customers?startDate=2025-01-01&endDate=2025-03-31
```

### Get rewards for a single customer (last 3 months)

```
GET /api/v1/rewards/customers/last-three-months/{customerId}
```

### Get rewards for all customers (last 3 months)

```
GET /api/v1/rewards/customers/last-three-months
```

---

## Sample Response

`GET /api/v1/rewards/customers/1?startDate=2025-01-01&endDate=2025-01-31`

```json
{
  "customerId": 1,
  "customerName": "Alice Johnson",
  "customerEmail": "alice@example.com",
  "monthlyBreakdown": [
    {
      "month": "JANUARY 2025",
      "year": 2025,
      "monthNumber": 1,
      "points": 340,
      "transactions": [
        {
          "id": 1,
          "amount": 120.00,
          "transactionDate": "2025-01-05",
          "description": "Electronics purchase",
          "pointsEarned": 90
        },
        {
          "id": 2,
          "amount": 200.00,
          "transactionDate": "2025-01-14",
          "description": "Furniture",
          "pointsEarned": 250
        },
        {
          "id": 3,
          "amount": 45.00,
          "transactionDate": "2025-01-22",
          "description": "Grocery top-up",
          "pointsEarned": 0
        }
      ]
    }
  ],
  "totalPoints": 340
}
```

---

## Error Responses

The API returns a consistent error structure for all failures:

```json
{
  "status": 404,
  "error": "Customer Not Found",
  "message": "Customer not found with ID: 99",
  "path": "/api/v1/rewards/customers/99",
  "timestamp": "2025-01-15T10:30:00"
}
```

| Scenario | HTTP Status |
|---|---|
| Customer ID not found | 404 |
| No transactions in date range | 404 |
| Start date after end date | 400 |
| Missing or invalid date parameter | 400 |

---

## H2 Console

The H2 console is available at `http://localhost:8080/h2-console` while the app is running.

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:rewardsdb` |
| Username | `sa` |
| Password | *(leave blank)* |
