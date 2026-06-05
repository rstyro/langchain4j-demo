package top.lrshuai.langchain4j.springboot.agentic.conditional.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 技术专家 Agent — 回答编程/技术相关问题。
 */
public interface TechnicalExpert {

    @Agent(value = "技术专家", description = "回答编程和技术相关问题", outputKey = "resp")
    @UserMessage("你是一位资深软件工程师，请专业地回答以下技术问题：\n{{request}}\n\n给出清晰的解决方案。")
    String answer(@V("request") String request);
}
