package com.sseumx2.message.dto;

import java.time.OffsetDateTime;

public record ParsedTransactionDto(
		String merchantName,
		long price,
		TransactionType transactionType,
		String category,
		String paymentMethod,
		OffsetDateTime occurredAt,
		String rawMessage
) {
}
