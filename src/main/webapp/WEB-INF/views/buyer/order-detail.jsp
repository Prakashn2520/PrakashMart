<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Order #${order.id} Details — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/orders">My Orders</a> &rsaquo;
        <span>Order #<c:out value="${order.id}" /></span>
    </p>

    <!-- Header & Order Actions -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-8); flex-wrap: wrap; gap: var(--space-4);">
        <div>
            <h1 style="font-size: 2rem; margin-bottom: var(--space-1);">Order #<c:out value="${order.id}" /></h1>
            <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">
                Placed on <c:out value="${order.createdAt}" /> &bull; Reference Token: PM-<c:out value="${order.id}" />
            </p>
        </div>

        <div style="display: flex; gap: var(--space-3); align-items: center;">
            <c:choose>
                <c:when test="${order.status == 'DELIVERED'}">
                    <span class="badge badge-in-stock" style="font-size: 0.875rem; padding: 0.4rem 0.8rem;">Delivered</span>
                </c:when>
                <c:when test="${order.status == 'SHIPPED'}">
                    <span class="badge badge-gold" style="font-size: 0.875rem; padding: 0.4rem 0.8rem;">In Transit (Shipped)</span>
                </c:when>
                <c:when test="${order.status == 'CANCELLED'}">
                    <span class="badge badge-out-of-stock" style="font-size: 0.875rem; padding: 0.4rem 0.8rem;">Cancelled</span>
                </c:when>
                <c:otherwise>
                    <span class="badge badge-warning" style="font-size: 0.875rem; padding: 0.4rem 0.8rem;"><c:out value="${order.status}" /></span>
                </c:otherwise>
            </c:choose>

            <c:if test="${order.status == 'PENDING'}">
                <form action="${pageContext.request.contextPath}/orders" method="POST" style="display: inline;" onsubmit="return confirm('Confirm cancellation of order #${order.id}?');">
                    <input type="hidden" name="_csrf" value="${csrfToken}">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="orderId" value="${order.id}">
                    <button type="submit" class="btn btn-danger btn-sm">Cancel Order</button>
                </form>
            </c:if>
        </div>
    </div>

    <!-- Order Details Layout Grid -->
    <div style="display: grid; grid-template-columns: 1.6fr 1fr; gap: var(--space-8); align-items: start;">
        <!-- Left Column: Ordered Products Table -->
        <div>
            <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-6);">
                <h3 style="font-size: 1.25rem; margin-bottom: var(--space-4); padding-bottom: var(--space-3); border-bottom: 1px solid var(--border-subtle);">
                    Purchased Creations
                </h3>

                <div class="table-responsive" style="border: none;">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th>Unit Price</th>
                                <th>Quantity</th>
                                <th style="text-align: right;">Line Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${items}">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: var(--space-3);">
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${item.productId}" style="font-weight: 600; color: var(--color-primary);">
                                                    Product #<c:out value="${item.productId}" />
                                                </a>
                                            </div>
                                        </div>
                                    </td>
                                    <td>$<c:out value="${item.unitPrice}" /></td>
                                    <td><c:out value="${item.quantity}" /></td>
                                    <td style="text-align: right; font-weight: 600; color: var(--color-primary);">
                                        $<c:out value="${item.unitPrice * item.quantity}" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- Right Column: Price Breakdown & Delivery Summary -->
        <div>
            <!-- Financial Summary -->
            <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-6);">
                <h3 style="font-size: 1.25rem; margin-bottom: var(--space-4); padding-bottom: var(--space-3); border-bottom: 1px solid var(--border-subtle);">
                    Payment &amp; Totals
                </h3>

                <div class="cart-summary-row">
                    <span>Items Subtotal:</span>
                    <span>$<c:out value="${order.totalAmount}" /></span>
                </div>
                <div class="cart-summary-row">
                    <span>Courier Insurance &amp; Shipping:</span>
                    <span style="color: var(--success-text); font-weight: 600;">Complimentary</span>
                </div>
                <div class="cart-total-row">
                    <span>Grand Total:</span>
                    <span>$<c:out value="${order.totalAmount}" /></span>
                </div>

                <div style="margin-top: var(--space-4); padding-top: var(--space-4); border-top: 1px solid var(--border-subtle); font-size: 0.8125rem; color: var(--text-muted);">
                    Payment Status: <strong style="color: var(--success-text);">&#10003; Escrow Secured</strong>
                </div>
            </div>

            <!-- Fulfillment & Escrow Guarantee -->
            <div class="card" style="padding: var(--space-6); background-color: var(--bg-surface);">
                <h4 style="font-size: 1rem; margin-bottom: var(--space-2);">Merchant Guarantee</h4>
                <p style="font-size: 0.875rem; color: var(--text-secondary); margin: 0; line-height: 1.6;">
                    Your funds are held in secure simulated escrow until item delivery and inspection is completed.
                </p>
                <div style="margin-top: var(--space-4);">
                    <a href="${pageContext.request.contextPath}/orders" class="btn btn-outline btn-block btn-sm">&larr; Back to All Orders</a>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
