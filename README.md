# Tier-Based Retail Loyalty Points Engine

`LoyaltyPoints` is a simple Spring Boot REST API for a retail loyalty program. Customers earn points from purchases, receive discounts based on their tier, and can redeem available points.

## Features

- Customer creation and lookup
- Purchase discounts based on the current tier
- Configurable points-per-rupee setting
- Earned and redeemed points ledger
- Silver, Gold, and Platinum tiers
- Validation and simple JSON error responses
- Swagger UI for testing the API

## Technology

Java 17, Spring Boot 3.2, Maven, Spring Web, Spring Data JPA, Hibernate, MySQL, Jakarta Validation, SpringDoc OpenAPI, JUnit 5, and Mockito.

## Database setup

Create the database in MySQL:

```sql
CREATE DATABASE loyalty_points_db;
```

Open `src/main/resources/application.properties` and replace `YOUR_PASSWORD` with the password for your local MySQL `root` user. Do not commit a real password.

The application creates the default tiers and `POINTS_PER_RUPEE` setting on startup if they do not already exist.

## Run the project

Make sure MySQL is running, then run:

```bash
mvn clean test
mvn spring-boot:run
```

The server runs at `http://localhost:8080`.

## Swagger

Open `http://localhost:8080/swagger-ui.html`.

## REST endpoints

- `POST /api/customers` - create a customer
- `GET /api/customers` - list customers
- `GET /api/customers/{id}` - get one customer
- `POST /api/purchases/{customerId}` - create a purchase and earn points
- `POST /api/points/redeem/{customerId}` - redeem points
- `GET /api/points/{customerId}` - get current and lifetime points
- `GET /api/points/{customerId}/history` - get the point ledger
- `GET /api/tiers` - list tiers
- `POST /api/tiers` - create a tier
- `PUT /api/tiers/{id}` - update a tier

## Example requests

Create a customer:

```json
{
  "name": "Arun Kumar",
  "email": "arun@gmail.com"
}
```

Create a purchase:

```json
{
  "purchaseAmount": 1000
}
```

Redeem points:

```json
{
  "points": 300
}
```

## Points and tiers

With the default setting, one rupee earns one point. A purchase of 1000 earns 1000 points. The purchase discount uses the customer's tier before that purchase's points are added.

- Silver: 0-499 lifetime points, 5% discount
- Gold: 500-999 lifetime points, 10% discount
- Platinum: 1000+ lifetime points, 15% discount

Tier is based on lifetime points. Redemption reduces available points only, so a customer who reaches Platinum never downgrades after redeeming points.

## Tests

Run the three service tests with:

```bash
mvn clean test
```
