package top.lrshuai.langchain4j.chat.memory;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;
import top.lrshuai.langchain4j.chat.memory.service.AiAssistant;

/**
 * 简单内存记忆demo
 */
@Slf4j
public class SimpleInMemoryAiServiceDemo {


    public static void main(String[] args) {
        String apiKey = System.getenv("AI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请设置环境变量 AI_API_KEY");
            System.exit(1);
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(LlmConfig.LLM_BASE_URL)
                .modelName(LlmConfig.LLM_MODEL_DOUBAO)
                .logRequests(true)
                .logResponses(true)
                .build();

        // 记忆存储（内存）
        ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

        // AI Services 是 LangChain4j 提供的高层、声明式 API 层，目标是：少写样板代码、
        // 把 LLM / 记忆 / 工具 / RAG 拼装成一个 “服务接口”，你只定义 Java 接口，框架动态生成代理实现
        AiAssistant aiAssistant = AiServices.builder(AiAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId->MessageWindowChatMemory.builder()
                        .id(memoryId) // 把 memoryId 绑到这个 Memory 实例
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();

        // 测试多用户隔离
        // 用户1
        System.out.println(aiAssistant.chat("user_1001", "我叫张三，今年20岁"));
        System.out.println(aiAssistant.chat("user_1001", "我叫什么？多大了？"));

        // 用户2
        System.out.println(aiAssistant.chat("user_1002", "我叫李四，今年30岁"));
        System.out.println(aiAssistant.chat("user_1002", "我叫什么？多大了？"));

    }
}
