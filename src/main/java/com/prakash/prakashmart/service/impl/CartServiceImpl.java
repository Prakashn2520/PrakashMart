package com.prakash.prakashmart.service.impl;

import com.prakash.prakashmart.dao.CartDAO;
import com.prakash.prakashmart.dao.ProductDAO;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.InsufficientStockException;
import com.prakash.prakashmart.exception.ProductNotFoundException;
import com.prakash.prakashmart.exception.ResourceNotFoundException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.model.CartItem;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.service.CartService;
import com.prakash.prakashmart.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for shopping cart management and server-side pricing.
 * Client-provided totals and prices are never trusted.
 */
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartDAO cartDAO;
    private final ProductDAO productDAO;

    public CartServiceImpl(CartDAO cartDAO, ProductDAO productDAO) {
        this.cartDAO = cartDAO;
        this.productDAO = productDAO;
    }

    @Override
    public List<CartItemDTO> getCart(Long userId) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID");
        }
        // Server calculates totals dynamically based on current DB prices
        List<CartItemDTO> items = cartDAO.getCartByUserId(userId);
        for (CartItemDTO item : items) {
            Optional<Product> prodOpt = productDAO.findById(item.getProductId());
            if (prodOpt.isPresent()) {
                Product p = prodOpt.get();
                item.setUnitPrice(p.getPrice());
                item.setAvailableStock(p.getStockQty());
                item.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return items;
    }

    @Override
    public BigDecimal calculateCartTotal(Long userId) throws AppException {
        List<CartItemDTO> items = getCart(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDTO item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    @Override
    public void addToCart(Long userId, Long productId, int quantity) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID");
        }
        ValidationUtil.validateId(productId, "productId");
        ValidationUtil.validateCartQuantity(quantity);

        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (Boolean.FALSE.equals(product.getIsActive())) {
            throw new ValidationException("productId", "This product is currently inactive");
        }

        // Check if item already exists in user's cart
        Optional<CartItem> existingItem = cartDAO.findItem(userId, productId);
        int totalRequested = quantity;
        if (existingItem.isPresent()) {
            totalRequested += existingItem.get().getQuantity();
        }

        if (product.getStockQty() < totalRequested) {
            throw new InsufficientStockException(productId, totalRequested, product.getStockQty());
        }

        cartDAO.add(userId, productId, quantity);
        logger.info("Added product {} (qty: {}) to user {} cart", productId, quantity, userId);
    }

    @Override
    public void updateQuantity(Long cartItemId, int quantity) throws AppException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new ValidationException("cartItemId", "Invalid cart item ID");
        }

        if (quantity <= 0) {
            cartDAO.remove(cartItemId);
            return;
        }

        CartItem cartItem = cartDAO.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        Product product = productDAO.findById(cartItem.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(cartItem.getProductId()));

        if (product.getStockQty() < quantity) {
            throw new InsufficientStockException(product.getId(), quantity, product.getStockQty());
        }

        cartDAO.update(cartItemId, quantity);
        logger.info("Updated cart item {} to quantity {}", cartItemId, quantity);
    }

    @Override
    public void removeFromCart(Long cartItemId) throws AppException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new ValidationException("cartItemId", "Invalid cart item ID");
        }
        cartDAO.remove(cartItemId);
    }

    @Override
    public void clearCart(Long userId) throws AppException {
        if (userId == null || userId <= 0) {
            throw new ValidationException("userId", "Invalid user ID");
        }
        cartDAO.clear(userId);
    }
}
