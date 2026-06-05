package top.lrshuai.langchain4j.springboot.mcp.client.config;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Slf4j
@Configuration
public class McpClientConfig {

    @Value("${mcp.server.url:http://localhost:3000}")
    private String mcpServerUrl;

    @Bean
    public McpClient mcpClient() {
        McpTransport transport = StreamableHttpMcpTransport.builder()
                .url(mcpServerUrl)
                .build();

        McpClient mcpClient = DefaultMcpClient.builder()
                .key("remote-mcp-server")
                .transport(transport)
                .build();
        log.info("MCP Client 已配置，连接到: {}", mcpServerUrl);
        return mcpClient;
    }
}
