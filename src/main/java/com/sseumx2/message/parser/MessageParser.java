package com.sseumx2.message.parser;

import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.sseumx2.common.exception.MessageParsingException;
import com.sseumx2.message.dto.MessageRequestDto;
import com.sseumx2.message.dto.ParsedTransactionDto;
import com.sseumx2.message.dto.TransactionType;

@Component
public class MessageParser {

	private static final Pattern AMOUNT_PATTERN = Pattern.compile("([\\d,]+)\\s*원");
	private static final Pattern PAYMENT_METHOD_PATTERN = Pattern.compile("^\\s*\\[([^\\]]+)]\\s*$");
	private static final Pattern BANK_DATE_TIME_PATTERN = Pattern.compile("^\\s*(\\S+?)(\\d{1,2}/\\d{1,2}\\s+\\d{1,2}:\\d{2})\\s*$");
	private static final Pattern BANK_TRANSACTION_PATTERN = Pattern.compile("(출금|입금)(?:액)?[\\s\\p{Zs}:]*([\\d,]+)");
	private static final Pattern DATE_TIME_PATTERN = Pattern.compile("(\\d{1,2})/(\\d{1,2})\\s+(\\d{1,2}):(\\d{2})");
	private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^\\d{2,6}(-\\d{2,6}){1,4}$");
	private static final Pattern BALANCE_PATTERN = Pattern.compile("^잔액\\s+[\\d,]+$");
	private static final String WEB_SENDER_PREFIX = "[Web발신]";
	private static final String DEFAULT_CATEGORY = "기타";

	public ParsedTransactionDto parse(MessageRequestDto request) {
		String rawMessage = request.message();
		List<String> lines = splitLines(rawMessage);

		ParsedAmount parsedAmount = parseAmount(rawMessage, lines);
		String paymentMethod = parsePaymentMethod(lines);
		OffsetDateTime occurredAt = parseOccurredAt(rawMessage, request.receivedAt());
		String merchantName = parseMerchantName(lines);

		return new ParsedTransactionDto(
				merchantName,
				parsedAmount.price(),
				parsedAmount.transactionType(),
				DEFAULT_CATEGORY,
				paymentMethod,
				occurredAt,
				rawMessage
		);
	}

	private List<String> splitLines(String message) {
		return Arrays.stream(message.split("\\R"))
				.map(String::trim)
				.filter(line -> !line.isBlank())
				.toList();
	}

	private ParsedAmount parseAmount(String message, List<String> lines) {
		Matcher matcher = AMOUNT_PATTERN.matcher(message);
		if (matcher.find()) {
			return new ParsedAmount(parseAmountText(matcher.group(1)), TransactionType.WITHDRAWAL);
		}

		return lines.stream()
				.map(BANK_TRANSACTION_PATTERN::matcher)
				.filter(Matcher::find)
				.map(matched -> new ParsedAmount(
						parseAmountText(matched.group(2)),
						parseTransactionType(matched.group(1))
				))
				.findFirst()
				.orElseThrow(() -> new MessageParsingException("결제 문자에서 금액을 추출할 수 없습니다."));
	}

	private TransactionType parseTransactionType(String transactionTypeText) {
		return switch (transactionTypeText) {
			case "출금" -> TransactionType.WITHDRAWAL;
			case "입금" -> TransactionType.DEPOSIT;
			default -> throw new MessageParsingException("결제 문자 거래 종류가 올바르지 않습니다.");
		};
	}

	private long parseAmountText(String amountText) {
		try {
			return Long.parseLong(amountText.replace(",", ""));
		} catch (NumberFormatException exception) {
			throw new MessageParsingException("결제 문자 금액 형식이 올바르지 않습니다.");
		}
	}

	private String parsePaymentMethod(List<String> lines) {
		return lines.stream()
				.map(PAYMENT_METHOD_PATTERN::matcher)
				.filter(Matcher::matches)
				.map(matcher -> matcher.group(1).trim())
				.filter(paymentMethod -> !WEB_SENDER_PREFIX.equals("[" + paymentMethod + "]"))
				.findFirst()
				.or(() -> parseBankPaymentMethod(lines))
				.orElseThrow(() -> new MessageParsingException("결제 문자에서 결제수단을 추출할 수 없습니다."));
	}

	private Optional<String> parseBankPaymentMethod(List<String> lines) {
		return lines.stream()
				.map(BANK_DATE_TIME_PATTERN::matcher)
				.filter(Matcher::matches)
				.map(matcher -> matcher.group(1).trim())
				.findFirst();
	}

	private OffsetDateTime parseOccurredAt(String message, OffsetDateTime receivedAt) {
		Matcher matcher = DATE_TIME_PATTERN.matcher(message);
		if (!matcher.find()) {
			return receivedAt.withSecond(0).withNano(0);
		}

		int month = Integer.parseInt(matcher.group(1));
		int day = Integer.parseInt(matcher.group(2));
		int hour = Integer.parseInt(matcher.group(3));
		int minute = Integer.parseInt(matcher.group(4));
		MonthDay.of(month, day);

		ZoneOffset offset = receivedAt.getOffset();
		LocalDateTime localDateTime = LocalDateTime.of(
				receivedAt.getYear(),
				month,
				day,
				hour,
				minute
		);

		return OffsetDateTime.of(localDateTime, offset);
	}

	private String parseMerchantName(List<String> lines) {
		return lines.stream()
				.filter(line -> !PAYMENT_METHOD_PATTERN.matcher(line).matches())
				.filter(line -> !WEB_SENDER_PREFIX.equals(line))
				.filter(line -> !BANK_DATE_TIME_PATTERN.matcher(line).matches())
				.filter(line -> !DATE_TIME_PATTERN.matcher(line).find())
				.filter(line -> !AMOUNT_PATTERN.matcher(line).find())
				.filter(line -> !BANK_TRANSACTION_PATTERN.matcher(line).find())
				.filter(line -> !ACCOUNT_NUMBER_PATTERN.matcher(line).matches())
				.filter(line -> !BALANCE_PATTERN.matcher(line).matches())
				.findFirst()
				.orElseThrow(() -> new MessageParsingException("결제 문자에서 가맹점을 추출할 수 없습니다."));
	}

	private record ParsedAmount(
			long price,
			TransactionType transactionType
	) {
	}
}
