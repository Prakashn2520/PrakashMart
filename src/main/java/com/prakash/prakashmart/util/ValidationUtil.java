package com.prakash.prakashmart.util;

import com.prakash.prakashmart.exception.ValidationException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Reusable, framework-independent validation utility for PrakashMart.
 * Provides validation checks and exception-throwing validation helpers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // At least 8 characters, at least one letter and one digit
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,100}$");

    private ValidationUtil() {
        // Prevent instantiation
    }

    // -------------------------------------------------------------
    // Generic Checks
    // -------------------------------------------------------------

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isPositive(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isNonNegative(Integer value) {
        return value != null && value >= 0;
    }

    // -------------------------------------------------------------
    // Required Fields Validation
    // -------------------------------------------------------------

    public static void validateRequired(String value, String fieldName) throws ValidationException {
        if (!isNotEmpty(value)) {
            throw new ValidationException(fieldName, fieldName + " is required and cannot be blank.");
        }
    }

    public static void validateNotNull(Object value, String fieldName) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null.");
        }
    }

    // -------------------------------------------------------------
    // Email Validation
    // -------------------------------------------------------------

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static void validateEmail(String email) throws ValidationException {
        validateRequired(email, "email");
        if (!isValidEmail(email)) {
            throw new ValidationException("email", "Invalid email address format: " + email);
        }
    }

    // -------------------------------------------------------------
    // Password Validation (min 8 chars, 1 letter, 1 number)
    // -------------------------------------------------------------

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static void validatePassword(String password) throws ValidationException {
        validateRequired(password, "password");
        if (!isValidPassword(password)) {
            throw new ValidationException("password",
                    "Password must be at least 8 characters long and contain at least one letter and one number.");
        }
    }

    // -------------------------------------------------------------
    // Product Validations
    // -------------------------------------------------------------

    public static void validateProductName(String name) throws ValidationException {
        validateRequired(name, "name");
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 255) {
            throw new ValidationException("name", "Product name must be between 2 and 255 characters.");
        }
    }

    public static void validateProductDescription(String description) throws ValidationException {
        if (description != null && description.length() > 5000) {
            throw new ValidationException("description", "Product description must not exceed 5000 characters.");
        }
    }

    public static void validatePrice(BigDecimal price) throws ValidationException {
        validateNotNull(price, "price");
        if (!isNonNegative(price)) {
            throw new ValidationException("price", "Price must be greater than or equal to 0.00.");
        }
    }

    public static void validateStockQuantity(Integer stockQty) throws ValidationException {
        validateNotNull(stockQty, "stockQty");
        if (!isNonNegative(stockQty)) {
            throw new ValidationException("stockQty", "Stock quantity must be greater than or equal to 0.");
        }
    }

    public static void validateCategory(String category) throws ValidationException {
        validateRequired(category, "category");
        if (category.trim().length() > 100) {
            throw new ValidationException("category", "Category name must not exceed 100 characters.");
        }
    }

    // -------------------------------------------------------------
    // Cart Validations
    // -------------------------------------------------------------

    public static void validateCartQuantity(Integer quantity) throws ValidationException {
        validateNotNull(quantity, "quantity");
        if (quantity <= 0) {
            throw new ValidationException("quantity", "Cart item quantity must be at least 1.");
        }
        if (quantity > 1000) {
            throw new ValidationException("quantity", "Cart item quantity cannot exceed 1000 per line item.");
        }
    }

    // -------------------------------------------------------------
    // Review Validations
    // -------------------------------------------------------------

    public static void validateRating(Integer rating) throws ValidationException {
        validateNotNull(rating, "rating");
        if (rating < 1 || rating > 5) {
            throw new ValidationException("rating", "Review rating must be an integer between 1 and 5.");
        }
    }

    public static void validateComment(String comment) throws ValidationException {
        if (comment != null && comment.length() > 2000) {
            throw new ValidationException("comment", "Review comment must not exceed 2000 characters.");
        }
    }

    // -------------------------------------------------------------
    // ID and Pagination Validations
    // -------------------------------------------------------------

    public static void validateId(Long id, String fieldName) throws ValidationException {
        validateNotNull(id, fieldName);
        if (id <= 0) {
            throw new ValidationException(fieldName, fieldName + " must be a positive integer.");
        }
    }

    public static int validatePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    public static int validateLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 12; // default page size
        }
        return Math.min(limit, 100); // max 100
    }

    public static String sanitizeSearchKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }
        String trimmed = keyword.trim();
        // Limit keyword length to prevent excessive regex/like scans
        if (trimmed.length() > 100) {
            trimmed = trimmed.substring(0, 100);
        }
        return trimmed;
    }
}
