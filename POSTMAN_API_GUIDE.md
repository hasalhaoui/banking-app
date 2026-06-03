# Guide Postman - Banking Account Management System

Ce document explique comment tester les APIs exposees par Swagger avec Postman, en passant de preference par l'API Gateway.

Projet cible: `C:\Users\salha\IdeaProjects\banking-app`

## 1. URLs utiles

Point d'entree recommande pour Postman:

```text
{{gatewayUrl}} = http://localhost:8080
```

Swagger par service:

```text
Identity      http://localhost:8081/swagger-ui.html
Profile       http://localhost:8082/swagger-ui.html
Account       http://localhost:8083/swagger-ui.html
Payment       http://localhost:8084/swagger-ui.html
Notification  http://localhost:8085/swagger-ui.html
```

OpenAPI JSON importable dans Postman:

```text
http://localhost:8081/v3/api-docs
http://localhost:8082/v3/api-docs
http://localhost:8083/v3/api-docs
http://localhost:8084/v3/api-docs
http://localhost:8085/v3/api-docs
```

Dans Postman: `Import` -> `Link` -> coller une URL `/v3/api-docs`.

## 2. Environnement Postman

Creer un environnement `banking-local` avec les variables suivantes:

| Variable | Valeur initiale |
| --- | --- |
| `gatewayUrl` | `http://localhost:8080` |
| `identityUrl` | `http://localhost:8081` |
| `profileUrl` | `http://localhost:8082` |
| `accountUrl` | `http://localhost:8083` |
| `paymentUrl` | `http://localhost:8084` |
| `notificationUrl` | `http://localhost:8085` |
| `customerToken` | vide |
| `adminToken` | vide |
| `supportToken` | vide |
| `complianceToken` | vide |
| `customerUserId` | vide |
| `accountId` | vide |
| `accountIban` | vide |
| `beneficiaryId` | vide |
| `paymentId` | vide |
| `notificationId` | vide |
| `idempotencyKey` | `PAY-LOCAL-001` |

Headers standards:

```text
Content-Type: application/json
Accept: application/json
X-Correlation-Id: postman-local-001
Accept-Language: fr
```

Pour les endpoints securises:

```text
Authorization: Bearer {{customerToken}}
```

Pour les endpoints paiement:

```text
Idempotency-Key: {{idempotencyKey}}
```

## 3. Comptes disponibles en dev

Les comptes sont crees par Flyway dans `identity-service`.

| Username | Password | Role |
| --- | --- | --- |
| `admin` | `password` | `ADMIN` |
| `customer` | `password` | `CUSTOMER` |
| `support` | `password` | `SUPPORT` |
| `compliance` | `password` | `COMPLIANCE` |

Important: ces mots de passe sont uniquement pour le profil local/dev.

## 4. Scripts Postman utiles

### Capturer un token apres login

Dans l'onglet `Tests` de la requete login client:

```javascript
pm.test("Login OK", function () {
  pm.response.to.have.status(200);
});

const body = pm.response.json();
pm.environment.set("customerToken", body.accessToken);
```

Pour admin:

```javascript
const body = pm.response.json();
pm.environment.set("adminToken", body.accessToken);
```

Pour support:

```javascript
const body = pm.response.json();
pm.environment.set("supportToken", body.accessToken);
```

Pour compliance:

```javascript
const body = pm.response.json();
pm.environment.set("complianceToken", body.accessToken);
```

### Capturer les IDs

Exemple apres creation de compte:

```javascript
const body = pm.response.json();
pm.environment.set("accountId", body.id);
pm.environment.set("accountIban", body.iban);
```

Exemple apres creation de beneficiaire:

```javascript
const body = pm.response.json();
pm.environment.set("beneficiaryId", body.id);
```

Exemple apres creation de paiement:

```javascript
const body = pm.response.json();
pm.environment.set("paymentId", body.id);
```

## 5. Health checks

### Gateway health

```text
GET {{gatewayUrl}}/actuator/health
```

Reponse attendue:

```json
{
  "status": "UP"
}
```

Repeter si besoin pour:

```text
GET {{identityUrl}}/actuator/health
GET {{profileUrl}}/actuator/health
GET {{accountUrl}}/actuator/health
GET {{paymentUrl}}/actuator/health
GET {{notificationUrl}}/actuator/health
```

