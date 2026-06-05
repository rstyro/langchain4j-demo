package top.lrshuai.langchain4j.springboot.agentic.conditional.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 通用助手 Agent — 兜底回答无法分类的请求。
 */
public interface DefaultExpert {

    @Agent(value = "通用助手", description = "回答无法分类到特定领域的问题", outputKey = "resp")
    @UserMessage("你是一位通用助手，请友好地回答以下问题：\n{{request}}\n\n如果问题超出你的能力范围，请礼貌地告知用户并给出建议。")
    String answer(@V("request") String request);
}
