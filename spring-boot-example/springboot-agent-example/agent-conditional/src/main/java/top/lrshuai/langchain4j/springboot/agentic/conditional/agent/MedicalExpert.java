package top.lrshuai.langchain4j.springboot.agentic.conditional.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 医疗专家 Agent — 回答医疗相关问题。
 */
public interface MedicalExpert {

    @Agent(value = "医疗专家", description = "回答医疗健康相关问题", outputKey = "resp")
    @UserMessage("你是一位资深医生，请专业地回答以下健康问题：\n{{request}}\n\n只给出专业建议。")
    String answer(@V("request") String request);
}
