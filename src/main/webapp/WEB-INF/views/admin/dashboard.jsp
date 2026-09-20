<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Administration Console — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <span>Administration Console</span>
    </p>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-8); flex-wrap: wrap; gap: var(--space-4);">
        <div>
            <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Platform Administration Console</h1>
            <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">System oversight, user accounts governance, and platform-wide order dispatch moderation</p>
        </div>
        <div style="display: flex; gap: var(--space-2);">
            <span class="badge badge-in-stock" style="font-size: 0.8125rem; padding: 0.4rem 0.75rem;">
                &#9679; Engine Operational
            </span>
        </div>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${param.updated == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Administrative Action Completed.</strong> The requested platform status change has been applied.
        </div>
    </c:if>

    <!-- Platform KPI Metrics Grid -->
    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-6); margin-bottom: var(--space-8);">
        <div class="card" style="padding: var(--space-6); border-left: 4px solid var(--color-primary);">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Registered Accounts</div>
            <div style="font-size: 2rem; font-weight: 700; color: var(--color-primary); margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.totalUsers != null ? dashboard.totalUsers : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Buyers, sellers &amp; admins</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid var(--color-accent);">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Platform GMV</div>
            <div style="font-size: 2rem; font-weight: 700; color: var(--color-accent); margin-top: var(--space-2); font-family: var(--font-serif);">
                $<c:out value="${dashboard.totalGmv != null ? dashboard.totalGmv : '0.00'}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Cumulative marketplace trade</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid #10b981;">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Platform Orders</div>
            <div style="font-size: 2rem; font-weight: 700; color: #10b981; margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.totalOrders != null ? dashboard.totalOrders : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Total placed buyer orders</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid #6366f1;">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Catalog Listings</div>
            <div style="font-size: 2rem; font-weight: 700; color: #6366f1; margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.totalProducts != null ? dashboard.totalProducts : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Published active creations</div>
        </div>
    </div>

    <!-- Section 1: User Accounts Management -->
    <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-10);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-6); padding-bottom: var(--space-4); border-bottom: 1px solid var(--border-subtle);">
            <div>
                <h2 style="font-size: 1.35rem; margin-bottom: var(--space-1);">Registered User Accounts</h2>
                <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0;">Platform user registry, role privileges, and account status</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty users}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Name</th>
                                <th>Email Address</th>
                                <th>Assigned Role</th>
                                <th style="text-align: right;">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="u" items="${users}">
                                <tr>
                                    <td><strong>#<c:out value="${u.id}" /></strong></td>
                                    <td><strong><c:out value="${u.name}" /></strong></td>
                                    <td style="color: var(--text-secondary);"><c:out value="${u.email}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${u.role == 'ADMIN'}">
                                                <span class="badge badge-gold">Administrator</span>
                                            </c:when>
                                            <c:when test="${u.role == 'SELLER'}">
                                                <span class="badge badge-in-stock">Artisan Seller</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background-color: var(--bg-subtle); color: var(--text-secondary); border: 1px solid var(--border-medium);">Buyer</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <span class="badge badge-in-stock">Active</span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-state-icon">&#128101;</div>
                    <h3 class="empty-state-title">No Users Registered</h3>
                    <p class="empty-state-desc">No accounts found in the database.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Section 2: Platform Orders Moderation -->
    <div class="card" style="padding: var(--space-6);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-6); padding-bottom: var(--space-4); border-bottom: 1px solid var(--border-subtle);">
            <div>
                <h2 style="font-size: 1.35rem; margin-bottom: var(--space-1);">All Platform Orders Moderation</h2>
                <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0;">Oversee buyer orders and override delivery lifecycle statuses</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty orders}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Order #</th>
                                <th>Buyer ID</th>
                                <th>Date Placed</th>
                                <th>Order Amount</th>
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
                                            <button type="submit" class="btn btn-outline btn-sm">Set Status</button>
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
                    <h3 class="empty-state-title">No Platform Orders Recorded</h3>
                    <p class="empty-state-desc">Transactions initiated across the marketplace will be listed here.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
