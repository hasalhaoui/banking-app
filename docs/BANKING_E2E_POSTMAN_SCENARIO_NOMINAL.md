# Scenario nominal Postman - Parcours bancaire bout en bout

Objectif: tester un parcours complet client:

```text
Creation utilisateur -> login -> creation profil -> KYC -> consentement
-> ouverture compte -> creation beneficiaire -> virement
-> approbation paiement -> notifications
```

Point d'entree recommande:

```text
{{gatewayUrl}} = http://localhost:8080
```

## 1. Prerequis

Services a lancer:

```text
identity-service      8081
profile-service       8082
account-service       8083
payment-service       8084
notification-service  8085
api-gateway           8080
```

Pour recevoir les notifications de bout en bout, Kafka doit etre actif sur:

```text
localhost:9092
```

Sans Kafka, le scenario API peut aller jusqu'au paiement, mais les notifications evenementielles peuvent rester absentes.

## 2. Variables Postman a creer

Dans l'environnement `banking-local`:

| Variable | Valeur |
| --- | --- |
| `gatewayUrl` | `http://localhost:8080` |
| `e2eSuffix` | vide |
| `customerUsername` | vide |
| `customerEmail` | vide |
| `customerPassword` | `StrongPass@2026` |
| `customerToken` | vide |
| `customerUserId` | vide |
| `complianceToken` | vide |
| `profileId` | vide |
| `accountId` | vide |
| `accountIban` | vide |
| `beneficiaryId` | vide |
| `paymentId` | vide |
| `idempotencyKey` | vide |
| `notificationId` | vide |

Headers communs:

```text
Content-Type: application/json
Accept: application/json
Accept-Language: fr
X-Correlation-Id: postman-e2e-nominal
```

## 3. Initialiser les donnees du run

Creer une requete Postman nommee:

```text
00 - Init E2E variables
```

Methode:

```text
GET {{gatewayUrl}}/actuator/health
```

Authorization:

```text
No Auth
```

Dans l'onglet `Pre-request Script`:

```javascript
const suffix = Date.now();
pm.environment.set("e2eSuffix", suffix);
pm.environment.set("customerUsername", "client.e2e." + suffix);
pm.environment.set("customerEmail", "client.e2e." + suffix + "@example.com");
pm.environment.set("customerPassword", "StrongPass@2026");
pm.environment.set("idempotencyKey", "PAY-E2E-" + suffix);
```

Dans l'onglet `Tests`:

```javascript
pm.test("Gateway health is UP", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().status).to.eql("UP");
});
```

Resultat attendu:

```json
{
  "status": "UP",
  "groups": ["liveness", "readiness"]
}
```

## 4. Login compliance

Cette etape sert a approuver le paiement plus tard.

```text
POST {{gatewayUrl}}/api/auth/login
```

Authorization:

```text
No Auth
```

Body:

```json
{
  "username": "compliance",
  "password": "password"
}
```

Tests:

```javascript
pm.test("Compliance login OK", function () {
  pm.response.to.have.status(200);
});

const body = pm.response.json();
pm.environment.set("complianceToken", body.accessToken);
```

Resultat attendu:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresAt": "date",
  "mfaRequired": true,
  "roles": ["COMPLIANCE"]
}
```

## 5. Creer un client bancaire

```text
POST {{gatewayUrl}}/api/auth/register
```

Authorization:

```text
No Auth
```

Body:

```json
{
  "username": "{{customerUsername}}",
  "email": "{{customerEmail}}",
  "password": "{{customerPassword}}"
}
```

Tests:

```javascript
pm.test("Customer created", function () {
  pm.response.to.have.status(201);
});

