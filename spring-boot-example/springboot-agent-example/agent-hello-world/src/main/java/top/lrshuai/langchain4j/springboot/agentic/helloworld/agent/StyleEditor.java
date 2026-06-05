package top.lrshuai.langchain4j.springboot.agentic.helloworld.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 风格编辑 Agent — 按照指定风格润色故事。
 * <p>
 * 流水线第三步（最后一步）：接收上游的 {@code story} 和用户指定的 {@code style}，输出最终结果。
 */
public interface StyleEditor {

    @Agent(value = "风格编辑", description = "按照指定文学风格对故事进行润色", outputKey = "story")
    @UserMessage("你是一位文学编辑，请以「{{style}}」风格润色以下故事：\n{{story}}\n\n只返回润色后的故事正文，不要额外内容。")
    String editByStyle(@V("story") String story, @V("style") String style);
}
