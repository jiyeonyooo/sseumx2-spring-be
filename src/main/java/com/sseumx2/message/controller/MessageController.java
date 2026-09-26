package com.sseumx2.message.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sseumx2.common.response.ApiResponse;
import com.sseumx2.message.dto.MessageRequestDto;
import com.sseumx2.message.dto.ParsedTransactionDto;
import com.sseumx2.message.service.MessageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

	private final MessageService messageService;

	public MessageController(MessageService messageService) {
		this.messageService = messageService;
	}

	@PostMapping("/import")
	public ResponseEntity<ApiResponse<ParsedTransactionDto>> importMessage(@Valid @RequestBody MessageRequestDto request) {
		ParsedTransactionDto transaction = messageService.importMessage(request);
		return ResponseEntity.ok(ApiResponse.success(
				"MESSAGE_IMPORT_SUCCESS",
				"결제 문자 파싱에 성공했습니다.",
				transaction
		));
	}
}
