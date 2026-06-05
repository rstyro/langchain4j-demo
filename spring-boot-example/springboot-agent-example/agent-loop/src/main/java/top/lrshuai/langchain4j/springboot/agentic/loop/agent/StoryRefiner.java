package top.lrshuai.langchain4j.springboot.agentic.loop.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 故事优化 Agent — 根据评分反馈对故事进行改进。
 * <p>
 * Loop 循环体中执行：接收当前故事和风格，输出优化后的故事。
 */
public interface StoryRefiner {

    @Agent(value = "故事优化师", description = "根据风格要求优化故事内容", outputKey = "story")
    @UserMessage("请以「{{style}}」风格优化以下故事，使其更加精彩：\n{{story}}\n\n只返回优化后的故事正文。")
    String refine(@V("story") String story, @V("style") String style);
}
