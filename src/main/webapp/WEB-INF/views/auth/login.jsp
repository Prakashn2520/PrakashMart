<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Sign In — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="auth-wrapper" style="min-height: calc(100vh - 350px); display: flex; align-items: center; justify-content: center; padding: var(--space-8) var(--space-4);">
    <div class="card auth-card" style="width: 100%; max-width: 420px; padding: var(--space-8); box-shadow: var(--shadow-lg);">
        <div style="text-align: center; margin-bottom: var(--space-6);">
            <div class="brand-symbol" style="margin: 0 auto var(--space-3); width: 44px; height: 44px; font-size: 1.5rem;">P</div>
            <h1 style="font-size: 1.75rem; margin-bottom: var(--space-1);">Welcome Back</h1>
            <p style="font-size: 0.875rem; color: var(--text-muted); margin: 0;">Sign in to your PrakashMart account</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" style="background-color: var(--danger-bg); color: var(--danger-text); border: 1px solid var(--danger-border); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.875rem; margin-bottom: var(--space-4);">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>
        <c:if test="${param.loggedOut == 'true'}">
            <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.875rem; margin-bottom: var(--space-4);">
                You have successfully signed out.
            </div>
        </c:if>
        <c:if test="${param.registered == 'true'}">
            <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-3) var(--space-4); border-radius: var(--radius-sm); font-size: 0.875rem; margin-bottom: var(--space-4);">
                Account registered successfully! Please sign in below.
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST" class="form-auth" id="loginForm" onsubmit="PrakashMart.showFormLoading(this)">
            <input type="hidden" name="_csrf" value="${csrfToken}">

            <div class="form-group">
                <label for="email" class="form-label">Email Address <span class="required">*</span></label>
                <input type="email" id="email" name="email" class="form-control" required autofocus placeholder="name@domain.com" autocomplete="email">
            </div>

            <div class="form-group">
                <div style="display: flex; justify-content: space-between; align-items: baseline;">
                    <label for="password" class="form-label">Password <span class="required">*</span></label>
                </div>
                <div style="position: relative; display: flex; align-items: center;">
                    <input type="password" id="password" name="password" class="form-control password-field" required placeholder="••••••••" autocomplete="current-password" style="padding-right: 60px;">
                    <button type="button" class="btn-text btn-password-toggle" onclick="PrakashMart.togglePassword('password', this)" style="position: absolute; right: 4px; padding: 4px 8px; font-size: 0.75rem; color: var(--text-muted);" aria-label="Toggle password visibility">
                        Show
                    </button>
                </div>
            </div>

            <div style="margin-top: var(--space-6);">
                <button type="submit" class="btn btn-primary btn-block btn-lg submit-btn">
                    Sign In &rarr;
                </button>
            </div>
        </form>

        <div style="margin-top: var(--space-6); padding-top: var(--space-4); border-top: 1px solid var(--border-subtle); text-align: center; font-size: 0.875rem; color: var(--text-muted);">
            Don't have an account yet? 
            <a href="${pageContext.request.contextPath}/register" style="font-weight: 600; color: var(--color-accent);">Create an account</a>
        </div>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
