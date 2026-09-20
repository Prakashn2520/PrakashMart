package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dao.impl.ProductDAOImpl;
import com.prakash.prakashmart.dao.impl.ReviewDAOImpl;
import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.dto.ReviewRequest;
import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.exception.AuthenticationException;
import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.service.ReviewService;
import com.prakash.prakashmart.service.impl.ReviewServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Controller handling reviews and ratings:
 * - GET /reviews?productId={id}, /api/reviews?productId={id}
 * - POST /reviews, /api/reviews
 */
@WebServlet(name = "ReviewServlet", urlPatterns = {
        "/reviews",
        "/api/reviews", "/api/reviews/*",
        "/api/v1/reviews", "/api/v1/reviews/*"
})
public class ReviewServlet extends BaseServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);
    private ReviewService reviewService;

    public ReviewServlet() {
    }

    public ReviewServlet(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public void init() {
        if (this.reviewService == null) {
            this.reviewService = new ReviewServiceImpl(new ReviewDAOImpl(), new ProductDAOImpl(), new UserDAOImpl());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        boolean isApi = servletPath.startsWith("/api");

        try {
            String pIdStr = req.getParameter("productId");
            if (pIdStr == null || pIdStr.trim().isEmpty()) {
                throw new ValidationException("productId", "productId parameter is required");
            }
            Long productId = Long.parseLong(pIdStr.trim());

            List<ReviewResponse> reviews = reviewService.getProductReviews(productId);
            double avgRating = reviewService.getAverageRating(productId);

            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_OK, reviews);
            } else {
                req.setAttribute("reviews", reviews);
                req.setAttribute("avgRating", avgRating);
                req.setAttribute("productId", productId);
                forwardToJsp(req, resp, "buyer/reviews.jsp");
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
            if (user == null) {
                throw new AuthenticationException("Authentication required to submit review");
            }

            ReviewRequest request;
            if (isApi) {
                request = readJsonBody(req, ReviewRequest.class);
            } else {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int rating = Integer.parseInt(req.getParameter("rating"));
                String comment = req.getParameter("comment");
                request = new ReviewRequest(productId, rating, comment);
            }

            ReviewResponse response = reviewService.addReview(user.getId(), request);

            if (isApi) {
                writeJsonResponse(resp, HttpServletResponse.SC_CREATED, "Review submitted successfully", response);
            } else {
                resp.sendRedirect(req.getContextPath() + "/product?id=" + request.getProductId() + "&reviewed=true");
            }
        } catch (Exception e) {
            handleException(resp, e, isApi);
        }
    }
}
