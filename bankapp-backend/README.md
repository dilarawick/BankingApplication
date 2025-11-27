# Banking Application Backend

A comprehensive Spring Boot banking application with transaction management, balance validation, and digital receipt generation.

## Features

✅ **Automatic Balance Retrieval** - Fetch user balance on login  
✅ **Balance Validation** - Check sufficient funds before transfer  
✅ **Smart Transfer Controls** - Enable/disable transfer button based on balance  
✅ **Transaction Processing** - Complete bank transfer with validation  
✅ **Digital Receipts** - Auto-generate detailed transaction receipts  
✅ **Input Validation** - Comprehensive validation for all fields  
✅ **Exception Handling** - User-friendly error messages  
✅ **RESTful API** - Clean and well-documented endpoints  
✅ **Security** - HTTP Basic Authentication with Spring Security

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Data JPA**
- **Spring Security**
- **MySQL Database**
- **Maven**

## Prerequisites

- Java 21 or higher
- MySQL 8.0 or higher
- Maven 3.x (or use included Maven wrapper)

## Database Setup

1. **Install MySQL** if not already installed

2. **Create Database** (optional - auto-created by application):

   ```sql
   CREATE DATABASE bankapp_db;
   ```

3. **Configure Database Connection** in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/bankapp_db
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

## Installation & Running

### 1. Clone the Repository

```bash
cd c:\Users\h2005\OneDrive\Documents\GitHub\BankingApplication\bankapp-backend
```

### 2. Update Database Configuration

Edit `src/main/resources/application.properties` with your MySQL credentials.

### 3. Build the Application

```bash
# Using Maven Wrapper (recommended)
.\mvnw.cmd clean install

# OR using Maven if installed
mvn clean install
```

### 4. Run the Application

```bash
# Using Maven Wrapper
.\mvnw.cmd spring-boot:run

# OR using Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Test Users

The application automatically creates test users on first run:

| Username   | Password    | Account Number | Balance   |
| ---------- | ----------- | -------------- | --------- |
| john_doe   | password123 | 1234567890     | 50,000.00 |
| jane_smith | password123 | 9876543210     | 25,000.00 |

## API Endpoints

### Authentication Required

All endpoints require HTTP Basic Authentication.

### Available Endpoints

#### 1. Get Balance

```http
GET /api/transactions/balance
Authorization: Basic {credentials}
```

#### 2. Check Balance Sufficiency

```http
GET /api/transactions/balance/check?amount=5000
Authorization: Basic {credentials}
```

#### 3. Get Available Banks

```http
GET /api/transactions/banks
Authorization: Basic {credentials}
```

#### 4. Process Transfer

```http
POST /api/transactions/transfer
Authorization: Basic {credentials}
Content-Type: application/json

{
  "amount": 5000.00,
  "recipientAccountNumber": "9876543210",
  "recipientAccountName": "Dilara S",
  "recipientBank": "STATE_BANK",
  "description": "Payment for services"
}
```

#### 5. Get Transaction Receipt

```http
GET /api/transactions/receipt/{transactionId}
Authorization: Basic {credentials}
```

## Quick Test with cURL

```bash
# Get Balance
curl -X GET http://localhost:8080/api/transactions/balance -u john_doe:password123

# Process Transfer
curl -X POST http://localhost:8080/api/transactions/transfer \
  -u john_doe:password123 \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "recipientAccountNumber": "9876543210",
    "recipientAccountName": "Jane Smith",
    "recipientBank": "NOVA_BANK",
    "description": "Test transfer"
  }'
