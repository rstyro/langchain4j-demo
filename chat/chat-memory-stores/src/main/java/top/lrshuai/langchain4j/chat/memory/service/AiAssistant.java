package top.lrshuai.langchain4j.chat.memory.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface AiAssistant {
    /**
     * 普通提问
     * @param userId 会话ID,用于隔离历史记录
     * @param userMessage 用户消息
     * @return 回答
     */
    String chat(@MemoryId String userId, @UserMessage String userMessage);
}
