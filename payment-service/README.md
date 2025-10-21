# Payment Service

Payment processing service for course purchases using Stripe integration.

## Features

- **Stripe Integration**: Secure payment processing
- **Multiple Payment Methods**: Credit Card, PayPal, Bank Transfer
- **Payment History**: Track all transactions
- **Refund Management**: Process full and partial refunds
- **Invoice Generation**: Automatic invoice creation
- **Payment Status Tracking**: Pending, Completed, Failed, Refunded
- **User Payment History**: View all payments by user
- **Course Payment History**: Track payments for specific courses

## Tech Stack

- Spring Boot 3.3.4
- Spring Data JPA
- PostgreSQL
- Stripe Java SDK
- Eureka Client
- Lombok
- Jakarta Validation

## Configuration

### Stripe API Keys

Get your Stripe API keys from: https://dashboard.stripe.com/apikeys

Update `application.properties`:

```properties
stripe.api.key=sk_test_your_secret_key_here
stripe.publishable.key=pk_test_your_publishable_key_here
```

**Note:** Use test keys for development, live keys for production.

### Database Setup

Create PostgreSQL database:

```sql
CREATE DATABASE payment_service_db;
```

Tables will be auto-created by Hibernate.

## API Endpoints

### Payment Endpoints

#### Create Payment (Process Payment)
```http
POST /api/payments
Content-Type: application/json

{
  "userId": 1,
  "courseId": 1,
  "amount": 99.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "stripeToken": "tok_visa"
}
```

**Response:** 201 Created
```json
{
  "id": 1,
  "userId": 1,
  "courseId": 1,
  "amount": 99.99,
  "currency": "USD",
  "status": "COMPLETED",
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "ch_3ABC123...",
  "invoiceNumber": "INV-2025-001",
  "paymentDate": "2025-10-21T14:30:00"
}
```

#### Get Payment by ID
```http
GET /api/payments/{id}
```

#### Get All Payments
```http
GET /api/payments
```

#### Get Payments by User
```http
GET /api/payments/user/{userId}
```

#### Get Payments by Course
```http
GET /api/payments/course/{courseId}
```

#### Get Payments by Status
```http
GET /api/payments/status/{status}
```
Status values: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

#### Process Refund
```http
POST /api/payments/{id}/refund
Content-Type: application/json

{
  "reason": "Student requested refund within 30-day policy",
  "amount": 99.99
}
```

**Full Refund:** Omit `amount` or use same amount as original payment  
**Partial Refund:** Specify `amount` less than original

#### Get User Payment Statistics
```http
GET /api/payments/user/{userId}/total
```

**Response:**
```json
{
  "totalSpent": 299.97,
  "paymentCount": 3,
  "lastPaymentDate": "2025-10-21T14:30:00"
}
```

---

## Database Schema

### payments Table
```sql
- id (PK)
- user_id (FK to User Service)
- course_id (FK to Course Service)
- amount (DECIMAL)
- currency (VARCHAR - USD, EUR, etc.)
- status (ENUM: PENDING, COMPLETED, FAILED, REFUNDED)
- payment_method (ENUM: CREDIT_CARD, PAYPAL, BANK_TRANSFER)
- transaction_id (Stripe charge ID)
- stripe_customer_id (Stripe customer ID)
- invoice_number (Unique: INV-2025-001)
- refund_id (Stripe refund ID if refunded)
- refund_amount (Amount refunded)
- refund_reason (Why refunded)
- payment_date
- refund_date
- created_at
- updated_at
```

## Payment Flow

### 1. Checkout Flow (Frontend → Backend)

```
Student clicks "Enroll" on course
    ↓
Frontend collects payment info (Stripe.js)
    ↓
Stripe generates token (tok_visa)
    ↓
Frontend sends token + courseId to Payment Service
    ↓
Payment Service processes payment with Stripe
    ↓
Payment Service saves payment record
    ↓
Payment Service calls Enrollment Service (auto-enroll student)
    ↓
Return success + invoice
```

### 2. Stripe Integration Flow

```java
// In PaymentService.java
Stripe.apiKey = stripeApiKey;

// Create charge
ChargeCreateParams params = ChargeCreateParams.builder()
    .setAmount(amountInCents)
    .setCurrency(currency.toLowerCase())
    .setSource(stripeToken)
    .setDescription("Course: " + courseId)
    .build();

Charge charge = Charge.create(params);
```

### 3. Refund Flow

```
Instructor/Admin initiates refund
    ↓
Payment Service validates payment exists and is COMPLETED
    ↓
Call Stripe API to process refund
    ↓
Update payment status to REFUNDED
    ↓
Call Enrollment Service to revoke access
    ↓
Send refund confirmation email (Notification Service)
```

