package top.lrshuai.langchain4j.springboot.agentic.supervisor.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 汇率转换 Agent — 将币种 A 的金额转换为币种 B。
 * <p>
 * 绑定 ExchangeTool.exchange() 工具。
 */
public interface ExchangeAgent {

    @Agent(value = "币种转换Agent", description = "将指定币种的金额转换为目标币种，需要原币种、目标币种、金额三个参数")
    @UserMessage("请执行汇率转换：将 {{amount}} {{origin}} 转换为 {{target}}。调用 exchange 工具完成操作。")
    Double exchange(@V("origin") String origin, @V("target") String target, @V("amount") Double amount);
}
