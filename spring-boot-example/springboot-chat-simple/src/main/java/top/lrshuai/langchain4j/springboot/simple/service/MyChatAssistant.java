package top.lrshuai.langchain4j.springboot.simple.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.stereotype.Component;

@Component
@AiService
public interface MyChatAssistant {

    @SystemMessage("你是一个乐于助人的 AI 助手，用简洁清晰的中文回答问题。")
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    TokenStream chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
