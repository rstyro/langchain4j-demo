package top.lrshuai.langchain4j.springboot.agentic.advanced.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 写作 Agent — 纯粹的「主题→故事」生成器。
 * <p>
 * 不关心会话、不负责路由，只做一件事：根据 topic 写故事。
 */
public interface Writer {

    @Agent(value = "作家", description = "根据主题生成短篇故事", outputKey = "story")
    @UserMessage("围绕「{{topic}}」写一段不超过3句话的短篇故事，只返回故事正文。")
    String write(@V("topic") String topic);
}
