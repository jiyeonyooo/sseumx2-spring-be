package com.sseumx2.common.response;

public record ApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data
) {
	public static <T> ApiResponse<T> success(String code, String message, T data) {
		return new ApiResponse<>(true, code, message, data);
	}
}
