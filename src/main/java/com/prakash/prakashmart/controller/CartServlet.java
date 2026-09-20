package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.CartDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.dto.CartItemRequest;
import com.prakash.prakashmart.dto.CartResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AuthenticationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.service.CartService;
import com.prakash.prakashmart.service.impl.CartServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller handling shopping cart operations both for Web UI and REST API:
 * - GET /cart, /api/cart, /api/v1/cart
 * - POST /cart/add, /api/cart/add, /api/v1/cart/add
 * - POST /cart/update, /api/cart/update, /api/v1/cart/update
 * - POST /cart/remove, /api/cart/remove, /api/v1/cart/remove
 */
@WebServlet(name = "CartServlet", urlPatterns = {
        "/cart", "/cart/add", "/cart/update", "/cart/remove",
        "/api/cart", "/api/cart/*",
        "/api/v1/cart", "/api/v1/cart/*"
})
public class CartServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);
    private CartService cartService;

    public CartServlet() {
    }

    // Constructor injection for testing
    public CartServlet(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void init() {
        if (this.cartService == null) {
            this.cartService = new CartServiceImpl(new CartDAOImpl(), new ProductDAOImpl());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null) {
                if (isApi) {
                    throw new AuthenticationException("Authentication required to view cart");
                }
                resp.sendRedirect(req.getContextPath() + "/login?redirect=" + req.getRequestURI());
                return;
            }

            List<CartItemDTO> cartItems = cartService.getCart(user.getId());
            BigDecimal cartTotal = cartService.calculateCartTotal(user.getId());

            if (isApi) {
                CartResponse cartResponse = new CartResponse(cartItems, cartTotal);
                writeJsonResponse(resp, HttpServletResponse.SC_OK, cartResponse);
            } else {
                req.setAttribute("cartItems", cartItems);
                req.setAttribute("cartTotal", cartTotal);
                forwardToJsp(req, resp, "buyer/cart.jsp");
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
                throw new AuthenticationException("Authentication required for cart operations");
            }

            String action = determineAction(servletPath, pathInfo);

            if ("add".equalsIgnoreCase(action)) {
                Long productId;
                int quantity = 1;

                if (isApi) {
                    CartItemRequest body = readJsonBody(req, CartItemRequest.class);
                    if (body == null || body.getProductId() == null) {
                        throw new ValidationException("productId", "Product ID is required");
                    }
                    productId = body.getProductId();
                    if (body.getQuantity() != null) {
                        quantity = body.getQuantity();
                    }
                } else {
                    String pIdStr = req.getParameter("productId");
                    if (pIdStr == null || pIdStr.trim().isEmpty()) {
                        throw new ValidationException("productId", "Product ID is required");
                    }
                    productId = Long.parseLong(pIdStr.trim());
                    String qStr = req.getParameter("quantity");
                    if (qStr != null && !qStr.trim().isEmpty()) {
                        quantity = Integer.parseInt(qStr.trim());
                    }
                }

                cartService.addToCart(user.getId(), productId, quantity);

                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, "Item added to cart successfully", null);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/cart");
                }

            } else if ("update".equalsIgnoreCase(action)) {
                Long cartItemId;
                int quantity;

                if (isApi) {
                    CartItemRequest body = readJsonBody(req, CartItemRequest.class);
                    if (body == null || body.getCartItemId() == null) {
                        throw new ValidationException("cartItemId", "Cart item ID is required");
                    }
                    cartItemId = body.getCartItemId();
                    quantity = body.getQuantity() != null ? body.getQuantity() : 1;
                } else {
                    cartItemId = Long.parseLong(req.getParameter("cartItemId"));
                    quantity = Integer.parseInt(req.getParameter("quantity"));
                }

                cartService.updateQuantity(cartItemId, quantity);

                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, "Cart updated successfully", null);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/cart");
                }

            } else if ("remove".equalsIgnoreCase(action)) {
                Long cartItemId;

                if (isApi) {
                    CartItemRequest body = readJsonBody(req, CartItemRequest.class);
                    if (body != null && body.getCartItemId() != null) {
                        cartItemId = body.getCartItemId();
                    } else {
                        cartItemId = parseIdFromPath(pathInfo);
                    }
                } else {
                    cartItemId = Long.parseLong(req.getParameter("cartItemId"));
                }

                if (cartItemId == null) {
                    throw new ValidationException("cartItemId", "Cart item ID is required");
                }

                cartService.removeFromCart(cartItemId);

                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, "Item removed from cart successfully", null);
                } else {
                    resp.sendRedirect(req.getContextPath() + "/cart");
                }

            } else {
                writeJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Cart action not recognized");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    private String determineAction(String servletPath, String pathInfo) {
        String combined = (servletPath != null ? servletPath : "") + (pathInfo != null ? pathInfo : "");
        if (combined.contains("/add")) {
            return "add";
        }
        if (combined.contains("/update")) {
            return "update";
        }
        if (combined.contains("/remove")) {
            return "remove";
        }
        return "";
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return null;
        }
        String clean = pathInfo.replaceAll("/remove", "").replaceAll("^/+", "").replaceAll("/+$", "");
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
