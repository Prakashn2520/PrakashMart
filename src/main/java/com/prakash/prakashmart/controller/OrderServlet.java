package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.CartDAOImpl;
import com.prakash.prakashmart.dao.impl.OrderDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dto.OrderResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AuthenticationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.Order;
import com.prakash.prakashmart.model.OrderItem;
import com.prakash.prakashmart.service.OrderService;
import com.prakash.prakashmart.service.impl.OrderServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller handling checkout, order history, and REST API order management:
 * - GET /orders, /api/orders, /api/v1/orders
 * - GET /api/orders/{id}, /api/v1/orders/{id}
 * - POST /checkout, /api/orders (atomic checkout)
 * - POST /api/orders/{id}/cancel (order cancellation)
 */
@WebServlet(name = "OrderServlet", urlPatterns = {
        "/orders", "/checkout",
        "/api/orders", "/api/orders/*",
        "/api/v1/orders", "/api/v1/orders/*"
})
public class OrderServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);
    private OrderService orderService;
    private com.prakash.prakashmart.service.CartService cartService;

    public OrderServlet() {
    }

    public OrderServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderServlet(OrderService orderService, com.prakash.prakashmart.service.CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @Override
    public void init() {
        if (this.orderService == null) {
            this.orderService = new OrderServiceImpl(new OrderDAOImpl(), new CartDAOImpl(), new ProductDAOImpl());
        }
        if (this.cartService == null) {
            this.cartService = new com.prakash.prakashmart.service.impl.CartServiceImpl(new CartDAOImpl(), new ProductDAOImpl());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null) {
                if (isApi) {
                    throw new AuthenticationException("Authentication required to view orders");
                }
                resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
                return;
            }

            if ("/checkout".equals(servletPath)) {
                if (cartService != null) {
                    List<com.prakash.prakashmart.dto.CartItemDTO> cartItems = cartService.getCart(user.getId());
                    java.math.BigDecimal cartTotal = cartService.calculateCartTotal(user.getId());
                    req.setAttribute("cartItems", cartItems);
                    req.setAttribute("cartTotal", cartTotal);
                }
                forwardToJsp(req, resp, "buyer/checkout.jsp");
                return;
            }

            Long orderId = parseIdFromPath(pathInfo);
            if (orderId == null && req.getParameter("id") != null && !req.getParameter("id").trim().isEmpty()) {
                try {
                    orderId = Long.parseLong(req.getParameter("id").trim());
                } catch (NumberFormatException ignored) {}
            }

            if (orderId != null) {
                Order order = orderService.getOrderById(orderId, user.getId(), user.getRole());
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                OrderResponse responseDTO = OrderResponse.fromOrder(order, items);

                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, responseDTO);
                } else {
                    req.setAttribute("order", order);
                    req.setAttribute("items", items);
                    forwardToJsp(req, resp, "buyer/order-detail.jsp");
                }
            } else {
                List<Order> orders;
                if ("SELLER".equalsIgnoreCase(user.getRole())) {
                    orders = orderService.getOrdersForSeller(user.getId());
                } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    orders = orderService.getAllOrders();
                } else {
                    orders = orderService.getOrdersByBuyer(user.getId());
                }

                if (isApi) {
                    List<OrderResponse> responseList = new ArrayList<>();
                    for (Order o : orders) {
                        List<OrderItem> items = orderService.getOrderItems(o.getId());
                        responseList.add(OrderResponse.fromOrder(o, items));
                    }
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, responseList);
                } else {
                    req.setAttribute("orders", orders);
                    forwardToJsp(req, resp, "buyer/orders.jsp");
                }
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null) {
                throw new AuthenticationException("Authentication required for order operations");
            }

            // Check if cancellation request: e.g. /api/orders/{id}/cancel or ?action=cancel&orderId=...
            String action = req.getParameter("action");
            if ((pathInfo != null && pathInfo.contains("/cancel")) || "cancel".equalsIgnoreCase(action)) {
                Long orderId = parseIdFromPath(pathInfo);
                if (orderId == null && req.getParameter("orderId") != null) {
                    try {
                        orderId = Long.parseLong(req.getParameter("orderId").trim());
                    } catch (NumberFormatException ignored) {}
                }
                if (orderId == null) {
                    throw new ValidationException("orderId", "Order ID must be provided");
                }
                orderService.cancelOrder(orderId, user.getId(), user.getRole());
                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, "Order cancelled successfully", null);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/orders?cancelled=true&orderId=" + orderId);
                }
                return;
            }

            // Default POST action is checkout
            Order order = orderService.checkout(user.getId());

            if (isApi) {
                List<OrderItem> items = orderService.getOrderItems(order.getId());
                OrderResponse responseDTO = OrderResponse.fromOrder(order, items);
                writeJsonResponse(resp, HttpServletResponse.SC_CREATED, "Order placed successfully", responseDTO);
            } else {
                resp.sendRedirect(req.getContextPath() + "/orders?placed=true&orderId=" + order.getId());
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return null;
        }
        String clean = pathInfo.replaceAll("/cancel", "").replaceAll("^/+", "").replaceAll("/+$", "");
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
