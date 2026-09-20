# PrakashMart — Multi-Seller E-Commerce Marketplace

**Anna University R2025 Semester 3 Capstone Project**  
*Technology Stack: Java Servlets 4.0 · Raw JDBC · Apache Tomcat 9.0.x · H2 Database · JSP 2.3 & JSTL 1.2*

---

## 1. Project Overview

**PrakashMart** is an enterprise-grade, native multi-seller e-commerce marketplace built strictly with standard Java EE / Jakarta EE fundamentals (Servlet 4.0, JSP/JSTL, raw JDBC with HikariCP, and H2 Database). It enables independent sellers to publish and manage product listings, buyers to browse, search, manage shopping carts, and place orders via simulated escrow payment, and platform administrators to govern users, catalog creations, and platform transactions.

### Key Architectural Constraints
- **Zero Heavy Frameworks**: Pure Java EE Servlets, JSP/JSTL, and raw JDBC via HikariCP (No Spring Boot, Hibernate, JPA, React, or Node.js).
- **Relational Database**: H2 Database running in server mode for live deployment and embedded in-memory mode for automated testing.
- **Transactional Integrity**: Multi-table atomic checkout transactions (`conn.setAutoCommit(false)`, `conn.commit()`, `rollbackTransaction`) ensuring zero stock inconsistency.
- **Security-First Engineering**: BCrypt password hashing (cost factor 12), session fixation defense via session ID regeneration, strict XSS escaping via JSTL `<c:out>`, CSRF protection across all state-changing endpoints, and parameterized PreparedStatement execution everywhere.
- **Premium Classical UI**: Clean, restrained design system without excessive animations or glassmorphism, fully responsive across mobile, tablet, laptop, and desktop.

---

## 2. Technology Stack

| Layer / Component | Technology & Version | Description |
| :--- | :--- | :--- |
| **Language & Runtime** | Java 17 LTS | OpenJDK 17 / 21 LTS |
| **Servlet Container** | Apache Tomcat 9.0.x | Java EE 8 Servlet 4.0 API (`javax.servlet.*`) |
| **Build & Packaging** | Apache Maven 3.9+ | WAR packaging and dependency lifecycle |
| **Database** | H2 Database 2.2.x | Server mode (deploy) & In-memory mode (tests) |
| **Connection Pooling** | HikariCP 5.1.x | Managed strictly by `AppContextListener` |
| **Presentation** | JSP 2.3 + JSTL 1.2 | Classical, server-rendered views with layout decoupling |
| **Client Scripting** | Vanilla JavaScript | Asynchronous `fetch()` calls and toast notifications |
| **JSON Serialization** | Google Gson 2.10.x | API request/response serialization |
| **Password Security** | jBCrypt 0.4 | Salted BCrypt password hashing (cost 12) |
| **Logging** | SLF4J 2.0 + Logback 1.5 | Structured logging with MDC `requestId` tracing |
| **Testing** | JUnit 5 + Mockito 5 | DAO tests on embedded H2, Service tests with mocks |
| **Continuous Integration** | GitHub Actions | Ubuntu runner executing `mvn -B clean verify` |

---

## 3. System Architecture

```mermaid
flowchart TD
    Client["Browser / Client (HTML5, Classical CSS, Vanilla JS Fetch)"]
    
    subgraph Security_Filter_Pipeline ["Security & Filter Pipeline"]
        F1["EncodingFilter (UTF-8)"]
        F2["LoggingFilter (UUID Request ID -> SLF4J MDC)"]
        F3["CsrfFilter (Token Verification & Security Headers)"]
        F4["AuthFilter (Session Check & RBAC: Buyer / Seller / Admin)"]
    end

    subgraph Controller_Layer ["Controller / Servlet Layer"]
        S_Auth["AuthServlet (/login, /register, /logout)"]
        S_Prod["ProductServlet (/products, /product, /api/products)"]
        S_Cart["CartServlet (/cart, /api/cart)"]
        S_Order["OrderServlet (/checkout, /orders, /api/orders)"]
        S_Review["ReviewServlet (/reviews, /api/reviews)"]
        S_Seller["SellerServlet (/seller/*, /api/seller/*)"]
        S_Admin["AdminServlet (/admin/*, /api/admin/*)"]
        S_Chat["ChatServlet (/chat, /api/chat)"]
    end

    subgraph Service_Layer ["Service Layer (Business Rules & Transactions)"]
        Svc_User["UserService / AuthServiceImpl"]
        Svc_Prod["ProductServiceImpl"]
        Svc_Cart["CartServiceImpl"]
        Svc_Order["OrderServiceImpl (ACID Checkout)"]
        Svc_Review["ReviewServiceImpl"]
    end

    subgraph DAO_Layer ["DAO Layer (PreparedStatement & Raw JDBC)"]
        DAO_User["UserDAOImpl"]
        DAO_Prod["ProductDAOImpl"]
        DAO_Cart["CartDAOImpl"]
        DAO_Order["OrderDAOImpl"]
        DAO_Review["ReviewDAOImpl"]
    end

    subgraph Persistence ["Persistence Layer"]
        Pool["HikariCP Connection Pool (AppContextListener)"]
        DB["H2 Relational Database"]
    end

    Client --> Security_Filter_Pipeline
    Security_Filter_Pipeline --> Controller_Layer
    Controller_Layer --> Service_Layer
    Service_Layer --> DAO_Layer
    DAO_Layer --> Pool
    Pool --> DB
```

