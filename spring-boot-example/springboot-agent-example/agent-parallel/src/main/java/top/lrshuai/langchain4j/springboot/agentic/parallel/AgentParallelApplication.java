package top.lrshuai.langchain4j.springboot.agentic.parallel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic — Parallel 并行与 ParallelMapper 批量并发工作流示例。
 * <p>
 * 演示场景：根据情绪并行推荐美食+电影，组合约会方案；ParallelMapper 批量生成星座运势。
 * <p>
 */
@SpringBootApplication
public class AgentParallelApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentParallelApplication.class, args);
    }
}
