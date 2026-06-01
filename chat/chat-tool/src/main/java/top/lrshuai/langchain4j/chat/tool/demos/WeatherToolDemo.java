package top.lrshuai.langchain4j.chat.tool.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;
import top.lrshuai.langchain4j.chat.tool.tools.WeatherTool;

/**
 * 天气工具独立演示
 *
 * @author rstyro
 */
@Slf4j
public class WeatherToolDemo {

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请设置环境变量 " + LlmConfig.API_KEY_ENV);
            System.exit(1);
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(LlmConfig.LLM_BASE_URL)
                .modelName(LlmConfig.LLM_MODEL_DOUBAO)
                .logRequests(true)
                .logResponses(true)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(chatModel)
                .tools(new WeatherTool())
                .build();

        log.info("\n========== 天气工具演示 ==========");

        // 查询当前天气
        String question1 = "北京今天天气怎么样？适合出门玩吗？";
        log.info("用户: {}", question1);
        log.info("AI: {}\n", assistant.chat(question1));

        // 查询未来天气预报
        String question2 = "上海未来3天天气怎么样，我要去出差需要带伞吗？";
        log.info("用户: {}", question2);
        log.info("AI: {}\n", assistant.chat(question2));

        // 对比无工具和有工具的区别
        log.info("如果没有注册 WeatherTool，LLM 只能给出基于训练数据的过时天气信息，无法获取实时数据");
    }
}
