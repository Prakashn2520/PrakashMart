# D3: Sequence Diagram — Place Order Flow

Derived from Section 2 & Section 5 of the Capstone Project Requirements:
Flow: `Browser -> Servlet -> Service -> DAO -> Database`, including transactional checkout and response path.

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Browser (Buyer)
    participant Filter as AuthFilter / LoggingFilter
    participant Servlet as OrderServlet
    participant Service as OrderServiceImpl
    participant OrderDAO as OrderDAOImpl
    participant CartDAO as CartDAOImpl
    participant ProdDAO as ProductDAOImpl
    participant DB as H2 Database

    Buyer->>Filter: POST /orders (Checkout Request)
    Filter->>Filter: Verify session & buyer role
    Filter->>Servlet: Forward authorized request
    Servlet->>Service: checkout(buyerId)

    Service->>CartDAO: getCartByUserId(buyerId)
    CartDAO->>DB: SELECT * FROM cart_items WHERE user_id=?
    DB-->>CartDAO: List of CartItemDTOs
    CartDAO-->>Service: Cart items

    Service->>Service: Validate non-empty cart & stock

    Service->>DB: Begin Transaction (conn.setAutoCommit(false))
    
    Service->>OrderDAO: save(conn, order)
    OrderDAO->>DB: INSERT INTO orders VALUES (...)
    DB-->>OrderDAO: generated order_id
    OrderDAO-->>Service: Saved Order

    loop For each cart item
        Service->>OrderDAO: saveItem(conn, orderItem)
        OrderDAO->>DB: INSERT INTO order_items VALUES (...)
        DB-->>OrderDAO: generated item_id

        Service->>ProdDAO: updateStock(productId, newStock)
        ProdDAO->>DB: UPDATE products SET stock_qty=? WHERE id=?
        DB-->>ProdDAO: rows affected
    end

    Service->>CartDAO: clearCart(conn, buyerId)
    CartDAO->>DB: DELETE FROM cart_items WHERE user_id=?
    DB-->>CartDAO: rows deleted

    Service->>DB: Commit Transaction (conn.commit())
    Service-->>Servlet: Order confirmation entity

    Servlet-->>Buyer: 302 Redirect to /orders?placed=true&orderId=...
```
