# Banking Account Management System - Diagrammes UML

Ce document regroupe les diagrammes conceptuels principaux de l'application `banking-app`.

Les diagrammes sont ecrits en Mermaid pour etre exploitables dans GitLab, IntelliJ, VS Code et plusieurs viewers Markdown.

## 1. Diagramme UML - Cas d'utilisation

```mermaid
flowchart LR
    Customer["Actor: CUSTOMER"]
    Admin["Actor: ADMIN"]
    Support["Actor: SUPPORT"]
    Compliance["Actor: COMPLIANCE"]
    Auditor["Actor: AUDITOR"]

    subgraph Identity["Identity and Access"]
        Register(("Register customer"))
        Login(("Login"))
        ManageUsers(("Manage users"))
        ManageRoles(("Manage roles"))
        LockUser(("Lock user"))
        Mfa(("Enable / disable MFA flag"))
    end

    subgraph Profile["Customer Profile"]
        CreateProfile(("Create profile"))
        UpdateProfile(("Update profile"))
        SubmitKyc(("Submit KYC"))
        CaptureConsent(("Capture consent"))
        ViewCustomer(("View customer profile"))
    end

    subgraph Account["Account Management"]
        OpenAccount(("Open account"))
        ListAccounts(("List accounts"))
        Statement(("View statement"))
        SetLimit(("Set daily limit"))
        FreezeAccount(("Freeze / unfreeze account"))
        ManualTxn(("Post manual transaction"))
    end

    subgraph Payment["Payment Management"]
        CreateBeneficiary(("Create beneficiary"))
        InitiatePayment(("Initiate payment"))
        CancelPayment(("Cancel payment"))
        ApprovePayment(("Approve payment"))
        RejectPayment(("Reject payment"))
    end

    subgraph Notification["Notification"]
        ListNotifications(("List notifications"))
        MarkRead(("Mark notification read"))
    end

    Customer --> Register
    Customer --> Login
    Customer --> CreateProfile
    Customer --> UpdateProfile
    Customer --> SubmitKyc
    Customer --> CaptureConsent
    Customer --> OpenAccount
    Customer --> ListAccounts
    Customer --> Statement
    Customer --> SetLimit
    Customer --> CreateBeneficiary
    Customer --> InitiatePayment
    Customer --> CancelPayment
    Customer --> ListNotifications
    Customer --> MarkRead

    Admin --> ManageUsers
    Admin --> ManageRoles
    Admin --> LockUser
    Admin --> Mfa
    Admin --> ViewCustomer
    Admin --> FreezeAccount
    Admin --> ManualTxn
    Admin --> ApprovePayment
    Admin --> RejectPayment

    Support --> ViewCustomer
    Support --> FreezeAccount
    Support --> ManualTxn
    Support --> ApprovePayment
    Support --> RejectPayment

    Compliance --> ViewCustomer
    Compliance --> ApprovePayment
    Compliance --> RejectPayment

    Auditor --> ViewCustomer
```

## 2. Diagramme UML - Composants

```mermaid
flowchart TB
    Client["Client<br/>Browser / Postman / Mobile"]

    subgraph Gateway["api-gateway"]
        GatewayController["ProxyController"]
        GatewayPages["PageController / Thymeleaf"]
        GatewaySecurity["SecurityConfig"]
    end

    subgraph Identity["identity-service"]
        AuthController["AuthController"]
        UserAdminController["UserAdminController"]
        AuthenticationService["AuthenticationService"]
        UserManagementService["UserManagementService"]
        JwtTokenService["JwtTokenService"]
        IdentityKafka["IdentityEventPublisher"]
        IdentityRepo["User/Role/LoginAudit Repositories"]
    end

    subgraph Profile["profile-service"]
        ProfileController["ProfileController"]
        ProfileService["ProfileService"]
        ProfileKafka["ProfileEventPublisher"]
        ProfileRepo["CustomerProfile Repository"]
    end

    subgraph Account["account-service"]
        AccountController["AccountController"]
        AccountService["AccountService"]
        AccountOutbox["OutboxEventPublisher"]
        AccountRepo["Account/Transaction Repositories"]
    end

    subgraph Payment["payment-service"]
        PaymentController["PaymentController"]
        PaymentService["PaymentService"]
        PaymentOutbox["OutboxEventPublisher"]
        PaymentRepo["Payment/Beneficiary Repositories"]
    end

    subgraph Notification["notification-service"]
        NotificationController["NotificationController"]
        EventConsumer["BankingEventConsumer"]
        NotificationService["NotificationService"]
        NotificationRepo["Notification Repository"]
    end

    Client --> GatewayController
    GatewayController --> AuthController
    GatewayController --> UserAdminController
    GatewayController --> ProfileController
    GatewayController --> AccountController
    GatewayController --> PaymentController
    GatewayController --> NotificationController

    AuthController --> AuthenticationService
    AuthController --> UserManagementService
    UserAdminController --> UserManagementService
    AuthenticationService --> JwtTokenService
    AuthenticationService --> IdentityRepo
    UserManagementService --> IdentityRepo
    UserManagementService --> IdentityKafka

    ProfileController --> ProfileService
    ProfileService --> ProfileRepo
    ProfileService --> ProfileKafka

    AccountController --> AccountService
    AccountService --> AccountRepo
    AccountService --> AccountOutbox

    PaymentController --> PaymentService
    PaymentService --> PaymentRepo
    PaymentService --> PaymentOutbox

    NotificationController --> NotificationService
    EventConsumer --> NotificationService
    NotificationService --> NotificationRepo
```

