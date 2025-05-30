# Payment Service

A Spring Boot application for managing payments according to Baltic Amadeus Java developer test

---

## Requirements

The application was developed using
- **Java 17** (JDK)
- **Maven 3.9.5**
- **Docker Desktop 4.22.0 (Engine 24.0.5)** and **Docker Compose v2.20.2-desktop.1**

The application is using H2 in-memory database, so no need for any database related setup.

---

## Running the application

You can start the application by running the build_and_run.bat script. The server will be accessible at http://localhost:8080/.

---

## API Endpoints

- POST /payments - Create a new payment
- GET /payments - List all active payments (optional filter by amountMin and amountMax query parameters)
- GET /payments/{id} - Get payment by ID (returns ID and fee amount (only if payment was cancelled) )
- POST /payments/{id}/cancel - Cancel payment by ID (returns ID and fee amount)

---

I hope the code up to your standards, and thank you for your consideration.