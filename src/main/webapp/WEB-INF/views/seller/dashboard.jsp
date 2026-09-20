<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Seller Hub — PrakashMart" scope="request" />
<jsp:include page="../common/header.jsp" />

<div class="container" style="padding-top: var(--space-6); padding-bottom: var(--space-12);">
    <!-- Breadcrumb & Header -->
    <p style="font-size: 0.8125rem; color: var(--text-muted); margin-bottom: var(--space-6);">
        <a href="${pageContext.request.contextPath}/home">Home</a> &rsaquo;
        <span>Seller Hub</span>
    </p>

    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-8); flex-wrap: wrap; gap: var(--space-4);">
        <div>
            <h1 style="font-size: 2.25rem; margin-bottom: var(--space-1);">Seller Operations Hub</h1>
            <p style="color: var(--text-muted); font-size: 0.9375rem; margin: 0;">Manage your catalog inventory, fulfill buyer orders, and monitor sales performance</p>
        </div>
        <button type="button" class="btn btn-primary" onclick="PrakashMart.openModal('addProductModal')">
            &plus; Add New Creation
        </button>
    </div>

    <!-- Feedback Alerts -->
    <c:if test="${param.created == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Listing Published!</strong> Your new creation is now live on the marketplace.
        </div>
    </c:if>
    <c:if test="${param.updated == 'true'}">
        <div class="alert alert-success" style="background-color: var(--success-bg); color: var(--success-text); border: 1px solid var(--success-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>&#10003; Update Confirmed!</strong> Listing details or order status successfully saved.
        </div>
    </c:if>
    <c:if test="${param.deleted == 'true'}">
        <div class="alert alert-warning" style="background-color: var(--warning-bg); color: var(--warning-text); border: 1px solid var(--warning-border); padding: var(--space-4); border-radius: var(--radius-sm); margin-bottom: var(--space-6);">
            <strong>Listing Removed.</strong> The selected creation has been removed from the catalog.
        </div>
    </c:if>

    <!-- KPI Metric Cards Grid -->
    <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-6); margin-bottom: var(--space-10);">
        <div class="card" style="padding: var(--space-6); border-left: 4px solid var(--color-primary);">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Active Listings</div>
            <div style="font-size: 2rem; font-weight: 700; color: var(--color-primary); margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.totalProducts != null ? dashboard.totalProducts : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Published creations</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid var(--color-accent);">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Gross Merchant Sales</div>
            <div style="font-size: 2rem; font-weight: 700; color: var(--color-accent); margin-top: var(--space-2); font-family: var(--font-serif);">
                $<c:out value="${dashboard.totalRevenue != null ? dashboard.totalRevenue : '0.00'}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Verified order GMV</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid #10b981;">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Customer Orders</div>
            <div style="font-size: 2rem; font-weight: 700; color: #10b981; margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.totalOrders != null ? dashboard.totalOrders : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Assigned buyer orders</div>
        </div>

        <div class="card" style="padding: var(--space-6); border-left: 4px solid #f59e0b;">
            <div style="font-size: 0.8125rem; color: var(--text-muted); text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600;">Low Stock Warnings</div>
            <div style="font-size: 2rem; font-weight: 700; color: #f59e0b; margin-top: var(--space-2); font-family: var(--font-serif);">
                <c:out value="${dashboard.lowStockCount != null ? dashboard.lowStockCount : 0}" />
            </div>
            <div style="font-size: 0.8125rem; color: var(--text-muted); margin-top: 4px;">Items below 5 in inventory</div>
        </div>
    </div>

    <!-- Section 1: Product Inventory Management -->
    <div class="card" style="padding: var(--space-6); margin-bottom: var(--space-10);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-6); padding-bottom: var(--space-4); border-bottom: 1px solid var(--border-subtle);">
            <div>
                <h2 style="font-size: 1.35rem; margin-bottom: var(--space-1);">Catalog Inventory Management</h2>
                <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0;">Update prices, adjust stock levels, or retire existing listings</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty products}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Creation</th>
                                <th>Category</th>
                                <th>Unit Price</th>
                                <th>Stock Level</th>
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
                                                        <span style="color: var(--text-muted); font-size: 1.1rem;">&#9733;</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div>
                                                <a href="${pageContext.request.contextPath}/product?id=${p.id}" target="_blank" style="font-weight: 600; color: var(--color-primary);">
                                                    <c:out value="${p.name}" />
                                                </a>
                                                <div style="font-size: 0.75rem; color: var(--text-muted);">ID: #<c:out value="${p.id}" /></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td><span class="badge badge-gold"><c:out value="${p.category}" /></span></td>
                                    <td><strong style="font-family: var(--font-serif); font-size: 1.05rem;">$<c:out value="${p.price}" /></strong></td>
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
                                        <div style="display: inline-flex; gap: var(--space-2);">
                                            <form action="${pageContext.request.contextPath}/seller/products" method="POST" style="display: inline;" onsubmit="return confirm('Are you sure you wish to delete this creation?');">
                                                <input type="hidden" name="_csrf" value="${csrfToken}">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="productId" value="${p.id}">
                                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                            </form>
                                        </div>
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

    <!-- Section 2: Seller Orders & Status Dispatch -->
    <div class="card" style="padding: var(--space-6);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-6); padding-bottom: var(--space-4); border-bottom: 1px solid var(--border-subtle);">
            <div>
                <h2 style="font-size: 1.35rem; margin-bottom: var(--space-1);">Fulfillment &amp; Customer Orders</h2>
                <p style="color: var(--text-muted); font-size: 0.875rem; margin: 0;">Review orders containing your listings and update shipping dispatch statuses</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty orders}">
                <div class="table-responsive">
                    <table class="table-premium">
                        <thead>
                            <tr>
                                <th>Order #</th>
                                <th>Placed Date</th>
                                <th>Total Amount</th>
                                <th>Current Status</th>
                                <th style="text-align: right;">Update Dispatch Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="o" items="${orders}">
                                <tr>
                                    <td><strong>#<c:out value="${o.id}" /></strong></td>
                                    <td style="color: var(--text-secondary); font-size: 0.875rem;"><c:out value="${o.createdAt}" /></td>
                                    <td><strong style="font-family: var(--font-serif); color: var(--color-primary); font-size: 1.05rem;">$<c:out value="${o.totalAmount}" /></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${o.status == 'DELIVERED'}">
                                                <span class="badge badge-in-stock">Delivered</span>
                                            </c:when>
                                            <c:when test="${o.status == 'SHIPPED'}">
                                                <span class="badge badge-gold">In Transit</span>
                                            </c:when>
                                            <c:when test="${o.status == 'CANCELLED'}">
                                                <span class="badge badge-out-of-stock">Cancelled</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-warning"><c:out value="${o.status}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: right;">
                                        <form action="${pageContext.request.contextPath}/seller/orders/status" method="POST" style="display: inline-flex; gap: var(--space-2); align-items: center;">
                                            <input type="hidden" name="_csrf" value="${csrfToken}">
                                            <input type="hidden" name="orderId" value="${o.id}">
                                            <select name="status" class="form-select form-select-sm" style="width: auto; padding: 0.3rem 0.6rem; font-size: 0.8125rem;">
                                                <option value="PENDING" ${o.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                                                <option value="CONFIRMED" ${o.status == 'CONFIRMED' ? 'selected' : ''}>Confirmed</option>
                                                <option value="SHIPPED" ${o.status == 'SHIPPED' ? 'selected' : ''}>Shipped</option>
                                                <option value="DELIVERED" ${o.status == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                                                <option value="CANCELLED" ${o.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                            </select>
                                            <button type="submit" class="btn btn-outline btn-sm">Update</button>
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
                    <div class="empty-state-icon">&#128230;</div>
                    <h3 class="empty-state-title">No Orders Received</h3>
                    <p class="empty-state-desc">Buyer orders containing your creations will appear here for dispatch tracking.</p>
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
