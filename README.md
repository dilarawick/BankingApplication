#  Banking Web Application

A **banking web application** built with **Java Spring Boot (Maven)** for the backend and **plain HTML, CSS, and JavaScript** for the frontend.

---

##  Overview

The project consists of two main parts:

1. **Backend** – Built with Spring Boot using Java and Maven (packaged as JAR)
2. **Frontend** – Built using plain HTML, CSS, and JavaScript

The backend handles:
- User authentication (with password hashing)
- Money transfers & account management
- Database CRUD operations via REST APIs

The frontend is a lightweight static interface that communicates with the backend via HTTP (AJAX/Fetch).

---

##  Tech Stack

| Layer | Technology Used |
|--------|------------------|
| **Backend Framework** | Spring Boot 3.5.7 |
| **Language** | Java 17+ |
| **Build Tool** | Maven |
| **Packaging Type** | JAR |
| **Configuration Format** | `.properties` |
| **Database** | MySQL |
| **ORM** | Spring Data JPA |
| **Frontend** | HTML, CSS, JavaScript |
| **Password Hashing** | BCrypt (Spring Security Crypto) |

---

##  Spring Initializr Configuration

Generated from [https://start.spring.io](https://start.spring.io) with the following options:

| Setting | Value |
|----------|--------|
| Project | Maven |
| Language | Java |
| Spring Boot Version | 3.5.7 |
| Packaging | JAR |
| Java Version | 21 |
| Group | `com.bankapp` |
| Artifact | `bankapp-backend` |

### Dependencies Selected:
- **Spring Web**
- **Spring Data JPA**
- **MySQL Driver**
- **Spring Boot DevTools**
- **Validation**
- **Spring Security**

---

## 📁 Folder Structure

```
BankingApplication/
├── bankapp-backend/                     ← Spring Boot backend
│   ├── src/main/
│   │       ├── java/com/bankapp/...     ← Java packages (controller, service, etc.)
│   │       └── resources/
│   │           ├── application.properties
│   │           ├── templates/
│   │           └── static/              ← Frontend (static files)
│   │               ├── index.html
│   │               ├── js/
│   │               │   └── main.js
│   │               ├── css/
│   │               │   └── style.css
│   │               └── img/
│   │                   └── (logos, icons, etc.)
│   │
│   ├── database/
│   │   ├── schema.sql                   ← Database schema
│   │   └── seed.sql                     ← Initial seed data (dummy records)
│   ├── pom.xml
│   └── target/                          ← Compiled JAR and build output
│
└── README.md
```

---
##  Backend Configuration (`application.properties`)

Located at:  
`bankapp-backend/src/main/resources/application.properties`
```
# MySQL
spring.datasource.url=jdbc:mysql://localhost:YOUR_MYSQL_PORT/YOUR_DATABASE_NAME?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# Mail (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=novabanking.noreply@gmail.com
spring.mail.password=mrdzqlqrmdqkrlof
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Server port
server.port=8080
```

### What Contributors Must Update:
| Property | Description |
|----------|--------|
| spring.datasource.url | Your local MySQL port |
| spring.datasource.url | Your database name |
| spring.datasource.username | Your local MySQL username |
| spring.datasource.password | Your MySQL password |

---

## Running the Application

The application will run at:  
`http://localhost:8080/login.html`