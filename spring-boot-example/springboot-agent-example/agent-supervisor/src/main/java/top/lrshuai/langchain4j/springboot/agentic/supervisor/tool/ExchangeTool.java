package top.lrshuai.langchain4j.springboot.agentic.supervisor.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 汇率转换工具 — 提供币种换算。
 * <p>
 * 绑定到 ExchangeAgent，支持 EUR→USD、CNY→USD 等转换。
 */
public class ExchangeTool {

    /**
     * 币种换算
     *
     * @param origin 原币种（如 EUR、CNY）
     * @param target 目标币种（如 USD）
     * @param amount 金额
     * @return 换算后的金额
     */
    @Tool("币种换算（EUR→USD: ×1.15, CNY→USD: ÷7.2）")
    public Double exchange(@P("原币种") String origin,
                           @P("目标币种") String target,
                           @P("金额") Double amount) {
        double rate = switch (origin.toUpperCase() + "->" + target.toUpperCase()) {
            case "EUR->USD" -> 1.15;
            case "CNY->USD" -> 1.0 / 7.2;
            default -> 1.0; // 默认 1:1
        };
        double result = amount * rate;
        System.out.printf("[ExchangeTool] %s → %s: %.2f × %.4f = %.2f%n",
                origin, target, amount, rate, result);
        return result;
    }
}
