package com.e_commerce.e_commerce.controller;

import com.e_commerce.e_commerce.dto.response.ApiResponse;
import com.e_commerce.e_commerce.service.ChatbotService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChatbotController {
    ChatbotService chatbotService;

    @PostMapping
    ApiResponse<String> chat(@RequestBody String message) {
        return ApiResponse.<String>builder()
                .result(chatbotService.chat(message))
                .build();
    }
}
