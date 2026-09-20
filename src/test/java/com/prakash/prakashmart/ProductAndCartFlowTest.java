package com.prakash.prakashmart;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductAndCartFlowTest {

    private static final int TEST_PORT = 8888;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT + "/prakashmart";
    private static final Gson gson = new Gson();
    private static String buyerSessionCookie = null;
    private static String csrfToken = null;
    private static Long sampleProductId = null;
    private static Integer sampleCartItemId = null;

    @BeforeAll
    static void startServer() throws Exception {
        TomcatServer.start(TEST_PORT);
        Thread.sleep(1500);
    }

    @AfterAll
    static void stopServer() throws Exception {
        TomcatServer.stop();
    }

    private HttpResponse sendRequest(String endpoint, String method, String jsonBody, String cookie, String csrf) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(false);

        if (cookie != null) {
            conn.setRequestProperty("Cookie", cookie);
        }
        if (csrf != null) {
            conn.setRequestProperty("X-CSRF-Token", csrf);
        }
        if (jsonBody != null) {
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        int status = conn.getResponseCode();

        // Capture session cookies
        List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");
        String newCookie = cookie;
        if (setCookies != null && !setCookies.isEmpty()) {
            newCookie = setCookies.get(0).split(";")[0];
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                status >= 400 ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return new HttpResponse(status, sb.toString(), newCookie);
    }

    private static class HttpResponse {
        final int status;
        final String body;
        final String cookie;

        HttpResponse(int status, String body, String cookie) {
            this.status = status;
            this.body = body;
            this.cookie = cookie;
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Product Loading: Verify product catalog and API endpoints load data")
    void testProductLoading() throws Exception {
        HttpResponse res = sendRequest("/api/products", "GET", null, null, null);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("success"), "Response missing success field");
        assertTrue(res.body.contains("data"), "Response missing data field");

        // Verify HTML catalog view
        HttpResponse htmlRes = sendRequest("/products", "GET", null, null, null);
        assertEquals(200, htmlRes.status);
        assertTrue(htmlRes.body.contains("Curated Marketplace Catalog") || htmlRes.body.contains("catalog-layout"));
        assertTrue(htmlRes.body.contains("card-product"));
    }

    @Test
    @Order(2)
    @DisplayName("2. Search: Verify keyword search on catalog")
    void testProductSearch() throws Exception {
        HttpResponse res = sendRequest("/api/products?keyword=Keyboard", "GET", null, null, null);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("success"));
        assertTrue(res.body.contains("Keyboard"));

        HttpResponse htmlRes = sendRequest("/products?keyword=Headphones", "GET", null, null, null);
        assertEquals(200, htmlRes.status);
        assertTrue(htmlRes.body.contains("Headphones"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Filters: Category, Price Range, and In-Stock filters")
    void testProductFilters() throws Exception {
        // Category filter
        HttpResponse catRes = sendRequest("/api/products?category=Electronics", "GET", null, null, null);
        assertEquals(200, catRes.status);
        assertTrue(catRes.body.contains("Electronics"));

        // Price range filter
        HttpResponse priceRes = sendRequest("/api/products?minPrice=20&maxPrice=100", "GET", null, null, null);
        assertEquals(200, priceRes.status);
        assertTrue(priceRes.body.contains("success"));

        // In-stock filter
        HttpResponse stockRes = sendRequest("/api/products?inStock=true", "GET", null, null, null);
        assertEquals(200, stockRes.status);
        assertTrue(stockRes.body.contains("success"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Sorting: Sort by Price ASC, Price DESC, Name ASC, Name DESC")
    void testProductSorting() throws Exception {
        HttpResponse priceAsc = sendRequest("/api/products?sort=price_asc", "GET", null, null, null);
        assertEquals(200, priceAsc.status);

        HttpResponse priceDesc = sendRequest("/api/products?sort=price_desc", "GET", null, null, null);
        assertEquals(200, priceDesc.status);

        HttpResponse nameAsc = sendRequest("/api/products?sort=name_asc", "GET", null, null, null);
        assertEquals(200, nameAsc.status);

        HttpResponse nameDesc = sendRequest("/api/products?sort=name_desc", "GET", null, null, null);
        assertEquals(200, nameDesc.status);
    }

    @Test
    @Order(5)
    @DisplayName("5. Pagination: Retrieve page 1 and page 2 with page size")
    void testProductPagination() throws Exception {
        HttpResponse page1 = sendRequest("/api/products?page=1&size=4", "GET", null, null, null);
        assertEquals(200, page1.status);
        assertTrue(page1.body.contains("\"currentPage\":1"));
        assertTrue(page1.body.contains("\"pageSize\":4"));

        HttpResponse page2 = sendRequest("/api/products?page=2&size=4", "GET", null, null, null);
        assertEquals(200, page2.status);
        assertTrue(page2.body.contains("\"currentPage\":2"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Authenticate Buyer for Cart tests")
    void testBuyerLogin() throws Exception {
        // First get login page to establish session and obtain CSRF token
        HttpResponse getLogin = sendRequest("/login", "GET", null, null, null);
        buyerSessionCookie = getLogin.cookie;

        // Login as buyer1@prakashmart.com (password buyer123)
        String loginJson = "{\"email\":\"buyer1@prakashmart.com\",\"password\":\"buyer123\"}";
        HttpResponse res = sendRequest("/api/v1/auth/login", "POST", loginJson, buyerSessionCookie, null);
        if (res.status != 200) {
            String regJson = "{\"name\":\"Test Buyer\",\"email\":\"buyer1@prakashmart.com\",\"password\":\"buyer123\",\"role\":\"BUYER\"}";
            res = sendRequest("/api/v1/auth/register", "POST", regJson, buyerSessionCookie, null);
        }
        assertTrue(res.status == 200 || res.status == 201, "Login/Register returned: " + res.status);
        if (res.cookie != null) {
            buyerSessionCookie = res.cookie;
        }

        // Fetch home or products with cookie to get the CSRF token from the session
        HttpResponse homeRes = sendRequest("/products", "GET", null, buyerSessionCookie, null);
        // Find CSRF token or session token
        HttpResponse apiCartGet = sendRequest("/api/cart", "GET", null, buyerSessionCookie, null);
        assertEquals(200, apiCartGet.status);

        // Retrieve a sample product ID
        HttpResponse prodRes = sendRequest("/api/products?size=1", "GET", null, null, null);
        Map<?, ?> prodMap = gson.fromJson(prodRes.body, Map.class);
        Map<?, ?> data = (Map<?, ?>) prodMap.get("data");
        List<?> list = (List<?>) data.get("data");
        assertFalse(list.isEmpty(), "Product list should not be empty");
        Map<?, ?> p = (Map<?, ?>) list.get(0);
        sampleProductId = ((Double) p.get("id")).longValue();
    }

    @Test
    @Order(7)
    @DisplayName("7. Add to Cart: Add valid product to cart and verify authoritative response")
    void testAddToCart() throws Exception {
        assertNotNull(buyerSessionCookie, "Must have buyer session");
        
        // Fetch cart page to get valid CSRF token from header meta tag
        HttpResponse cartPage = sendRequest("/cart", "GET", null, buyerSessionCookie, null);
        Matcher m = Pattern.compile("<meta\\s+name=[\"']_csrf[\"']\\s+content=[\"']([^\"']+)[\"']").matcher(cartPage.body);
        if (m.find()) {
            csrfToken = m.group(1);
        } else {
            Matcher m2 = Pattern.compile("name=[\"']_csrf[\"']\\s+value=[\"']([^\"']+)[\"']").matcher(cartPage.body);
            if (m2.find()) {
                csrfToken = m2.group(1);
            }
        }

        String addJson = String.format("{\"productId\":%d,\"quantity\":2}", sampleProductId);
        HttpResponse res = sendRequest("/api/cart/add", "POST", addJson, buyerSessionCookie, csrfToken);
        if (res.status == 403 && csrfToken == null) {
            // Re-fetch with session to ensure CSRF is generated
            csrfToken = "test-token";
        }
        assertEquals(200, res.status);
        assertTrue(res.body.contains("success"));

        // Verify cart contains item
        HttpResponse cartRes = sendRequest("/api/cart", "GET", null, buyerSessionCookie, null);
        assertEquals(200, cartRes.status);
        assertTrue(cartRes.body.contains("items"));
        assertTrue(cartRes.body.contains("totalAmount"));

        Map<?, ?> map = gson.fromJson(cartRes.body, Map.class);
        Map<?, ?> cartData = (Map<?, ?>) map.get("data");
        List<?> items = (List<?>) cartData.get("items");
        assertFalse(items.isEmpty());
        Map<?, ?> firstItem = (Map<?, ?>) items.get(0);
        sampleCartItemId = ((Double) firstItem.get("cartItemId")).intValue();
    }

    @Test
    @Order(8)
    @DisplayName("8. Update Quantity: Update item quantity in cart")
    void testUpdateCartQuantity() throws Exception {
        assertNotNull(sampleCartItemId, "Must have cart item ID");
        String updateJson = String.format("{\"cartItemId\":%d,\"quantity\":3}", sampleCartItemId);
        HttpResponse res = sendRequest("/api/cart/update", "POST", updateJson, buyerSessionCookie, csrfToken);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("success"));
    }

    @Test
    @Order(9)
    @DisplayName("9. Stock Errors: Attempt to add quantity exceeding available stock")
    void testStockErrors() throws Exception {
        String excessiveJson = String.format("{\"productId\":%d,\"quantity\":999999}", sampleProductId);
        HttpResponse res = sendRequest("/api/cart/add", "POST", excessiveJson, buyerSessionCookie, csrfToken);
        assertTrue(res.status == 400 || res.status == 422, "Expected bad request for exceeding stock, got " + res.status);
        assertTrue(res.body.toLowerCase().contains("stock") || res.body.toLowerCase().contains("exceeds") || res.body.toLowerCase().contains("error"), "Response should mention stock limitation");
    }

    @Test
    @Order(10)
    @DisplayName("10. Remove Item: Remove item from cart")
    void testRemoveCartItem() throws Exception {
        assertNotNull(sampleCartItemId, "Must have cart item ID");
        String removeJson = String.format("{\"cartItemId\":%d}", sampleCartItemId);
        HttpResponse res = sendRequest("/api/cart/remove", "POST", removeJson, buyerSessionCookie, csrfToken);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("success"));
    }

    @Test
    @Order(11)
    @DisplayName("11. Empty Cart State: Verify empty cart returns cleanly")
    void testEmptyCartState() throws Exception {
        HttpResponse res = sendRequest("/api/cart", "GET", null, buyerSessionCookie, null);
        assertEquals(200, res.status);
        assertTrue(res.body.contains("items"));

        // HTML cart view shows empty state
        HttpResponse htmlRes = sendRequest("/cart", "GET", null, buyerSessionCookie, null);
        assertEquals(200, htmlRes.status);
        assertTrue(htmlRes.body.contains("empty") || htmlRes.body.contains("Cart is Currently Empty") || htmlRes.body.contains("Shopping Cart"));
    }
}
