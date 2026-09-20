package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.CartDAOImpl;
import com.prakash.prakashmart.dao.impl.OrderDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderStatus;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.service.UserService;
import com.prakash.prakashmart.service.impl.OrderServiceImpl;
import com.prakash.prakashmart.service.impl.ProductServiceImpl;
import com.prakash.prakashmart.service.impl.UserServiceImpl;
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
 * Controller/Servlet managing administrative moderation and metrics:
 * - Admin Dashboard (system-wide KPIs: users, products, orders, GMV)
 * - User Moderation (listing all users and inspecting profiles)
 * - Product Moderation (listing all catalog products)
 * - Order Moderation (viewing all platform orders and updating status)
 */
@WebServlet(name = "AdminServlet", urlPatterns = {
        "/admin/dashboard",
        "/admin/users", "/admin/users/*",
        "/admin/products", "/admin/products/*",
        "/admin/orders", "/admin/orders/*",
        "/api/admin/*", "/api/v1/admin/*"
})
public class AdminServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(AdminServlet.class);

    private UserService userService;
    private ProductService productService;
    private OrderService orderService;

    public AdminServlet() {
    }

    public AdminServlet(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @Override
    public void init() {
        if (this.userService == null) {
            this.userService = new UserServiceImpl(new UserDAOImpl());
        }
        if (this.productService == null) {
            this.productService = new ProductServiceImpl(new ProductDAOImpl());
        }
        if (this.orderService == null) {
            this.orderService = new OrderServiceImpl(new OrderDAOImpl(), new CartDAOImpl(), new ProductDAOImpl());
        }
    }

    private UserResponseDTO requireAdmin(HttpServletRequest req) throws AuthorizationException {
        UserResponseDTO user = getSessionUser(req);
        if (user == null || !Role.ADMIN.name().equalsIgnoreCase(user.getRole())) {
            throw new AuthorizationException("Admin privileges required to access this endpoint");
        }
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        boolean isApi = uri.contains("/api/");

        try {
            requireAdmin(req);

            if (uri.contains("/users")) {
                handleUsers(req, resp, isApi);
            } else if (uri.contains("/products")) {
                handleProducts(req, resp, isApi);
            } else if (uri.contains("/orders")) {
                handleOrders(req, resp, isApi);
            } else {
                handleDashboard(req, resp, isApi);
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
            requireAdmin(req);

            String methodOverride = req.getParameter("_method");
            if ("PUT".equalsIgnoreCase(methodOverride)) {
                doPut(req, resp);
                return;
            }

            if (uri.contains("/orders") && uri.contains("/status")) {
                handleUpdateOrderStatus(req, resp, isApi);
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
            requireAdmin(req);

            if (uri.contains("/orders") && uri.contains("/status")) {
                handleUpdateOrderStatus(req, resp, isApi);
            } else {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Endpoint not found");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    private void handleDashboard(HttpServletRequest req, HttpServletResponse resp, boolean isApi) throws Exception {
        List<UserResponseDTO> users = userService.getAllUsers();
        PaginatedResult<ProductDTO> productsResult = productService.getProductsPaginated(
                null, null, null, null, null, 1, 10, "id", "DESC");
        List<Order> orders = orderService.getAllOrders();

        BigDecimal totalGmv = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", users.size());
        dashboard.put("totalProducts", productsResult.getTotalElements());
        dashboard.put("totalOrders", orders.size());
        dashboard.put("totalGmv", totalGmv);
        dashboard.put("recentOrders", orders.size() > 10 ? orders.subList(0, 10) : orders);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Admin dashboard data retrieved", dashboard);
        } else {
            req.setAttribute("dashboard", dashboard);
            req.setAttribute("users", users);
            req.setAttribute("orders", orders);
            forwardToJsp(req, resp, "admin/dashboard.jsp");
        }
    }

    private void handleUsers(HttpServletRequest req, HttpServletResponse resp, boolean isApi) throws Exception {
        Long userId = parseIdFromPath(req.getPathInfo());
        if (userId != null) {
            UserResponseDTO user = userService.getUserById(userId);
            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, user);
            } else {
                req.setAttribute("userProfile", user);
                forwardToJsp(req, resp, "admin/user-detail.jsp");
            }
        } else {
            List<UserResponseDTO> users = userService.getAllUsers();
            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, users);
            } else {
                req.setAttribute("users", users);
                forwardToJsp(req, resp, "admin/users.jsp");
            }
        }
    }

    private void handleProducts(HttpServletRequest req, HttpServletResponse resp, boolean isApi) throws Exception {
        int page = 1;
        int size = 20;
        if (req.getParameter("page") != null) {
            try { page = Integer.parseInt(req.getParameter("page")); } catch (NumberFormatException ignored) {}
        }
        if (req.getParameter("size") != null) {
            try { size = Integer.parseInt(req.getParameter("size")); } catch (NumberFormatException ignored) {}
        }
        String keyword = req.getParameter("keyword");
        String category = req.getParameter("category");

        PaginatedResult<ProductDTO> result = productService.getProductsPaginated(
                keyword, category, null, null, null, page, size, "id", "DESC");

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, result);
        } else {
            req.setAttribute("productsResult", result);
            forwardToJsp(req, resp, "admin/products.jsp");
        }
    }

    private void handleOrders(HttpServletRequest req, HttpServletResponse resp, boolean isApi) throws Exception {
        Long orderId = parseIdFromPath(req.getPathInfo());
        if (orderId != null) {
            Order order = orderService.getOrderById(orderId);
            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, order);
            } else {
                req.setAttribute("order", order);
                forwardToJsp(req, resp, "admin/order-detail.jsp");
            }
        } else {
            List<Order> orders = orderService.getAllOrders();
            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, orders);
            } else {
                req.setAttribute("orders", orders);
                forwardToJsp(req, resp, "admin/orders.jsp");
            }
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
            throw new ValidationException("New status is required", "MISSING_STATUS");
        }

        OrderStatus newStatus = OrderStatus.fromString(statusStr);
        orderService.updateOrderStatus(orderId, newStatus);

        if (isApi) {
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Order status updated to " + newStatus, null);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/orders?updated=true");
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
