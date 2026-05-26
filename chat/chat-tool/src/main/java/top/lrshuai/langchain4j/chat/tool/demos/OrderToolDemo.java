package top.lrshuai.langchain4j.chat.tool.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.chat.tool.tools.OrderTool;

/**
 * 订单工具独立演示
 *
 * @author rstyro
 */
@Slf4j
public class OrderToolDemo {

    private static final String LLM_BASE_URL = "https://ark.cn-beijing.volces.com/api/coding/v3";
    private static final String LLM_MODEL_NAME = "doubao-seed-2.0-pro";

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("AI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请设置环境变量 AI_API_KEY");
            System.exit(1);
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(LLM_BASE_URL)
                .modelName(LLM_MODEL_NAME)
//                .logRequests(true)
                .logResponses(true)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(new OrderTool())
                .build();

        log.info("\n========== 订单工具演示 ==========");

        // 查询订单详情
        String question1 = "帮我查一下订单ORD001的情况，花了多少钱？什么时候能到？";
        log.info("用户: {}", question1);
        log.info("AI: {}\n", assistant.chat(question1));

        // 查询物流
        String question2 = "我的ORD002订单到哪了？大概什么时候能收到？";
        log.info("用户: {}", question2);
        log.info("AI: {}\n", assistant.chat(question2));

        // 取消订单
        String question3 = "我要取消ORD003，原因是我不想买了";
        log.info("用户: {}", question3);
        log.info("AI: {}\n", assistant.chat(question3));

        // 查询不存在的订单
        String question4 = "帮我查ORD999这个订单的物流";
        log.info("用户: {}", question4);
        log.info("AI: {}", assistant.chat(question4));
    }
}
