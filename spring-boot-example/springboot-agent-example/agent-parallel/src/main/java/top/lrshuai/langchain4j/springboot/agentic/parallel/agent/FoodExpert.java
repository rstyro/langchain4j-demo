package top.lrshuai.langchain4j.springboot.agentic.parallel.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * 美食推荐 Agent — 根据情绪推荐餐食。
 * <p>
 * 并行工作流中与 MovieExpert 同时执行。
 */
public interface FoodExpert {

    @Agent(value = "美食专家", description = "根据用户情绪推荐3个餐食", outputKey = "meals")
    @UserMessage("根据「{{mood}}」的情绪，推荐3个餐食名称，其他不要返回")
    List<String> recommendFood(@V("mood") String mood);
}
