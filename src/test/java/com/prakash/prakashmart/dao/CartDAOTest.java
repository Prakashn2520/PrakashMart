package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dao.impl.CartDAOImpl;
import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.CartItemDTO;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.CartItem;
import com.prakash.prakashmart.model.Product;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartDAOTest extends BaseDAOTest {

    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;

    private Long buyerId;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() throws DatabaseException {
        this.cartDAO = new CartDAOImpl();
        this.productDAO = new ProductDAOImpl();
        this.userDAO = new UserDAOImpl();

        // Create seller, buyer, and products
        User seller = userDAO.save(new User(null, "Seller", "seller@test.com", "hash", Role.SELLER, null));
        User buyer = userDAO.save(new User(null, "Buyer", "buyer@test.com", "hash", Role.BUYER, null));
        this.buyerId = buyer.getId();

        Product p1 = productDAO.save(new Product(null, seller.getId(), "Item 1", "Desc", new BigDecimal("20.00"), 100, "Cat", null, null));
        Product p2 = productDAO.save(new Product(null, seller.getId(), "Item 2", "Desc", new BigDecimal("15.50"), 50, "Cat", null, null));
        this.productId1 = p1.getId();
        this.productId2 = p2.getId();
    }

    @Test
    @DisplayName("Should add items to cart and calculate correct subtotals")
    void testAddToCartAndRetrieve() throws DatabaseException {
        cartDAO.add(buyerId, productId1, 2);
        cartDAO.add(buyerId, productId2, 3);

        List<CartItemDTO> cart = cartDAO.getCartByUserId(buyerId);
        assertEquals(2, cart.size());

        CartItemDTO item1 = cart.stream().filter(i -> i.getProductId().equals(productId1)).findFirst().orElseThrow();
        assertEquals(2, item1.getQuantity());
        assertEquals(0, new BigDecimal("40.00").compareTo(item1.getSubtotal()));

        CartItemDTO item2 = cart.stream().filter(i -> i.getProductId().equals(productId2)).findFirst().orElseThrow();
        assertEquals(3, item2.getQuantity());
        assertEquals(0, new BigDecimal("46.50").compareTo(item2.getSubtotal()));

        List<CartItem> rawItems = cartDAO.findByUser(buyerId);
        assertEquals(2, rawItems.size());
    }

    @Test
    @DisplayName("Should increment quantity when adding existing product")
    void testAddExistingItemIncrementsQuantity() throws DatabaseException {
        cartDAO.add(buyerId, productId1, 1);
        cartDAO.add(buyerId, productId1, 2);

        Optional<CartItem> item = cartDAO.findItem(buyerId, productId1);
        assertTrue(item.isPresent());
        assertEquals(3, item.get().getQuantity());
    }

    @Test
    @DisplayName("Should update quantity, remove item, and clear cart")
    void testCartItemMutations() throws DatabaseException {
        cartDAO.add(buyerId, productId1, 1);
        cartDAO.add(buyerId, productId2, 2);

        Optional<CartItem> item1 = cartDAO.findItem(buyerId, productId1);
        assertTrue(item1.isPresent());

        // Update quantity
        cartDAO.update(item1.get().getId(), 5);
        Optional<CartItem> updated = cartDAO.findById(item1.get().getId());
        assertTrue(updated.isPresent());
        assertEquals(5, updated.get().getQuantity());

        // Remove single item
        cartDAO.remove(item1.get().getId());
        assertEquals(1, cartDAO.getCartByUserId(buyerId).size());

        // Clear cart
        cartDAO.clear(buyerId);
        assertTrue(cartDAO.getCartByUserId(buyerId).isEmpty());
        assertTrue(cartDAO.findByUser(buyerId).isEmpty());
    }

    @Test
    @DisplayName("Should return empty list for user with no items and empty optional for invalid item ID")
    void testEmptyCartAndInvalidId() throws DatabaseException {
        List<CartItemDTO> emptyCart = cartDAO.getCartByUserId(999999L);
        assertTrue(emptyCart.isEmpty());

        List<CartItem> emptyRaw = cartDAO.findByUser(999999L);
        assertTrue(emptyRaw.isEmpty());

        Optional<CartItem> missing = cartDAO.findById(888888L);
        assertFalse(missing.isPresent());
    }
}