```

## Project Structure

```
src/main/java/com/bankapp/bankapp_backend/
├── config/
│   ├── DataInitializer.java         # Database initialization
│   └── SecurityConfig.java          # Security configuration
├── controller/
│   └── TransactionController.java   # REST API endpoints
├── dto/
│   ├── BalanceResponse.java         # Balance response DTO
│   ├── TransactionReceipt.java      # Receipt DTO
│   └── TransferRequest.java         # Transfer request DTO
├── entity/
│   ├── Transaction.java             # Transaction entity
│   └── User.java                    # User entity
├── enums/
│   └── BankName.java                # Bank enumeration
├── exception/
│   ├── GlobalExceptionHandler.java  # Global exception handling
│   ├── InsufficientBalanceException.java
│   ├── TransactionFailedException.java
│   └── UserNotFoundException.java
├── repository/
│   ├── TransactionRepository.java   # Transaction data access
│   └── UserRepository.java          # User data access
├── service/
│   └── TransactionService.java      # Business logic
└── BankappBackendApplication.java   # Main application
```

## Validation Rules

### Transfer Request Validation

- **Amount**: Required, must be > 0, max 15 digits with 2 decimal places
- **Recipient Account Number**: Required, 10-16 digits
- **Recipient Account Name**: Required, 2-100 characters
- **Recipient Bank**: Required, must be from available banks
- **Description**: Required, max 500 characters

### Balance Validation

- Transfer amount must not exceed available balance
- Balance check performed before transaction processing

## Error Handling

The application provides comprehensive error handling:

- **400 Bad Request**: Validation errors or insufficient balance
- **401 Unauthorized**: Missing or invalid credentials
- **404 Not Found**: User or transaction not found
- **500 Internal Server Error**: Unexpected server errors

Example error response:

```json
{
  "status": 400,
  "message": "Insufficient balance. Current balance: 500.00, Required: 5000.00",
  "timestamp": "2025-11-28T10:30:00"
}
```

## Frontend Integration

See `API_DOCUMENTATION.md` for detailed frontend integration guide including:

- Page load balance retrieval
- Real-time balance validation
- Form submission handling
- Digital receipt display

## Database Schema

### Users Table

- id (Primary Key)
- username (Unique)
- password
- account_number (Unique)
- account_name
- balance
- email
- created_at
- updated_at

### Transactions Table

- id (Primary Key)
- transaction_id (Unique)
- sender_id (Foreign Key to Users)
- sender_account_number
- sender_account_name
- amount
- recipient_account_number
- recipient_account_name
- recipient_bank
- description
- status
- balance_after_transaction
- transaction_date
- created_at

## Available Banks

- NOVA_BANK
- STATE_BANK
- CITY_BANK
- NATIONAL_BANK
- FEDERAL_BANK
- METRO_BANK
- REGIONAL_BANK
- COMMERCIAL_BANK
- PEOPLE_BANK
- TRUST_BANK

## Security Features

- HTTP Basic Authentication
- BCrypt password encoding
- CSRF protection (disabled for API)
- Role-based access control (USER role)

## Development Notes

### Logging

Application includes detailed logging for:

- Transaction processing
- Balance validation
- Error tracking

Check console output or configure logging in `application.properties`.

### Transaction Safety

All transfer operations are wrapped in database transactions (`@Transactional`) to ensure data consistency.

## Next Steps

1. **Connect Frontend**: Integrate the REST APIs with your web application
2. **Customize Banks**: Modify `BankName` enum to match your requirements
3. **Production Setup**:
   - Implement database-backed user authentication
   - Add JWT token-based authentication
   - Configure proper CORS policies
   - Set up production database
4. **Additional Features**:
   - Transaction history
   - PDF receipt export
   - Email notifications
   - Transfer limits
   - Transaction cancellation
   - Account statements

## Troubleshooting

### MySQL Connection Issues

- Ensure MySQL is running: `mysql -u root -p`
- Verify database credentials in `application.properties`
- Check if port 3306 is available

### Build Issues

```bash
# Clean and rebuild
.\mvnw.cmd clean install -U
```

### Port Already in Use

Change server port in `application.properties`:

```properties
server.port=8081
```

## License

This project is part of the Banking Application system.

## Support

For detailed API documentation, see `API_DOCUMENTATION.md`.

---

**Built with ❤️ using Spring Boot**
