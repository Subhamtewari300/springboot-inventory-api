# Inventory Reservation API — Spring Boot

Independent Java demonstration service for **concurrency-safe inventory reservations**, with no employer/client data or code.

**Stack:** Java 21 · Spring Boot 3.4 · Spring Data JPA · H2 · JUnit 5 · Maven

## Features

- Create products with SKU and initial stock.
- Reserve stock with an idempotency key: repeated matching requests return the same reservation.
- Reusing a key with a different product or quantity returns 409 Conflict.
- Row-level locking serializes reservations for the same product to prevent overselling.
- Cancellation returns stock once; integration tests cover basic invariants.

## Run

```bash
mvn spring-boot:run
mvn test
curl -X POST http://localhost:8081/api/products -H 'Content-Type: application/json' -d '{"sku":"BOOK-001","name":"Notebook","stock":20}'
curl -X POST http://localhost:8081/api/reservations -H 'Content-Type: application/json' -d '{"productId":1,"quantity":3,"idempotencyKey":"order-123"}'
```

To cancel: `DELETE /api/reservations/{id}`.

This starter uses **in-memory H2** and is a portfolio demo, not a production service. Production use would require authentication, monitoring, migrations and database-specific integration tests.
