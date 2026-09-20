package com.prakash.prakashmart.service;

import com.prakash.prakashmart.exception.AppException;

/**
 * Service interface for AI chatbot support and customer assistance.
 * Clean abstraction keeping server-side provider and API keys isolated.
 */
public interface ChatService {

    /**
     * Processes customer inquiry and returns an intelligent contextual response.
     *
     * @param userMessage Message query from buyer
     * @param userId      Optional authenticated user ID
     * @return AI assistant reply
     * @throws AppException if query cannot be processed
     */
    String processMessage(String userMessage, Long userId) throws AppException;
}
