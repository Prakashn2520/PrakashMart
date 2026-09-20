<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="User Governance — Admin Console" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/admin/dashboard">Admin Console</a> &rsaquo;
        <span>User Accounts</span>
    </p>

    <div style="margin-bottom: var(--space-8);">
        <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Registered Accounts</h1>
        <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Marketplace buyers, sellers, and administrator accounts</p>
    </div>

    <div class="card" style="padding: var(--space-6);">
        <c:choose>
            <c:when test="${not empty users}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
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
                    <h3 class="empty-state-title">No User Accounts Found</h3>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
