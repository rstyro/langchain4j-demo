package top.lrshuai.langchain4j.springboot.agentic.conditional.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 法律专家 Agent — 回答法律相关问题。
 */
public interface LegalExpert {

    @Agent(value = "法律专家", description = "回答法律相关问题", outputKey = "resp")
    @UserMessage("你是一位资深律师，请专业地回答以下法律问题：\n{{request}}\n\n只给出专业建议，不构成正式法律意见。")
    String answer(@V("request") String request);
}
