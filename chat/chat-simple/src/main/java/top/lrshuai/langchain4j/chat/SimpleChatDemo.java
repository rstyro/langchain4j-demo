package top.lrshuai.langchain4j.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;

import java.util.List;

/**
 * LangChain4j ChatRequest 完整使用示例
 * @author rstyro
 */
@Slf4j
public class SimpleChatDemo {

    public static void main(String[] args) {
        // 第一步：获取并校验 API Key
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("环境变量 " + LlmConfig.API_KEY_ENV + " 未配置，请在运行前设置：");
            log.error("  Windows PowerShell: $env:" + LlmConfig.API_KEY_ENV + "=\"your-api-key\"");
            log.error("  Linux/Mac: export " + LlmConfig.API_KEY_ENV + "=\"your-api-key\"");
            System.exit(1);
        }

        // 第二步：构建 LLM 聊天模型
        // OpenAiChatModel 是 LangChain4j 提供的 OpenAI 协议兼容模型封装，
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                // API Key：从环境变量读取，保证安全性
                .apiKey(apiKey)
                // 服务地址：火山引擎豆包/DeepSeek API 的 OpenAI 兼容端点
                .baseUrl(LlmConfig.LLM_BASE_URL)
                // 模型名称：指定使用的模型
                .modelName(LlmConfig.LLM_MODEL_DEEPSEEK)
                .logRequests(true)   // 开启请求日志（记录发送给 LLM 的完整请求内容）
                .logResponses(true)  // 开启响应日志（记录 LLM 返回的完整响应内容）
                .build();

        // 第三步：演示 1 - 单轮对话（自定义 ChatRequest）
        // ChatRequest 是 LangChain4j 中最灵活的请求构建方式，
        // 支持精细控制消息内容、生成参数、工具调用等。
        log.info("========== 演示 1：单轮对话（自定义 ChatRequest）==========");
        demoCustomChatRequest(chatModel);

        // 第四步：演示 2 - 多轮对话
        // 多轮对话通过维护消息列表实现，每次将历史的 User 和 AI 消息一并传入，
        // 使 LLM 能够理解对话上下文，实现连续交互。
        log.info("\n========== 演示 2：多轮对话 ==========");
        demoMultiTurnChat(chatModel);

        // 第五步：演示 3 - JSON 格式输出
        // 设置 ResponseFormat.JSON 可以强制 LLM 输出结构化 JSON，
        // 适合用于数据提取、格式化输出等场景。
        log.info("\n========== 演示 3：JSON 格式输出 ==========");
        demoJsonResponse(chatModel);