## Building and Running

### Build
```bash
cd payment-service
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

The service will start on **port 8087**.

## Testing with Postman

### Test Payment (Stripe Test Mode)

Use Stripe test cards: https://stripe.com/docs/testing

**Test Card Numbers:**
- **Success:** `4242 4242 4242 4242`
- **Decline:** `4000 0000 0000 0002`
- **Insufficient Funds:** `4000 0000 0000 9995`

**Test Token:**
```bash
# Use Stripe CLI to generate test token
stripe tokens create --card-number=4242424242424242 --card-exp-month=12 --card-exp-year=2026 --card-cvc=123
```

Or use Stripe.js in frontend to generate tokens.

### 1. Create Payment
```http
POST http://localhost:8087/api/payments
Content-Type: application/json

{
  "userId": 1,
  "courseId": 1,
  "amount": 99.99,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "stripeToken": "tok_visa"
}
```

### 2. Get User Payments
```http
GET http://localhost:8087/api/payments/user/1
```

### 3. Process Refund
```http
POST http://localhost:8087/api/payments/1/refund
Content-Type: application/json

{
  "reason": "Course cancelled",
  "amount": 99.99
}
```

## Integration with Other Services

### Course Service
- Fetch course price: `GET /api/courses/{id}`
- Verify course exists before payment

### Enrollment Service
- Auto-enroll on successful payment: `POST /api/enrollments`
- Revoke enrollment on refund: `DELETE /api/enrollments/{id}`

### User Service
- Fetch user email for invoice: `GET /api/users/{id}`
- Verify user exists

### Notification Service
- Send payment confirmation email
- Send refund confirmation email
- Send invoice via email

## Stripe Webhook (Future Enhancement)

For production, set up webhook endpoint to receive payment events:

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleStripeWebhook(
    @RequestBody String payload,
    @RequestHeader("Stripe-Signature") String signature) {
    
    Event event = Webhook.constructEvent(payload, signature, webhookSecret);
    
    switch (event.getType()) {
        case "charge.succeeded":
            // Update payment status to COMPLETED
            break;
        case "charge.failed":
            // Update payment status to FAILED
            break;
        case "charge.refunded":
            // Update payment status to REFUNDED
            break;
    }
    
    return ResponseEntity.ok("Received");
}
```

## Security Considerations

1. **Never store card details**: Use Stripe tokens
2. **HTTPS only**: Always use SSL in production
3. **API key security**: Store in environment variables, not code
4. **Idempotency**: Prevent duplicate charges
5. **Validate amounts**: Ensure price matches course price
6. **Audit trail**: Log all payment attempts
7. **PCI compliance**: Use Stripe's hosted payment page

## Error Handling

Common errors:

| Error | Reason | Solution |
|-------|--------|----------|
| `card_declined` | Card declined by bank | Try different card |
| `insufficient_funds` | Not enough balance | Add funds or try another card |
| `expired_card` | Card expired | Use valid card |
| `incorrect_cvc` | Wrong CVV | Check CVV code |
| `processing_error` | Stripe processing issue | Retry after few minutes |

## Invoice Number Generation

Format: `INV-{YEAR}-{SEQUENCE}`

Example: `INV-2025-001`, `INV-2025-002`

```java
private String generateInvoiceNumber() {
    String year = String.valueOf(LocalDateTime.now().getYear());
    Long count = paymentRepository.countByYear(year);
    return String.format("INV-%s-%03d", year, count + 1);
}
```

## Business Logic

### Payment Validation
- Verify course exists and has price
- Verify user exists
- Check if user already enrolled (prevent duplicate payments)
- Validate amount matches course price

### Refund Rules
- Full refund: Within 30 days of purchase
- Partial refund: Case-by-case basis
- No refund: After course completion or > 30 days

### Currency Support
- Primary: USD
- Supported: EUR, GBP, CAD, AUD
- Stripe automatically handles currency conversion

## Future Enhancements

- [ ] Subscription payments (monthly courses)
- [ ] Payment plans (installments)
- [ ] Coupon codes and discounts
- [ ] Gift cards
- [ ] Multiple payment gateways (PayPal, Square)
- [ ] Cryptocurrency payments
- [ ] Automatic retry for failed payments
- [ ] Payment reminders
- [ ] Revenue analytics dashboard
- [ ] Tax calculation and invoicing

## Port Information

- **Payment Service**: 8087
- **Eureka Server**: 8761
- **API Gateway**: 8080

## Environment Variables

```bash
STRIPE_API_KEY=sk_test_your_secret_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_publishable_key
DB_URL=jdbc:postgresql://localhost:5432/payment_service_db
DB_USERNAME=postgres
DB_PASSWORD=123456
```

## License

Proprietary - All Rights Reserved