## 3. Diagramme UML - Sequence login JWT

```mermaid
sequenceDiagram
    actor User
    participant Gateway as api-gateway
    participant Identity as identity-service
    participant AuthService as AuthenticationService
    participant UserRepo as UserAccountRepository
    participant Audit as LoginAuditService
    participant Jwt as JwtTokenService

    User->>Gateway: POST /api/auth/login
    Gateway->>Identity: Forward login request
    Identity->>AuthService: login(request)
    AuthService->>UserRepo: findByUsername or findByEmail
    UserRepo-->>AuthService: UserAccount
    AuthService->>AuthService: verify password and status
    AuthService->>Audit: record success
    AuthService->>Jwt: issueToken(user)
    Jwt-->>AuthService: TokenResponse
    AuthService-->>Identity: TokenResponse
    Identity-->>Gateway: 200 OK
    Gateway-->>User: JWT accessToken
```

## 4. Diagramme UML - Sequence creation profil et KYC

```mermaid
sequenceDiagram
    actor Customer
    participant Gateway as api-gateway
    participant ProfileController as ProfileController
    participant ProfileService as ProfileService
    participant ProfileRepo as CustomerProfileRepository
    participant Kafka as Kafka

    Customer->>Gateway: POST /api/profiles/me + JWT
    Gateway->>ProfileController: Forward request
    ProfileController->>ProfileService: create(userId, request)
    ProfileService->>ProfileRepo: existsByUserId(userId)
    ProfileRepo-->>ProfileService: false
    ProfileService->>ProfileRepo: save(CustomerProfile)
    ProfileRepo-->>ProfileService: saved profile
    ProfileService-->>Customer: 201 ProfileResponse

    Customer->>Gateway: POST /api/profiles/me/kyc
    Gateway->>ProfileController: Forward request
    ProfileController->>ProfileService: submitKyc(userId, request)
    ProfileService->>ProfileRepo: load profile
    ProfileService->>ProfileService: kycStatus=PENDING_REVIEW
    ProfileService->>Kafka: publish profile-kyc-submitted
    ProfileService-->>Customer: 200 ProfileResponse
```

## 5. Diagramme UML - Sequence paiement avec approbation

```mermaid
sequenceDiagram
    actor Customer
    actor Compliance
    participant Gateway as api-gateway
    participant Payment as payment-service
    participant PaymentService as PaymentService
    participant PaymentDB as payment_db
    participant Outbox as OutboxEventPublisher
    participant Kafka as Kafka
    participant Notification as notification-service

    Customer->>Gateway: POST /api/payments + Idempotency-Key
    Gateway->>Payment: Forward request
    Payment->>PaymentService: initiate(customerId, key, request)
    PaymentService->>PaymentDB: findByCustomerIdAndIdempotencyKey
    alt existing key
        PaymentDB-->>PaymentService: existing PaymentInstruction
        PaymentService-->>Customer: existing PaymentResponse
    else new key
        PaymentService->>PaymentService: validate beneficiary ownership
        PaymentService->>PaymentService: score risk
        PaymentService->>PaymentDB: save PaymentInstruction
        PaymentService->>PaymentDB: save OutboxEvent(PENDING)
        PaymentService-->>Customer: 201 PaymentResponse
    end

    Outbox->>PaymentDB: poll pending events
    Outbox->>Kafka: publish payment-initiated
    Kafka->>Notification: consume event
    Notification->>Notification: create CustomerNotification

    Compliance->>Gateway: POST /api/payments/admin/{id}/approve
    Gateway->>Payment: Forward approve
    Payment->>PaymentService: approve(paymentId)
    PaymentService->>PaymentDB: load payment
    PaymentService->>PaymentService: status EXECUTED or SCHEDULED
    PaymentService->>PaymentDB: save OutboxEvent
    PaymentService-->>Compliance: PaymentResponse
```

