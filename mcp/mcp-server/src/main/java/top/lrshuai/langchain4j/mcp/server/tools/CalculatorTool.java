package top.lrshuai.langchain4j.mcp.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 计算器工具
 */
@Slf4j
public class CalculatorTool {

    @Tool("两个数相加")
    public double add(@P("第一个加数") double a, @P("第二个加数") double b) {
        log.info("执行 add: {} + {}", a, b);
        return a + b;
    }

    @Tool("两个数相减")
    public double subtract(@P("被减数") double a, @P("减数") double b) {
        log.info("执行 subtract: {} - {}", a, b);
        return a - b;
    }

    @Tool("两个数相乘")
    public double multiply(@P("乘数1") double a, @P("乘数2") double b) {
        log.info("执行 multiply: {} * {}", a, b);
        return a * b;
    }

    @Tool("两个数相除")
    public double divide(@P("被除数") double a, @P("除数") double b) {
        log.info("执行 divide: {} / {}", a, b);
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为零");
        }
        return a / b;
    }

    @Tool("计算平方根")
    public double sqrt(@P("要计算平方根的数") double x) {
        log.info("执行 sqrt: {}", x);
        return Math.sqrt(x);
    }

    @Tool("计算幂次方（x的y次方）")
    public double pow(@P("底数") double base, @P("指数") double exponent) {
        log.info("执行 pow: {} ^ {}", base, exponent);
        return Math.pow(base, exponent);
    }
}
