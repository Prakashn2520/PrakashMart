<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'PrakashMart — Curated Classical Marketplace'}" /></title>
    <meta name="description" content="PrakashMart - Premium Multi-Seller Marketplace built on Java Servlets, H2, and Tomcat.">
    <meta name="_csrf" content="${csrfToken}">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/main.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/layout.css">
</head>
<body>

<!-- Top Prestige Status Ribbon -->
<div class="prestige-bar">
    <div class="container prestige-container">
        <div>
            <span class="prestige-badge">Curated Marketplace</span> &bull; Verified Merchants &bull; Secure Checkout
        </div>
        <div>
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <span>Signed in as <strong><c:out value="${sessionScope.currentUser.name}" /></strong> (<c:out value="${sessionScope.currentUser.role}" />)</span>
                </c:when>
                <c:otherwise>
                    <span>Customer Concierge &amp; Order Inquiries</span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- Main Header -->
<header class="site-header">
    <div class="container header-inner">
        <!-- Brand Logo -->
        <a href="${pageContext.request.contextPath}/home" class="brand-logo" aria-label="PrakashMart Home">
            <div class="brand-symbol">P</div>
            <span class="brand-name">Prakash<span>Mart</span></span>
        </a>

        <!-- Universal Search Bar -->
        <div class="header-search">
            <form action="${pageContext.request.contextPath}/products" method="GET" class="search-bar-form" role="search">
                <select name="category" class="search-category-select" aria-label="Select Category">
                    <option value="">All Categories</option>
                    <option value="Electronics" ${selectedCategory == 'Electronics' ? 'selected' : ''}>Electronics</option>
                    <option value="Books" ${selectedCategory == 'Books' ? 'selected' : ''}>Books</option>
                    <option value="Clothing" ${selectedCategory == 'Clothing' ? 'selected' : ''}>Clothing</option>
                    <option value="Home" ${selectedCategory == 'Home' ? 'selected' : ''}>Home &amp; Kitchen</option>
                </select>
                <input type="search" name="keyword" value="<c:out value='${keyword}' />" class="search-input" placeholder="Search curated products by name or keyword..." aria-label="Search Catalog">
                <button type="submit" class="search-btn" aria-label="Submit Search">Search</button>
            </form>
        </div>

        <!-- Navigation Menu -->
        <nav aria-label="Main Navigation">
            <button class="mobile-toggle" aria-expanded="false" aria-label="Toggle navigation menu">&#9776;</button>
            <ul class="nav-menu">
                <li><a href="${pageContext.request.contextPath}/home" class="nav-link">Home</a></li>
                <li><a href="${pageContext.request.contextPath}/products" class="nav-link">Products</a></li>
                <li><a href="${pageContext.request.contextPath}/home#categories" class="nav-link">Categories</a></li>

                <c:if test="${sessionScope.currentUser.role == 'SELLER' || sessionScope.currentUser.role == 'ADMIN'}">
                    <li><a href="${pageContext.request.contextPath}/seller/dashboard" class="nav-link">Seller Hub</a></li>
                </c:if>
                <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                    <li><a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">Admin</a></li>
                </c:if>

                <c:choose>
                    <c:when test="${not empty sessionScope.currentUser}">
                        <li><a href="${pageContext.request.contextPath}/orders" class="nav-link">Orders</a></li>
                        <li>
                            <a href="${pageContext.request.contextPath}/cart" class="nav-link cart-indicator" aria-label="Shopping Cart">
                                Cart <span class="cart-count" style="display:none;">0</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline btn-sm">Logout</a>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li>
                            <a href="${pageContext.request.contextPath}/cart" class="nav-link cart-indicator" aria-label="Shopping Cart">
                                Cart <span class="cart-count" style="display:none;">0</span>
                            </a>
                        </li>
                        <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Sign In</a></li>
                        <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Join</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </nav>
    </div>
</header>

<main class="main-content" id="main-content">
