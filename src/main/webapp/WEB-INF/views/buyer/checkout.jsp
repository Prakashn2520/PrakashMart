<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Secure Checkout — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/cart">Shopping Cart</a> &rsaquo;
        <span>Secure Checkout</span>
    </p>

    <div style="margin-bottom: var(--space-8);">
        <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Order Checkout</h1>
        <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Review your delivery destination and confirm simulated escrow payment</p>
    </div>

    <c:choose>
        <c:when test="${not empty cartItems}">
            <div class="checkout-grid" style="display: grid; grid-template-columns: 1.4fr 1fr; gap: var(--space-8); align-items: start;">
                <!-- Left Column: Shipping & Payment -->
                <div>
                    <!-- 1. Customer & Shipping Details -->
                    <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-6);">
                        <div style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-4); padding-bottom: var(--space-3); border-bottom: 1px solid var(--border-subtle);">
                            <span style="width: 28px; height: 28px; border-radius: var(--radius-pill); background-color: var(--color-primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.875rem; font-weight: 700;">1</span>
                            <h2 style="font-size: 1.25rem; margin: 0;">Delivery &amp; Customer Information</h2>
                        </div>

                        <form id="checkoutForm" action="${pageContext.request.contextPath}/orders" method="POST" onsubmit="PrakashMart.showFormLoading(this)">
                            <input type="hidden" name="_csrf" value="${csrfToken}">

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                                <div class="form-group">
                                    <label for="buyerName" class="form-label">Full Name <span class="required">*</span></label>
                                    <input type="text" id="buyerName" name="buyerName" value="<c:out value='${sessionScope.currentUser.name}' />" class="form-control" required placeholder="Johnathan Doe">
                                </div>
                                <div class="form-group">
                                    <label for="buyerEmail" class="form-label">Email Address <span class="required">*</span></label>
                                    <input type="email" id="buyerEmail" name="buyerEmail" value="<c:out value='${sessionScope.currentUser.email}' />" class="form-control" required placeholder="john@example.com" readonly>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="streetAddress" class="form-label">Street Address <span class="required">*</span></label>
                                <input type="text" id="streetAddress" name="streetAddress" class="form-control" required placeholder="42 Heritage Way, Suite 100">
                            </div>

                            <div style="display: grid; grid-template-columns: 1.2fr 1fr 1fr; gap: var(--space-4);">
                                <div class="form-group">
                                    <label for="city" class="form-label">City <span class="required">*</span></label>
                                    <input type="text" id="city" name="city" class="form-control" required placeholder="Seattle">
                                </div>
                                <div class="form-group">
                                    <label for="state" class="form-label">State / Province <span class="required">*</span></label>
                                    <input type="text" id="state" name="state" class="form-control" required placeholder="WA">
                                </div>
                                <div class="form-group">
                                    <label for="postalCode" class="form-label">Postal Code <span class="required">*</span></label>
                                    <input type="text" id="postalCode" name="postalCode" class="form-control" required placeholder="98101">
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="phoneNumber" class="form-label">Contact Phone <span class="required">*</span></label>
                                <input type="tel" id="phoneNumber" name="phoneNumber" class="form-control" required placeholder="+1 (555) 019-2834">
                            </div>

                            <!-- 2. Mock Payment Section -->
                            <div style="margin-top: var(--space-8); padding-top: var(--space-6); border-top: 1px solid var(--border-subtle);">
                                <div style="display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-4);">
                                    <span style="width: 28px; height: 28px; border-radius: var(--radius-pill); background-color: var(--color-primary); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 0.875rem; font-weight: 700;">2</span>
                                    <h2 style="font-size: 1.25rem; margin: 0;">Payment Processing Method</h2>
                                </div>

                                <div style="background-color: var(--bg-subtle); border: 1px solid var(--border-medium); border-radius: var(--radius-sm); padding: var(--space-4); margin-bottom: var(--space-4);">
                                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--space-2);">
                                        <span class="badge badge-gold">Simulated Escrow Gateway</span>
                                        <span style="font-size: 0.75rem; color: var(--text-muted);">&#128274; 256-Bit SSL Mock Encrypted</span>
                                    </div>
                                    <p style="font-size: 0.875rem; color: var(--text-secondary); margin: 0;">
                                        In compliance with project specifications, payment is processed via simulated instant escrow authorization. No real monetary transactions take place.
                                    </p>
                                </div>

                                <div style="display: flex; flex-direction: column; gap: var(--space-3); margin-bottom: var(--space-6);">
                                    <label style="display: flex; align-items: center; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--color-accent-border); background-color: #fff; border-radius: var(--radius-sm); cursor: pointer;">
                                        <input type="radio" name="mockPaymentType" value="ESCROW_CARD" checked>
                                        <div style="flex: 1;">
                                            <strong style="font-size: 0.9375rem; color: var(--color-primary);">Direct Merchant Escrow (Instant Demo Approval)</strong>
                                            <div style="font-size: 0.8125rem; color: var(--text-muted);">Simulates instant credit/debit transaction clearance.</div>
                                        </div>
                                    </label>

                                    <label style="display: flex; align-items: center; gap: var(--space-3); padding: var(--space-3); border: 1px solid var(--border-subtle); background-color: #fff; border-radius: var(--radius-sm); cursor: pointer;">
                                        <input type="radio" name="mockPaymentType" value="COD">
                                        <div style="flex: 1;">
                                            <strong style="font-size: 0.9375rem; color: var(--color-primary);">Pay Upon Courier Delivery (Cash / POS)</strong>
                                            <div style="font-size: 0.8125rem; color: var(--text-muted);">Pay securely upon receipt and inspection of creations.</div>
                                        </div>
                                    </label>
                                </div>

                                <button type="submit" class="btn btn-primary btn-block btn-lg submit-btn">
                                    Place Order ($<c:out value="${cartTotal}" />) &rarr;
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Right Column: Order Summary -->
                <div class="card" style="padding: var(--space-6); position: sticky; top: 90px;">
                    <h3 style="font-size: 1.25rem; margin-bottom: var(--space-4); padding-bottom: var(--space-3); border-bottom: 1px solid var(--border-subtle);">
                        Order Summary
                    </h3>

                    <!-- Itemized Cart Products -->
                    <div style="display: flex; flex-direction: column; gap: var(--space-4); max-height: 320px; overflow-y: auto; margin-bottom: var(--space-6); padding-right: 4px;">
                        <c:forEach var="item" items="${cartItems}">
                            <div style="display: flex; gap: var(--space-3); align-items: center;">
                                <div style="width: 52px; height: 52px; border-radius: var(--radius-sm); overflow: hidden; background-color: #fafbfc; border: 1px solid var(--border-subtle); flex-shrink: 0; display: flex; align-items: center; justify-content: center;">
                                    <c:choose>
                                        <c:when test="${not empty item.imageUrl}">
                                            <img src="<c:out value='${item.imageUrl}' />" alt="<c:out value='${item.productName}' />" style="width: 100%; height: 100%; object-fit: cover;">
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--text-muted); font-size: 1.25rem;">&#9733;</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div style="flex: 1; min-width: 0;">
                                    <h4 style="font-size: 0.875rem; margin: 0; font-family: var(--font-sans); white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                        <c:out value="${item.productName}" />
                                    </h4>
                                    <span style="font-size: 0.8125rem; color: var(--text-muted);">Qty: <c:out value="${item.quantity}" /> &times; $<c:out value="${item.unitPrice}" /></span>
                                </div>
                                <strong style="font-size: 0.9375rem; color: var(--color-primary);">$<c:out value="${item.subtotal}" /></strong>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Price Calculations -->
                    <div style="border-top: 1px solid var(--border-subtle); padding-top: var(--space-4);">
                        <div class="cart-summary-row">
                            <span>Creations Subtotal:</span>
                            <span>$<c:out value="${cartTotal}" /></span>
                        </div>
                        <div class="cart-summary-row">
                            <span>Standard Insured Courier:</span>
                            <span style="color: var(--success-text); font-weight: 600;">Complimentary</span>
                        </div>
                        <div class="cart-total-row">
                            <span>Total Payable:</span>
                            <span>$<c:out value="${cartTotal}" /></span>
                        </div>
                    </div>

                    <div style="margin-top: var(--space-6); text-align: center;">
                        <a href="${pageContext.request.contextPath}/cart" class="btn btn-text btn-sm">&larr; Return to Modify Cart</a>
                    </div>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">&#128722;</div>
                <h2 class="empty-state-title">Your Cart is Currently Empty</h2>
                <p class="empty-state-desc">Select items from our curated catalog before initiating checkout.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Explore Catalog</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
