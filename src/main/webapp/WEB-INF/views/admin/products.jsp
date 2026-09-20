<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Catalog Moderation — Admin Console" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/admin/dashboard">Admin Console</a> &rsaquo;
        <span>Catalog Moderation</span>
    </p>

    <div style="margin-bottom: var(--space-8);">
        <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Catalog Moderation</h1>
        <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Inspect all products published across all artisan merchants</p>
    </div>

    <div class="card" style="padding: var(--space-6);">
        <c:choose>
            <c:when test="${not empty productsResult.data}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Creation</th>
                                <th>Category</th>
                                <th>Seller ID</th>
                                <th>Unit Price</th>
                                <th>Inventory</th>
                                <th style="text-align: right;">View</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${productsResult.data}">
                                <tr>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" style="font-weight: 600; color: var(--color-primary);">
                                            <c:out value="${p.name}" />
                                        </a>
                                        <div style="font-size: 0.75rem; color: var(--text-muted);">ID: #${p.id}</div>
                                    </td>
                                    <td><span class="badge badge-gold"><c:out value="${p.category}" /></span></td>
                                    <td>Seller #<c:out value="${p.sellerId}" /></td>
                                    <td><strong style="font-family: var(--font-serif);">$<c:out value="${p.price}" /></strong></td>
                                    <td><span class="badge badge-in-stock"><c:out value="${p.stock}" /> in Stock</span></td>
                                    <td style="text-align: right;">
                                        <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" class="btn btn-outline btn-sm">Inspect</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-state-icon">&#128220;</div>
                    <h3 class="empty-state-title">No Catalog Creations Found</h3>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
