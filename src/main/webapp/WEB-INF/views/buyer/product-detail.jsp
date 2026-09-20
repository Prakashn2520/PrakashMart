<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="${product.name} — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/products">Catalog</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/products?category=${product.category}"><c:out value="${product.category}" /></a> &rsaquo;
        <span><c:out value="${product.name}" /></span>
    </p>

    <!-- Main Product Presentation Grid -->
    <div class="product-detail-grid">
        <!-- Media / Gallery Column -->
        <div class="product-gallery">
            <div class="product-gallery-main">
                <c:choose>
                    <c:when test="${not empty product.imageUrl}">
                        <img src="<c:out value='${product.imageUrl}' />" alt="<c:out value='${product.name}' />" loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="card-product-placeholder" style="padding: var(--space-12);">
                            <span style="font-size: 3rem; margin-bottom: 8px;">&#9733;</span>
                            <span style="font-size: 1.1rem; font-weight: 600;"><c:out value="${product.category}" /></span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:if test="${not empty product.imageUrl}">
                <div class="product-gallery-thumbs" style="display: flex; gap: var(--space-2); padding: var(--space-3); border-top: 1px solid var(--border-subtle); background-color: var(--bg-surface);">
                    <img src="<c:out value='${product.imageUrl}' />" alt="Primary View" class="product-thumb-img active" style="width: 54px; height: 54px; object-fit: cover; border-radius: var(--radius-sm); border: 2px solid var(--color-accent); cursor: pointer;" loading="lazy">
                </div>
            </c:if>
        </div>

        <!-- Details & Purchase Actions Column -->
        <div class="product-details-content">
            <span class="badge badge-gold" style="width: fit-content; margin-bottom: var(--space-2);">
                <c:out value="${product.category}" />
            </span>

            <h1 class="product-detail-title"><c:out value="${product.name}" /></h1>

            <div class="product-detail-meta">
                <div class="stars" style="font-size: 1rem;">
                    &#9733;&#9733;&#9733;&#9733;&#9733;
                </div>
                <span>(5.0 Rating)</span>
                &bull;
                <c:choose>
                    <c:when test="${product.stockQty > 0}">
                        <span class="badge badge-in-stock"><c:out value="${product.stockQty}" /> In Stock</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-out-of-stock">Currently Sold Out</span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="product-detail-price">
                $<c:out value="${product.price}" />
            </div>

            <div class="product-detail-desc">
                <p><c:out value="${product.description}" /></p>
            </div>

            <!-- Merchant & Authenticity Guarantee -->
            <div style="background-color: var(--bg-surface); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: var(--space-4); margin-bottom: var(--space-6);">
                <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">
                    Merchant Assurance
                </div>
                <div style="font-size: 0.9375rem; color: var(--text-primary); margin-top: 4px;">
                    Listed by Verified Artisan Seller &bull; Guaranteed Original with 7-Day Return Escrow
                </div>
            </div>

            <!-- Purchase Controls -->
            <form action="${pageContext.request.contextPath}/cart/add" method="POST" class="ajax-add-cart-form">
                <input type="hidden" name="productId" value="${product.id}">

                <div style="display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-4);">
                    <label for="detail-quantity" style="font-weight: 600; font-size: 0.9rem;">Quantity:</label>
                    <div class="qty-control">
                        <button type="button" class="qty-btn qty-dec" aria-label="Decrease quantity">&minus;</button>
                        <input type="number" id="detail-quantity" name="quantity" value="1" min="1" max="${product.stockQty}" class="qty-input" aria-label="Selected quantity">
                        <button type="button" class="qty-btn qty-inc" aria-label="Increase quantity">&plus;</button>
                    </div>
                </div>

                <div class="product-detail-purchase">
                    <button type="submit" class="btn btn-primary btn-lg" style="flex: 1;" ${product.stockQty <= 0 ? 'disabled' : ''}>
                        Add to Cart
                    </button>
                    <a href="${pageContext.request.contextPath}/cart" class="btn btn-gold btn-lg" style="flex: 1;">
                        Buy Now
                    </a>
                </div>
            </form>
        </div>
    </div>

    <!-- Verified Customer Reviews Section -->
    <section style="margin-top: var(--space-12); padding-top: var(--space-8); border-top: 1px solid var(--border-subtle);">
        <div class="section-header">
            <div>
                <h2 style="font-size: 1.75rem;">Verified Customer Reviews</h2>
                <p class="section-subtitle">Read feedback from authenticated buyers or share your own experience</p>
            </div>
            <c:if test="${not empty avgRating && avgRating > 0}">
                <div style="text-align: right;">
                    <div style="font-size: 1.5rem; font-weight: 700; color: var(--color-primary);">${avgRating} / 5.0</div>
                    <div class="stars">&#9733;&#9733;&#9733;&#9733;&#9733;</div>
                </div>
            </c:if>
        </div>

        <c:if test="${param.reviewed == 'true'}">
            <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
                <strong>&#10003; Review submitted successfully!</strong> Thank you for your feedback.
            </div>
        </c:if>

        <!-- Submit Review Form Card -->
        <c:choose>
            <c:when test="${not empty sessionScope.currentUser}">
                <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-8); background-color: var(--bg-surface);">
                    <h3 style="font-size: 1.15rem; margin-bottom: var(--space-3);">Leave a Review</h3>
                    <form action="${pageContext.request.contextPath}/reviews" method="POST" class="form-review" onsubmit="PrakashMart.showFormLoading(this)">
                        <input type="hidden" name="_csrf" value="${csrfToken}">
                        <input type="hidden" name="productId" value="${product.id}">

                        <div style="display: flex; gap: var(--space-6); margin-bottom: var(--space-4); flex-wrap: wrap;">
                            <div class="form-group" style="margin-bottom: 0; min-width: 180px;">
                                <label for="reviewRating" class="form-label">Your Rating <span class="required">*</span></label>
                                <select id="reviewRating" name="rating" class="form-select" required>
                                    <option value="5">&#9733;&#9733;&#9733;&#9733;&#9733; (5 - Exceptional)</option>
                                    <option value="4">&#9733;&#9733;&#9733;&#9733;&#9734; (4 - Very Good)</option>
                                    <option value="3">&#9733;&#9733;&#9733;&#9734;&#9734; (3 - Good)</option>
                                    <option value="2">&#9733;&#9733;&#9734;&#9734;&#9734; (2 - Fair)</option>
                                    <option value="1">&#9733;&#9734;&#9734;&#9734;&#9734; (1 - Poor)</option>
                                </select>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="reviewComment" class="form-label">Comments &amp; Impressions <span class="required">*</span></label>
                            <textarea id="reviewComment" name="comment" class="form-control" rows="3" required placeholder="Describe your experience with this creation (materials, delivery, satisfaction)..." style="resize: vertical;"></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary btn-sm submit-btn">
                            Submit Verified Review
                        </button>
                    </form>
                </div>
            </c:when>
            <c:otherwise>
                <div style="background-color: var(--bg-subtle); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: var(--space-4); margin-bottom: var(--space-6); text-align: center; font-size: 0.875rem;">
                    <span>Have you purchased this creation? <a href="${pageContext.request.contextPath}/login?redirect=${pageContext.request.requestURI}" style="font-weight: 600; color: var(--color-accent);">Sign in</a> to leave a verified review.</span>
                </div>
            </c:otherwise>
        </c:choose>

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
                    <p class="empty-state-desc">Be among the first discerning buyers to experience and review this creation after purchase.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</div>

<jsp:include page="../common/footer.jsp" />
