package top.lrshuai.langchain4j.springboot.agentic.conditional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic — Conditional 条件路由工作流示例。
 * <p>
 * 演示场景：用户问题分类（医疗/法律/技术/未知）→ 路由对应专家回答。
 * <p>
 */
@SpringBootApplication
public class AgentConditionalApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentConditionalApplication.class, args);
    }
}
