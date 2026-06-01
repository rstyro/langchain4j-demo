package top.lrshuai.langchain4j.chat.memory;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;
import top.lrshuai.langchain4j.chat.memory.service.WeatherAssistant;

/**
 * json结构化输出 Demo
 */
@Slf4j
public class StructuredOutputDemo {


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
                // 结果返回json格式，(需要模型支持)
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build();

        // 记忆存储（内存）
        ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();

        // WeatherAssistant 是一个自定义的天气助手
        WeatherAssistant aiAssistant = AiServices.builder(WeatherAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId->MessageWindowChatMemory.builder()
                        .id(memoryId) // 把 memoryId 绑到这个 Memory 实例
                        .maxMessages(20)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();

        // 结构化输出 (需要模型支持)
        WeatherAssistant.WeatherResultVo resultVo = aiAssistant.chat("user_1001", "今日深圳的天气怎样");
        System.out.println(resultVo.toString());

        WeatherAssistant.WeatherResultVo resultVo2 = aiAssistant.chat("user_1002", "今日深圳的天气怎样");
        System.out.println(resultVo2.toString());
    }
}
