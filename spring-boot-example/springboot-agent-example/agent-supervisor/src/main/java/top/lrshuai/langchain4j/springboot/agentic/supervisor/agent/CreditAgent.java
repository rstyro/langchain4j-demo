package top.lrshuai.langchain4j.springboot.agentic.supervisor.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 存款 Agent — 向用户账户存入 USD。
 * <p>
 * 绑定 BankTool.credit() 工具。
 */
public interface CreditAgent {

    @Agent(value = "美元存款Agent", description = "向指定用户账户存入指定金额（USD），需要用户名和金额两个参数")
    @UserMessage("请执行存款操作：向 {{user}} 账户存入 {{amount}} 美元。调用 credit 工具完成操作。")
    String credit(@V("user") String user, @V("amount") Double amount);
}