## 6. Identity API

### 6.1 Register customer

```text
POST {{gatewayUrl}}/api/auth/register
```

Headers:

```text
Content-Type: application/json
Accept-Language: fr
```

Body:

```json
{
  "username": "john.doe",
  "email": "john.doe@example.com",
  "password": "StrongPass@2026"
}
```

Reponse attendue: `201 Created`.

Exemple:

```json
{
  "id": 5,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "status": "ACTIVE",
  "mfaEnabled": true,
  "lastLoginAt": null,
  "roles": ["CUSTOMER"]
}
```

Tests Postman:

```javascript
pm.response.to.have.status(201);
const body = pm.response.json();
pm.environment.set("customerUserId", body.id);
```

Regles de validation:

```text
username: obligatoire, 4 a 120 caracteres
email: obligatoire et valide
password: au moins 12 caracteres, majuscule, minuscule, chiffre, caractere special
```

### 6.2 Login customer

```text
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "username": "john.doe",
  "password": "StrongPass@2026"
}
```

Reponse attendue: `200 OK`.

Exemple:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresAt": "2026-05-13T15:30:00Z",
  "mfaRequired": true,
  "roles": ["CUSTOMER"]
}
```

Tests Postman:

```javascript
pm.response.to.have.status(200);
const body = pm.response.json();
pm.environment.set("customerToken", body.accessToken);
```

### 6.3 Login admin

```text
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "username": "admin",
  "password": "password"
}
```

Tests Postman:

```javascript
pm.response.to.have.status(200);
pm.environment.set("adminToken", pm.response.json().accessToken);
```

### 6.4 Login support

```text
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "username": "support",
  "password": "password"
}
```

Tests Postman:

```javascript
pm.response.to.have.status(200);
pm.environment.set("supportToken", pm.response.json().accessToken);
```

### 6.5 Login compliance

```text
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "username": "compliance",
  "password": "password"
}
```

Tests Postman:

```javascript
pm.response.to.have.status(200);
pm.environment.set("complianceToken", pm.response.json().accessToken);
```

### 6.6 List users

Role requis: `ADMIN`.

```text
GET {{gatewayUrl}}/api/admin/users
Authorization: Bearer {{adminToken}}
```

Reponse attendue: `200 OK`.

### 6.7 Update user roles

Role requis: `ADMIN`.

```text
PUT {{gatewayUrl}}/api/admin/users/{{customerUserId}}/roles
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

Body:

```json
{
  "roles": ["CUSTOMER", "SUPPORT"]
}
```

Roles possibles:

```text
ADMIN
CUSTOMER
SUPPORT
COMPLIANCE
AUDITOR
```

Reponse attendue: `200 OK`.

### 6.8 Update MFA flag

```text
PATCH {{gatewayUrl}}/api/admin/users/{{customerUserId}}/mfa
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

Body:

```json
{
  "enabled": false
}
```

Reponse attendue: `200 OK`.

### 6.9 Lock user

```text
PATCH {{gatewayUrl}}/api/admin/users/{{customerUserId}}/lock
Authorization: Bearer {{adminToken}}
```

Reponse attendue: `200 OK`, status utilisateur `LOCKED`.

Note: il n'y a pas encore d'endpoint API pour deverrouiller un utilisateur. Pour le dev local, remettre en base:

```sql
UPDATE user_account
SET status = 'ACTIVE', failed_login_attempts = 0
WHERE id = 5;
```

## 7. Profile API

### 7.1 Create my profile

Role requis: utilisateur authentifie.

```text
POST {{gatewayUrl}}/api/profiles/me
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+212600000000",
  "dateOfBirth": "1990-01-15",
  "nationality": "MA",
  "taxResidencyCountry": "MA",
  "addresses": [
    {
      "type": "HOME",
      "line1": "1 Main Street",
      "line2": "Apt 4",
      "city": "Casablanca",
      "postalCode": "20000",
      "countryCode": "MA"
    }
  ]
}
```

Reponse attendue: `201 Created`.

### 7.2 Get my profile

```text
GET {{gatewayUrl}}/api/profiles/me
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 7.3 Update my profile

