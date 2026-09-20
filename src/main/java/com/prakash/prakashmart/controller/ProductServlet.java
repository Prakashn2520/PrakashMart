package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.ReviewDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.PaginatedResult;
import com.prakash.prakashmart.dto.ProductDTO;
import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AppException;
import com.prakash.prakashmart.exception.AuthorizationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.service.ProductService;
import com.prakash.prakashmart.service.ReviewService;
import com.prakash.prakashmart.service.impl.ProductServiceImpl;
import com.prakash.prakashmart.service.impl.ReviewServiceImpl;
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
 * Thin controller managing product browsing, searching, filtering, and REST API endpoints:
 * - GET /home, /index (landing page)
 * - GET /products, /product?id={id}
 * - GET /api/products, /api/v1/products
 * - GET /api/products/{id}, /api/v1/products/{id}
 * - POST /api/products, /api/v1/products (seller)
 * - PUT /api/products/{id}, /api/v1/products/{id} (seller)
 * - DELETE /api/products/{id}, /api/v1/products/{id} (seller)
 */
@WebServlet(name = "ProductServlet", urlPatterns = {
        "/home", "/index",
        "/products", "/product",
        "/api/products", "/api/products/*",
        "/api/v1/products", "/api/v1/products/*"
})
public class ProductServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);
    private ProductService productService;
    private ReviewService reviewService;

    public ProductServlet() {
    }

    // Constructor injection for testing
    public ProductServlet(ProductService productService) {
        this.productService = productService;
    }

    public ProductServlet(ProductService productService, ReviewService reviewService) {
        this.productService = productService;
        this.reviewService = reviewService;
    }

    @Override
    public void init() {
        if (this.productService == null) {
            this.productService = new ProductServiceImpl(new ProductDAOImpl());
        }
        if (this.reviewService == null) {
            this.reviewService = new ReviewServiceImpl(new ReviewDAOImpl(), new ProductDAOImpl(), new UserDAOImpl());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        boolean isApi = servletPath.startsWith("/api");

        try {
            if ("/home".equals(servletPath) || "/index".equals(servletPath)) {
                PaginatedResult<ProductDTO> featured = productService.getProductsPaginated(
                        null, null, null, null, true, 1, 8, "id", "DESC");
                req.setAttribute("featuredProducts", featured.getData());
                forwardToJsp(req, resp, "home.jsp");
                return;
            }

            Long productId = parseIdFromPath(pathInfo);
            if (productId == null && "/product".equals(servletPath)) {
                String idParam = req.getParameter("id");
                if (idParam != null && !idParam.trim().isEmpty()) {
                    productId = Long.parseLong(idParam.trim());
                }
            }

            if (productId != null) {
                // Fetch single product by ID
                ProductDTO product = productService.getProductById(productId);
                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, product);
                } else {
                    req.setAttribute("product", product);
                    if (reviewService != null) {
                        try {
                            List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
                            double avgRating = reviewService.getAverageRating(productId);
                            req.setAttribute("reviews", reviews);
                            req.setAttribute("avgRating", avgRating);
                        } catch (Exception ignored) {
                        }
                    }
                    forwardToJsp(req, resp, "buyer/product-detail.jsp");
                }
            } else {
                // Catalog search, filter, sort & pagination
                String category = req.getParameter("category");
                String keyword = req.getParameter("keyword");
                if (keyword == null) {
                    keyword = req.getParameter("search"); // Support ?search=...
                }

                int page = 1;
                if (req.getParameter("page") != null) {
                    try { page = Integer.parseInt(req.getParameter("page")); } catch (NumberFormatException ignored) {}
                }

                int size = 12;
                if (req.getParameter("size") != null) {
                    try { size = Integer.parseInt(req.getParameter("size")); } catch (NumberFormatException ignored) {}
                } else if (req.getParameter("pageSize") != null) {
                    try { size = Integer.parseInt(req.getParameter("pageSize")); } catch (NumberFormatException ignored) {}
                }

                BigDecimal minPrice = null;
                if (req.getParameter("minPrice") != null) {
                    try { minPrice = new BigDecimal(req.getParameter("minPrice")); } catch (Exception ignored) {}
                }

                BigDecimal maxPrice = null;
                if (req.getParameter("maxPrice") != null) {
                    try { maxPrice = new BigDecimal(req.getParameter("maxPrice")); } catch (Exception ignored) {}
                }

                Boolean inStockOnly = req.getParameter("inStock") != null ? Boolean.parseBoolean(req.getParameter("inStock")) : null;

                String sortParam = req.getParameter("sort");
                String sortBy = "id";
                String sortOrder = "DESC";
                if (sortParam != null) {
                    if ("price_asc".equalsIgnoreCase(sortParam)) {
                        sortBy = "price";
                        sortOrder = "ASC";
                    } else if ("price_desc".equalsIgnoreCase(sortParam)) {
                        sortBy = "price";
                        sortOrder = "DESC";
                    } else if ("name_asc".equalsIgnoreCase(sortParam)) {
                        sortBy = "name";
                        sortOrder = "ASC";
                    } else if ("name_desc".equalsIgnoreCase(sortParam)) {
                        sortBy = "name";
                        sortOrder = "DESC";
                    }
                }

                PaginatedResult<ProductDTO> paginated = productService.getProductsPaginated(
                        keyword, category, minPrice, maxPrice, inStockOnly, page, size, sortBy, sortOrder
                );

                if (isApi) {
                    writeJsonResponse(resp, HttpServletResponse.SC_OK, paginated);
                } else {
                    req.setAttribute("products", paginated.getData());
                    req.setAttribute("paginated", paginated);
                    req.setAttribute("selectedCategory", category);
                    req.setAttribute("keyword", keyword);
                    req.setAttribute("minPrice", minPrice);
                    req.setAttribute("maxPrice", maxPrice);
                    req.setAttribute("inStock", inStockOnly);
                    req.setAttribute("sort", sortParam);
                    req.setAttribute("currentPage", page);
                    forwardToJsp(req, resp, "buyer/product-list.jsp");
                }
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
                throw new AuthorizationException("Only sellers or administrators can list new products");
            }

            ProductDTO dto = readJsonBody(req, ProductDTO.class);
            if (dto == null) {
                throw new ValidationException("Product payload is required");
            }
            // Force authenticated seller ID
            dto.setSellerId(user.getId());

            ProductDTO created = productService.createProduct(dto);
            writeJsonResponse(resp, HttpServletResponse.SC_CREATED, "Product created successfully", created);
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
                throw new AuthorizationException("Only sellers or administrators can update products");
            }

            Long productId = parseIdFromPath(req.getPathInfo());
            ProductDTO dto = readJsonBody(req, ProductDTO.class);
            if (dto == null) {
                throw new ValidationException("Product update payload is required");
            }
            if (productId != null) {
                dto.setId(productId);
            }
            dto.setSellerId(user.getId());

            productService.updateProduct(dto);
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Product updated successfully", dto);
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.startsWith("/api");

        try {
            UserResponseDTO user = getSessionUser(req);
            if (user == null || (!"SELLER".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
                throw new AuthorizationException("Only sellers or administrators can delete products");
            }

            Long productId = parseIdFromPath(req.getPathInfo());
            if (productId == null) {
                throw new ValidationException("productId", "Product ID must be provided in URL path");
            }

            productService.deleteProduct(productId, user.getId());
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Product deleted successfully", null);
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }

    private Long parseIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.trim().isEmpty() || "/".equals(pathInfo.trim())) {
            return null;
        }
        String clean = pathInfo.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
