package top.lrshuai.langchain4j.springboot.agentic.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic 入门示例 — Agent 定义、AgenticScope、Sequential 顺序工作流。
 * <p>
 * 演示场景：故事创作流水线 — 写作 → 受众改编 → 风格润色。
 * <p>
 */
@SpringBootApplication
public class AgentHelloWorldApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentHelloWorldApplication.class, args);
    }
}
