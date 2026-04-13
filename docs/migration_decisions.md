# Migration Decision Log

This document details the architectural decisions made during the monolith-to-microservices refactoring of the Food Delivery Platform. Each service boundary is justified based on domain-driven design principles and the specific coupling issues identified in the original monolith.

## Table of Contents

- [Service Boundaries Overview](#service-boundaries-overview)
- [API Gateway](#api-gateway)
- [Eureka Server (Service Registry)](#eureka-server-service-registry)
- [Customer Service](#customer-service)
- [Restaurant Service](#restaurant-service)
- [Order Service](#order-service)
- [Delivery Service](#delivery-service)
- [Cross-Cutting Decisions](#cross-cutting-decisions)

---

## Service Boundaries Overview

The original monolith contained four tightly coupled domain entities: `Customer`, `Restaurant`, `Order`, and `Delivery`. These were coupled through:

1. **JPA relationships** - Direct `@OneToMany`, `@ManyToOne`, and `@OneToOne` mappings across domain boundaries
2. **Service layer coupling** - `OrderService` directly calling `CustomerService`, `RestaurantService`, and `DeliveryService`
3. **Synchronous workflows** - Order placement blocking until delivery was assigned

The microservices architecture decouples these by:

- Storing foreign keys as plain `Long` fields instead of JPA entity relationships
- Using asynchronous event-driven communication via RabbitMQ for cross-service workflows
- Implementing REST calls via Feign clients for synchronous data lookups

---

## API Gateway

**Port:** 8080

### Boundary Rationale

The API Gateway serves as the single entry point for all client requests. It handles:

- **JWT Token Validation** - Validates all incoming requests and extracts user identity
- **Rate Limiting** - Uses Redis for distributed rate limiting
- **Request Routing** - Routes requests to appropriate microservices based on URL paths

### Migration from Monolith

In the monolith, JWT authentication was handled by `JwtAuthenticationFilter` and `SecurityConfig` within the application. The Gateway centralizes this cross-cutting concern, eliminating the need for each service to parse JWT tokens. Instead, the Gateway forwards authenticated user details via HTTP headers (`X-User-Id`, `X-User-Role`, `X-User-Name`).

### Why This Boundary

- **Security** - Single point of authentication reduces attack surface
- **Observability** - Centralized logging and monitoring of all traffic
- **Decoupling** - Services no longer need security dependencies; they trust the Gateway

---

## Eureka Server (Service Registry)

**Port:** 8761

### Boundary Rationale

Eureka provides service discovery, allowing services to locate each other without hardcoded URLs. This is essential for:

- Dynamic service registration and discovery
- Load balancing across service instances
- Resilience when services restart or scale

### Migration from Monolith

In the monolith, all components ran in-process, so service discovery was unnecessary. The microservices need Eureka because:

- Services communicate over the network
- Container orchestration (Docker Compose) may assign dynamic ports
- Future scaling requires dynamic registration

---

## Customer Service

**Port:** 8081 | **Database:** customer_db

### Domain Scope

Manages all customer-related functionality:

- Customer registration and profile management
- Authentication (login/JWT token generation)
- Customer data retrieval for other services

### Migration from Monolith

| Monolith Component                                            | Microservice Implementation                                     |
| ------------------------------------------------------------- | --------------------------------------------------------------- |
| `Customer` entity with `@OneToMany` to `Order`                | Removed relationship; Order Service stores `customerId` as Long |
| `Customer` entity with `@ManyToOne` from `Restaurant` (owner) | Restaurant Service stores `ownerId` as Long                     |
| `CustomerService`                                             | Migrated as-is with authentication logic                        |
| `AuthController` / `AuthRequest` / `AuthResponse`             | Migrated to `CustomerController` login endpoint                 |
| `CustomerRepository`                                          | Migrated as-is                                                  |

### Why This Boundary

1. **Cohesion** - Customer data, authentication, and authorization naturally belong together
2. **Independent Scaling** - Authentication endpoints may have different load patterns than other operations
3. **Security** - Passwords and authentication tokens should remain in a dedicated service

### Key Architectural Changes

- Removed `@OneToMany` relationship to `Order` from the monolith
- Customer is now referenced only by ID (`Long`) in other services
- Other services fetch customer details via REST calls when needed

---

## Restaurant Service

**Port:** 8082 | **Database:** restaurant_db

### Domain Scope

Manages restaurant-related functionality:

- Restaurant creation and management
- Menu item CRUD operations
- Restaurant search and filtering

### Migration from Monolith

| Monolith Component                                           | Microservice Implementation                      |
| ------------------------------------------------------------ | ------------------------------------------------ |
| `Restaurant` entity                                          | Migrated with key structural changes             |
| `Restaurant` owner relationship (`@ManyToOne` to `Customer`) | Replaced with `ownerId` (Long) field             |
| `@OneToMany` to `MenuItem`                                   | Retained (MenuItem belongs to Restaurant domain) |
| `@OneToMany` to `Order`                                      | Removed; Order Service stores `restaurantId`     |
| `RestaurantService`                                          | Migrated with business logic                     |
| `RestaurantController`                                       | Migrated as REST endpoints                       |
| `MenuItem` entity                                            | Migrated (belongs to Restaurant domain)          |

### Why This Boundary

1. **Domain Ownership** - Menu items are intrinsic to restaurant data; they have no meaning outside a restaurant's context
2. **Restaurant-Owner Relationship** - The owner-to-restaurant association is a natural boundary
3. **Independent Deployability** - Restaurant management (menu updates, availability) has different release cycles than order processing

### Key Architectural Changes

- Removed direct JPA `@ManyToOne` to `Customer`; now stores `ownerId` as a plain Long
- Other services validate owner existence via REST call to Customer Service
- Menu items remain within Restaurant Service (they're a child aggregate)

---

## Order Service

**Port:** 8083 | **Database:** order_db

### Domain Scope

Manages order processing and lifecycle:

- Order creation and validation
- Order status management
- Integration with Restaurant and Customer services for validation

### Migration from Monolith

| Monolith Component                      | Microservice Implementation                        |
| --------------------------------------- | -------------------------------------------------- |
| `Order` entity                          | Migrated with key structural changes               |
| `@ManyToOne` to `Customer`              | Replaced with `customerId` (Long) field            |
| `@ManyToOne` to `Restaurant`            | Replaced with `restaurantId` (Long) field          |
| `@OneToOne` to `Delivery`               | Removed; Delivery Service stores `orderId`         |
| `@OneToMany` to `OrderItem`             | Retained (OrderItem belongs to Order domain)       |
| `OrderService.placeOrder()`             | Migrated; synchronous validation via Feign clients |
| `OrderService.createDeliveryForOrder()` | Replaced with async `OrderPlacedEvent` to RabbitMQ |

### Why This Boundary

1. **Business Process Center** - Order is the central transaction that coordinates Customer, Restaurant, and Delivery
2. **Saga Orchestration** - Orders orchestrate multiple services; this is a natural integration point
3. **Transaction Boundary** - Order creation is a transactional operation that requires consistency

### Key Architectural Changes

- Replaced JPA relationships with plain ID fields (`customerId`, `restaurantId`)
- Uses **Feign Clients** for synchronous calls:
  - `CustomerClient` - Validate customer exists and fetch delivery address
  - `RestaurantClient` - Validate restaurant, fetch menu items, calculate totals
- Uses **RabbitMQ** for asynchronous communication:
  - Publishes `OrderPlacedEvent` when order is created
  - Publishes `OrderCancelledEvent` when order is cancelled

### Event-Driven Workflow

```
Order Service                          Delivery Service
     │                                        │
     ├── OrderPlacedEvent (RabbitMQ) ───────► │
     │                                        │
     │                                        ├── Creates Delivery from event
     │                                        │
     │                                   DeliveryStatusUpdatedEvent
     ◄──────────────────────────────────────┤
     │ (Updates order status)
```

---

## Delivery Service

**Port:** 8084 | **Database:** delivery_db

### Domain Scope

Manages delivery logistics:

- Delivery assignment and tracking
- Driver management
- Delivery status updates
- Route coordination

### Migration from Monolith

| Monolith Component                              | Microservice Implementation           |
| ----------------------------------------------- | ------------------------------------- |
| `Delivery` entity                               | Migrated with key structural changes  |
| `@OneToOne` to `Order`                          | Replaced with `orderId` (Long) field  |
| `DeliveryService`                               | Migrated; now event-driven            |
| `DeliveryController`                            | Migrated as REST endpoints            |
| Synchronous delivery creation in `OrderService` | Replaced with async event consumption |

### Why This Boundary

1. **Complex Lifecycle** - Delivery has distinct states (PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED)
2. **Independent Scaling** - Delivery operations may scale differently based on order volume
3. **Driver Management** - Driver assignment and tracking is a distinct domain
4. **Event-Driven Decoupling** - Order Service should not block waiting for delivery assignment

### Key Architectural Changes

- Replaced JPA `@OneToOne` relationship with plain `orderId` field
- **Event Consumer** - Listens to `OrderPlacedEvent` from RabbitMQ to create deliveries asynchronously
- **Event Publisher** - Publishes `DeliveryStatusUpdatedEvent` when delivery status changes
- Order Service consumes these events to update order status

### Event-Driven Workflow

```
Order Service                          Delivery Service
     │                                        │
     ├── OrderPlacedEvent ───────────────►   │  (Async)
     │                                        │
     │                                   Delivery created
     │                                        │
     │                              DeliveryStatusUpdatedEvent
     ◄──────────────────────────────────────┤  (Async)
     │ (Order status updated)
```

---

## Cross-Cutting Decisions

### Database Per Service

Each service has its own dedicated database in PostgreSQL:

- `customer_db` - Customer Service
- `restaurant_db` - Restaurant Service
- `order_db` - Order Service
- `delivery_db` - Delivery Service

**Rationale:** This follows the Database per Service pattern, ensuring complete isolation. Cross-database joins are avoided; services use REST calls or events to share data.

### Synchronous vs. Asynchronous Communication

| Use Case                          | Communication Pattern    |
| --------------------------------- | ------------------------ |
| Validate customer exists          | Synchronous REST (Feign) |
| Validate restaurant/menu          | Synchronous REST (Feign) |
| Create delivery for order         | Asynchronous (RabbitMQ)  |
| Update order from delivery status | Asynchronous (RabbitMQ)  |

**Rationale:**

- Synchronous calls for validation ensure data consistency at request time
- Asynchronous events for state propagation prevent blocking and enable independence

### Fallback Patterns

Services implement fallback clients using `@FeignClient` with `fallback` properties:

- `CustomerClientFallback` - Returns default/empty data if Customer Service is unavailable
- `RestaurantClientFallback` - Returns default/empty data if Restaurant Service is unavailable
- `OrderClientFallback` - Returns default/empty data if Order Service is unavailable

**Rationale:** Circuit breaker pattern improves resilience; services degrade gracefully rather than failing completely.

---

## Summary of Boundary Mappings

| Monolith Entity/Service                      | Microservices      | Key Architectural Change                                                              |
| -------------------------------------------- | ------------------ | ------------------------------------------------------------------------------------- |
| `Customer` + `AuthController`                | Customer Service   | Removed cross-domain JPA relationships                                                |
| `Restaurant` + `MenuItem`                    | Restaurant Service | Replaced `ownerId` with Long field                                                    |
| `Order` + `OrderItem`                        | Order Service      | Replaced `customerId`/`restaurantId` with Long fields; event-driven delivery creation |
| `Delivery`                                   | Delivery Service   | Replaced `orderId` with Long field; event-driven lifecycle                            |
| `SecurityConfig` + `JwtAuthenticationFilter` | API Gateway        | Centralized JWT validation                                                            |
| N/A                                          | Eureka Server      | Added for service discovery                                                           |
| N/A                                          | RabbitMQ           | Added for async event communication                                                   |

---

## References

- [Monolith Architecture Analysis](./monolith_architecture.md) - Detailed coupling points in the original monolith
- [Configuration Guide](./config.md) - Environment variables and service configuration