---

## 4. Key Diagrams

### A. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram
    users ||--o{ products : "sells"
    users ||--o{ orders : "places"
    users ||--o{ cart_items : "maintains"
    users ||--o{ reviews : "authors"
    products ||--o{ order_items : "ordered_in"
    products ||--o{ cart_items : "present_in"
    products ||--o{ reviews : "reviewed_in"
    orders ||--|{ order_items : "contains"

    users {
        bigint id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar role "CHECK BUYER, SELLER, ADMIN"
        timestamp created_at
    }

    products {
        bigint id PK
        bigint seller_id FK
        varchar name
        text description
        decimal price "DECIMAL(10,2)"
        int stock_qty
        varchar category
        varchar image_url
        boolean is_active
        timestamp created_at
    }

    orders {
        bigint id PK
        bigint buyer_id FK
        varchar status "PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
        decimal total_amount "DECIMAL(10,2)"
        timestamp created_at
    }

    order_items {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        decimal unit_price "DECIMAL(10,2)"
    }

    cart_items {
        bigint id PK
        bigint user_id FK
        bigint product_id FK
        int quantity
    }

    reviews {
        bigint id PK
        bigint product_id FK
        bigint user_id FK
        int rating "1 to 5"
        text comment
        timestamp created_at
    }
```

### B. Use Case Diagram

```mermaid
flowchart LR
    Buyer((Buyer))
    Seller((Seller))
    Admin((Admin))

    subgraph Auth_Module ["Authentication"]
        UC1([Register / Sign In])
        UC2([Sign Out])
    end

    subgraph Buyer_Module ["Buyer Operations"]
        UC3([Browse & Search Catalog])
        UC4([Filter by Price, Category, Stock, Rating])
        UC5([Manage Cart & Quantities])
        UC6([Place Order via Simulated Escrow])
        UC7([View Order History & Itemized Details])
        UC8([Cancel Pending Order])
        UC9([Submit Verified Product Review])
    end

    subgraph Seller_Module ["Seller Operations Hub"]
        UC10([View Seller Dashboard & KPIs])
        UC11([Publish New Product Listing])
        UC12([Manage / Delete Own Products])
        UC13([View Assigned Customer Orders])
        UC14([Update Order Dispatch Status])
    end

    subgraph Admin_Module ["Admin Console"]
        UC15([View Platform Metrics & GMV])
        UC16([Manage User Accounts])
        UC17([Moderate Product Catalog])
        UC18([Moderate & Override Order Status])
    end

    Buyer --> UC1
    Buyer --> UC2
    Buyer --> UC3
    Buyer --> UC4
    Buyer --> UC5
    Buyer --> UC6
    Buyer --> UC7
    Buyer --> UC8
    Buyer --> UC9

    Seller --> UC1
    Seller --> UC2
    Seller --> UC10
    Seller --> UC11
    Seller --> UC12
    Seller --> UC13
    Seller --> UC14

    Admin --> UC1
    Admin --> UC2
    Admin --> UC15
    Admin --> UC16
    Admin --> UC17
    Admin --> UC18
```

### C. Place Order Transactional Sequence Diagram

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Browser (Buyer)
    participant Filter as AuthFilter / CsrfFilter
    participant Servlet as OrderServlet
    participant Service as OrderServiceImpl
    participant OrderDAO as OrderDAOImpl
    participant CartDAO as CartDAOImpl
    participant ProdDAO as ProductDAOImpl
    participant DB as H2 Relational DB

    Buyer->>Filter: POST /orders (Address Details & _csrf)
    Filter->>Filter: Verify session authentication & CSRF token
    Filter->>Servlet: Forward authorized request
    Servlet->>Service: checkout(buyerId)

    Service->>CartDAO: getCartByUserId(buyerId)
    CartDAO->>DB: SELECT * FROM cart_items WHERE user_id=?
    DB-->>CartDAO: List of CartItemDTOs
    CartDAO-->>Service: Active cart items

    Service->>Service: Validate non-empty cart & inventory availability

    Service->>DB: Begin Atomic Transaction (conn.setAutoCommit(false))
    
    Service->>OrderDAO: save(conn, newOrder)
    OrderDAO->>DB: INSERT INTO orders (buyer_id, status, total_amount) VALUES (...)
    DB-->>OrderDAO: generated order_id
    OrderDAO-->>Service: Created Order (#ID)

    loop For each cart item
        Service->>OrderDAO: saveItem(conn, orderItem)
        OrderDAO->>DB: INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (...)
        DB-->>OrderDAO: generated item_id

        Service->>ProdDAO: updateStock(productId, remainingStock)
        ProdDAO->>DB: UPDATE products SET stock_qty=? WHERE id=?
        DB-->>ProdDAO: rows affected
    end

    Service->>CartDAO: clearCart(conn, buyerId)
    CartDAO->>DB: DELETE FROM cart_items WHERE user_id=?
    DB-->>CartDAO: rows deleted

    Service->>DB: Commit Transaction (conn.commit())
    Service-->>Servlet: Order confirmation entity

    Servlet-->>Buyer: 302 Redirect to /orders?id={orderId}&placed=true
```

---

## 5. REST & JSON API Overview

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | Register new user account (`BUYER` / `SELLER`) |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user and establish secure session |
| `POST` | `/api/v1/auth/logout` | Authenticated | Invalidate session |
| `GET` | `/api/products` | Public | Paginated product search, category filter & sorting |
| `GET` | `/api/products/{id}` | Public | Inspect single product details |
| `GET` | `/api/cart` | Authenticated | Retrieve current user's active shopping cart |
| `POST` | `/api/cart/add` | Authenticated | Add product to shopping cart |
| `PUT` | `/api/cart/items/{id}` | Authenticated | Update quantity of a cart line item |
| `DELETE`| `/api/cart/items/{id}` | Authenticated | Remove line item from shopping cart |
| `GET` | `/api/orders` | Authenticated | Retrieve authenticated user's order history |
| `GET` | `/api/orders/{id}` | Authenticated | Retrieve itemized details for an authorized order |
| `POST` | `/api/orders` | Authenticated | Execute atomic checkout transaction |
| `POST` | `/api/orders/{id}/cancel` | Authenticated | Cancel a pending or confirmed order |
| `GET` | `/api/reviews?productId={id}` | Public | List customer reviews for a product |
| `POST` | `/api/reviews` | Buyer | Submit a verified customer review |
| `GET` | `/api/seller/dashboard` | Seller | Retrieve seller KPI metrics and sales revenue |
| `GET` | `/api/seller/products` | Seller | List catalog products owned by authenticated seller |
| `POST` | `/api/seller/products` | Seller | Publish a new product listing |
| `PUT` | `/api/seller/products/{id}` | Seller | Update an existing product listing |
| `DELETE`| `/api/seller/products/{id}` | Seller | Delete an existing product listing |
| `PUT` | `/api/seller/orders/{id}/status` | Seller | Update shipping status for assigned order |
| `GET` | `/api/admin/dashboard` | Admin | Retrieve platform metrics and marketplace GMV |
| `GET` | `/api/admin/users` | Admin | List all registered user accounts |
| `GET` | `/api/admin/orders` | Admin | List platform orders for administrative moderation |
| `PUT` | `/api/admin/orders/{id}/status` | Admin | Administrative order status override |
| `POST` | `/api/chat` | Public | Assistant chatbot endpoint |

### Standard Response Envelopes

**Success Envelope:**
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

**Error Envelope:**
```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "Detailed error description"
}
```

---

## 6. Seed Accounts (Development & Testing)

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@prakashmart.com` | `admin123` | Platform console, user governance, order moderation |
| **SELLER** | `seller1@prakashmart.com` | `seller123` | Seller hub, inventory management, order dispatch |
| **SELLER** | `seller2@prakashmart.com` | `seller123` | Seller hub (BookWorld Store listings) |
| **BUYER** | `buyer1@prakashmart.com` | `buyer123` | Catalog browse, shopping cart, checkout, reviews |
| **BUYER** | `buyer2@prakashmart.com` | `buyer123` | Catalog browse, shopping cart, checkout, reviews |

---

## 7. Local Setup, Build & Deployment

### Prerequisites
- **Java**: OpenJDK 17 LTS (or JDK 21 LTS) installed and configured on `PATH`.
- **Maven**: Apache Maven 3.9+ (or `mvnd` / `./mvnw`).
- **Servlet Container**: Apache Tomcat 9.0.x.

### Build & Verification Commands
```bash
# 1. Clean, compile, and execute all 148 automated tests
mvn clean verify

# 2. Package production WAR file
mvn clean package -DskipTests
```
The packaged deployable WAR will be created at `target/prakashmart.war`.

### Running with Embedded Tomcat (Development & Testing)
Automated tests automatically launch embedded Apache Tomcat 9 at `http://localhost:8888/prakashmart` during test suite execution.

### Deploying to Standalone Apache Tomcat 9
1. Copy `target/prakashmart.war` to `$CATALINA_HOME/webapps/prakashmart.war`.
2. Start Tomcat:
   - Linux/macOS: `$CATALINA_HOME/bin/startup.sh`
   - Windows: `%CATALINA_HOME%\bin\startup.bat`
3. Access the web application in any browser at:  
   `http://localhost:8080/prakashmart/`
#   P r a k a s h M a r t  
 