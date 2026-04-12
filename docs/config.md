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
| `.env` | Local environment variables (do not commit) | Local |
| `.env.example` | Template for required environment variables | Local |

## Environment Variables

All environment variables must be set in the `.env` file before running the application. Copy from `.env.example`:

```bash
cp .env.example .env
```

### Required Variables

All environment variables MUST be provided. The application will fail to start if any are missing.

| Variable | Description | Required | Used By |
|----------|-------------|----------|--------|
| `POSTGRES_USER` | PostgreSQL database username | Yes | All services |
| `POSTGRES_PASSWORD` | PostgreSQL database password | Yes | All services |
| `DB_PASSWORD` | Alias for POSTGRES_PASSWORD | Yes | All services |
| `RABBIT_USER` | RabbitMQ username | Yes | order-service, delivery-service |
| `RABBIT_PASSWORD` | RabbitMQ password | Yes | order-service, delivery-service |
| `JWT_SECRET` | Secret key for JWT token signing (min 32 chars) | Yes | api-gateway, customer-service |

### Variable Details

#### Database Configuration

```
POSTGRES_USER=postgres
POSTGRES_PASSWORD=your_secure_password_here
```

These variables are used by:
- **customer-service**: Connects to `customer_db`
- **restaurant-service**: Connects to `restaurant_db`
- **order-service**: Connects to `order_db`
- **delivery-service**: Connects to `delivery_db`

Docker Compose maps `POSTGRES_PASSWORD` to `DB_PASSWORD` for service containers.

#### RabbitMQ Configuration

```
RABBIT_USER=guest
RABBIT_PASSWORD=your_secure_password_here
```

Used by:
- **order-service**: Publishes and consumes events via RabbitMQ
- **delivery-service**: Listens for delivery events

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
export POSTGRES_USER=postgres
export POSTGRES_PASSWORD=postgres
export DB_PASSWORD=postgres
export RABBIT_USER=guest
export RABBIT_PASSWORD=guest
export JWT_SECRET=changeme_super_secret_key_min_32_chars

# Run a service
cd customer-service
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