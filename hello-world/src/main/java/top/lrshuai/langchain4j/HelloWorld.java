package top.lrshuai.langchain4j;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class HelloWorld {
    public static void main(String[] args) {
        String apiKey = System.getenv("AI_API_KEY");
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://ark.cn-beijing.volces.com/api/coding/v3")
                .modelName("deepseek-v3.2")
                .temperature(0.7)                          // 控制随机性，0-2 之间
                .logRequests(true)                         // 开启请求日志，便于调试
                .logResponses(true)                        // 开启响应日志
                .build();

        String answer = model.chat("你好呀，介绍一下你自己");
        System.out.println(answer); // Hello World
    }
}
