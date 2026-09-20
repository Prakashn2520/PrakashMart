package com.prakash.prakashmart;

import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TomcatVisualInspectionTest {

    private static final int TEST_PORT = 8888;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT + "/prakashmart";

    @BeforeAll
    static void startServer() throws Exception {
        TomcatServer.start(TEST_PORT);
        // Allow container to initialize, run migrations, and start H2
        Thread.sleep(1500);
    }

    @AfterAll
    static void stopServer() throws Exception {
        TomcatServer.stop();
    }

    private String fetch(String endpoint, int expectedStatus) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setInstanceFollowRedirects(false);

        int status = conn.getResponseCode();
        assertEquals(expectedStatus, status, "Failed endpoint: " + endpoint);

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    @Test
    @Order(1)
    @DisplayName("Verify static CSS design system assets render with HTTP 200")
    void testDesignSystemCssAssets() throws Exception {
        String mainCss = fetch("/assets/css/main.css", 200);
        assertTrue(mainCss.contains("--color-primary"), "main.css missing color variables");
        assertTrue(mainCss.contains("--font-serif"), "main.css missing font-serif token");

        String compCss = fetch("/assets/css/components.css", 200);
        assertTrue(compCss.contains(".btn-primary"), "components.css missing .btn-primary");
        assertTrue(compCss.contains(".card-product"), "components.css missing .card-product");
        assertTrue(compCss.contains(".toast"), "components.css missing .toast");

        String layoutCss = fetch("/assets/css/layout.css", 200);
        assertTrue(layoutCss.contains(".hero-section"), "layout.css missing .hero-section");
        assertTrue(layoutCss.contains(".catalog-layout"), "layout.css missing .catalog-layout");
    }

    @Test
    @Order(2)
    @DisplayName("Verify main JavaScript utilities render with HTTP 200")
    void testClientJavaScriptAssets() throws Exception {
        String js = fetch("/assets/js/main.js", 200);
        assertTrue(js.contains("Toast"), "main.js missing Toast utility");
        assertTrue(js.contains("Modal"), "main.js missing Modal utility");
        assertTrue(js.contains("initQuickAddToCart"), "main.js missing initQuickAddToCart");
    }

    @Test
    @Order(3)
    @DisplayName("Verify Landing Page renders classical hero, categories, and footer")
    void testLandingPageRender() throws Exception {
        String html = fetch("/home", 200);
        assertTrue(html.contains("PrakashMart"), "Landing page missing brand logo");
        assertTrue(html.contains("Crafted for Excellence"), "Landing page missing headline");
        assertTrue(html.contains("Explore Catalog"), "Landing page missing primary CTA");
        assertTrue(html.contains("Curated Categories"), "Landing page missing categories section");
        assertTrue(html.contains("Electronics"), "Landing page missing Electronics category");
        assertTrue(html.contains("site-footer"), "Landing page missing site-footer");
    }

    @Test
    @Order(4)
    @DisplayName("Verify Product Catalog renders sidebar filters and product grid")
    void testProductCatalogRender() throws Exception {
        String html = fetch("/products", 200);
        assertTrue(html.contains("Curated Marketplace Catalog"), "Catalog missing header");
        assertTrue(html.contains("catalog-sidebar"), "Catalog missing sidebar filter");
        assertTrue(html.contains("Price Range"), "Catalog missing price filter");
        assertTrue(html.contains("product-grid"), "Catalog missing product grid");
    }

    @Test
    @Order(5)
    @DisplayName("Verify Category filtering and Search render filtered catalog")
    void testFilteredCatalogRender() throws Exception {
        String htmlCategory = fetch("/products?category=Electronics", 200);
        assertTrue(htmlCategory.contains("Curated Marketplace Catalog"));

        String htmlSearch = fetch("/products?keyword=Laptop", 200);
        assertTrue(htmlSearch.contains("Curated Marketplace Catalog"));
    }

    @Test
    @Order(6)
    @DisplayName("Verify Product Detail page renders specs and purchase controls")
    void testProductDetailRender() throws Exception {
        String json = fetch("/api/products", 200);
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(json);
        if (m.find()) {
            String productId = m.group(1);
            String html = fetch("/product?id=" + productId, 200);
            assertTrue(html.contains("product-detail-title"), "Detail missing title");
            assertTrue(html.contains("product-detail-price"), "Detail missing price");
            assertTrue(html.contains("qty-control"), "Detail missing quantity selector");
            assertTrue(html.contains("Add to Cart"), "Detail missing Add to Cart");
            assertTrue(html.contains("Buy Now"), "Detail missing Buy Now");
        } else {
            // Check that catalog empty state works cleanly
            String html = fetch("/products", 200);
            assertTrue(html.contains("catalog-sidebar"));
        }
    }

    @Test
    @Order(7)
    @DisplayName("Verify Empty Cart page renders when unauthenticated")
    void testCartRenderRedirectOrEmpty() throws Exception {
        URL url = new URL(BASE_URL + "/cart");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        int status = conn.getResponseCode();
        assertTrue(status == 302 || status == 200, "Cart returned unexpected status: " + status);
    }
}
