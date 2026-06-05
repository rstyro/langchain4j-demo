package top.lrshuai.langchain4j.springboot.agentic.supervisor.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 取款 Agent — 从用户账户扣除 USD。
 * <p>
 * 绑定 BankTool.withdraw() 工具。
 */
public interface WithdrawAgent {

    @Agent(value = "美元取款Agent", description = "从指定用户账户中扣除指定金额（USD），需要用户名和金额两个参数")
    @UserMessage("请执行取款操作：从 {{user}} 账户扣除 {{amount}} 美元。调用 withdraw 工具完成操作。")
    String withdraw(@V("user") String user, @V("amount") Double amount);
}