```text
PUT {{gatewayUrl}}/api/profiles/me
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.updated@example.com",
  "phone": "+212611111111",
  "dateOfBirth": "1990-01-15",
  "nationality": "MA",
  "taxResidencyCountry": "FR",
  "addresses": [
    {
      "type": "HOME",
      "line1": "22 Avenue Hassan II",
      "city": "Rabat",
      "postalCode": "10000",
      "countryCode": "MA"
    }
  ]
}
```

Reponse attendue: `200 OK`.

### 7.4 Submit KYC

```text
POST {{gatewayUrl}}/api/profiles/me/kyc
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "documentType": "PASSPORT",
  "documentReference": "MA123456"
}
```

Reponse attendue: `200 OK`, `kycStatus = PENDING_REVIEW`.

### 7.5 Capture consent

```text
POST {{gatewayUrl}}/api/profiles/me/consents
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "consentType": "TERMS_AND_CONDITIONS",
  "granted": true
}
```

Reponse attendue: `200 OK`.

### 7.6 Admin get profile by user id

Roles autorises: `ADMIN`, `COMPLIANCE`, `SUPPORT`.

```text
GET {{gatewayUrl}}/api/profiles/admin/{{customerUserId}}
Authorization: Bearer {{complianceToken}}
```

Reponse attendue: `200 OK`.

## 8. Account API

### 8.1 Open account

```text
POST {{gatewayUrl}}/api/accounts
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "type": "CHECKING",
  "currency": "EUR",
  "openingBalance": 1000.00
}
```

Types possibles:

```text
CHECKING
SAVINGS
BUSINESS
MULTI_CURRENCY
```

Reponse attendue: `201 Created`.

Tests Postman:

```javascript
pm.response.to.have.status(201);
const body = pm.response.json();
pm.environment.set("accountId", body.id);
pm.environment.set("accountIban", body.iban);
```

### 8.2 List my accounts

```text
GET {{gatewayUrl}}/api/accounts
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 8.3 Get account by id

```text
GET {{gatewayUrl}}/api/accounts/{{accountId}}
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 8.4 Account statement

```text
GET {{gatewayUrl}}/api/accounts/{{accountId}}/statement?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 8.5 Update daily transfer limit

```text
PUT {{gatewayUrl}}/api/accounts/{{accountId}}/limits
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "dailyTransferLimit": 7500.00
}
```

Reponse attendue: `200 OK`.

### 8.6 Post account transaction

Roles autorises: `ADMIN`, `SUPPORT`.

```text
POST {{gatewayUrl}}/api/accounts/admin/{{accountId}}/transactions
Authorization: Bearer {{supportToken}}
Content-Type: application/json
```

Body credit:

```json
{
  "type": "CREDIT",
  "amount": 250.00,
  "reference": "DEP-POSTMAN-001",
  "description": "Cash deposit from Postman"
}
```

Body debit:

```json
{
  "type": "DEBIT",
  "amount": 50.00,
  "reference": "DEB-POSTMAN-001",
  "description": "Manual debit from Postman"
}
```

Types possibles:

```text
CREDIT
DEBIT
HOLD
RELEASE
```

Reponse attendue: `200 OK`.

### 8.7 Freeze account

Roles autorises: `ADMIN`, `SUPPORT`.

```text
PATCH {{gatewayUrl}}/api/accounts/admin/{{accountId}}/freeze
Authorization: Bearer {{supportToken}}
```

Reponse attendue: `200 OK`, `status = FROZEN`.

### 8.8 Unfreeze account

```text
PATCH {{gatewayUrl}}/api/accounts/admin/{{accountId}}/unfreeze
Authorization: Bearer {{supportToken}}
```

Reponse attendue: `200 OK`, `status = ACTIVE`.

## 9. Payment API

### 9.1 Create beneficiary

```text
POST {{gatewayUrl}}/api/payments/beneficiaries
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "nickname": "Alice",
  "accountHolderName": "Alice Smith",
  "iban": "FR7630006000011234567890189",
  "bic": "AGRIFRPP",
  "countryCode": "FR"
}
```

Reponse attendue: `201 Created`.

Tests Postman:

```javascript
pm.response.to.have.status(201);
pm.environment.set("beneficiaryId", pm.response.json().id);
```

### 9.2 List beneficiaries

```text
GET {{gatewayUrl}}/api/payments/beneficiaries
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 9.3 Initiate SEPA payment

