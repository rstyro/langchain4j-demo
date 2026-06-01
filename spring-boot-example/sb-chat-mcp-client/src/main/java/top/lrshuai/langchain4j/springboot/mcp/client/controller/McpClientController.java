package top.lrshuai.langchain4j.springboot.mcp.client.controller;

import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.lrshuai.langchain4j.common.resp.R;
import top.lrshuai.langchain4j.springboot.mcp.client.service.McpAssistant;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/mcp-client")
@RequiredArgsConstructor
@CrossOrigin
public class McpClientController {

    private final McpAssistant assistant;

    @PostMapping("/chat")
    @CrossOrigin
    public R<String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isBlank()) {
            return R.error("message 不能为空");
        }
        int memoryId = parseMemoryId(request);
        String reply = assistant.chat(memoryId, message);
        return R.ok(reply);
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin
    public SseEmitter chatStream(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message 不能为空");
        }
        int memoryId = parseMemoryId(request);
        log.info("收到流式问题 [memoryId={}]: {}", memoryId, message);
        SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(5));
        TokenStream tokenStream = assistant.chatStream(memoryId, message);
        tokenStream.onPartialResponse(token -> {
                    try {
                        emitter.send(SseEmitter.event().data(R.ok(token)));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .onCompleteResponse(response -> emitter.complete())
                .onError(e -> {
                    try {
                        emitter.send(SseEmitter.event().data(R.error("AI 流式响应异常: " + e.getMessage())));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                })
                .start();
        return emitter;
    }

    private int parseMemoryId(Map<String, String> request) {
        String memoryIdStr = request.getOrDefault("memoryId", "0");
        try {
            return Integer.parseInt(memoryIdStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
