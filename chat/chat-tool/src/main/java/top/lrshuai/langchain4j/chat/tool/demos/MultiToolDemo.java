package top.lrshuai.langchain4j.chat.tool.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.chat.tool.tools.CalculatorTool;
import top.lrshuai.langchain4j.chat.tool.tools.OrderTool;
import top.lrshuai.langchain4j.chat.tool.tools.WeatherTool;

/**
 * 多工具组合演示
 * <p>
 * 同时注册多个工具，LLM 会根据用户问题自动选择最合适的工具回答。
 *
 * @author rstyro
 */
@Slf4j
public class MultiToolDemo {

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
//                .logRequests(true)  // 打印request参数相关的，因为有tools 打印太多了
                .logResponses(true)
                .build();

        // 同时注册三个工具
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(new WeatherTool(), new CalculatorTool(), new OrderTool())
                .build();

        log.info("\n========== 多工具组合演示 ==========");

        // 天气问题 → 自动调用 WeatherTool
        String question1 = "广州今天热不热，适合穿短袖吗？";
        log.info("用户: {}", question1);
        log.info("AI: {}\n", assistant.chat(question1));

        // 计算问题 → 自动调用 CalculatorTool
        String question2 = "我买了3个苹果，每个12.5元，一共要花多少钱？我付了50元该找我多少？";
        log.info("用户: {}", question2);
        log.info("AI: {}\n", assistant.chat(question2));

        // 订单问题 → 自动调用 OrderTool
        String question3 = "我的ORD001订单发货了吗？如果已经发了我现在取消还可以吗？";
        log.info("用户: {}", question3);
        log.info("AI: {}\n", assistant.chat(question3));

        // 混合问题 → 自动选择对应工具
        String question4 = "现在是几点？帮我算下365乘以24等于多少？再帮我看看深圳明天的天气";
        log.info("用户: {}", question4);
        log.info("AI: {}", assistant.chat(question4));

        log.info("多工具模式下，LLM 会自动判断需要调用哪个工具，不需要人工指定");
    }
}