```text
POST {{gatewayUrl}}/api/payments
Authorization: Bearer {{customerToken}}
Idempotency-Key: {{idempotencyKey}}
Content-Type: application/json
```

Body:

```json
{
  "sourceAccountId": {{accountId}},
  "sourceIban": "{{accountIban}}",
  "beneficiaryId": {{beneficiaryId}},
  "type": "SEPA",
  "amount": 100.00,
  "currency": "EUR",
  "remittanceInformation": "Invoice 2026-001",
  "scheduledFor": null
}
```

Types possibles:

```text
INTERNAL
SEPA
SWIFT
STANDING_ORDER
```

Reponse attendue: `201 Created`.

Statut attendu:

```text
PENDING_MFA pour un paiement faible risque
PENDING_APPROVAL pour montant >= 10000 ou type SWIFT
```

Tests Postman:

```javascript
pm.response.to.have.status(201);
pm.environment.set("paymentId", pm.response.json().id);
```

### 9.4 Initiate high-risk SWIFT payment

Changer `idempotencyKey` avant l'envoi, par exemple `PAY-LOCAL-SWIFT-001`.

```text
POST {{gatewayUrl}}/api/payments
Authorization: Bearer {{customerToken}}
Idempotency-Key: {{idempotencyKey}}
Content-Type: application/json
```

Body:

```json
{
  "sourceAccountId": {{accountId}},
  "sourceIban": "{{accountIban}}",
  "beneficiaryId": {{beneficiaryId}},
  "type": "SWIFT",
  "amount": 15000.00,
  "currency": "EUR",
  "remittanceInformation": "International supplier payment",
  "scheduledFor": null
}
```

Reponse attendue: `201 Created`, statut `PENDING_APPROVAL`.

### 9.5 List my payments

```text
GET {{gatewayUrl}}/api/payments
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

### 9.6 Cancel my payment

Possible si le paiement n'est pas encore `EXECUTED`.

```text
POST {{gatewayUrl}}/api/payments/{{paymentId}}/cancel
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`, statut `CANCELLED`.

### 9.7 Approve payment

Roles autorises: `ADMIN`, `COMPLIANCE`, `SUPPORT`.

```text
POST {{gatewayUrl}}/api/payments/admin/{{paymentId}}/approve
Authorization: Bearer {{complianceToken}}
```

Reponse attendue: `200 OK`.

Si `scheduledFor = null`, statut final probable: `EXECUTED`.
Si `scheduledFor` est dans le futur, statut final probable: `SCHEDULED`.

### 9.8 Reject payment

```text
POST {{gatewayUrl}}/api/payments/admin/{{paymentId}}/reject
Authorization: Bearer {{complianceToken}}
Content-Type: application/json
```

Body:

```json
{
  "reason": "Suspicious transaction pattern"
}
```

Reponse attendue: `200 OK`, statut `REJECTED`.

### 9.9 Idempotency test

1. Mettre `idempotencyKey = IDEMPOTENCY-POSTMAN-001`.
2. Envoyer `POST /api/payments`.
3. Renvoyer exactement la meme requete avec le meme header.
4. Verifier que la reponse retourne le meme paiement.

Objectif: eviter la duplication d'un ordre de paiement en cas de retry client.

## 10. Notification API

Ces endpoints necessitent que `notification-service` soit lance. Pour recevoir des notifications generees par les evenements, Kafka doit etre actif sur `localhost:9092`.

### 10.1 List my notifications

```text
GET {{gatewayUrl}}/api/notifications
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

Tests Postman:

```javascript
pm.response.to.have.status(200);
const body = pm.response.json();
if (Array.isArray(body) && body.length > 0) {
  pm.environment.set("notificationId", body[0].id);
}
```

### 10.2 Mark notification as read

