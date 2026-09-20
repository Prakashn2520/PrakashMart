package com.prakash.prakashmart.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Security utility for input sanitization, XSS escaping, and CSRF token generation/verification.
 */
public final class SecurityUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Generates a cryptographically strong random token for CSRF protection.
     *
     * @return 32-byte Base64-encoded string
     */
    public static String generateCsrfToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Verifies CSRF token safely with constant-time equality check to prevent timing attacks.
     *
     * @param tokenA First token (e.g. from session)
     * @param tokenB Second token (e.g. from request parameter/header)
     * @return true if both tokens match exactly
     */
    public static boolean verifyCsrfToken(String tokenA, String tokenB) {
        if (tokenA == null || tokenB == null) {
            return false;
        }
        if (tokenA.length() != tokenB.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < tokenA.length(); i++) {
            result |= tokenA.charAt(i) ^ tokenB.charAt(i);
        }
        return result == 0;
    }

    /**
     * Escapes untrusted text to safely prevent Cross-Site Scripting (XSS).
     *
     * @param input Raw user string
     * @return HTML entity escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder out = new StringBuilder(Math.max(16, input.length()));
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#x27;");
                    break;
                case '/':
                    out.append("&#x2F;");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }
}
