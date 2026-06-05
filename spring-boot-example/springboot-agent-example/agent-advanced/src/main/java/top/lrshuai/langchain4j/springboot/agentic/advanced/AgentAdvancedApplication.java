package top.lrshuai.langchain4j.springboot.agentic.advanced;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic — 高级特性综合示例。
 * <p>
 * 演示：Optional 可选 Agent、ErrorHandler 异常处理、AgentMonitor 可观测监控。
 * <p>
 * 对应 README 教程 §4。
 */
@SpringBootApplication
public class AgentAdvancedApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentAdvancedApplication.class, args);
    }
}
