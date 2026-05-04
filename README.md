# Banking Account Management System

Microservices platform for a modern international banking account management system, built with Java 21, Spring Boot 4, Thymeleaf, PostgreSQL, Kafka, Docker, Kubernetes, and GitLab CI.

## Architecture

```text
api-gateway
identity-service
profile-service
account-service
payment-service
notification-service
banking-common
```

Each business service owns its PostgreSQL database and Flyway migrations. Kafka is used for event-driven integration, notifications, and auditable domain events.

## Main Use Cases

- Customer registration and JWT authentication
- RBAC roles: `ADMIN`, `CUSTOMER`, `SUPPORT`, `COMPLIANCE`, `AUDITOR`
- MFA enablement flag, account lockout, login audit
- OAuth2 Google login support in the identity service
- Customer profile management, addresses, consent capture, KYC submission
- Account opening, balances, statements, daily transfer limits
- Account freeze/unfreeze by authorized staff
- Transaction posting with balance consistency
- Beneficiary management
- Internal, SEPA, SWIFT, and standing-order payment initiation
- Payment idempotency via `Idempotency-Key`
- Manual approval workflow for high-value/high-risk payments
- Event-driven notification inbox with idempotent Kafka consumers

## Security

- JWT bearer tokens with role claims
- Method-level authorization
- Centralized structured error handling
- Strong password validation
- Login risk controls and lockout
- OAuth2/OIDC hooks
- CORS per environment
- Kafka producer idempotence and outbox pattern
- Consumer idempotency table in notification service
- Secrets externalized for UAT/PP/Prod/Kubernetes

## Spring Profiles

Every service supports:

- `dev`
- `tst`
- `uat`
- `pp`
- `prod`

Each profile configures database, Kafka, logging, and security settings.

## Local Run

```bash
docker compose up --build
```

Services:

- Gateway: `http://localhost:8080`
- Identity: `http://localhost:8081`
- Profile: `http://localhost:8082`
- Account: `http://localhost:8083`
- Payment: `http://localhost:8084`
- Notification: `http://localhost:8085`

Swagger UI is available at `/swagger-ui.html` on every service.

Seeded identity users use password `password`:

- `admin`
- `customer`
- `support`
- `compliance`

Rotate all seeded credentials outside local development.

## Build And Test

```bash
mvn -B -DskipTests compile
mvn -B test
```

Repository integration tests use Testcontainers and run when Docker is available.

## Kubernetes

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployments.yaml
kubectl apply -f k8s/services.yaml
```

The manifests are deployable to KIND and cloud Kubernetes. For EKS/GKE/AKS, point DB URLs and Kafka bootstrap servers to managed PostgreSQL and Kafka services.

## API Flow

1. Register or login through `identity-service`.
2. Use the returned JWT as `Authorization: Bearer <token>`.
3. Create `/api/profiles/me`.
4. Open accounts through `/api/accounts`.
5. Add beneficiaries through `/api/payments/beneficiaries`.
6. Initiate payments through `/api/payments` with an `Idempotency-Key` header.
7. Read notifications through `/api/notifications`.
