# Monolith Architecture Analysis

This document details the coupling points and design flaws in the `food-delivery-platform-monolith` that need to be resolved when migrating to a microservices architecture.

## 1. JPA Entity Relationships & Cross-Domain Foreign Keys

The monolithic application heavily uses direct JPA entity relationships across different bounded contexts, creating tight coupling at the database level:

*   **`Customer` Entity:** Has direct `@OneToMany` relationships to `Order` and `Restaurant` (as owner).
*   **`Order` Entity:** Has direct `@ManyToOne` relationships to both `Customer` and `Restaurant`, plus a direct `@OneToOne` mapping to `Delivery`.
*   **`OrderItem` Entity:** Has a direct `@ManyToOne` relationship to the `MenuItem` entity.
*   **`Restaurant` Entity:** Has a direct `@ManyToOne` relationship to `Customer` (to represent ownership) and a direct `@OneToMany` relationship to `Order`.
*   **`Delivery` Entity:** Has a direct `@OneToOne` relationship to the `Order` entity.

In the microservices architecture, these hard foreign keys must be replaced with weak references (e.g., storing `customerId` and `restaurantId` as simple `Long` or `UUID` fields).

## 2. Cross-Domain Service and Repository Calls

Services in the monolith frequently inject and call repositories or services belonging to other domains:

*   **`OrderService` → `CustomerService`**: Injects `CustomerService` to validate the customer and fetch the `Customer` entity during order placement.
*   **`OrderService` → `RestaurantService`**: Injects `RestaurantService` to fetch `Restaurant` and `MenuItem` entities to calculate totals and validate availability.
*   **`OrderService` → `DeliveryService`**: Injects `DeliveryService` to synchronously create a delivery record.
*   **`RestaurantService` → `CustomerRepository`**: Directly injects the `CustomerRepository` to validate if a customer exists before allowing them to create a restaurant.

## 3. Synchronous In-Process Calls (To Become Async/REST)

The monolith relies on synchronous in-process method calls for workflows that span multiple domains. These need to be decoupled:

*   **Order Placement (`OrderService.placeOrder`)**:
    *   Currently fetches customer and restaurant data synchronously (should become synchronous REST calls via Feign Client or handled by API Composition).
    *   Currently calls `DeliveryService.createDeliveryForOrder` synchronously. This blocks the order response until a delivery is assigned. In microservices, this must become an asynchronous `OrderPlacedEvent` published to RabbitMQ.
*   **Delivery Status Update (`DeliveryService.updateStatus`)**:
    *   When a delivery is marked as `DELIVERED`, the method directly modifies the associated `Order` entity's status (`delivery.getOrder().setStatus(...)`). In microservices, this should publish a `DeliveryStatusUpdatedEvent` for the Order Service to consume.

## 4. JWT Security Configuration

*   Currently, a single `SecurityConfig` and `JwtAuthenticationFilter` handle authentication and authorization for all endpoints in the monolith.
*   **Migration Plan**: The JWT validation logic and `SecurityFilterChain` must be relocated to the API Gateway. The Gateway will validate the JWT token on incoming requests and forward the authenticated user's details (e.g., `X-User-Id`, `X-User-Role`) as HTTP headers to the downstream microservices. The individual services will no longer need to parse JWT tokens directly.