        log.info("\n所有演示完成！");
    }

    /**
     * 演示 1：使用 ChatRequest 构建自定义聊天请求
     */
    private static void demoCustomChatRequest(OpenAiChatModel chatModel) {
        // 构建自定义聊天请求
        ChatRequest chatRequest = ChatRequest.builder()
                // ===== 1. 消息构建 =====
                // SystemMessage：设定 AI 的角色和行为准则（系统提示词）
                .messages(List.of(
                        SystemMessage.from("你是一个资深的 Java 后端开发专家，精通 LangChain4j 等框架。" +
                                "请用简洁准确的语言回答问题，必要时提供代码示例。"),
                        UserMessage.from("请用 LangChain4j 写一个 ChatRequest 的完整使用示例。")
                ))

                // ===== 2. 模型名称（可选，取决于 ChatModel 实现）=====
                // 如果需要在请求级别覆盖模型，可以在这里指定
                // .modelName("kimi-k2.5")

                // ===== 3. 生成参数 =====
                // temperature（温度）：控制生成文本的随机性
                //   - 取值范围 0.0 ~ 2.0
                //   - 值越低，输出越确定和保守（适合代码生成、事实问答）
                //   - 值越高，输出越有创造性和多样性（适合创意写作、头脑风暴）
                .temperature(0.3)
                // topP（核采样）：控制候选 Token 的概率累加阈值
                //   - 取值范围 0.0 ~ 1.0
                //   - 只考虑概率累加达到 topP 的 Token
                //   - 与 temperature 通常二选一使用
                .topP(0.95)
                // topK：限制候选 Token 的数量（部分模型支持，如 Gemini/Claude）
                // .topK(40)
                // frequencyPenalty（频率惩罚）：根据 Token 出现频率进行惩罚
                //   - 取值范围 -2.0 ~ 2.0
                //   - 正值会减少重复用词，鼓励使用更多样化的词汇
                .frequencyPenalty(0.0)
                // presencePenalty（存在惩罚）：根据 Token 是否出现过进行惩罚
                //   - 取值范围 -2.0 ~ 2.0
                //   - 正值会鼓励讨论新主题，避免重复话题
                .presencePenalty(0.0)
                // maxOutputTokens（最大输出 Token 数）：限制 LLM 回复的最大长度
                .maxOutputTokens(2048)

                // ===== 4. 停止词 =====
                // 当 LLM 生成到停止词时，会立即停止生成
                // 适用于需要截断特定标记的场景，如代码生成时遇到 "###" 停止
                .stopSequences(List.of("###", "END"))

                // ===== 5. Function / Tool Calling =====
                // 工具调用允许 LLM 在回复中请求调用外部函数
                // 适用于需要 LLM 查询数据库、调用 API 或执行计算的场景
                // .toolSpecifications(List.of(
                //         ToolSpecification.builder()
                //                 .name("getWeather")
                //                 .description("查询指定城市的天气")
                //                 .addParameter("city", JsonSchemaProperty.STRING)
                //                 .build()
                // ))
                // ToolChoice 控制 LLM 是否/如何选择工具：
                //   - AUTO：让模型自主决定是否使用工具（默认）
                //   - NONE：禁止模型使用工具
                //   - REQUIRED：强制模型必须使用某个工具
                .toolChoice(ToolChoice.AUTO)

                // ===== 6. 输出格式 =====
                // ResponseFormat 控制 LLM 的输出格式：
                //   - 不设置：默认普通文本输出
                //   - JSON：强制输出合法 JSON（需要模型支持）
                // .responseFormat(ResponseFormat.JSON)

                .build();

        // ===== 发送请求并处理响应 =====
        log.info("正在发送聊天请求...");
        ChatResponse chatResponse = chatModel.chat(chatRequest);

        // 提取 AI 回复消息
        AiMessage aiMessage = chatResponse.aiMessage();
        String answer = aiMessage.text();
        String thinking = aiMessage.thinking();
        TokenUsage tokenUsage = chatResponse.tokenUsage();
        FinishReason finishReason = chatResponse.finishReason();

        // 输出回复内容
        System.out.println("\n========== 自定义请求回复 ==========");
        System.out.println("AI 回答:\n" + answer);

        // 输出思考过程（如果模型支持思维链/推理）
        if (thinking != null && !thinking.isBlank()) {
            System.out.println("\n思考过程:\n" + thinking);
        }

        // ===== 分析响应元数据 =====
        System.out.println("\n--- 响应元数据 ---");
        System.out.println("结束原因: " + formatFinishReason(finishReason));
        if (tokenUsage != null) {
            System.out.println("Token 用量:");
            System.out.println("  输入 Token : " + tokenUsage.inputTokenCount());
            System.out.println("  输出 Token : " + tokenUsage.outputTokenCount());
            System.out.println("  总计 Token : " + tokenUsage.totalTokenCount());
        }
    }

    /**
     * 演示 2：多轮对话
     */
    private static void demoMultiTurnChat(OpenAiChatModel chatModel) {
        // ===== 第一轮对话 =====
        System.out.println("\n--- 第一轮对话 ---");
        ChatRequest turn1Request = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from("你是一个 Java 编程助手。"),
                        UserMessage.from("Java 中 List 和 Set 有什么区别？")
                ))
                .temperature(0.3)
                .maxOutputTokens(512)
                .build();
        ChatResponse turn1Response = chatModel.chat(turn1Request);
        System.out.println("用户: Java 中 List 和 Set 有什么区别？");
        System.out.println("AI: " + turn1Response.aiMessage().text());

        // ===== 第二轮对话（携带历史上下文） =====
        System.out.println("\n--- 第二轮对话（携带历史上下文） ---");
        ChatRequest turn2Request = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from("你是一个 Java 编程助手。"),
                        // 传入第一轮的历史消息
                        UserMessage.from("Java 中 List 和 Set 有什么区别？"),
                        turn1Response.aiMessage(),
                        // 当前用户的新问题
                        UserMessage.from("那 HashMap 和 ConcurrentHashMap 呢？")
                ))
                .temperature(0.3)
                .maxOutputTokens(512)
                .build();
        ChatResponse turn2Response = chatModel.chat(turn2Request);
        System.out.println("用户: 那 HashMap 和 ConcurrentHashMap 呢？");
        System.out.println("AI: " + turn2Response.aiMessage().text());

        // 输出第二轮对话的 Token 消耗
        TokenUsage turn2Tokens = turn2Response.tokenUsage();
        if (turn2Tokens != null) {
            System.out.println("\n（第二轮对话 Token 用量：输入 "
                    + turn2Tokens.inputTokenCount() + "，输出 "
                    + turn2Tokens.outputTokenCount() + "）");
        }
    }

    /**
     * 演示 3：结构化输出（Prompt Engineering 方式）
     */
    private static void demoJsonResponse(OpenAiChatModel chatModel) {
        // 使用 Prompt Engineering 引导模型输出结构化文本
        // 通过明确的指令让模型按照指定的格式输出
        ChatRequest jsonRequest = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from("你是一个数据提取助手。请严格按照以下 JSON 格式输出，"
                                + "不要添加任何解释性文字、不要使用代码块标记、直接输出纯 JSON：\n"
                                + "{\"projectName\":\"项目名称\",\"javaVersion\":\"Java版本\","
                                + "\"framework\":\"框架\",\"llmSdk\":\"LLM SDK\"}"),
                        UserMessage.from("请将以下信息提取为 JSON：\n" +
                                "项目名称：LangChain4j Demo\n" +
                                "Java 版本：17\n" +
                                "框架：Spring Boot 4.0.6\n" +
                                "LLM SDK：LangChain4j 1.15.0")
                ))
                .temperature(0.1) // 低温度确保输出格式稳定
                .maxOutputTokens(1024)
