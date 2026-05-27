package top.lrshuai.langchain4j.chat.rag.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

/**
 * 带记忆的RAG助手接口
 */
public interface MemoryRagAssistant {

    /**
     * 对话方法
     * @param memoryId 记忆ID，用来区分不同用户/会话的记忆
     * @param userMessage 用户问题
     * @return 回答
     */
    String chat(@MemoryId String memoryId, @UserMessage String userMessage);

}
