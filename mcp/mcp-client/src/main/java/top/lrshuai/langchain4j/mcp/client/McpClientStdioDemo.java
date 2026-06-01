package top.lrshuai.langchain4j.mcp.client;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;

import java.util.List;

/**
 * MCP 客户端 - Stdio 方式调用（子进程模式）
 * <p>
 * ✅ 本项目推荐用法：与本项目 McpServerDemo 直接配套使用。
 * 通过 java -jar 启动 fat jar 作为子进程，用 stdin/stdout 通信来调用工具。
 * <p>
 * 前置条件：先将 mcp-server 打包成 fat jar（含所有依赖）
 * <pre>
 *   mvn package -DskipTests -pl mcp/mcp-server -am
 * </pre>
 */
@Slf4j
public class McpClientStdioDemo {

    interface Assistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请先设置环境变量 {}", LlmConfig.API_KEY_ENV);
            return;
        }

        // =====================================================
        // 步骤1：配置 Stdio 传输 —— 通过 java -jar 启动 fat jar
        // =====================================================
        McpTransport transport = StdioMcpTransport.builder()
                .command(List.of(
                        "java",
                        "-Dfile.encoding=UTF-8",
                        "-jar",
                        "mcp/mcp-server/target/mcp-server-1.0.0.jar"
                ))
                .logEvents(true)
                .build();

        // =====================================================
        // 步骤2：创建 MCP 客户端
        // =====================================================
        McpClient mcpClient = DefaultMcpClient.builder()
                .key("my-mcp-server")
                .transport(transport)
                .build();

        // =====================================================
        // 步骤3：从 MCP Client 获取工具
        // =====================================================
        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        // =====================================================
        // 步骤4：创建 AI 助手（LLM + MCP 工具）
        // =====================================================
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(LlmConfig.LLM_BASE_URL)
                        .modelName(LlmConfig.LLM_MODEL_DEEPSEEK)
                        .temperature(0.7)
//                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .toolProvider(toolProvider)  // 关键：注入 MCP 工具
                .build();

        try {
            // =====================================================
            // 步骤5：测试对话 —— AI 会自动调用 MCP 工具
            // =====================================================
            log.info("========== MCP Stdio Client 演示 ==========");

            // 测试1：计算器
            String answer1 = assistant.chat("123.5 乘以 4.2 等于多少？");
            log.info("回答1：{}", answer1);

            // 测试2：日期时间
            String answer2 = assistant.chat("今天是星期几？");
            log.info("回答2：{}", answer2);

            // 测试3：文本处理
            String answer3 = assistant.chat("把 'Hello MCP World' 反转过来");
            log.info("回答3：{}", answer3);

            // 测试4：文件系统
            String answer4 = assistant.chat("帮我看看 pom.xml 这个文件有多大");
            log.info("回答4：{}", answer4);

            // 测试5：多工具联动
            String answer5 = assistant.chat("先帮我算一下(10+20)*3的结果，再告诉我结果的反转文本是什么");
            log.info("回答5：{}", answer5);

        } catch (Exception e) {
            log.error("MCP 调用失败", e);
        } finally {
            try {
                mcpClient.close();
            } catch (Exception e) {
                log.error("关闭 MCP 客户端失败", e);
            }
        }

        log.info("\nMCP Stdio Client 演示完成！");
    }
}