## 6. Diagramme UML - Etats UserAccount

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: register / seed data
    ACTIVE --> LOCKED: 5 failed login attempts
    ACTIVE --> DISABLED: administrative action
    LOCKED --> ACTIVE: manual DB reset currently
    DISABLED --> ACTIVE: future admin endpoint
```

## 7. Diagramme UML - Etats KYC

```mermaid
stateDiagram-v2
    [*] --> NOT_STARTED: profile created
    NOT_STARTED --> PENDING_REVIEW: submit KYC
    PENDING_REVIEW --> VERIFIED: future compliance validation
    PENDING_REVIEW --> NOT_STARTED: future rejection / resubmit
```

## 8. Diagramme UML - Etats Account

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: open account
    ACTIVE --> FROZEN: freeze by ADMIN/SUPPORT
    FROZEN --> ACTIVE: unfreeze by ADMIN/SUPPORT
    ACTIVE --> CLOSED: future close account
    FROZEN --> CLOSED: future close account
```

## 9. Diagramme UML - Etats PaymentInstruction

```mermaid
stateDiagram-v2
    [*] --> PENDING_MFA: low risk payment
    [*] --> PENDING_APPROVAL: high risk payment
    PENDING_MFA --> CANCELLED: customer cancel
    PENDING_APPROVAL --> CANCELLED: customer cancel
    PENDING_MFA --> EXECUTED: approve without scheduledFor
    PENDING_APPROVAL --> EXECUTED: approve without scheduledFor
    PENDING_MFA --> SCHEDULED: approve with scheduledFor
    PENDING_APPROVAL --> SCHEDULED: approve with scheduledFor
    PENDING_MFA --> REJECTED: reject
    PENDING_APPROVAL --> REJECTED: reject
    SCHEDULED --> EXECUTED: future scheduler
    EXECUTED --> [*]
    REJECTED --> [*]
    CANCELLED --> [*]
```

## 10. Diagramme UML - Etats Notification

```mermaid
stateDiagram-v2
    [*] --> UNREAD: event consumed
    UNREAD --> READ: markRead
    UNREAD --> FAILED: future delivery failure
    FAILED --> UNREAD: future retry/recreate
```

## 11. Diagramme UML - Classes Identity

```mermaid
classDiagram
    class UserAccount {
        Long id
        String username
        String email
        String passwordHash
        UserStatus status
        boolean mfaEnabled
        int failedLoginAttempts
        Instant lastLoginAt
        String externalProvider
        String externalSubject
    }

    class Role {
        Long id
        BankingRole name
        Set~String~ permissions
    }

    class LoginAudit {
        Long id
        String username
        boolean success
        String ipAddress
        String userAgent
        String failureReason
        Instant occurredAt
    }

    class UserStatus {
        <<enumeration>>
        ACTIVE
        LOCKED
        DISABLED
    }

    class BankingRole {
        <<enumeration>>
        ADMIN
        CUSTOMER
        SUPPORT
        COMPLIANCE
        AUDITOR
    }

    UserAccount "*" -- "*" Role : user_roles
    UserAccount --> UserStatus
    Role --> BankingRole
```

## 12. Diagramme UML - Classes Profile

```mermaid
classDiagram
    class CustomerProfile {
        Long id
        Long userId
        String customerNumber
        String firstName
        String lastName
        String email
        String phone
        LocalDate dateOfBirth
        String nationality
        KycStatus kycStatus
        RiskRating riskRating
        String taxResidencyCountry
    }

    class Address {
        Long id
        String type
        String line1
        String line2
        String city
        String postalCode
        String countryCode
    }

    class CustomerConsent {
        Long id
        String consentType
        boolean granted
        Instant capturedAt
    }

    class KycStatus {
        <<enumeration>>
        NOT_STARTED
        PENDING_REVIEW
        VERIFIED
    }

    class RiskRating {
        <<enumeration>>
        LOW
        MEDIUM
        HIGH
    }

    CustomerProfile "1" o-- "*" Address : addresses
    CustomerProfile "1" o-- "*" CustomerConsent : consents
    CustomerProfile --> KycStatus
    CustomerProfile --> RiskRating
```

## 13. Diagramme UML - Classes Account

