package top.lrshuai.langchain4j.springboot.agentic.advanced.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.UntypedAgent;
import top.lrshuai.langchain4j.springboot.agentic.advanced.agent.StoryAssistant;

import java.util.HashMap;
import java.util.Map;

/**
 * 故事创作工具 — 桥接 StoryAssistant 与 Writer → Editor 序列。
 * <p>
 * {@link StoryAssistant} 通过此 {@link Tool} 调用底层 Agent 序列
 * (Writer → OptionalStyleEditor)，而不是直接访问 Writer。
 */
public class StoryTool {

    private final UntypedAgent storyWorkflow;

    public StoryTool(UntypedAgent storyWorkflow) {
        this.storyWorkflow = storyWorkflow;
    }

    /**
     * 创作故事，可选风格润色。
     *
     * @param topic 故事主题
     * @param style 风格（如 搞笑、科幻、悬疑），可不传
     * @return 创作结果
     */
    @Tool("创作故事。当用户想写故事时调用，支持指定风格润色。")
    public String writeStory(@P("故事主题") String topic,@P("风格，如搞笑、科幻、悬疑；不指定则留空") String style) {
        Map<String, Object> params = new HashMap<>();
        // 关键：空 topic 不入 params → Writer 的 @V("topic") 找不到 → MissingArgumentException
        // → sequence 的 ErrorHandler 填默认值 "白雪公主与7个小矮人" → retry
        if (topic != null && !topic.isBlank()) {
            params.put("topic", topic);
        }
        if (style != null && !style.isBlank()) {
            params.put("style", style);
        }
        return (String) storyWorkflow.invoke(params);
    }
}
