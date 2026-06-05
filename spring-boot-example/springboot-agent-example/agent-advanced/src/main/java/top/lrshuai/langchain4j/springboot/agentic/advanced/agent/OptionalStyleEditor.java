package top.lrshuai.langchain4j.springboot.agentic.advanced.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 风格编辑 Agent — 可选 Agent，scope 中缺 {@code style} 时自动跳过。
 */
public interface OptionalStyleEditor {

    @Agent(value = "风格编辑(可选)", description = "按指定风格润色故事，缺style参数时跳过", outputKey = "story", optional = true)
    @UserMessage("以「{{style}}」风格润色以下故事，只返回润色后正文：\n{{story}}")
    String edit(@V("story") String story, @V("style") String style);
}
