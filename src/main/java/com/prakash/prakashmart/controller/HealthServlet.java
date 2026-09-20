package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.util.DatabaseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint verifying service and database availability.
 */
@WebServlet(name = "HealthServlet", urlPatterns = "/api/v1/health")
public class HealthServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                status.put("db", "UP");
            } else {
                status.put("db", "DOWN");
            }
        } catch (Exception e) {
            status.put("db", "DOWN");
        }

        writeJsonResponse(resp, HttpServletResponse.SC_OK, status);
    }
}
