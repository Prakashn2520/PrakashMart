<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Curated Catalog — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Title -->
    <div style="margin-bottom: var(--space-6);">
        <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-1);">
            <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
            <span>Marketplace Catalog</span>
        </p>
        <h1 style="font-size: 2rem;">Curated Marketplace Catalog</h1>
    </div>

    <!-- Catalog Layout: Sidebar Filters + Main Grid -->
    <div class="catalog-layout">
        <!-- Sidebar Filter Form -->
        <aside class="catalog-sidebar">
            <div class="filter-header">
                <h3>Filter Selections</h3>
                <a href="${pageContext.request.contextPath}/products" class="btn btn-text btn-sm" style="font-size: 0.75rem; color: var(--color-accent);">Clear All</a>
            </div>

            <form action="${pageContext.request.contextPath}/products" method="GET" id="catalog-filter-form">
                <!-- Retain current search term -->
                <c:if test="${not empty keyword}">
                    <input type="hidden" name="keyword" value="<c:out value='${keyword}' />">
                </c:if>

                <!-- Categories Filter -->
                <div class="filter-section">
                    <h4 class="filter-title">Category</h4>
                    <ul class="filter-list">
                        <li>
                            <a href="${pageContext.request.contextPath}/products?keyword=${keyword}&sort=${sort}" 
                               class="${empty selectedCategory ? 'active' : ''}">
                                <span>All Categories</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=Electronics&keyword=${keyword}&sort=${sort}" 
                               class="${selectedCategory == 'Electronics' ? 'active' : ''}">
                                <span>Electronics</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=Books&keyword=${keyword}&sort=${sort}" 
                               class="${selectedCategory == 'Books' ? 'active' : ''}">
                                <span>Books</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=Clothing&keyword=${keyword}&sort=${sort}" 
                               class="${selectedCategory == 'Clothing' ? 'active' : ''}">
                                <span>Clothing</span>
                            </a>
                        </li>
                        <li>
                            <a href="${pageContext.request.contextPath}/products?category=Home&keyword=${keyword}&sort=${sort}" 
                               class="${selectedCategory == 'Home' ? 'active' : ''}">
                                <span>Home &amp; Kitchen</span>
                            </a>
                        </li>
                    </ul>
                    <input type="hidden" name="category" value="<c:out value='${selectedCategory}' />">
                </div>

                <!-- Price Range Filter -->
                <div class="filter-section">
                    <h4 class="filter-title">Price Range ($)</h4>
                    <div class="price-inputs">
                        <input type="number" name="minPrice" value="<c:out value='${minPrice}' />" placeholder="Min" min="0" step="1" class="form-control form-control-sm" style="padding: 0.4rem 0.5rem; font-size: 0.85rem;">
                        <span>&ndash;</span>
                        <input type="number" name="maxPrice" value="<c:out value='${maxPrice}' />" placeholder="Max" min="0" step="1" class="form-control form-control-sm" style="padding: 0.4rem 0.5rem; font-size: 0.85rem;">
                    </div>
                </div>

                <!-- Stock Filter -->
                <div class="filter-section">
                    <h4 class="filter-title">Availability</h4>
                    <label style="display: flex; align-items: center; gap: var(--space-2); font-size: 0.875rem; cursor: pointer;">
                        <input type="checkbox" name="inStock" value="true" ${inStock ? 'checked' : ''} onchange="this.form.submit()">
                        <span>In Stock Only</span>
                    </label>
                </div>

                <!-- Rating Filter (UI filter element) -->
                <div class="filter-section">
                    <h4 class="filter-title">Minimum Rating</h4>
                    <label style="display: flex; align-items: center; gap: var(--space-2); font-size: 0.875rem; margin-bottom: 6px; cursor: pointer;">
                        <input type="radio" name="rating" value="4" ${param.rating == '4' ? 'checked' : ''} onchange="this.form.submit()">
                        <span class="stars">&#9733;&#9733;&#9733;&#9733;&#9734;</span> &amp; Up
                    </label>
                    <label style="display: flex; align-items: center; gap: var(--space-2); font-size: 0.875rem; cursor: pointer;">
                        <input type="radio" name="rating" value="3" ${param.rating == '3' ? 'checked' : ''} onchange="this.form.submit()">
                        <span class="stars">&#9733;&#9733;&#9733;&#9734;&#9734;</span> &amp; Up
                    </label>
                </div>

                <!-- Submit Button -->
                <div style="margin-top: var(--space-4);">
                    <button type="submit" class="btn btn-primary btn-block btn-sm">Apply Filters</button>
                </div>
            </form>
        </aside>

        <!-- Main Product Content Area -->
        <main>
            <!-- Toolbar: Result Count & Sorting -->
            <div class="catalog-toolbar">
                <div class="catalog-results-count">
                    Showing <strong><c:out value="${paginated != null ? paginated.data.size() : products.size()}" /></strong> of 
                    <strong><c:out value="${paginated != null ? paginated.totalResults : products.size()}" /></strong> creations
                    <c:if test="${not empty keyword}">
                        for "<em><c:out value="${keyword}" /></em>"
                    </c:if>
                </div>

                <div style="display: flex; align-items: center; gap: var(--space-2);">
                    <label for="sort-select" style="font-size: 0.8125rem; color: var(--text-muted);">Sort By:</label>
                    <select id="sort-select" class="form-select" style="width: auto; padding: 0.35rem 0.75rem; font-size: 0.8125rem;" 
                            onchange="location = this.value;">
                        <option value="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=" ${empty sort ? 'selected' : ''}>Newest First</option>
                        <option value="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Price: Low to High</option>
                        <option value="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Price: High to Low</option>
                        <option value="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Name: A to Z</option>
                    </select>
                </div>
            </div>

            <!-- Product Grid -->
            <div class="product-grid">
                <c:choose>
                    <c:when test="${not empty products}">
                        <c:forEach var="p" items="${products}">
                            <div class="card-product">
                                <div class="card-product-media">
                                    <button type="button" class="btn-wishlist" aria-label="Save to Wishlist">&#9825;</button>
                                    <c:choose>
                                        <c:when test="${not empty p.imageUrl}">
                                            <img src="<c:out value='${p.imageUrl}' />" alt="<c:out value='${p.name}' />" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="card-product-placeholder">
                                                <span style="font-size: 1.75rem; margin-bottom: 4px;">&#9733;</span>
                                                <span><c:out value="${p.category}" /></span>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div class="card-product-body">
                                    <span class="card-product-category"><c:out value="${p.category}" /></span>
                                    <h3 class="card-product-title">
                                        <a href="${pageContext.request.contextPath}/product?id=${p.id}"><c:out value="${p.name}" /></a>
                                    </h3>
                                    <div class="card-product-rating">
                                        <span class="stars">&#9733;&#9733;&#9733;&#9733;&#9733;</span>
                                        <span>(5.0)</span>
                                    </div>
                                    <div class="card-product-price-row">
                                        <span class="card-product-price">$<c:out value="${p.price}" /></span>
                                        <c:choose>
                                            <c:when test="${p.stockQty > 0}">
                                                <span class="badge badge-in-stock">In Stock</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-out-of-stock">Sold Out</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="card-product-actions">
                                        <form action="${pageContext.request.contextPath}/cart/add" method="POST" class="ajax-add-cart-form" style="width: 100%;">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <input type="hidden" name="quantity" value="1">
                                            <button type="submit" class="btn btn-primary btn-block btn-sm" ${p.stockQty <= 0 ? 'disabled' : ''}>
                                                Add to Cart
                                            </button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="empty-state" style="grid-column: 1 / -1;">
                            <div class="empty-state-icon">&#128269;</div>
                            <h3 class="empty-state-title">No Matching Creations Found</h3>
                            <p class="empty-state-desc">We could not find any items matching your selected criteria. Try adjusting keywords or clearing category and price filters.</p>
                            <a href="${pageContext.request.contextPath}/products" class="btn btn-outline">Reset All Filters</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Pagination Bar -->
            <c:if test="${paginated != null && paginated.totalPages > 1}">
                <nav class="pagination-bar" aria-label="Pagination">
                    <c:if test="${paginated.currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=${sort}&page=${paginated.currentPage - 1}" 
                           class="page-link" aria-label="Previous Page">&laquo;</a>
                    </c:if>

                    <c:forEach begin="1" end="${paginated.totalPages}" var="pageNo">
                        <a href="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=${sort}&page=${pageNo}" 
                           class="page-link ${paginated.currentPage == pageNo ? 'active' : ''}">
                            <c:out value="${pageNo}" />
                        </a>
                    </c:forEach>

                    <c:if test="${paginated.currentPage < paginated.totalPages}">
                        <a href="${pageContext.request.contextPath}/products?category=${selectedCategory}&keyword=${keyword}&minPrice=${minPrice}&maxPrice=${maxPrice}&inStock=${inStock}&sort=${sort}&page=${paginated.currentPage + 1}" 
                           class="page-link" aria-label="Next Page">&raquo;</a>
                    </c:if>
                </nav>
            </c:if>
        </main>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
