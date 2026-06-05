package top.lrshuai.langchain4j.springboot.agentic.supervisor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LangChain4j-Agentic — Supervisor 自主智能体示例。
 * <p>
 * 演示场景：银行转账系统 — Supervisor 理解用户指令，
 * 自主规划执行链路（汇率转换 → 扣款 → 入账）。
 * <p>
 * 对应 README 教程 §6。
 */
@SpringBootApplication
public class AgentSupervisorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentSupervisorApplication.class, args);
    }
}
