# Banking Transaction API Documentation

## Overview

This is a comprehensive banking transaction feature for the web application with balance validation, transfer processing, and digital receipt generation.

## Base URL

```
http://localhost:8080
```

## Authentication

All transaction endpoints require HTTP Basic Authentication.

**Test Users:**

- Username: `john_doe` | Password: `password123` | Balance: 50,000.00
- Username: `jane_smith` | Password: `password123` | Balance: 25,000.00

## API Endpoints

### 1. Get User Balance

Retrieve the current user's account balance (called automatically when user logs in).

**Endpoint:** `GET /api/transactions/balance`

**Headers:**

```
Authorization: Basic <base64-encoded-credentials>
```

**Response:** 200 OK

```json
{
  "accountNumber": "1234567890",
  "accountName": "John Doe",
  "balance": 50000.0,
  "sufficientBalance": true,
  "message": null
}
```

---

### 2. Check Balance Sufficiency

Validate if user has sufficient balance for a specific transfer amount.

**Endpoint:** `GET /api/transactions/balance/check?amount=5000`

**Headers:**

```
Authorization: Basic <base64-encoded-credentials>
```

**Query Parameters:**

- `amount` (required): Transfer amount to validate

**Response (Sufficient):** 200 OK

```json
{
  "accountNumber": "1234567890",
  "accountName": "John Doe",
  "balance": 50000.0,
  "sufficientBalance": true,
  "message": "Sufficient balance available"
}
```

**Response (Insufficient):** 200 OK

```json
{
  "accountNumber": "1234567890",
  "accountName": "John Doe",
  "balance": 500.0,
  "sufficientBalance": false,
  "message": "Insufficient balance. Current balance: 500.00"
}
```

---

### 3. Get Available Banks

Retrieve list of banks for dropdown selection.

**Endpoint:** `GET /api/transactions/banks`

**Headers:**

```
Authorization: Basic <base64-encoded-credentials>
```

**Response:** 200 OK

```json
[
  "NOVA_BANK",
  "STATE_BANK",
  "CITY_BANK",
  "NATIONAL_BANK",
  "FEDERAL_BANK",
  "METRO_BANK",
  "REGIONAL_BANK",
  "COMMERCIAL_BANK",
  "PEOPLE_BANK",
  "TRUST_BANK"
]
```

---

### 4. Process Bank Transfer

Submit a transfer request with validation and transaction recording.

**Endpoint:** `POST /api/transactions/transfer`

**Headers:**

```
Authorization: Basic <base64-encoded-credentials>
Content-Type: application/json
```

**Request Body:**

```json
{
  "amount": 5000.0,
  "recipientAccountNumber": "9876543210",
  "recipientAccountName": "Dilara S",
  "recipientBank": "STATE_BANK",
  "description": "Payment for services"
}
```

**Validation Rules:**

- `amount`: Required, must be > 0, max 15 digits with 2 decimal places
- `recipientAccountNumber`: Required, 10-16 digits
- `recipientAccountName`: Required, 2-100 characters
- `recipientBank`: Required, must be from available banks
- `description`: Required, max 500 characters

**Response (Success):** 201 Created

```json
{
  "transactionId": "TXN17326490001234",
  "timestamp": "2025-11-28T10:30:00",
  "senderAccountNumber": "1234567890",
  "senderAccountName": "John Doe",
  "amountTransferred": 5000.0,
  "recipientAccountNumber": "9876543210",
  "recipientAccountName": "Dilara S",
  "recipientBank": "STATE_BANK",
  "description": "Payment for services",
  "remainingBalance": 45000.0,
  "status": "SUCCESS",
  "message": "Transaction completed successfully"
}
```

**Response (Insufficient Balance):** 400 Bad Request

```json
{
  "status": 400,
  "message": "Insufficient balance. Current balance: 500.00, Required: 5000.00",
  "timestamp": "2025-11-28T10:30:00"
}
```

**Response (Validation Error):** 400 Bad Request

```json
{
  "status": 400,
  "errors": {
    "amount": "Amount is required",
    "recipientAccountNumber": "Account number must be 10-16 digits"
  },
  "timestamp": "2025-11-28T10:30:00"
}
```

---

### 5. Get Transaction Receipt

Retrieve transaction details by transaction ID.

**Endpoint:** `GET /api/transactions/receipt/{transactionId}`

**Headers:**

```
Authorization: Basic <base64-encoded-credentials>
```

**Path Parameters:**

- `transactionId`: The unique transaction identifier

