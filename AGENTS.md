# OpenCode Agent Instructions

## Repository Overview

- **Type**: Java 21, Spring Boot 3.3.6 multi-module monorepo.
- **Architecture**: Microservices (Eureka Server, API Gateway, Customer, Restaurant, Order, Delivery) communicating via REST and RabbitMQ.
- **State**: Actively being refactored from a monolith into microservices. Many services contain stubbed logic.

## Build and Run

- **Build**: Use `mvn clean install` from the root directory to build all modules.
- **Start Local Environment**:
  1. `cp .env.example .env` (Required first step)
  2. `docker compose up --build`
- **Ports**: API Gateway (`:8080`), Eureka (`:8761`), RabbitMQ (`:15672`), Postgres (`:5432`), Redis (`:6379`). Individual services run on `:8081` through `:8084`.

## Toolchain Quirks

- **Lombok & MapStruct**: Used heavily. MapStruct generates mapper implementations at compile time. If you create or modify a mapper interface, you **must run** `mvn compile` or `mvn clean install` to generate the implementation class before the code will run successfully.
- **Authentication**: JWT validation happens at the API Gateway via a filter. The login endpoint needs to be implemented in `customer-service`.
- **Database**: Each service has its own dedicated logical database in Postgres (`customer_db`, `restaurant_db`, `order_db`, `delivery_db`). Avoid direct cross-database queries; use REST endpoints or RabbitMQ events instead.

## Tasks & Workflow

- **Monolith Reference**: The source code for the original application is located in `food-delivery-platform-monolith/`. Use this as the reference for migrating business logic, entities, and endpoint behavior into the microservices.
- **DTOs**: DTOs should be implemented as Java records whenever possible. If a DTO represents an entity within the same service (i.e., not an external DTO from another service), it should include a `public static fromEntity(Entity e)` method to handle mapping (see `CustomerResponseDto` for reference).
- Before testing or executing code, always ensure you have run the Maven build to trigger necessary codegen (MapStruct).
- When implementing stubbed methods, map entities to DTOs correctly using MapStruct.
- No test suite is currently configured. Rely on the compiler (`mvn clean compile`) to verify structural correctness.

