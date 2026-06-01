package top.lrshuai.langchain4j.mcp.server;

import dev.langchain4j.community.mcp.server.McpServer;
import dev.langchain4j.community.mcp.server.transport.StdioMcpServerTransport;
import dev.langchain4j.mcp.protocol.McpImplementation;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.mcp.server.tools.CalculatorTool;
import top.lrshuai.langchain4j.mcp.server.tools.DateTimeTool;
import top.lrshuai.langchain4j.mcp.server.tools.FileSystemTool;
import top.lrshuai.langchain4j.mcp.server.tools.TextTool;

import java.util.List;

@Slf4j
public class McpServerDemo {

    public static void main(String[] args) throws InterruptedException {
        McpImplementation serverInfo = new McpImplementation();
        serverInfo.setName("my-java-mcp-server");
        serverInfo.setVersion("1.0.0");

        // 注册工具
        List<Object> tools = List.of(
                new CalculatorTool(),
                new DateTimeTool(),
                new TextTool(),
                new FileSystemTool()
        );
        log.info("已注册 {} 个工具：计算器、日期时间、文本处理、文件系统", tools.size());

        McpServer mcpServer = new McpServer(tools, serverInfo);

        log.info("启动 MCP Server");
        new StdioMcpServerTransport(System.in, System.out, mcpServer);
        // 在stdio打开时保持进程存活
        Thread.currentThread().join();
    }
}
