package com.prakash.prakashmart.controller;

import com.prakash.prakashmart.dto.UserResponseDTO;
import com.prakash.prakashmart.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServletTest {

    @Mock
    private ChatService chatService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private ChatServlet servlet;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ChatServlet(chatService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("POST /api/chat with valid message returns AI assistant response")
    void testChatValidMessage() throws Exception {
        when(request.getSession(false)).thenReturn(session);
        UserResponseDTO user = new UserResponseDTO();
        user.setId(10L);
        when(session.getAttribute("currentUser")).thenReturn(user);

        String json = "{\"message\":\"What is your return policy?\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(chatService.processMessage(eq("What is your return policy?"), eq(10L)))
                .thenReturn("We offer a 30-day hassle-free return policy.");

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        String jsonOutput = responseWriter.toString();
        assertTrue(jsonOutput.contains("\"success\":true"));
        assertTrue(jsonOutput.contains("30-day hassle-free return policy"));
    }

    @Test
    @DisplayName("POST /api/chat with empty message returns 400 validation error")
    void testChatEmptyMessageReturns400() throws Exception {
        when(request.getSession(false)).thenReturn(null);
        String json = "{\"message\":\"\"}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String jsonOutput = responseWriter.toString();
        assertTrue(jsonOutput.contains("\"success\":false"));
        assertTrue(jsonOutput.contains("EMPTY_MESSAGE"));
    }
}
