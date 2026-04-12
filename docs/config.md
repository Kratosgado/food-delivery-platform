# Configuration Guide

This document describes the configuration strategy for the Food Delivery Platform microservices, following the [12-Factor App](https://12factor.net/config) methodology.

## Configuration Strategy

The application follows Factor III of the 12-Factor App methodology: **store config in the environment**. Credentials, API keys, and other sensitive values must not be hardcoded in source code or configuration files committed to version control.

### Environment Variable Usage

All services use environment variables for configuration that may vary between environments (dev, staging, production):

- **Database credentials**: Loaded from environment variables (NO fallback values)
- **RabbitMQ credentials**: Loaded from environment variables (NO fallback values)
- **JWT secrets**: Loaded from environment variables (NO fallback values)

**IMPORTANT**: All environment variables MUST be provided. The application will fail to start if any required variable is missing.

### Configuration Files

| File | Purpose | Environment |
|------|---------|--------------|
| `application.yml` | Base configuration for local development | Local (`dev` profile implicit) |
| `application-docker.yml` | Overrides for Docker Compose environment | Docker |
| `application-prod.yml` | Production-optimized configuration | Production |
| `.env` | Local environment variables (do not commit) | Local |
| `.env.example` | Template for required environment variables | Local |

## Production Profile (prod)

The `prod` profile provides production-optimized configurations for all services. It follows 12-Factor App principles with no fallback values for environment variables.

### What It Optimizes

| Setting | Default | Production | Rationale |
|---------|---------|------------|-----------|
| `spring.jpa.hibernate.ddl-auto` | `update` | `validate` | Prevents accidental schema changes in production |
| `spring.jpa.show-sql` | `false` | `false` | Already disabled in prod |
| `hibernate.format_sql` | `true` | `false` | Reduces log verbosity |
| `logging.level.root` | INFO | WARN | Reduces disk I/O and log volume |
| `logging.level.com.fooddelivery` | DEBUG | INFO | Reduces disk I/O and log volume |
| `management.endpoints.web.exposure.include` | varies | `health,info,metrics,prometheus` | Only exposes observability endpoints |

### How to Activate

Set the `SPRING_PROFILES_ACTIVE` environment variable:

```bash
# Docker Compose
SPRING_PROFILES_ACTIVE=prod docker compose up --build

# Or in .env file
SPRING_PROFILES_ACTIVE=prod
```

For Kubernetes, set the environment variable in your deployment manifest:

```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"
```

### Service-Specific Notes

- **customer-service, restaurant-service, order-service, delivery-service**: Include JPA optimizations (ddl-auto, show-sql, format_sql)
- **api-gateway**: No database, includes logging and actuator optimizations
- **eureka-server**: No database, includes logging and actuator optimizations

## Environment Variables

All environment variables must be set in the `.env` file before running the application. Copy from `.env.example`:

```bash
cp .env.example .env
```

### Required Variables

All environment variables MUST be provided. The application will fail to start if any are missing.

| Variable | Description | Required | Used By |
|----------|-------------|----------|--------|
| `GATEWAY_PORT` | API Gateway server port (default: 8080) | Yes | api-gateway |
| `CUSTOMER_PORT` | Customer service port (default: 8081) | Yes | customer-service |
| `RESTAURANT_PORT` | Restaurant service port (default: 8082) | Yes | restaurant-service |
| `ORDER_PORT` | Order service port (default: 8083) | Yes | order-service |
| `DELIVERY_PORT` | Delivery service port (default: 8084) | Yes | delivery-service |
| `EUREKA_PORT` | Eureka server port (default: 8761) | Yes | All services (for registration), eureka-server (its own port) |
| `POSTGRES_HOST` | PostgreSQL server hostname | Yes | All services |
| `POSTGRES_PORT` | PostgreSQL server port | Yes | All services |
| `POSTGRES_USER` | PostgreSQL database username | Yes | All services |
| `POSTGRES_PASSWORD` | PostgreSQL database password | Yes | All services |
| `DB_PASSWORD` | Alias for POSTGRES_PASSWORD | Yes | All services |
| `EUREKA_HOST` | Eureka server hostname | Yes | All services |
| `RABBIT_HOST` | RabbitMQ server hostname | Yes | order-service, delivery-service |
| `RABBIT_PORT` | RabbitMQ server port | Yes | order-service, delivery-service |
| `RABBIT_USER` | RabbitMQ username | Yes | order-service, delivery-service |
| `RABBIT_PASSWORD` | RabbitMQ password | Yes | order-service, delivery-service |
| `REDIS_HOST` | Redis server hostname | Yes | api-gateway |
| `REDIS_PORT` | Redis server port | Yes | api-gateway |
| `JWT_SECRET` | Secret key for JWT token signing (min 32 chars) | Yes | api-gateway, customer-service |

### Variable Details

#### PostgreSQL Configuration

```
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password_here
```

These variables are used by:
- **customer-service**: Connects to `customer_db`
- **restaurant-service**: Connects to `restaurant_db`
- **order-service**: Connects to `order_db`
- **delivery-service**: Connects to `delivery_db`

Docker Compose maps `POSTGRES_HOST=postgres` for service containers.

#### Eureka Configuration

```
EUREKA_HOST=localhost
EUREKA_PORT=8761
```

Used by:
- **All services**: Register with Eureka for service discovery

Docker Compose maps `EUREKA_HOST=eureka-server` for service containers.

#### RabbitMQ Configuration

```
RABBIT_HOST=localhost
RABBIT_PORT=5672
RABBIT_USER=guest
RABBIT_PASSWORD=your_secure_password_here
```

Used by:
- **order-service**: Publishes and consumes events via RabbitMQ
- **delivery-service**: Listens for delivery events

Docker Compose maps `RABBIT_HOST=rabbitmq` for service containers.

#### Redis Configuration

```
REDIS_HOST=localhost
REDIS_PORT=6379
```

Used by:
- **api-gateway**: Rate limiting and caching

Docker Compose maps `REDIS_HOST=redis` for service containers.

#### JWT Configuration

```
JWT_SECRET=your_very_long_secure_secret_key_min_32_chars
```

**Security Requirement**: Must be at least 32 characters for HMAC-SHA256 signing.

Used by:
- **api-gateway**: Validates JWT tokens on incoming requests
- **customer-service**: Issues JWT tokens on login

## Local Development

### Using Docker Compose

```bash
# 1. Copy the environment template
cp .env.example .env

# 2. Edit .env with your values
nano .env

# 3. Start all services
docker compose up --build

# 4. Verify services are healthy
curl http://localhost:8080/actuator/health
```

### Running Services Locally (without Docker)

When running services directly with Maven, ensure environment variables are set:

```bash
# Set environment variables before running
export GATEWAY_PORT=8080
export CUSTOMER_PORT=8081
export RESTAURANT_PORT=8082
export ORDER_PORT=8083
export DELIVERY_PORT=8084
export EUREKA_PORT=8761
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
export DB_PASSWORD=postgres
export EUREKA_HOST=localhost
export EUREKA_PORT=8761
export RABBIT_HOST=localhost
export RABBIT_PORT=5672
export RABBIT_USER=guest
export RABBIT_PASSWORD=guest
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=changeme_super_secret_key_min_32_chars

# Run a service
cd api-gateway
mvn spring-boot:run
```

## Production Deployment

In ALL environments (including local development):

1. **Set all required environment variables** - The application will NOT start without them
2. **Use strong secrets** - Generate random strings for passwords and JWT_SECRET
3. **Never commit `.env` files** - Add `.env` to `.gitignore`
4. **Use secrets management** - Consider using Docker secrets, Kubernetes secrets, or a vault solution

### Example Production Environment Variables

```bash
# Generate secure passwords:
# - PostgreSQL: min 16 characters
# - RabbitMQ: min 16 characters
# - JWT_SECRET: min 32 characters (use 64+ for production)

POSTGRES_USER=foodapp
POSTGRES_PASSWORD=<generate_STRONG_password>
DB_PASSWORD=<generate_STRONG_password>
RABBIT_USER=foodapp
RABBIT_PASSWORD=<generate_STRONG_password>
JWT_SECRET=<generate_64_character_random_string>
```

## Troubleshooting

### Application fails to start with "DB_PASSWORD" errors

Ensure `POSTGRES_PASSWORD` is set in your `.env` file and the file is loaded by Docker Compose.

### JWT authentication fails

Ensure `JWT_SECRET` is set consistently across all services that require authentication.

### Cannot connect to RabbitMQ

Verify `RABBIT_USER` and `RABBIT_PASSWORD` match the credentials configured in RabbitMQ.

## Related Documentation

- [Monolith Architecture Analysis](./monolith_architecture.md)
- [API Gateway Documentation](../api-gateway/README.md)
- [Service-Specific Documentation](../*/README.md)