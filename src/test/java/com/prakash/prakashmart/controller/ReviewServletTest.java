package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.ReviewResponse;
import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServletTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ReviewServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReviewServlet(reviewService);
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("GET /api/reviews?productId=1 returns product reviews")
    void testGetReviewsApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/reviews");
        when(request.getParameter("productId")).thenReturn("1");

        ReviewResponse rev = new ReviewResponse();
        rev.setId(10L);
        rev.setProductId(1L);
        rev.setRating(5);
        rev.setComment("Excellent build quality!");

        when(reviewService.getProductReviews(1L)).thenReturn(Collections.singletonList(rev));
        when(reviewService.getAverageRating(1L)).thenReturn(5.0);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String json = responseWriter.toString();
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("Excellent build quality!"));
    }

    @Test
    @DisplayName("POST /api/reviews adds review when authenticated")
    void testAddReviewApi() throws Exception {
        when(request.getServletPath()).thenReturn("/api/reviews");
        when(request.getSession(false)).thenReturn(session);

        UserResponseDTO user = new UserResponseDTO();
        user.setId(3L);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = "{\"productId\":1,\"rating\":5,\"comment\":\"Super fast shipping!\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        ReviewResponse responseReview = new ReviewResponse();
        responseReview.setId(15L);
        responseReview.setProductId(1L);
        responseReview.setRating(5);
        responseReview.setComment("Super fast shipping!");

        when(reviewService.addReview(any(), any())).thenReturn(responseReview);

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String jsonOutput = responseWriter.toString();
        assertTrue(jsonOutput.contains("\"success\":true"));
        assertTrue(jsonOutput.contains("Super fast shipping!"));
    }
}
