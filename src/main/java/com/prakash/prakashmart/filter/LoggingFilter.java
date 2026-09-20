package com.prakash.prakashmart.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter generating a unique request ID per incoming HTTP request and binding
 * it to SLF4J MDC for structured tracing across the controller, service, and DAO layers.
 */
@WebFilter(filterName = "LoggingFilter", urlPatterns = "/*")
public class LoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestId = httpRequest.getHeader("X-Request-ID");
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }

        MDC.put(REQUEST_ID_KEY, requestId);
        httpResponse.setHeader("X-Request-ID", requestId);

        long startTime = System.currentTimeMillis();
        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        logger.info("Incoming HTTP Request: {} {}", method, uri);

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed HTTP Request: {} {} with status {} in {} ms",
                    method, uri, httpResponse.getStatus(), duration);
            MDC.remove(REQUEST_ID_KEY);
        }
    }

    @Override
    public void destroy() {
        // No resources to release
    }
}
