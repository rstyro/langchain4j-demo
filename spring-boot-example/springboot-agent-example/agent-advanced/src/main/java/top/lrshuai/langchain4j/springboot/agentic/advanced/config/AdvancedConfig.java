package top.lrshuai.langchain4j.springboot.agentic.advanced.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.advanced.agent.OptionalStyleEditor;
import top.lrshuai.langchain4j.springboot.agentic.advanced.agent.StoryAssistant;
import top.lrshuai.langchain4j.springboot.agentic.advanced.agent.Writer;
import top.lrshuai.langchain4j.springboot.agentic.advanced.tool.StoryTool;

/**
 * Agent 高级特性配置。
 * <p>
 * 架构：
 * <pre>
 *   StoryAssistant (上层路由，@MemoryId，ChatMemory)
 *     → LLM 判断意图
 *       ├— 写作 → StoryTool.writeStory()
 *       │          → 底层序列: Writer → OptionalStyleEditor
 *       └— 对话 → 直接基于 ChatMemory 回答
 * </pre>
 * <p>
 * 演示：
 *   ChatMemory：多轮会话记忆
 *   Agent Tool：Agent 序列暴露为 Tool
 *   Optional Agent：缺 style 自动跳过
 *   ErrorHandler：缺 topic 自动填充默认值并重试
 *   AgentMonitor：全链路执行监控
 */
@Configuration
public class AdvancedConfig {

    /**
     * ChatMemoryProvider — LangChain4j Spring Boot 标准会话记忆 Bean。
     */
    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
    }

    @Bean
    AgentMonitor agentMonitor() {
        return new AgentMonitor();
    }

    // ==================== 底层 Agent 序列 ====================

    @Bean
    Writer writer(ChatModel chatModel) {
        return AgenticServices.agentBuilder(Writer.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
    }

    @Bean
    OptionalStyleEditor optionalStyleEditor(ChatModel chatModel) {
        return AgenticServices.agentBuilder(OptionalStyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .optional(true)
                .build();
    }

    /**
     * 底层序列：Writer → OptionalStyleEditor。
     */
    @Bean
    UntypedAgent storyWorkflow(Writer writer,
                                OptionalStyleEditor editor,
                                AgentMonitor monitor) {
        return AgenticServices.sequenceBuilder()
                .subAgents(writer, editor)
                .outputKey("story")
                .errorHandler(ctx -> {
                    if (ctx.exception() instanceof MissingArgumentException ex
                            && "topic".equals(ex.argumentName())) {
                        ctx.agenticScope().writeState("topic", "白雪公主与7个小矮人");
                        return ErrorRecoveryResult.retry();
                    }
                    return ErrorRecoveryResult.throwException();
                })
                .listener(monitor)
                .build();
    }

    // ==================== 上层路由 Agent ====================

    /**
     * 把底层序列包装为 Tool，供 StoryAssistant 调用。
     */
    @Bean
    StoryTool storyTool(UntypedAgent storyWorkflow) {
        return new StoryTool(storyWorkflow);
    }

    /**
     * 上层路由 Agent — 带 ChatMemory，自主决定写作 or 对话。
     */
    @Bean
    StoryAssistant storyAssistant(ChatModel chatModel,
                                   ChatMemoryProvider chatMemoryProvider,
                                   StoryTool storyTool) {
        return AgenticServices.agentBuilder(StoryAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(storyTool)
                .build();
    }
}
