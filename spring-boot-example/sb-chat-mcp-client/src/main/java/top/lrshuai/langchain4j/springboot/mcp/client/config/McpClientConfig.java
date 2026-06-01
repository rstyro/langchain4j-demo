package top.lrshuai.langchain4j.springboot.mcp.client.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class McpClientConfig {

    @Value("${mcp.server.url}")
    private String mcpServerUrl;

    // 1. 传输层：HTTP+SSE
    @Bean
    public McpTransport mcpTransport() {
        return StreamableHttpMcpTransport.builder()
                .url(mcpServerUrl)
                .logRequests(true)   // 调试用
                .logResponses(true)
                .build();
    }

    // 2. 创建 MCP 客户端
    @Bean
    public McpClient mcpClient(McpTransport transport) {
        return new DefaultMcpClient.Builder()
                .key("mcp-8004")
                .transport(transport)
                .build();
    }

    // 3. 把远端工具包装成 LangChain4j 可用的 ToolProvider
    @Bean
    public ToolProvider mcpToolProvider(McpClient mcpClient) {
        return McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();
    }

}
