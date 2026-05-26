package top.lrshuai.langchain4j.chat.tool.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 计算器工具
 * <p>
 * 提供 LLM 不擅长的精确数学计算能力。
 * LLM 本质上是文本生成模型，对于复杂计算容易出错，
 * 通过工具调用可以确保计算结果的准确性。
 */
@Slf4j
public class CalculatorTool {


    @Tool("计算两个数的加法") // @Tool 标记方法为LLM可调用工具
    public double add(
            @P("第一个数") double a, // @P 说明参数含义
            @P("第二个数") double b
    ) {
        log.info("工具调用: add({}, {})", a, b);
        return a + b;
    }

    @Tool("计算两个数的减法")
    public double subtract(
            @P("被减数") double a,
            @P("减数") double b
    ) {
        log.info("工具调用: subtract({}, {})", a, b);
        return a - b;
    }

    @Tool("计算两个数的乘法")
    public double multiply(
            @P("第一个数") double a,
            @P("第二个数") double b
    ) {
        log.info("工具调用: multiply({}, {})", a, b);
        return a * b;
    }

    @Tool("计算两个数的除法")
    public double divide(
            @P("被除数") double a,
            @P("除数，不能为0") double b
    ) {
        log.info("工具调用: divide({}, {})", a, b);
        if (b == 0) {
            return Double.NaN;
        }
        return a / b;
    }

    @Tool("获取当前日期和时间")
    public String getCurrentDateTime() {
        log.info("工具调用: getCurrentDateTime()");
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
