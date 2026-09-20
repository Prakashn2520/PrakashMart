<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="PrakashMart — Curated Classical Marketplace" scope="request" />
<jsp:include page="common/header.jsp" />

<!-- Hero Section -->
<section class="hero-section">
    <div class="container hero-grid">
        <div>
            <div class="hero-tag">
                <span>&diams;</span> The Standard of Distinction
            </div>
            <h1 class="hero-headline">
                Crafted for Excellence,<br>Curated for the Discerning.
            </h1>
            <p class="hero-subhead">
                Discover a premier collection of authentic electronics, enduring literature, fine apparel, and artisanal home essentials direct from vetted merchants.
            </p>
            <div class="hero-actions">
                <a href="${pageContext.request.contextPath}/products" class="btn btn-primary btn-lg">Explore Catalog</a>
                <a href="${pageContext.request.contextPath}/register" class="btn btn-outline btn-lg">Become a Merchant</a>
            </div>

            <div class="hero-stats">
                <div class="stat-item">
                    <h4>100%</h4>
                    <p>Verified Authentic</p>
                </div>
                <div class="stat-item">
                    <h4>7-Day</h4>
                    <p>Hassle-Free Returns</p>
                </div>
                <div class="stat-item">
                    <h4>24 / 7</h4>
                    <p>Concierge Support</p>
                </div>
            </div>
        </div>

        <div>
            <div class="hero-media-card">
                <div style="aspect-ratio: 4/3; background: #f8fafc; border-radius: var(--radius-md); overflow: hidden; display: flex; align-items: center; justify-content: center; border: 1px solid var(--border-subtle);">
                    <img src="https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80" 
                         alt="PrakashMart Featured Collection" 
                         style="width: 100%; height: 100%; object-fit: cover;"
                         loading="lazy">
                </div>
                <div style="margin-top: var(--space-4); display: flex; justify-content: space-between; align-items: baseline;">
                    <div>
                        <span class="badge badge-gold">Featured Curation</span>
                        <h4 style="margin-top: var(--space-1); font-size: 1.15rem;">Studio Audio &amp; Acoustics</h4>
                    </div>
                    <a href="${pageContext.request.contextPath}/products?category=Electronics" class="btn btn-text btn-sm">Browse &rarr;</a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Pillars of Distinction -->
<section class="pillars-section">
    <div class="container pillars-grid">
        <div class="pillar-card">
            <div class="pillar-icon">&check;</div>
            <div>
                <h4>Vetted Merchants</h4>
                <p>Every seller undergoes identity and catalog authentication before listing.</p>
            </div>
        </div>
        <div class="pillar-card">
            <div class="pillar-icon">&reg;</div>
            <div>
                <h4>Certified Originality</h4>
                <p>Guaranteed authentic items backed by direct seller warranties.</p>
            </div>
        </div>
        <div class="pillar-card">
            <div class="pillar-icon">&infin;</div>
            <div>
                <h4>Escrow Protection</h4>
                <p>Funds remain secured until your delivery is safely confirmed.</p>
            </div>
        </div>
        <div class="pillar-card">
            <div class="pillar-icon">&hearts;</div>
            <div>
                <h4>Customer Concierge</h4>
                <p>Dedicated customer care team available for queries and returns.</p>
            </div>
        </div>
    </div>
</section>

<!-- Curated Categories Section -->
<section class="container" id="categories" style="padding-top: var(--space-12); padding-bottom: var(--space-6);">
    <div class="section-header">
        <div>
            <h2 class="section-title">Curated Categories</h2>
            <p class="section-subtitle">Explore fine selections arranged by craft and discipline</p>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">View All Categories &rarr;</a>
    </div>

    <div class="category-grid">
        <a href="${pageContext.request.contextPath}/products?category=Electronics" class="card-category">
            <div class="category-icon">&#128266;</div>
            <h3 class="category-title">Electronics</h3>
            <p style="font-size: 0.8125rem; color: var(--text-muted); margin: 0;">Audio, Computing &amp; Optics</p>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Books" class="card-category">
            <div class="category-icon">&#128218;</div>
            <h3 class="category-title">Literature</h3>
            <p style="font-size: 0.8125rem; color: var(--text-muted); margin: 0;">Rare Prints, Fiction &amp; Reference</p>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Clothing" class="card-category">
            <div class="category-icon">&#128084;</div>
            <h3 class="category-title">Apparel</h3>
            <p style="font-size: 0.8125rem; color: var(--text-muted); margin: 0;">Textiles, Leathercraft &amp; Attire</p>
        </a>
        <a href="${pageContext.request.contextPath}/products?category=Home" class="card-category">
            <div class="category-icon">&#127968;</div>
            <h3 class="category-title">Home Sanctuary</h3>
            <p style="font-size: 0.8125rem; color: var(--text-muted); margin: 0;">Cookware, Décor &amp; Furniture</p>
        </a>
    </div>
</section>

<!-- Featured Products Section -->
<section class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <div class="section-header">
        <div>
            <h2 class="section-title">Featured Creations</h2>
            <p class="section-subtitle">Hand-picked by our editors for craftsmanship and acclaim</p>
        </div>
        <a href="${pageContext.request.contextPath}/products" class="btn btn-outline btn-sm">Browse All Products &rarr;</a>
    </div>

    <div class="product-grid">
        <c:choose>
            <c:when test="${not empty featuredProducts}">
                <c:forEach var="p" items="${featuredProducts}">
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
                                <span>(Verified)</span>
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
                    <div class="empty-state-icon">&#128230;</div>
                    <h3 class="empty-state-title">Catalog Being Curated</h3>
                    <p class="empty-state-desc">Our merchants are preparing new selections. Visit our full catalog to discover all active listings.</p>
                    <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">View Products</a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</section>

<!-- Promotional / Seller Spotlight Banner -->
<section class="container" style="padding-bottom: var(--space-12);">
    <div class="promo-banner">
        <div class="promo-content">
            <span class="badge badge-gold" style="margin-bottom: var(--space-2);">Merchant Enrollment</span>
            <h3>Sell With Prestige On PrakashMart</h3>
            <p>
                Reach connoisseurs and institutional buyers. We provide zero upfront listing fees, transparent payouts, and enterprise analytics.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/register?role=SELLER" class="btn btn-gold btn-lg">Apply as Merchant</a>
        </div>
    </div>
</section>

<jsp:include page="common/footer.jsp" />
