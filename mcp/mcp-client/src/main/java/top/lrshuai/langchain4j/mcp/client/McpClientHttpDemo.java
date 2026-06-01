package top.lrshuai.langchain4j.mcp.client;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;
import top.lrshuai.langchain4j.mcp.client.consts.ConfigConst;

/**
 * MCP 客户端 - HTTP 方式调用（远程服务模式）
 * <p>
 * 适用于连接外部 HTTP MCP Server 的场景，例如：
 * <ul>
 *   <li>npx @modelcontextprotocol/server-filesystem（文件系统 MCP Server）</li>
 *   <li>npx @modelcontextprotocol/server-github（GitHub MCP Server）</li>
 *   <li>npx @modelcontextprotocol/server-postgres（Postgres MCP Server）</li>
 *   <li>其他提供了 HTTP 端点的第三方 MCP 服务器</li>
 * </ul>
 *
 * <p>
 * ⚠️ 注意：本项目的 McpServerDemo 只实现 Stdio 传输，不支持 HTTP。
 * 如果要连接自己写的 MCP Server，请使用 {@link McpClientStdioDemo}。
 *
 * <pre>
 * 运行前需要先启动一个 HTTP MCP Server，例如：
 *   npx @modelcontextprotocol/server-everything
 * 然后修改 url 指向该 Server 的实际地址。
 * </pre>
 */
@Slf4j
public class McpClientHttpDemo {

    interface Assistant {
        String chat(String message);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请先设置环境变量 {}", LlmConfig.API_KEY_ENV);
            return;
        }

        log.info("========== MCP HTTP Client 演示 ==========");
        log.info("注意：需要先启动一个第三方 HTTP MCP Server（如 npx @modelcontextprotocol/server-everything）");
        log.info("本项目自带的 McpServerDemo 不支持 HTTP，请用 McpClientStdioDemo 连接它");

        // =====================================================
        // 步骤1：配置 HTTP 传输 —— 连接到远程 MCP Server
        // =====================================================
        McpTransport transport = StreamableHttpMcpTransport.builder()
                .url("http://localhost:3001/mcp")  // 修改为你的 HTTP MCP Server 地址
                .logRequests(true)
                .logResponses(true)
                .build();

        // =====================================================
        // 步骤2：创建 MCP 客户端
        // =====================================================
        McpClient mcpClient = DefaultMcpClient.builder()
                .key("remote-mcp-server")
                .transport(transport)
                .build();

        // =====================================================
        // 步骤3：获取 MCP 工具并创建 AI 助手
        // =====================================================
        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(LlmConfig.LLM_BASE_URL)
                        .modelName(LlmConfig.LLM_MODEL_DEEPSEEK)
                        .temperature(0.7)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .toolProvider(toolProvider)
                .build();

        try {
            String answer = assistant.chat("列出当前目录下的所有文件");
            log.info("回答：{}", answer);
        } catch (Exception e) {
            log.error("MCP HTTP 调用失败，请确认 HTTP MCP Server 已启动", e);
        } finally {
            try {
                mcpClient.close();
            } catch (Exception e) {
                log.error("关闭 MCP 客户端失败", e);
            }
        }

        log.info("\nMCP HTTP Client 演示完成！");
    }
}
