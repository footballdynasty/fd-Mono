package com.footballdynasty.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {
    
    private ResponseUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Creates a standardized error response with the given status and message
     *
     * @param status  HTTP status code
     * @param message Error message
     * @return ResponseEntity with error response
     */
    public static ResponseEntity<Map<String, String>> createErrorResponse(HttpStatus status, String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return ResponseEntity.status(status).body(error);
    }
    
    /**
     * Creates a 500 Internal Server Error response with the given message
     *
     * @param message Error message
     * @return ResponseEntity with 500 status
     */
    public static ResponseEntity<Map<String, String>> createInternalServerError(String message) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    
    /**
     * Creates a 404 Not Found response with the given message
     *
     * @param message Error message
     * @return ResponseEntity with 404 status
     */
    public static ResponseEntity<Map<String, String>> createNotFoundError(String message) {
        return createErrorResponse(HttpStatus.NOT_FOUND, message);
    }
    
    /**
     * Creates a 400 Bad Request response with the given message
     *
     * @param message Error message
     * @return ResponseEntity with 400 status
     */
    public static ResponseEntity<Map<String, String>> createBadRequestError(String message) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, message);
    }
    
    /**
     * Creates a standardized paginated response
     *
     * @param content       The content to return
     * @param totalElements Total number of elements
     * @param totalPages    Total number of pages
     * @param currentPage   Current page number
     * @param size          Page size
     * @param hasNext       Whether there is a next page
     * @param hasPrevious   Whether there is a previous page
     * @return Map containing pagination metadata
     */
    public static Map<String, Object> createPaginatedResponse(
            Object content, 
            long totalElements, 
            int totalPages, 
            int currentPage, 
            int size, 
            boolean hasNext, 
            boolean hasPrevious) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("currentPage", currentPage);
        response.put("size", size);
        response.put("hasNext", hasNext);
        response.put("hasPrevious", hasPrevious);
        return response;
    }
}