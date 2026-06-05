package top.lrshuai.langchain4j.springboot.agentic.supervisor.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.agent.CreditAgent;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.agent.ExchangeAgent;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.agent.WithdrawAgent;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.tool.BankTool;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.tool.ExchangeTool;

/**
 * Supervisor 自主智能体配置。
 * <p>
 * 构建银行转账 Supervisor：
 * <ol>
 *   <li>注册三个子 Agent（取款/存款/汇率转换），各自绑定工具</li>
 *   <li>Supervisor 使用 LLM 理解用户指令，自主规划调用链路</li>
 * </ol>
 */
@Configuration
public class SupervisorConfig {

    /**
     * 银行账户工具（单例，模拟内存数据库）
     */
    @Bean
    BankTool bankTool() {
        return new BankTool();
    }

    @Bean
    ExchangeTool exchangeTool() {
        return new ExchangeTool();
    }

    @Bean
    WithdrawAgent withdrawAgent(ChatModel chatModel, BankTool bankTool) {
        return AgenticServices.agentBuilder(WithdrawAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();
    }

    @Bean
    CreditAgent creditAgent(ChatModel chatModel, BankTool bankTool) {
        return AgenticServices.agentBuilder(CreditAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();
    }

    @Bean
    ExchangeAgent exchangeAgent(ChatModel chatModel, ExchangeTool exchangeTool) {
        return AgenticServices.agentBuilder(ExchangeAgent.class)
                .chatModel(chatModel)
                .tools(exchangeTool)
                .build();
    }

    /**
     * 银行转账 Supervisor — LLM 自主调度子 Agent。
     * <p>
     * 全局约束：所有资金最终使用 USD 结算，优先内置工具，禁止外部接口。
     */
    @Bean
    SupervisorAgent bankSupervisor(ChatModel chatModel,
                                    WithdrawAgent withdraw,
                                    CreditAgent credit,
                                    ExchangeAgent exchange) {
        return AgenticServices.supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(withdraw, credit, exchange)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY)
                .supervisorContext("""
                        规则：
                        1. 所有资金操作最终使用 USD 结算
                        2. 转账流程：先汇率转换 → 源账户取款 → 目标账户存款
                        3. 优先使用内置工具，禁止调用外部接口
                        4. 操作完成后输出转账摘要
                        """)
                .build();
    }
}
