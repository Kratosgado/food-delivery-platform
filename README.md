# Food Delivery Platform — Microservices

Refactored from a Spring Boot monolith into four independently deployable microservices.

## Architecture

```
Client
  │
  ▼
API Gateway (:8080) ─── JWT Auth Filter ─── Rate Limiter (Redis)
  │
  ├── /api/customers/**  → Customer Service  (:8081) → customer_db
  ├── /api/restaurants/** → Restaurant Service (:8082) → restaurant_db
  ├── /api/orders/**     → Order Service     (:8083) → order_db
  └── /api/deliveries/** → Delivery Service  (:8084) → delivery_db

Eureka Server (:8761)  — service registry
RabbitMQ      (:5672)  — event bus
Redis         (:6379)  — rate limiter backing store
```

## Quick Start

```bash
# 1. Copy env file and set secrets
cp .env.example .env

# 2. Build and start everything
docker compose up --build

# 3. Verify
open http://localhost:8761        # Eureka dashboard
open http://localhost:15672       # RabbitMQ management (guest/guest)
curl http://localhost:8080/actuator/health
```

## Service Ports

| Service           | Local Port | Docker Name        |
|-------------------|------------|--------------------|
| API Gateway       | 8080       | api-gateway        |
| Customer Service  | 8081       | customer-service   |
| Restaurant Service| 8082       | restaurant-service |
| Order Service     | 8083       | order-service      |
| Delivery Service  | 8084       | delivery-service   |
| Eureka Server     | 8761       | eureka-server      |
| RabbitMQ          | 5672/15672 | rabbitmq           |
| PostgreSQL        | 5432       | postgres           |
| Redis             | 6379       | redis              |

## Prerequisites

- Java 21
- Docker 24+ and Docker Compose v2
- Maven 3.9+ (for local development without Docker)

## TODOs After Scaffold

1. Migrate all business logic from monolith into the stubbed service classes
2. Implement JWT login endpoint in `CustomerController` (migrate from monolith's `AuthController`)
3. Complete the `toResponseDto` mapping methods
4. Add Spring Security config to Customer Service if auth logic stays there
5. Import and run the Postman collection for end-to-end testing
