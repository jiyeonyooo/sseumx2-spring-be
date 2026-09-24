package com.sseumx2.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(fieldError -> fieldError.getField() + " 값이 올바르지 않습니다.")
				.orElse("요청 값이 올바르지 않습니다.");

		return ResponseEntity.badRequest()
				.body(ErrorResponse.of("INVALID_REQUEST", message));
	}

	@ExceptionHandler(MessageParsingException.class)
	public ResponseEntity<ErrorResponse> handleMessageParsingException(MessageParsingException exception) {
		log.warn("Failed to parse payment message. message={}", exception.getMessage());
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(ErrorResponse.of("MESSAGE_PARSE_FAILED", exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception) {
		log.error("Unhandled server error.", exception);
		return ResponseEntity.internalServerError()
				.body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다."));
	}

}
