package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.CartDAOImpl;
import com.prakash.prakashmart.dao.impl.OrderDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.service.impl.OrderServiceImpl;
import com.prakash.prakashmart.service.impl.ProductServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller/Servlet managing seller-specific operations:
 * - Seller Dashboard (metrics, summary)
 * - Product Management (create, update, delete own products)
 * - Order Management (view seller orders, update status)
 */
@WebServlet(name = "SellerServlet", urlPatterns = {
        "/seller/dashboard",
        "/seller/products", "/seller/products/*",
        "/seller/orders", "/seller/orders/*",
        "/api/seller/*", "/api/v1/seller/*"
})
public class SellerServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(SellerServlet.class);

    private ProductService productService;
    private OrderService orderService;

    public SellerServlet() {
    }

    public SellerServlet(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    public void init() {
        if (this.productService == null) {
            this.productService = new ProductServiceImpl(new ProductDAOImpl());
        }
        if (this.orderService == null) {
            this.orderService = new OrderServiceImpl(new OrderDAOImpl(), new CartDAOImpl(), new ProductDAOImpl());
        }
    }

    private UserResponseDTO requireSeller(HttpServletRequest req) throws AuthorizationException {
        UserResponseDTO user = getSessionUser(req);
        if (user == null || (!Role.SELLER.name().equalsIgnoreCase(user.getRole()) && !Role.ADMIN.name().equalsIgnoreCase(user.getRole()))) {
            throw new AuthorizationException("Seller privileges required to access this endpoint");
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        boolean isApi = uri.contains("/api/");

        try {
            UserResponseDTO seller = requireSeller(req);
            Long sellerId = seller.getId();

            if (uri.contains("/dashboard")) {
                handleDashboard(req, resp, sellerId, isApi);
            } else if (uri.contains("/products")) {
                handleGetProducts(req, resp, sellerId, isApi);
            } else if (uri.contains("/orders")) {
                handleGetOrders(req, resp, sellerId, isApi);
            } else {
                handleDashboard(req, resp, sellerId, isApi);
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        boolean isApi = uri.contains("/api/");

        try {
            UserResponseDTO seller = requireSeller(req);
            Long sellerId = seller.getId();

            String methodOverride = req.getParameter("_method");
            String action = req.getParameter("action");

            if ("PUT".equalsIgnoreCase(methodOverride) || "update".equalsIgnoreCase(action)) {
                doPut(req, resp);
                return;
            } else if ("DELETE".equalsIgnoreCase(methodOverride) || "delete".equalsIgnoreCase(action)) {
                doDelete(req, resp);
                return;
            }

            if (uri.contains("/orders") && uri.contains("/status")) {
                handleUpdateOrderStatus(req, resp, isApi);
            } else if (uri.contains("/products")) {
                handleCreateProduct(req, resp, sellerId, isApi);
            } else {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        boolean isApi = uri.contains("/api/");

        try {
            UserResponseDTO seller = requireSeller(req);
            Long sellerId = seller.getId();

            if (uri.contains("/orders") && uri.contains("/status")) {
                handleUpdateOrderStatus(req, resp, isApi);
            } else if (uri.contains("/products")) {
                handleUpdateProduct(req, resp, sellerId, isApi);
            } else {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String uri = req.getRequestURI();
        boolean isApi = uri.contains("/api/");

        try {
            UserResponseDTO seller = requireSeller(req);
            Long sellerId = seller.getId();

            Long productId = parseIdFromPath(req.getPathInfo());
            if (productId == null && req.getParameter("productId") != null) {
                productId = Long.parseLong(req.getParameter("productId").trim());
            }

            if (productId == null) {
                throw new ValidationException("Product ID is required for deletion", "MISSING_PRODUCT_ID");
            }

            productService.deleteProduct(productId, sellerId);

            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, "Product deleted successfully", null);
            } else {
                resp.sendRedirect(req.getContextPath() + "/seller/products?deleted=true");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp, Long sellerId, boolean isApi)
            throws Exception {
        List<ProductDTO> products = productService.getProductsBySeller(sellerId);
        List<Order> orders = orderService.getOrdersForSeller(sellerId);

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long lowStockCount = products.stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 5)
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalProducts", products.size());
        summary.put("totalOrders", orders.size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("lowStockCount", lowStockCount);
        summary.put("products", products);
        summary.put("orders", orders);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Seller dashboard data retrieved", summary);
        } else {
            req.setAttribute("dashboard", summary);
            req.setAttribute("products", products);
            req.setAttribute("orders", orders);
            forwardToJsp(req, resp, "seller/dashboard.jsp");
        }
    }

    private void handleGetProducts(HttpServletRequest req, HttpServletResponse resp, Long sellerId, boolean isApi)
            throws Exception {
        List<ProductDTO> products = productService.getProductsBySeller(sellerId);
        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, products);
        } else {
            req.setAttribute("products", products);
            forwardToJsp(req, resp, "seller/products.jsp");
        }
    }

    private void handleGetOrders(HttpServletRequest req, HttpServletResponse resp, Long sellerId, boolean isApi)
            throws Exception {
        List<Order> orders = orderService.getOrdersForSeller(sellerId);
        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, orders);
        } else {
            req.setAttribute("orders", orders);
            forwardToJsp(req, resp, "seller/orders.jsp");
        }
    }

    private void handleCreateProduct(HttpServletRequest req, HttpServletResponse resp, Long sellerId, boolean isApi)
            throws Exception {
        ProductDTO dto = readJsonBody(req, ProductDTO.class);
        if (dto == null) {
            dto = new ProductDTO();
            dto.setName(req.getParameter("name"));
            dto.setDescription(req.getParameter("description"));
            dto.setCategory(req.getParameter("category"));
            dto.setImageUrl(req.getParameter("imageUrl"));
            if (req.getParameter("price") != null) {
                dto.setPrice(new BigDecimal(req.getParameter("price").trim()));
            }
            if (req.getParameter("stock") != null) {
                dto.setStock(Integer.parseInt(req.getParameter("stock").trim()));
            }
        }
        dto.setSellerId(sellerId);

        ProductDTO created = productService.createProduct(dto);
        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_CREATED, "Product created successfully", created);
        } else {
            resp.sendRedirect(req.getContextPath() + "/seller/products?created=true");
        }
    }

    private void handleUpdateProduct(HttpServletRequest req, HttpServletResponse resp, Long sellerId, boolean isApi)
            throws Exception {
        ProductDTO dto = readJsonBody(req, ProductDTO.class);
        if (dto == null) {
            dto = new ProductDTO();
            Long id = parseIdFromPath(req.getPathInfo());
            if (id == null && req.getParameter("id") != null) {
                id = Long.parseLong(req.getParameter("id").trim());
            }
            dto.setId(id);
            dto.setName(req.getParameter("name"));
            dto.setDescription(req.getParameter("description"));
            dto.setCategory(req.getParameter("category"));
            dto.setImageUrl(req.getParameter("imageUrl"));
            if (req.getParameter("price") != null) {
                dto.setPrice(new BigDecimal(req.getParameter("price").trim()));
            }
            if (req.getParameter("stock") != null) {
                dto.setStock(Integer.parseInt(req.getParameter("stock").trim()));
            }
        }
        dto.setSellerId(sellerId);

        productService.updateProduct(dto);
        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Product updated successfully", dto);
        } else {
            resp.sendRedirect(req.getContextPath() + "/seller/products?updated=true");
        }
    }

    private void handleUpdateOrderStatus(HttpServletRequest req, HttpServletResponse resp, boolean isApi)
            throws Exception {
        Long orderId = parseIdFromPath(req.getPathInfo());
        String statusStr = null;

        @SuppressWarnings("unchecked")
        Map<String, Object> body = readJsonBody(req, Map.class);
        if (body != null && body.containsKey("status")) {
            statusStr = String.valueOf(body.get("status"));
            if (orderId == null && body.containsKey("orderId")) {
                orderId = Long.parseLong(String.valueOf(body.get("orderId")));
            }
        }

        if (statusStr == null) {
            statusStr = req.getParameter("status");
        }
        if (orderId == null && req.getParameter("orderId") != null) {
            orderId = Long.parseLong(req.getParameter("orderId").trim());
        }

        if (orderId == null) {
            throw new ValidationException("Order ID is required", "MISSING_ORDER_ID");
        }
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new ValidationException("New order status is required", "MISSING_STATUS");
        }

        OrderStatus newStatus = OrderStatus.fromString(statusStr);
        orderService.updateOrderStatus(orderId, newStatus);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Order status updated to " + newStatus, null);
        } else {
            resp.sendRedirect(req.getContextPath() + "/seller/orders?updated=true");
        }
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        for (String part : parts) {
            if (!part.isEmpty() && !"status".equalsIgnoreCase(part)) {
                try {
                    return Long.parseLong(part);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