**Response:** 200 OK

```json
{
  "transactionId": "TXN17326490001234",
  "timestamp": "2025-11-28T10:30:00",
  "senderAccountNumber": "1234567890",
  "senderAccountName": "John Doe",
  "amountTransferred": 5000.0,
  "recipientAccountNumber": "9876543210",
  "recipientAccountName": "Dilara S",
  "recipientBank": "STATE_BANK",
  "description": "Payment for services",
  "remainingBalance": 45000.0,
  "status": "SUCCESS",
  "message": "Transaction details retrieved"
}
```

---

## Testing with cURL

### 1. Get Balance

```bash
curl -X GET http://localhost:8080/api/transactions/balance \
  -u john_doe:password123
```

### 2. Check Balance

```bash
curl -X GET "http://localhost:8080/api/transactions/balance/check?amount=5000" \
  -u john_doe:password123
```

### 3. Get Banks

```bash
curl -X GET http://localhost:8080/api/transactions/banks \
  -u john_doe:password123
```

### 4. Process Transfer

```bash
curl -X POST http://localhost:8080/api/transactions/transfer \
  -u john_doe:password123 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000.00,
    "recipientAccountNumber": "9876543210",
    "recipientAccountName": "Dilara S",
    "recipientBank": "STATE_BANK",
    "description": "Payment for services"
  }'
```

### 5. Get Receipt

```bash
curl -X GET http://localhost:8080/api/transactions/receipt/TXN17326490001234 \
  -u john_doe:password123
```

---

## Testing with Postman

1. **Set Authorization:**

   - Type: Basic Auth
   - Username: `john_doe`
   - Password: `password123`

2. **Get Balance:**

   - Method: GET
   - URL: `http://localhost:8080/api/transactions/balance`

3. **Process Transfer:**
   - Method: POST
   - URL: `http://localhost:8080/api/transactions/transfer`
   - Body (raw JSON):
   ```json
   {
     "amount": 5000.0,
     "recipientAccountNumber": "9876543210",
     "recipientAccountName": "Dilara S",
     "recipientBank": "STATE_BANK",
     "description": "Payment for services"
   }
   ```

---

## Frontend Integration Flow

### 1. On Page Load (Bank Transfer Page)

```javascript
// Automatically fetch user balance
GET / api / transactions / balance;
// Display balance on the page
```

### 2. On Amount Input Change

```javascript
// Validate balance as user types
GET /api/transactions/balance/check?amount=${enteredAmount}
// Enable/disable transfer button based on sufficientBalance flag
// Show insufficient funds message if needed
```

### 3. On Form Submission

```javascript
// Submit transfer request
POST / api / transactions / transfer;
// Body: {amount, recipientAccountNumber, recipientAccountName, recipientBank, description}
// On success, display digital receipt with all transaction details
```

---

## Database Configuration

The application uses MySQL database. Update credentials in `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bankapp_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
```

---

## Running the Application

1. **Ensure MySQL is running** on localhost:3306

2. **Build the application:**

   ```bash
   mvn clean install
   ```

3. **Run the application:**

   ```bash
   mvn spring-boot:run
   ```

4. **Access the API:**
   ```
   http://localhost:8080/api/transactions/balance
   ```

---

## Error Handling

All errors return consistent JSON responses:

```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2025-11-28T10:30:00"
}
```

**Common HTTP Status Codes:**

- `200 OK`: Successful request
- `201 Created`: Transaction created successfully
- `400 Bad Request`: Validation error or insufficient balance
- `401 Unauthorized`: Missing or invalid credentials
- `404 Not Found`: User or transaction not found
- `500 Internal Server Error`: Unexpected server error

---

## Features Implemented

✅ Automatic balance retrieval on login  
✅ Balance validation logic  
✅ Enable/disable transfer based on balance  
✅ Insufficient funds message display  
✅ Complete transfer form with all required fields  
✅ Input validation for all fields  
✅ Transaction recording in database  
✅ Digital receipt generation  
✅ Receipt includes: timestamp, transaction ID, amount, recipient info, remaining balance  
✅ Exception handling with appropriate messages  
✅ RESTful API design  
✅ Spring Security authentication

---

## Next Steps

1. **Connect Frontend:** Integrate these APIs with your web application
2. **Customize Banks:** Modify the `BankName` enum to match your bank list
3. **Production Setup:** Replace in-memory authentication with database-backed user management
4. **Add Features:** Implement transaction history, export receipts, email notifications