const body = pm.response.json();
pm.environment.set("customerUserId", body.id);
```

Resultat attendu:

```json
{
  "id": 10,
  "username": "client.e2e.XXXXXXXX",
  "email": "client.e2e.XXXXXXXX@example.com",
  "status": "ACTIVE",
  "mfaEnabled": true,
  "lastLoginAt": null,
  "roles": ["CUSTOMER"]
}
```

## 6. Login client

```text
POST {{gatewayUrl}}/api/auth/login
```

Authorization:

```text
No Auth
```

Body:

```json
{
  "username": "{{customerUsername}}",
  "password": "{{customerPassword}}"
}
```

Tests:

```javascript
pm.test("Customer login OK", function () {
  pm.response.to.have.status(200);
});

const body = pm.response.json();
pm.environment.set("customerToken", body.accessToken);
```

Resultat attendu: `200 OK` avec `accessToken`.

## 7. Creer le profil client

```text
POST {{gatewayUrl}}/api/profiles/me
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Body:

```json
{
  "firstName": "Yassine",
  "lastName": "El Amrani",
  "email": "{{customerEmail}}",
  "phone": "+212600000001",
  "dateOfBirth": "1992-04-20",
  "nationality": "MA",
  "taxResidencyCountry": "MA",
  "addresses": [
    {
      "type": "HOME",
      "line1": "15 Avenue Mohammed V",
      "line2": "Appartement 8",
      "city": "Casablanca",
      "postalCode": "20000",
      "countryCode": "MA"
    }
  ]
}
```

Tests:

```javascript
pm.test("Profile created", function () {
  pm.response.to.have.status(201);
});

const body = pm.response.json();
pm.environment.set("profileId", body.id);
```

Resultat attendu:

```json
{
  "id": 1,
  "userId": 10,
  "customerNumber": "CUST-...",
  "firstName": "Yassine",
  "lastName": "El Amrani",
  "email": "client.e2e.XXXXXXXX@example.com",
  "phone": "+212600000001",
  "dateOfBirth": "1992-04-20",
  "nationality": "MA",
  "kycStatus": "NOT_STARTED",
  "riskRating": "MEDIUM",
  "taxResidencyCountry": "MA",
  "addresses": [
    {
      "id": 1,
      "type": "HOME",
      "line1": "15 Avenue Mohammed V",
      "line2": "Appartement 8",
      "city": "Casablanca",
      "postalCode": "20000",
      "countryCode": "MA"
    }
  ],
  "consents": []
}
```

## 8. Soumettre KYC

```text
POST {{gatewayUrl}}/api/profiles/me/kyc
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Body:

```json
{
  "documentType": "PASSPORT",
  "documentReference": "MA-E2E-123456"
}
```

Tests:

```javascript
pm.test("KYC submitted", function () {
  pm.response.to.have.status(200);
});

pm.test("KYC status is pending review", function () {
  pm.expect(pm.response.json().kycStatus).to.eql("PENDING_REVIEW");
});
```

Resultat attendu:

```json
{
  "kycStatus": "PENDING_REVIEW"
}
```

Note: cet appel publie un evenement `profile-kyc-submitted`. Si Kafka et `notification-service` sont actifs, une notification peut etre creee.

## 9. Capturer le consentement client

```text
POST {{gatewayUrl}}/api/profiles/me/consents
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Body:

```json
{
  "consentType": "TERMS_AND_CONDITIONS",
  "granted": true
}
```

Tests:

```javascript
pm.test("Consent captured", function () {
  pm.response.to.have.status(200);
});

pm.test("At least one consent exists", function () {
  pm.expect(pm.response.json().consents.length).to.be.above(0);
});
```

Resultat attendu: profil retourne avec au moins un consentement.

## 10. Ouvrir un compte courant

```text
POST {{gatewayUrl}}/api/accounts
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Body:

```json
{
  "type": "CHECKING",
  "currency": "EUR",
  "openingBalance": 25000.00
}
```

Tests:

```javascript
pm.test("Account opened", function () {
  pm.response.to.have.status(201);
});

