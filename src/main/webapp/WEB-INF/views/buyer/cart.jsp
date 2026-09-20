<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Your Shopping Cart — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Header & Breadcrumb -->
    <div style="margin-bottom: var(--space-6);">
        <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-1);">
            <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
            <span>Shopping Cart</span>
        </p>
        <h1 style="font-size: 2rem;">Your Shopping Cart</h1>
    </div>

    <c:choose>
        <c:when test="${not empty cartItems}">
            <div class="cart-layout">
                <!-- Cart Items Table Card -->
                <div class="cart-items-card">
                    <div class="table-responsive">
                        <table class="table-premium" aria-label="Shopping Cart Items">
                            <thead>
                                <tr>
                                    <th>Item</th>
                                    <th>Unit Price</th>
                                    <th>Quantity</th>
                                    <th>Subtotal</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${cartItems}">
                                    <tr>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: var(--space-4);">
                                                <div style="width: 60px; height: 60px; border-radius: var(--radius-sm); overflow: hidden; background: #f3f4f6; flex-shrink: 0; display: flex; align-items: center; justify-content: center; border: 1px solid var(--border-subtle);">
                                                    <c:choose>
                                                        <c:when test="${not empty item.imageUrl}">
                                                            <img src="<c:out value='${item.imageUrl}' />" alt="<c:out value='${item.productName}' />" style="width: 100%; height: 100%; object-fit: cover;">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="font-size: 1.25rem; color: var(--color-accent);">&#9733;</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                                <div>
                                                    <a href="${pageContext.request.contextPath}/product?id=${item.productId}" style="font-weight: 600; color: var(--color-primary);">
                                                        <c:out value="${item.productName}" />
                                                    </a>
                                                    <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 2px;">
                                                        Item ID: #<c:out value="${item.productId}" />
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span style="font-weight: 500;">$<c:out value="${item.unitPrice}" /></span>
                                        </td>
                                        <td>
                                            <div class="qty-control">
                                                <button type="button" class="qty-btn qty-dec" aria-label="Decrease quantity">&minus;</button>
                                                <input type="number" 
                                                       name="quantity" 
                                                       value="${item.quantity}" 
                                                       min="1" 
                                                       max="${item.availableStock}" 
                                                       class="qty-input cart-item-qty-input"
                                                       data-cart-item-id="${item.cartItemId}"
                                                       aria-label="Item quantity">
                                                <button type="button" class="qty-btn qty-inc" aria-label="Increase quantity">&plus;</button>
                                            </div>
                                        </td>
                                        <td>
                                            <strong style="color: var(--color-primary);">$<c:out value="${item.subtotal}" /></strong>
                                        </td>
                                        <td>
                                            <button type="button" 
                                                    class="btn btn-text btn-sm cart-remove-btn" 
                                                    style="color: var(--danger-text);"
                                                    data-cart-item-id="${item.cartItemId}"
                                                    data-product-name="<c:out value='${item.productName}' />"
                                                    aria-label="Remove item">
                                                Remove
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <div style="margin-top: var(--space-4); display: flex; justify-content: space-between; align-items: center;">
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">&larr; Continue Browsing</a>
                    </div>
                </div>

                <!-- Order Summary & Checkout Card -->
                <div class="cart-summary-card">
                    <h3 style="font-size: 1.25rem; margin-bottom: var(--space-4); padding-bottom: var(--space-2); border-bottom: 1px solid var(--border-subtle);">
                        Order Summary
                    </h3>

                    <div class="cart-summary-row">
                        <span>Items Subtotal</span>
                        <span>$<c:out value="${cartTotal}" /></span>
                    </div>

                    <div class="cart-summary-row">
                        <span>Standard Delivery</span>
                        <span style="color: var(--success-text); font-weight: 500;">Complimentary</span>
                    </div>

                    <div class="cart-summary-row">
                        <span>Tax &amp; Service</span>
                        <span>Calculated at checkout</span>
                    </div>

                    <div class="cart-total-row">
                        <span>Total Payable</span>
                        <span>$<c:out value="${cartTotal}" /></span>
                    </div>

                    <div style="margin-top: var(--space-6);">
                        <a href="${pageContext.request.contextPath}/checkout" class="btn btn-primary btn-block btn-lg">
                            Proceed to Checkout &rarr;
                        </a>
                    </div>

                    <div style="margin-top: var(--space-4); font-size: 0.75rem; color: var(--text-muted); text-align: center;">
                        <span>&lock; 256-Bit TLS Encryption &bull; Escrow Protection</span>
                    </div>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <!-- Empty Cart State -->
            <div class="empty-state">
                <div class="empty-state-icon">&#128722;</div>
                <h2 class="empty-state-title">Your Cart is Currently Empty</h2>
                <p class="empty-state-desc">
                    You have not yet added any creations to your shopping bag. Explore our curated catalog to discover premier products.
                </p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">Explore Catalog</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
