<<<<<<< HEAD
<<<<<<< HEAD
# 🚗 DriveEasy — Car Rental Management System

A full-stack Spring Boot car rental platform with separate, role-secured dashboards for
**customers** and **admins**, an end-to-end booking → approval → payment → receipt flow,
and email-based account verification.

content-ambition-production-8c81.up.railway.app

## Features

**Authentication & Security**
- Spring Security form login, BCrypt password hashing
- Role-based access control (`USER` vs `ADMIN`)
- Email verification on signup, forgot-password via OTP
- Session-based logout

**User (Customer) Portal**
- Browse/search/filter available cars
- Book a car for a date range with live price estimate
- Track booking status (Pending → Approved/Rejected → Completed)
- Pay for approved bookings through a simulated payment checkout (Card / UPI / Net
  Banking / Cash)
- Downloadable/printable payment receipts
- Profile management, password change

**Admin Portal**
- Live dashboard: total users, cars, bookings, revenue
- Full car inventory CRUD with image upload
- Approve / reject / complete bookings
- User management: view, enable/disable, delete
- Payment records & receipts
- Admin profile & password management

## Tech Stack

- Java, Spring Boot, Spring Security, Spring Data JPA (Hibernate)
- MySQL
- Thymeleaf (server-rendered UI)
- Maven

## Getting Started Locally

### Prerequisites
- JDK 21+
- Maven (or use the included `mvnw` wrapper)
- MySQL Server running locally

### 1. Clone
```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

### 2. Configure environment variables
The app reads all sensitive config from environment variables (see
`src/main/resources/application.properties`). Set at least:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password
export MAIL_USERNAME=your_gmail_address
export MAIL_PASSWORD=your_gmail_app_password
```

(MySQL will auto-create the `car_rental_system` database on first run.)

### 3. Run
```bash
./mvnw spring-boot:run
```
The app starts on `http://localhost:8080`.

### 4. Default admin account
A default administrator is seeded automatically on first startup:

```
email:    admin@driveeasy.com
password: Admin@123
```
**Change this password immediately after first login** (or override it before first run
via `ADMIN_EMAIL` / `ADMIN_PASSWORD` env vars).

## Screenshots

_Add a few screenshots or a short screen recording GIF here (login page, user dashboard,
admin dashboard, booking flow, payment checkout, receipt) — this is what most reviewers
will actually look at if they can't run the project themselves._

## Project Structure

```
src/main/java/com/carrental/
 ├── config/         # Static resource + startup seeding config
 ├── controller/      # MVC controllers (auth, admin, user, home)
 ├── entity/          # JPA entities (User, Car, Booking, Payment, ...)
 ├── repository/       # Spring Data JPA repositories
 ├── security/         # Spring Security configuration
 ├── service/          # Service interfaces
 └── serviceImpl/       # Service implementations
src/main/resources/
 ├── templates/         # Thymeleaf views (auth, admin, user)
 └── application.properties
```

## Notes

- CSRF protection is currently disabled to keep the hand-built forms simple; see the
  comment in `SecurityConfig.java` for how to re-enable it for a hardened production
  deployment.
- The payment flow is a **simulated** checkout (no real card/UPI network is contacted) —
  it demonstrates the full data flow (method selection, transaction ID generation,
  payment status, receipts) without needing a real payment gateway integration.
=======
# Car-Rental-Management-System
>>>>>>> 6a499567fc3b7ca4105a355b893bd184d9ffea61
=======
# Car-Rental-Management-System-Updated
>>>>>>> ad13f5da77b546c5049040e90b204d409966293e