```mermaid
classDiagram
    class BankAccount {
        Long id
        Long customerId
        String iban
        AccountType type
        AccountStatus status
        String currency
        BigDecimal ledgerBalance
        BigDecimal availableBalance
        BigDecimal dailyTransferLimit
    }

    class AccountTransaction {
        Long id
        TransactionType type
        BigDecimal amount
        String currency
        String reference
        String description
        Instant postedAt
    }

    class OutboxEvent {
        UUID id
        String aggregateType
        String aggregateId
        String eventType
        String topic
        String payload
        OutboxStatus status
        int attempts
        String lastError
        Instant createdAt
        Instant updatedAt
        Instant publishedAt
    }

    class AccountType {
        <<enumeration>>
        CHECKING
        SAVINGS
        BUSINESS
        MULTI_CURRENCY
    }

    class AccountStatus {
        <<enumeration>>
        ACTIVE
        FROZEN
        CLOSED
    }

    class TransactionType {
        <<enumeration>>
        CREDIT
        DEBIT
        HOLD
        RELEASE
    }

    BankAccount "1" o-- "*" AccountTransaction : transactions
    BankAccount --> AccountType
    BankAccount --> AccountStatus
    AccountTransaction --> TransactionType
```

## 14. Diagramme UML - Classes Payment

```mermaid
classDiagram
    class Beneficiary {
        Long id
        Long customerId
        String nickname
        String accountHolderName
        String iban
        String bic
        String countryCode
        BeneficiaryStatus status
    }

    class PaymentInstruction {
        Long id
        Long customerId
        Long sourceAccountId
        String sourceIban
        PaymentType type
        PaymentStatus status
        BigDecimal amount
        String currency
        String paymentReference
        String idempotencyKey
        String remittanceInformation
        BigDecimal riskScore
        Instant scheduledFor
        Instant approvedAt
        Instant executedAt
        String rejectionReason
    }

    class OutboxEvent {
        UUID id
        String aggregateType
        String aggregateId
        String eventType
        String topic
        String payload
        OutboxStatus status
        int attempts
    }

    class BeneficiaryStatus {
        <<enumeration>>
        ACTIVE
        SUSPENDED
        DELETED
    }

    class PaymentType {
        <<enumeration>>
        INTERNAL
        SEPA
        SWIFT
        STANDING_ORDER
    }

    class PaymentStatus {
        <<enumeration>>
        DRAFT
        PENDING_MFA
        PENDING_APPROVAL
        APPROVED
        SCHEDULED
        EXECUTED
        REJECTED
        CANCELLED
    }

    Beneficiary "1" <-- "*" PaymentInstruction : beneficiary
    Beneficiary --> BeneficiaryStatus
    PaymentInstruction --> PaymentType
    PaymentInstruction --> PaymentStatus
```

## 15. Diagramme UML - Classes Notification

```mermaid
classDiagram
    class CustomerNotification {
        Long id
        Long customerId
        NotificationChannel channel
        NotificationStatus status
        String eventType
        String title
        String message
        Instant generatedAt
    }

    class NotificationChannel {
        <<enumeration>>
        IN_APP
        EMAIL
        SMS
        PUSH
    }

    class NotificationStatus {
        <<enumeration>>
        UNREAD
        READ
        FAILED
    }

    CustomerNotification --> NotificationChannel
    CustomerNotification --> NotificationStatus
```

## 16. Diagramme UML - Activite ouverture compte

```mermaid
flowchart TD
    A([Start]) --> B["Validate JWT"]
    B --> C["Validate OpenAccountRequest"]
    C --> D["Resolve currency default EUR if missing"]
    D --> E["Resolve opening balance default 0"]
    E --> F["Generate IBAN"]
    F --> G["Create BankAccount ACTIVE"]
    G --> H["Save account"]
    H --> I{"openingBalance > 0?"}
    I -- Yes --> J["Create CREDIT transaction"]
    I -- No --> K["Enqueue account-opened event"]
    J --> K
    K --> L["Return AccountResponse"]
    L --> M([End])
```

## 17. Diagramme UML - Activite initiation paiement

```mermaid
flowchart TD
    A([Start]) --> B["Validate JWT"]
    B --> C["Require Idempotency-Key"]
    C --> D["Search existing payment by customerId + key"]
    D --> E{"Existing payment?"}
    E -- Yes --> F["Return existing PaymentResponse"]
    E -- No --> G["Load beneficiary"]
    G --> H{"Beneficiary belongs to customer?"}
    H -- No --> I["Throw AccessDenied"]
    H -- Yes --> J["Calculate risk score"]
    J --> K{"High risk or amount >= 10000?"}
    K -- Yes --> L["status = PENDING_APPROVAL"]
    K -- No --> M["status = PENDING_MFA"]
    L --> N["Save PaymentInstruction"]
    M --> N
    N --> O["Save OutboxEvent payment-initiated"]
    O --> P["Return PaymentResponse"]
    P --> Q([End])
```

