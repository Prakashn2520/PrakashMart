<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Internal Server Error (500) - PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="error-container">
    <div class="error-card">
        <h1 class="error-code">500</h1>
        <h2 class="error-title">Internal Server Error</h2>
        <p class="error-desc">An unexpected error occurred while processing your request. Please try again later.</p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
