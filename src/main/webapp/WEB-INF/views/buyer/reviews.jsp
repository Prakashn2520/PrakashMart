<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Product Reviews — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/product?id=${productId}">Product #${productId}</a> &rsaquo;
        <span>Verified Reviews</span>
    </p>

    <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: var(--space-8); flex-wrap: wrap; gap: var(--space-4);">
        <div>
            <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Verified Customer Reviews</h1>
            <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Buyer feedback and evaluations for Product #${productId}</p>
        </div>
        <c:if test="${not empty avgRating && avgRating > 0}">
            <div style="text-align: right;">
                <div style="font-size: 1.75rem; font-weight: 700; color: var(--color-primary); font-family: var(--font-serif);">${avgRating} / 5.0</div>
                <div class="stars">&#9733;&#9733;&#9733;&#9733;&#9733;</div>
            </div>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${not empty reviews}">
            <div style="display: flex; flex-direction: column; gap: var(--space-4);">
                <c:forEach var="r" items="${reviews}">
                    <div class="card" style="padding: var(--space-4);">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-2);">
                            <div>
                                <span class="stars">
                                    <c:forEach begin="1" end="${r.rating}">&#9733;</c:forEach><c:forEach begin="${r.rating + 1}" end="5">&#9734;</c:forEach>
                                </span>
                                <strong style="margin-left: var(--space-2); font-size: 0.9rem;"><c:out value="${r.rating}" /> / 5 Stars</strong>
                            </div>
                            <span style="font-size: 0.8125rem; color: var(--text-muted);"><c:out value="${r.createdAt}" /></span>
                        </div>
                        <p style="margin: 0; font-size: 0.9375rem; color: var(--text-secondary);"><c:out value="${r.comment}" /></p>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-state-icon">&#9998;</div>
                <h3 class="empty-state-title">No Reviews Yet</h3>
                <p class="empty-state-desc">No buyer reviews have been submitted for this creation yet.</p>
                <a href="${pageContext.request.contextPath}/product?id=${productId}" class="btn btn-primary">&larr; Return to Product Page</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../common/footer.jsp" />
