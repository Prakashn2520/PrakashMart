<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Order History — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <span>My Orders</span>
    </p>

    <div style="margin-bottom: var(--space-8);">
        <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Purchase History</h1>
        <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Track, review, and inspect the status of your past orders</p>
    </div>

    <c:if test="${param.placed == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6); display: flex; align-items: center; justify-content: space-between;">
            <div>
                <strong>&#10003; Order Successfully Confirmed!</strong>
                <div style="font-size: 0.875rem; margin-top: 2px;">Order <strong>#<c:out value="${param.orderId}" /></strong> has been placed and routed to artisan sellers for fulfillment.</div>
            </div>
            <a href="${pageContext.request.contextPath}/orders?id=${param.orderId}" class="btn btn-outline btn-sm">View Details</a>
        </div>
    </c:if>

    <c:if test="${param.cancelled == 'true'}">
        <div class="alert alert-warning" style="background-color: var(--warning-bg); color: var(--warning-text); border: 1px solid var(--warning-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>Order #<c:out value="${param.orderId}" /> has been cancelled.</strong> Any allocated funds have been released.
        </div>
    </c:if>

    <c:choose>
        <c:when test="${not empty orders}">
            <div class="table-responsive">
                <table class="table-premium">
                    <thead>
                        <tr>
                            <th>Order Reference</th>
                            <th>Date &amp; Time</th>
                            <th>Total Payable</th>
                            <th>Current Status</th>
                            <th style="text-align: right;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="o" items="${orders}">
                            <tr>
                                <td>
                                    <strong style="color: var(--color-primary);">#<c:out value="${o.id}" /></strong>
                                </td>
                                <td style="color: var(--text-secondary); font-size: 0.875rem;">
                                    <c:out value="${o.createdAt}" />
                                </td>
                                <td>
                                    <strong style="font-family: var(--font-serif); font-size: 1.0625rem; color: var(--color-primary);">$<c:out value="${o.totalAmount}" /></strong>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${o.status == 'DELIVERED'}">
                                            <span class="badge badge-in-stock">Delivered</span>
                                        </c:when>
                                        <c:when test="${o.status == 'SHIPPED'}">
                                            <span class="badge badge-gold">In Transit (Shipped)</span>
                                        </c:when>
                                        <c:when test="${o.status == 'CANCELLED'}">
                                            <span class="badge badge-out-of-stock">Cancelled</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-warning"><c:out value="${o.status}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: right;">
                                    <div style="display: inline-flex; gap: var(--space-2); align-items: center;">
                                        <a href="${pageContext.request.contextPath}/orders?id=${o.id}" class="btn btn-outline btn-sm">
                                            View Details
                                        </a>
                                        <c:if test="${o.status == 'PENDING'}">
                                            <form action="${pageContext.request.contextPath}/orders" method="POST" style="display: inline;" onsubmit="return confirm('Are you sure you wish to cancel order #${o.id}?');">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="action" value="cancel">
                                                <input type="hidden" name="orderId" value="${o.id}">
                                                <button type="submit" class="btn btn-danger btn-sm">Cancel</button>
                                            </form>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">&#128230;</div>
                <h2 class="empty-state-title">No Orders Recorded</h2>
                <p class="empty-state-desc">You have not placed any orders yet. Discover our curated catalog to begin your collection.</p>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Browse Marketplace</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
