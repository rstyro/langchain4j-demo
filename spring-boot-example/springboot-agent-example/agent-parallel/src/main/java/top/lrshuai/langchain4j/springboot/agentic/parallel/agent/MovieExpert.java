package top.lrshuai.langchain4j.springboot.agentic.parallel.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * 电影推荐 Agent — 根据情绪推荐电影。
 * <p>
 * 并行工作流中与 FoodExpert 同时执行。
 */
public interface MovieExpert {

    @Agent(value = "电影专家", description = "根据用户情绪推荐3部电影", outputKey = "movies")
    @UserMessage("根据「{{mood}}」的情绪，推荐3部电影名称，其他不要返回")
    List<String> recommendMovie(@V("mood") String mood);
}
