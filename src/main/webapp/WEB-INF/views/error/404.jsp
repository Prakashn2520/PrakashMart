<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Page Not Found (404) - PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="error-container">
    <div class="error-card">
        <h1 class="error-code">404</h1>
        <h2 class="error-title">Page Not Found</h2>
        <p class="error-desc">The requested resource or page could not be located on the server.</p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