```text
PATCH {{gatewayUrl}}/api/notifications/{{notificationId}}/read
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `200 OK`.

## 11. Scenarios d'erreur a tester

### 11.1 Mot de passe faible

```text
POST {{gatewayUrl}}/api/auth/register
```

Body:

```json
{
  "username": "weak.user",
  "email": "weak.user@example.com",
  "password": "test"
}
```

Reponse attendue: `400 Bad Request`, code `VALIDATION_ERROR`.

Structure d'erreur:

```json
{
  "timestamp": "2026-05-13T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "correlationId": "postman-local-001",
  "violations": [
    {
      "field": "password",
      "message": "validation message",
      "rejectedValue": "test"
    }
  ]
}
```

### 11.2 Utilisateur duplique

Envoyer deux fois le meme `POST /api/auth/register`.

Reponse attendue au deuxieme appel: `409 Conflict`, code `BUSINESS_RULE_VIOLATION`.

### 11.3 Login invalide puis lock

Envoyer 5 fois:

```text
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "username": "john.doe",
  "password": "WrongPassword@2026"
}
```

Resultat attendu: login refuse. Apres 5 echecs, l'utilisateur passe en `LOCKED`.

### 11.4 Acces admin avec token customer

```text
GET {{gatewayUrl}}/api/admin/users
Authorization: Bearer {{customerToken}}
```

Reponse attendue: `403 Forbidden`, code `ACCESS_DENIED`.

### 11.5 Profil duplique

Envoyer deux fois `POST /api/profiles/me`.

Reponse attendue au deuxieme appel: `409 Conflict`.

### 11.6 IBAN invalide

```text
POST {{gatewayUrl}}/api/payments/beneficiaries
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body:

```json
{
  "nickname": "Invalid",
  "accountHolderName": "Invalid IBAN",
  "iban": "bad-iban",
  "bic": "AGRIFRPP",
  "countryCode": "FR"
}
```

Reponse attendue: `400 Bad Request`, code `VALIDATION_ERROR`.

### 11.7 Debit superieur au solde

```text
POST {{gatewayUrl}}/api/accounts/admin/{{accountId}}/transactions
Authorization: Bearer {{supportToken}}
Content-Type: application/json
```

Body:

```json
{
  "type": "DEBIT",
  "amount": 999999.00,
  "reference": "DEB-TOO-HIGH-001",
  "description": "Debit greater than available balance"
}
```

Reponse attendue: `409 Conflict`, message `Insufficient available balance`.

### 11.8 Transaction reference dupliquee

Envoyer deux fois la meme transaction avec la meme `reference`.

Reponse attendue au deuxieme appel: `409 Conflict`.

### 11.9 Paiement sans Idempotency-Key

```text
POST {{gatewayUrl}}/api/payments
Authorization: Bearer {{customerToken}}
Content-Type: application/json
```

Body identique a un paiement standard.

Comportement cible attendu: `400 Bad Request`.
Si l'application retourne `500 INTERNAL_ERROR`, c'est une anomalie a corriger en ajoutant un handler pour `MissingRequestHeaderException`.

### 11.10 Paiement avec beneficiary d'un autre client

1. Creer un second customer.
2. Creer un beneficiaire avec ce second customer.
3. Se reconnecter avec le premier customer.
4. Utiliser le `beneficiaryId` du second customer dans `POST /api/payments`.

Reponse attendue: `403 Forbidden`.

## 12. Ordre recommande dans Postman

Creer les folders suivants:

```text
00 - Health
01 - Identity
02 - Profile
03 - Account
04 - Payment
05 - Notification
90 - Negative Tests
```

Ordre de run:

```text
1. Health gateway/services
2. Login admin
3. Login support
4. Login compliance
5. Register customer
6. Login customer
7. Create profile
8. Submit KYC
9. Capture consent
10. Open account
11. Post credit transaction
12. Create beneficiary
13. Initiate payment
14. Approve or reject payment
15. List notifications
16. Run negative tests
```

## 13. OAuth2 Google SSO

L'application expose le flux OAuth2 cote navigateur:

```text
GET http://localhost:8081/oauth2/authorization/google
```

En profil `dev`, les valeurs par defaut sont `dummy`. Pour tester Google SSO, configurer:

```text
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

Le SSO Google se teste mieux dans le navigateur que dans Postman, car il implique redirections, consent screen et callback OAuth2.

## 14. Remarques importantes

1. Tester via `{{gatewayUrl}}` permet de simuler le parcours reel frontend/API.
2. Tester directement via `{{identityUrl}}`, `{{profileUrl}}`, etc. aide au debug service par service.
3. Les IDs actuels sont des `Long`; recuperer toujours `id` depuis les reponses Postman au lieu de les deviner.
4. Les notifications ne seront completes que si Kafka tourne localement.
5. Pour les tests repetables, changer `username`, `email`, `reference` et `Idempotency-Key` a chaque run, sauf pour le test d'idempotence.