const body = pm.response.json();
pm.environment.set("accountId", body.id);
pm.environment.set("accountIban", body.iban);
```

Resultat attendu:

```json
{
  "id": 1,
  "customerId": 10,
  "iban": "FR76...",
  "type": "CHECKING",
  "status": "ACTIVE",
  "currency": "EUR",
  "ledgerBalance": 25000.0000,
  "availableBalance": 25000.0000,
  "dailyTransferLimit": 5000.0000
}
```

Note: l'ouverture avec solde initial cree aussi une transaction `CREDIT` et des evenements outbox `account-opened` / `transaction-posted`.

## 11. Verifier les comptes du client

```text
GET {{gatewayUrl}}/api/accounts
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Tests:

```javascript
pm.test("Accounts listed", function () {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json().length).to.be.above(0);
});
```

Resultat attendu: liste contenant le compte cree.

## 12. Creer un beneficiaire SEPA

```text
POST {{gatewayUrl}}/api/payments/beneficiaries
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Body:

```json
{
  "nickname": "Sarah France",
  "accountHolderName": "Sarah Martin",
  "iban": "FR7630006000011234567890189",
  "bic": "AGRIFRPP",
  "countryCode": "FR"
}
```

Tests:

```javascript
pm.test("Beneficiary created", function () {
  pm.response.to.have.status(201);
});

const body = pm.response.json();
pm.environment.set("beneficiaryId", body.id);
```

Resultat attendu:

```json
{
  "id": 1,
  "customerId": 10,
  "nickname": "Sarah France",
  "accountHolderName": "Sarah Martin",
  "iban": "FR7630006000011234567890189",
  "bic": "AGRIFRPP",
  "countryCode": "FR",
  "status": "ACTIVE"
}
```

## 13. Effectuer un virement SEPA

```text
POST {{gatewayUrl}}/api/payments
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Headers:

```text
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
  "amount": 1000.00,
  "currency": "EUR",
  "remittanceInformation": "Virement nominal E2E Postman",
  "scheduledFor": null
}
```

Tests:

```javascript
pm.test("Payment initiated", function () {
  pm.response.to.have.status(201);
});

const body = pm.response.json();
pm.environment.set("paymentId", body.id);

pm.test("Payment is awaiting MFA or approval", function () {
  pm.expect(["PENDING_MFA", "PENDING_APPROVAL"]).to.include(body.status);
});
```

Resultat attendu pour ce montant:

```json
{
  "id": 1,
  "customerId": 10,
  "sourceAccountId": 1,
  "sourceIban": "FR76...",
  "beneficiary": {
    "id": 1,
    "nickname": "Sarah France"
  },
  "type": "SEPA",
  "status": "PENDING_MFA",
  "amount": 1000.0000,
  "currency": "EUR",
  "paymentReference": "PAY-...",
  "idempotencyKey": "PAY-E2E-...",
  "remittanceInformation": "Virement nominal E2E Postman",
  "riskScore": 10.00,
  "scheduledFor": null,
  "approvedAt": null,
  "executedAt": null,
  "rejectionReason": null
}
```

Important: dans le code actuel, `payment-service` cree l'ordre de paiement et son cycle de validation. Le debit reel du solde du compte n'est pas encore integre entre `payment-service` et `account-service`.

## 14. Approuver le paiement

Role requis: `COMPLIANCE`, `ADMIN` ou `SUPPORT`.

```text
POST {{gatewayUrl}}/api/payments/admin/{{paymentId}}/approve
```

Authorization:

```text
Bearer Token: {{complianceToken}}
```

Body:

```text
aucun body
```

Tests:

```javascript
pm.test("Payment approved/executed", function () {
  pm.response.to.have.status(200);
});

pm.test("Payment status is EXECUTED or SCHEDULED", function () {
  pm.expect(["EXECUTED", "SCHEDULED"]).to.include(pm.response.json().status);
});
```

Resultat attendu:

```json
{
  "id": 1,
  "status": "EXECUTED",
  "approvedAt": "2026-06-18T...",
  "executedAt": "2026-06-18T..."
}
```

Comme `scheduledFor` vaut `null`, le statut attendu est `EXECUTED`.

## 15. Attendre la publication Kafka

