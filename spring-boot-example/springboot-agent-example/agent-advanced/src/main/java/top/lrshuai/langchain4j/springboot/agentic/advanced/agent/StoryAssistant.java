package top.lrshuai.langchain4j.springboot.agentic.advanced.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 故事助手 — 上层路由 Agent，带多轮会话记忆。
 * <p>
 * 根据用户输入由 LLM 自主判断意图：
 * <ul>
 *   <li>创作请求 → 调用 writeStory 工具，委托给底层序列</li>
 *   <li>对话/追问 → 直接基于 ChatMemory 中的历史上下文回答</li>
 *   <li>空输入 → 调用 writeStory（空 topic 触发序列的 ErrorHandler 填默认值）</li>
 * </ul>
 * <p>
 * {@link MemoryId} 注释的 userId 使同一用户的多次请求共享 ChatMemory。
 */
public interface StoryAssistant {

    @Agent("故事创作助手 — 写作路由 + 通用对话")
    @SystemMessage("""
            你是故事创作助手，按以下规则处理用户输入：

            1. 如果用户想创作故事（含"写"、"创作"、"来个故事"等意图），
               调用 writeStory 工具。topic 用用户指定的主题，
               如用户只说"写个故事"未给主题，topic 传空字符串，
               style 用用户指定的风格，未指定则传空字符串。

            2. 如果是普通对话、追问历史、闲聊，
               不要调用工具，直接基于对话上下文用自然语言回答。

            保持回答简洁自然，不要过度解释。
            """)
    @UserMessage("{{topic}}")
    String chat(@MemoryId String userId, @V("topic") String topic);
}
