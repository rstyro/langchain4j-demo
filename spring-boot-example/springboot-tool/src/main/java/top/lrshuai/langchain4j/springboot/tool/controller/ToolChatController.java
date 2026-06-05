package top.lrshuai.langchain4j.springboot.tool.controller;

import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.lrshuai.langchain4j.common.resp.R;
import top.lrshuai.langchain4j.springboot.tool.service.ToolAssistant;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ToolChatController {

    private final ToolAssistant assistant;

    @PostMapping("/chat")
    @CrossOrigin
    public R<String> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        if (message.isBlank()) {
            return R.error("message 不能为空");
        }
        int memoryId = parseMemoryId(request);
        log.info("收到消息 [memoryId={}]: {}", memoryId, message);
        String reply = assistant.chat(memoryId, message);
        log.info("AI 回复 [memoryId={}]: {}", memoryId, reply);
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
        log.info("收到流式消息 [memoryId={}]: {}", memoryId, message);

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
        try {
            return Integer.parseInt(request.getOrDefault("memoryId", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
