package com.sseumx2.message.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageRequestDto(
		@NotBlank String message,
		@NotNull OffsetDateTime receivedAt
) {
}
