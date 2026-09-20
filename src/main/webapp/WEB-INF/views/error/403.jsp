<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Access Forbidden (403) - PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="error-container">
    <div class="error-card">
        <h1 class="error-code">403</h1>
        <h2 class="error-title">Access Forbidden</h2>
        <p class="error-desc">You do not have administrative or seller privileges to view this restricted resource.</p>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">Return to Marketplace</a>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
