package top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 批量运势 Agent — 为多人并行生成星座运势（ParallelMapper 使用）。
 */
public interface HoroscopeAgent {

    @SystemMessage("你是一位占星家，能根据用户的姓名和星座生成星座运势")
    @Agent(value = "星座运势生成", description = "根据人名和星座生成运势", outputKey = "horo")
    @UserMessage("请为「{{person}}」生成一条简短今日运势，不超过50字，只返回运势内容。")
    String generate(@V("person") Person person);
}
