package top.lrshuai.langchain4j.springboot.mcp.client.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface McpAssistant {

    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    TokenStream chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
