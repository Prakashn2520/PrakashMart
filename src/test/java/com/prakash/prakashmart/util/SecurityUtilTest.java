package com.prakash.prakashmart.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    @Test
    @DisplayName("Should generate non-empty cryptographically strong CSRF tokens")
    void testGenerateCsrfToken() {
        String token1 = SecurityUtil.generateCsrfToken();
        String token2 = SecurityUtil.generateCsrfToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertFalse(token1.isBlank());
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should verify matching CSRF tokens and reject mismatched/null tokens")
    void testVerifyCsrfToken() {
        String token = "random-token-abc-123";
        assertTrue(SecurityUtil.verifyCsrfToken(token, "random-token-abc-123"));

        assertFalse(SecurityUtil.verifyCsrfToken(token, "different-token"));
        assertFalse(SecurityUtil.verifyCsrfToken(token, null));
        assertFalse(SecurityUtil.verifyCsrfToken(null, token));
        assertFalse(SecurityUtil.verifyCsrfToken(null, null));
    }

    @Test
    @DisplayName("Should escape malicious HTML characters to prevent XSS")
    void testEscapeHtml() {
        String malicious = "<script>alert('XSS & Attack')</script> \"quotes\" and 'single'";
        String safe = SecurityUtil.escapeHtml(malicious);

        assertFalse(safe.contains("<script>"));
        assertTrue(safe.contains("&lt;script&gt;"));
        assertTrue(safe.contains("&amp;"));
        assertTrue(safe.contains("&quot;"));
        assertTrue(safe.contains("&#x27;"));

        assertNull(SecurityUtil.escapeHtml(null));
        assertEquals("", SecurityUtil.escapeHtml(""));
    }
}