Les evenements `account-service` et `payment-service` passent par outbox avec un polling configure a environ 10 secondes.

Attendre:

```text
10 a 20 secondes
```

Evenements susceptibles de generer des notifications:

```text
profile-kyc-submitted
account-opened
transaction-posted
payment-initiated
payment-executed
```

## 16. Consulter les notifications client

```text
GET {{gatewayUrl}}/api/notifications
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Tests:

```javascript
pm.test("Notifications listed", function () {
  pm.response.to.have.status(200);
});

const notifications = pm.response.json();
pm.test("At least one notification exists", function () {
  pm.expect(notifications.length).to.be.above(0);
});

if (notifications.length > 0) {
  pm.environment.set("notificationId", notifications[0].id);
}
```

Resultat attendu:

```json
[
  {
    "id": 1,
    "customerId": 10,
    "channel": "IN_APP",
    "status": "UNREAD",
    "eventType": "PAYMENT_EXECUTED",
    "title": "...",
    "message": "...",
    "generatedAt": "2026-06-18T..."
  }
]
```

Le `eventType` exact depend de l'evenement consomme.

## 17. Marquer une notification comme lue

```text
PATCH {{gatewayUrl}}/api/notifications/{{notificationId}}/read
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Tests:

```javascript
pm.test("Notification marked as read", function () {
  pm.response.to.have.status(200);
});

pm.test("Status is READ", function () {
  pm.expect(pm.response.json().status).to.eql("READ");
});
```

Resultat attendu:

```json
{
  "id": 1,
  "status": "READ"
}
```

## 18. Verifier le paiement dans la liste client

```text
GET {{gatewayUrl}}/api/payments
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Tests:

```javascript
pm.test("Payments listed", function () {
  pm.response.to.have.status(200);
});

const payments = pm.response.json();
const paymentId = Number(pm.environment.get("paymentId"));
const found = payments.find(p => p.id === paymentId);

pm.test("Created payment exists", function () {
  pm.expect(found).to.not.be.undefined;
});

pm.test("Created payment is executed", function () {
  pm.expect(found.status).to.eql("EXECUTED");
});
```

## 19. Verifier le releve du compte

```text
GET {{gatewayUrl}}/api/accounts/{{accountId}}/statement?from=2026-01-01T00:00:00Z&to=2026-12-31T23:59:59Z
```

Authorization:

```text
Bearer Token: {{customerToken}}
```

Resultat attendu:

```json
{
  "accountId": 1,
  "from": "2026-01-01T00:00:00Z",
  "to": "2026-12-31T23:59:59Z",
  "transactions": [
    {
      "type": "CREDIT",
      "amount": 25000.0000,
      "description": "Opening balance"
    }
  ]
}
```

Note importante: le virement cree dans `payment-service` n'apparaitra pas forcement comme debit dans le releve, car l'integration comptable paiement -> compte n'est pas encore implementee dans le code actuel.

## 20. Resume de l'ordre Postman

```text
00 - Init E2E variables
01 - Login compliance
02 - Register customer
03 - Login customer
04 - Create profile
05 - Submit KYC
06 - Capture consent
07 - Open account
08 - List accounts
09 - Create beneficiary
10 - Initiate SEPA payment
11 - Approve payment
12 - Wait 10/20 seconds
13 - List notifications
14 - Mark notification as read
15 - List payments
16 - Account statement
```

## 21. Si les notifications ne remontent pas

Verifier:

1. Kafka est actif sur `localhost:9092`.
2. `notification-service` est lance.
3. `payment-service` et `account-service` n'affichent pas d'erreur outbox dans les logs.
4. Attendre au moins 20 secondes apres l'approbation paiement.
5. Refaire:

```text
GET {{gatewayUrl}}/api/notifications
Authorization: Bearer {{customerToken}}
```

Si le paiement est bien `EXECUTED` mais aucune notification n'apparait, le probleme est cote Kafka/outbox/consumer, pas cote API REST du paiement.

