<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="My Products — Seller Hub" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <a href="${pageContext.request.contextPath}/seller/dashboard">Seller Hub</a> &rsaquo;
        <span>Product Listings</span>
    </p>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-8); flex-wrap: wrap; gap: var(--space-4);">
        <div>
            <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Catalog Listings</h1>
            <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Manage and publish your marketplace creations</p>
        </div>
        <button type="button" class="btn btn-primary" onclick="PrakashMart.openModal('addProductModal')">
            &plus; Add New Creation
        </button>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${param.created == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Listing Published!</strong> Your new creation is live.
        </div>
    </c:if>
    <c:if test="${param.updated == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Listing Updated!</strong> Details saved.
        </div>
    </c:if>
    <c:if test="${param.deleted == 'true'}">
        <div class="alert alert-warning" style="background-color: var(--warning-bg); color: var(--warning-text); border: 1px solid var(--warning-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>Listing Deleted.</strong> Product has been removed.
        </div>
    </c:if>

    <div class="card" style="padding: var(--space-6);">
        <c:choose>
            <c:when test="${not empty products}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Category</th>
                                <th>Price</th>
                                <th>Stock</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="p" items="${products}">
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: var(--space-3);">
                                            <div style="width: 44px; height: 44px; border-radius: var(--radius-sm); overflow: hidden; background-color: #fafbfc; border: 1px solid var(--border-subtle); flex-shrink: 0; display: flex; align-items: center; justify-content: center;">
                                                <c:choose>
                                                    <c:when test="${not empty p.imageUrl}">
                                                        <img src="<c:out value='${p.imageUrl}' />" alt="<c:out value='${p.name}' />" style="width: 100%; height: 100%; object-fit: cover;">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: var(--text-muted);">&#9733;</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" style="font-weight: 600; color: var(--color-primary);">
                                                    <c:out value="${p.name}" />
                                                </a>
                                                <div style="font-size: 0.75rem; color: var(--text-muted);">ID: #${p.id}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge badge-gold"><c:out value="${p.category}" /></span></td>
                                    <td><strong style="font-family: var(--font-serif);">$<c:out value="${p.price}" /></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${p.stock <= 5}">
                                                <span class="badge badge-out-of-stock"><c:out value="${p.stock}" /> Low Stock</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-in-stock"><c:out value="${p.stock}" /> in Stock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <form action="${pageContext.request.contextPath}/seller/products" method="POST" style="display: inline;" onsubmit="return confirm('Are you sure you wish to delete this creation?');">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="productId" value="${p.id}">
                                            <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                        </form>
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
                    <h3 class="empty-state-title">No Catalog Listings Published</h3>
                    <p class="empty-state-desc">Begin your merchant journey by publishing your first crafted product listing.</p>
                    <button type="button" class="btn btn-primary" onclick="PrakashMart.openModal('addProductModal')">&plus; Add First Creation</button>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<!-- Modal Dialog: Add New Product -->
<div id="addProductModal" class="modal-backdrop">
    <div class="modal-dialog" style="max-width: 560px;">
        <div class="modal-header">
            <h3 class="modal-title">Publish New Catalog Creation</h3>
            <button type="button" class="modal-close" onclick="PrakashMart.closeModal('addProductModal')">&times;</button>
        </div>
        <form action="${pageContext.request.contextPath}/seller/products" method="POST" onsubmit="PrakashMart.showFormLoading(this)">
            <input type="hidden" name="_csrf" value="${csrfToken}">
            <div class="modal-body" style="padding: var(--space-6);">
                <div class="form-group">
                    <label for="newProdName" class="form-label">Creation Title <span class="required">*</span></label>
                    <input type="text" id="newProdName" name="name" class="form-control" required placeholder="e.g. Ergonomic Walnut Mechanical Keyboard">
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4);">
                    <div class="form-group">
                        <label for="newProdCategory" class="form-label">Category <span class="required">*</span></label>
                        <select id="newProdCategory" name="category" class="form-select" required>
                            <option value="Electronics">Electronics</option>
                            <option value="Books">Books</option>
                            <option value="Clothing">Clothing</option>
                            <option value="Home">Home &amp; Kitchen</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="newProdPrice" class="form-label">Price ($ USD) <span class="required">*</span></label>
                        <input type="number" id="newProdPrice" name="price" class="form-control" min="0.01" step="0.01" required placeholder="149.99">
                    </div>
                </div>

                <div class="form-group">
                    <label for="newProdStock" class="form-label">Initial Inventory Quantity <span class="required">*</span></label>
                    <input type="number" id="newProdStock" name="stock" class="form-control" min="1" step="1" required value="25">
                </div>

                <div class="form-group">
                    <label for="newProdImage" class="form-label">Image URL (Unsplash or direct asset)</label>
                    <input type="url" id="newProdImage" name="imageUrl" class="form-control" placeholder="https://images.unsplash.com/photo-...">
                </div>

                <div class="form-group" style="margin-bottom: 0;">
                    <label for="newProdDesc" class="form-label">Detailed Description <span class="required">*</span></label>
                    <textarea id="newProdDesc" name="description" class="form-control" rows="3" required placeholder="Describe craftsmanship, technical specifications, and key features..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="PrakashMart.closeModal('addProductModal')">Cancel</button>
                <button type="submit" class="btn btn-primary submit-btn">Publish Listing &rarr;</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="../common/footer.jsp" />
