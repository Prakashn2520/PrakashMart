<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Orders Moderation — Admin Console" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/admin/dashboard">Admin Console</a> &rsaquo;
        <span>All Orders</span>
    </p>

    <div style="margin-bottom: var(--space-8);">
        <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Platform Order Moderation</h1>
        <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Supervise order fulfillments and adjust delivery statuses across the marketplace</p>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${param.updated == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Status Override Applied.</strong> Order status updated.
        </div>
    </c:if>

    <div class="card" style="padding: var(--space-6);">
        <c:choose>
            <c:when test="${not empty orders}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Order #</th>
                                <th>Buyer ID</th>
                                <th>Date Placed</th>
                                <th>Total</th>
                                <th>Current Status</th>
                                <th style="text-align: right;">Status Moderation</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${orders}">
                                <tr>
                                    <td><strong>#<c:out value="${o.id}" /></strong></td>
                                    <td>Buyer #<c:out value="${o.buyerId}" /></td>
                                    <td style="color: var(--text-secondary); font-size: 0.875rem;"><c:out value="${o.createdAt}" /></td>
                                    <td><strong style="font-family: var(--font-serif); color: var(--color-primary); font-size: 1.05rem;">$<c:out value="${o.totalAmount}" /></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${o.status == 'DELIVERED'}">
                                                <span class="badge badge-in-stock">Delivered</span>
                                            </c:when>
                                            <c:when test="${o.status == 'SHIPPED'}">
                                                <span class="badge badge-gold">In Transit</span>
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
                                        <form action="${pageContext.request.contextPath}/admin/orders/status" method="POST" style="display: inline-flex; gap: var(--space-2); align-items: center;">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <select name="status" class="form-select form-select-sm" style="width: auto; padding: 0.3rem 0.6rem; font-size: 0.8125rem;">
                                                <option value="PENDING" ${o.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                                                <option value="CONFIRMED" ${o.status == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                                <option value="SHIPPED" ${o.status == 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                                                <option value="DELIVERED" ${o.status == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                                                <option value="CANCELLED" ${o.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                            </select>
                                            <button type="submit" class="btn btn-outline btn-sm">Override</button>
                                        </form>
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
                    <h3 class="empty-state-title">No Orders Recorded</h3>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
