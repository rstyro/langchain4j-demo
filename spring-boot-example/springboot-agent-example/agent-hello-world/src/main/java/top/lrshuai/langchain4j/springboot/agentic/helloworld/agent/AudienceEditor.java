package top.lrshuai.langchain4j.springboot.agentic.helloworld.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 受众编辑 Agent — 根据目标受众改写故事。
 * <p>
 * 流水线第二步：接收上游的 {@code story} 和用户指定的 {@code audience}，输出更新后的 {@code story}。
 */
public interface AudienceEditor {

    @Agent(value = "受众编辑", description = "根据目标受众群体改写故事内容", outputKey = "story")
    @UserMessage("你是一位内容编辑，请按照「{{audience}}」受众的阅读偏好改写以下故事：\n{{story}}\n\n只返回改写后的故事正文，不要额外内容。")
    String editByAudience(@V("story") String story, @V("audience") String audience);
}
