package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.exception.ValidationException;
import com.prakash.prakashmart.service.ChatService;
import com.prakash.prakashmart.service.impl.ChatServiceImpl;
import com.prakash.prakashmart.dto.UserResponseDTO;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * Controller/Servlet handling customer support AI chatbot interactions.
 * REST endpoint: POST /api/chat, /api/v1/chat
 */
@WebServlet(name = "ChatServlet", urlPatterns = {"/api/chat", "/api/v1/chat"})
public class ChatServlet extends BaseServlet {

    private final ChatService chatService;

    public ChatServlet() {
        this(new ChatServiceImpl());
    }

    public ChatServlet(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String message = null;
            
            // Try parsing JSON body first
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = readJsonBody(req, Map.class);
                if (body != null && body.containsKey("message")) {
                    message = String.valueOf(body.get("message"));
                }
            } catch (Exception ignored) {
            }

            // Fallback to form parameter
            if (message == null || message.trim().isEmpty()) {
                message = req.getParameter("message");
            }

            if (message == null || message.trim().isEmpty()) {
                throw new ValidationException("Chat message cannot be empty", "EMPTY_MESSAGE");
            }

            UserResponseDTO currentUser = getSessionUser(req);
            Long userId = (currentUser != null) ? currentUser.getId() : null;

            String reply = chatService.processMessage(message.trim(), userId);
            writeJsonResponse(resp, HttpServletResponse.SC_OK, "Chat response generated successfully",
                    Collections.singletonMap("reply", reply));
        } catch (Exception e) {
            handleException(resp, e, true);
        }
    }
}
