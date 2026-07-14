# Electronics Shop API

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-blue.svg)](https://spring.io/projects/spring-security)
[![MySQL](https://img.shields.io/badge/MySQL-8-blue.svg)](https://www.mysql.com/)
[![MapStruct](https://img.shields.io/badge/MapStruct-1.6.3-lightgrey.svg)](https://mapstruct.org/)
[![Swagger](https://img.shields.io/badge/Docs-Swagger%2FOpenAPI-85EA2D.svg)](https://swagger.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**A REST API backend for an online electronics components shop, built with Spring Boot.**

## About The Project

A Spring Boot REST API for an electronics e-commerce platform featuring JWT authentication, product catalog management, order processing, and role-based authorization. Built as a portfolio project focused on clean architecture and backend best practices.

## Features

- **Authentication & Authorization** — JWT authentication with role-based authorization
- **Product Catalog** — CRUD for products and categories with filtering and pagination
- **Order Management** — stock validation, price snapshotting, and controlled status transitions
- **Ownership-based Access Control** — customers can access only their own orders, while admins have full access
- **Centralized Exception Handling** — consistent JSON error responses
- **Bean Validation** — request validation
- **API Documentation** — interactive Swagger UI with built-in JWT bearer token support
- **Unit Tests** — Mockito-based unit test coverage for core service logic (orders, auth, products, categories, JWT)

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4
- **Security:** Spring Security, JWT (jjwt)
- **Persistence:** Spring Data JPA, MySQL
- **Mapping:** MapStruct
- **Documentation:** springdoc-openapi (Swagger UI)
- **Testing:** JUnit 5, Mockito, AssertJ
- **Build Tool:** Maven

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven
- MySQL Server

### Installation

1. Clone the repository:

```bash
git clone https://github.com/TheNa3ik/electronics-shop-api.git
cd electronics-shop-api
```

2. Rename `application.properties.example` to `application.properties` and configure your database credentials and JWT secret.

```properties
spring.application.name=electronics-shop-api

# Database Connection
spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DB_NAME
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

# JWT Configuration — use a random string at least 256 bits (32+ characters) long
jwt.secret=YOUR_JWT_SECRET
jwt.expiration-ms=86400000

spring.jpa.hibernate.ddl-auto=update
```

3. Build and run the application:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

Authenticate via `/api/v1/auth/login` or `/api/v1/auth/register`, copy the JWT, and authorize in Swagger UI.

### Key Endpoints

| Method | Endpoint                          | Description                          | Access         |
|--------|------------------------------------|---------------------------------------|----------------|
| POST   | `/api/v1/auth/register`           | Register a new user                   | Public         |
| POST   | `/api/v1/auth/login`              | Log in and receive a JWT              | Public         |
| GET    | `/api/v1/products`                | List products (paginated, filterable) | Public         |
| POST   | `/api/v1/products`                | Create a product                      | Admin          |
| GET    | `/api/v1/categories`              | List categories (paginated)           | Public         |
| POST   | `/api/v1/categories`              | Create a category                     | Admin          |
| POST   | `/api/v1/orders`                  | Place an order                        | Authenticated  |
| GET    | `/api/v1/orders/my`               | View your own orders                  | Authenticated  |
| GET    | `/api/v1/orders/{orderId}`        | View a specific order                 | Owner or Admin |
| GET    | `/api/v1/orders`                  | List all orders (filterable)          | Admin          |
| PATCH  | `/api/v1/orders/{orderId}/status` | Update order status                   | Admin          |

Full request/response schemas and all remaining endpoints are available in Swagger UI.

## Running Tests

```bash
./mvnw test
```

## What I Learned

This project was built as a hands-on way to learn backend fundamentals beyond basic CRUD:

- Designing a layered Spring Boot architecture
- Implementing JWT authentication and role-based authorization
- Handling business logic that spans multiple entities (stock decrement, price snapshotting, ownership checks)
- Writing centralized exception handling for consistent API error responses
- Unit testing service logic with Mockito, including mocking Spring Security context

## License

Distributed under the MIT License. See `LICENSE` for more information.