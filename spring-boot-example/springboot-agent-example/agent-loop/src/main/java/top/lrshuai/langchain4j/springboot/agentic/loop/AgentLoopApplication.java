package top.lrshuai.langchain4j.springboot.agentic.loop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic — Loop 循环工作流示例。
 * <p>
 * 演示场景：故事评分 → 迭代优化，分数 ≥ 0.8 退出，最大 5 轮。
 * <p>
 */
@SpringBootApplication
public class AgentLoopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentLoopApplication.class, args);
    }
}
