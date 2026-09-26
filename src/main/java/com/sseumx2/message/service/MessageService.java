package com.sseumx2.message.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sseumx2.message.dto.MessageRequestDto;
import com.sseumx2.message.dto.ParsedTransactionDto;
import com.sseumx2.message.parser.MessageParser;

@Service
public class MessageService {

	private static final Logger log = LoggerFactory.getLogger(MessageService.class);
	private final MessageParser messageParser;

	public MessageService(MessageParser messageParser) {
		this.messageParser = messageParser;
	}

	public ParsedTransactionDto importMessage(MessageRequestDto request) {
		ParsedTransactionDto transaction = messageParser.parse(request);
		log.info("Payment message parsed. merchant={} price={}", transaction.merchantName(), transaction.price());
		return transaction;
	}
}
