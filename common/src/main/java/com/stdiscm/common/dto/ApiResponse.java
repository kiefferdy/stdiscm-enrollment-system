package com.stdiscm.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
// Removed @AllArgsConstructor, will add manual constructor below
public class ApiResponse<T> implements Serializable {
    
    private boolean success;
    private String message;
    private T data;

    // Manually added constructor for all arguments
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        // Explicitly specify type T in constructor call
        return new ApiResponse<T>(true, message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        // Explicitly specify type T in constructor call
        return new ApiResponse<T>(true, message, null); 
    }
    
    public static <T> ApiResponse<T> error(String message) {
        // Explicitly specify type T in constructor call
        return new ApiResponse<T>(false, message, null);
    }
}
