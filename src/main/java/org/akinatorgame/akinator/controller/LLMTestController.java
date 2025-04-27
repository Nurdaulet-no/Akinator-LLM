package org.akinatorgame.akinator.controller;

import lombok.RequiredArgsConstructor;
import org.akinatorgame.akinator.dto.Message;
import org.akinatorgame.akinator.service.AkinatorLLMService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LLMTestController {
    private final AkinatorLLMService akinatorLLMService;

    @GetMapping("/ask-test")
    public ResponseEntity<String> askLLM(){
        List<Message> entryPrompt = akinatorLLMService.createInitPrompt();
        String llmResponse = akinatorLLMService.getLLMResponse(entryPrompt);

        return ResponseEntity.ok(llmResponse);
    }

    @PostMapping("ask")
    public ResponseEntity<String> askLLM(@RequestBody List<Message> history){
        String llmResponse = akinatorLLMService.getLLMResponse(history);
        return ResponseEntity.ok(llmResponse);
    }
}
