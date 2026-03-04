# MyBook - Online Bookstore (Spring Boot)

MyBook la ung dung web quan ly ban sach, gom giao dien nguoi dung va trang tri admin.
Du an duoc xay dung theo huong full-stack Java voi Spring Boot + Thymeleaf.

## 1) Tech stack

- Java 17
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Data JPA (Hibernate)
- Spring Security (Form Login + Google OAuth2)
- MySQL
- Flyway (database migration)
- Apache POI (xuat Excel/Word)
- JUnit + Spring Boot Test + H2 (test)
- Tich hop AI cover extraction (OpenRouter/OpenAI)

## 2) Tinh nang chinh

- Dang ky / dang nhap / dang xuat
- Dang nhap Google OAuth2
- Phan quyen ROLE_USER va ROLE_ADMIN
- CRUD sach, danh muc (admin)
- Gio hang (guest + user), checkout, tao don hang
- Quan ly don hang va cap nhat trang thai
- Ma giam gia (coupon) co thoi gian hieu luc + so luong su dung
- Wishlist va review sach
- Upload anh sach
- Dashboard admin + xuat bao cao Excel/Word
- API trich xuat tieu de/tac gia tu anh bia sach bang AI

## 3) Kien truc thu muc

```text
src/main/java/com/btvn/CaoMinhHuy
  |- config
  |- controllers
  |- controllers/api
  |- entities
  |- repositories
  |- services
  `- services/impl

src/main/resources
  |- templates
  |- static
  |- db/migration
  `- application.properties
```

## 4) Chay local

### Yeu cau

- JDK 17+
- MySQL 8+
- Maven Wrapper (`mvnw`/`mvnw.cmd`)

### Tao database

```sql
CREATE DATABASE bookstore;
```

### Cau hinh

Cap nhat file `src/main/resources/application.properties`:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Khuyen nghi dung bien moi truong cho secret:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI`
- `OPENROUTER_API_KEY` hoac `OPENAI_API_KEY`

### Run app

Windows:

```bash
.\mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Mac dinh app chay tai: `http://localhost:8082`

## 5) Du lieu seed mac dinh

Khi chay lan dau, app se seed:

- Tai khoan admin: `admin / admin123`
- 3 danh muc mau
- 10 sach mau
- Coupon mau: `SAVE20` (amount `20000`)

## 6) Chay test

Windows:

```bash
.\mvnw.cmd test
```

macOS/Linux:

```bash
./mvnw test
```

Test profile su dung H2 in-memory (`src/test/resources/application-test.properties`).

## 7) API tieu bieu

- `POST /api/ai/cover/extract`
  - form-data: `file=<image>`
  - tra ve JSON gom `title`, `author`

## 8) Luu y bao mat

- Khong commit API key, OAuth secret, mat khau DB that.
- Uu tien de secret trong bien moi truong hoac secret manager.

## 9) Dinh huong phat trien tiep

- Them CI/CD (GitHub Actions)
- Viet them integration test cho controller va security
- Tach ro front-end (REST API + SPA)
- Bo sung logging/monitoring (ELK, Prometheus/Grafana)