//                .responseFormat(ResponseFormat.JSON) // 有些模型不支持这个参数，后面可以用langchain4j的Prompt 硬约束 + Structured Output
                .build();

        log.info("请求结构化输出...");
        ChatResponse jsonResponse = chatModel.chat(jsonRequest);
        String jsonAnswer = jsonResponse.aiMessage().text();

        System.out.println("\n========== 结构化输出（Prompt 引导） ==========");
        System.out.println(jsonAnswer);
    }

    /**
     * 格式化结束原因，输出中文描述
     */
    private static String formatFinishReason(FinishReason finishReason) {
        if (finishReason == null) {
            return "未知";
        }
        switch (finishReason) {
            case STOP:
                // 正常结束：模型自然生成了完整的回复
                return "STOP - 正常结束（模型已完成回复）";
            case LENGTH:
                // 达到最大 Token 限制：回复被截断
                return "LENGTH - 达到最大 Token 限制（回复被截断）";
            case TOOL_EXECUTION:
                // 模型请求执行工具调用
                return "TOOL_EXECUTION - 触发工具调用";
            case CONTENT_FILTER:
                // 内容被过滤：回复触发了安全过滤机制
                return "CONTENT_FILTER - 内容被安全过滤";
            default:
                return finishReason.name() + " - 其他结束原因";
        }
    }
}
