package top.lrshuai.langchain4j.springboot.rag.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

/**
 * RAG 检索增强生成助手
 * <p>
 * {@code @AiService} 由 langchain4j-spring-boot4-starter 自动创建代理 Bean，
 * 自动注入上下文中的 {@code ChatModel} 和 {@code RetrievalAugmentor}。
 */
// 这里不指定也是可以的，默认就是 openAiChatModel
@AiService(chatModel = "openAiChatModel", streamingChatModel = "openAiStreamingChatModel")
public interface RagAssistant {

    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    TokenStream chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
