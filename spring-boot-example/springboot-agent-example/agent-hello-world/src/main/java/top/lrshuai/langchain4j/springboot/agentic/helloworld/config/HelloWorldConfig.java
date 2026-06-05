package top.lrshuai.langchain4j.springboot.agentic.helloworld.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.helloworld.agent.AudienceEditor;
import top.lrshuai.langchain4j.springboot.agentic.helloworld.agent.CreativeWriter;
import top.lrshuai.langchain4j.springboot.agentic.helloworld.agent.StyleEditor;

/**
 * Agentic Hello World 工作流配置。
 * <p>
 * 定义三个子 Agent（写作 → 受众编辑 → 风格编辑），
 * 并组装为 Sequential 顺序流水线。
 */
@Configuration
public class HelloWorldConfig {

    /**
     * 创意写作 Agent
     */
    @Bean
    CreativeWriter creativeWriter(ChatModel chatModel) {
        return AgenticServices.agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
    }

    /**
     * 受众编辑 Agent
     */
    @Bean
    AudienceEditor audienceEditor(ChatModel chatModel) {
        return AgenticServices.agentBuilder(AudienceEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
    }

    /**
     * 风格编辑 Agent
     */
    @Bean
    StyleEditor styleEditor(ChatModel chatModel) {
        return AgenticServices.agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
    }

    /**
     * Sequential 顺序流水线：写作 → 受众编辑 → 风格编辑。
     * <p>
     * UntypedAgent 是 langchain4j Agentic 框架中的核心类型，代表一个"无类型"的 Agent 容器。
     * 与 TypedAgent（有类型的 Agent，如 CreativeWriter、AudienceEditor）不同，
     * UntypedAgent 不绑定特定的接口定义，而是作为多个 Agent 的编排容器使用。
     * <p>
     * 核心特点：
     * 1. **无类型约束**：不实现特定业务接口，可灵活组合多个子 Agent
     * 2. **工作流编排**：通过 `AgenticServices.sequenceBuilder()` 构建顺序执行流
     * 3. **Scope 自动传递**：前一个 Agent 的输出通过 Scope（输出键 "story"）自动传递给下一个 Agent
     * 4. **统一入口**：提供统一的 `execute()` 方法执行整个工作流
     * <p>
     * 执行流程：
     * 1. CreativeWriter 生成初稿故事，写入 Scope 的 "story" 键
     * 2. AudienceEditor 读取 "story"，针对受众进行编辑优化
     * 3. StyleEditor 读取编辑后的 "story"，进行风格润色
     * <p>
     * 使用场景：当需要编排多个 Agent 形成协作流水线时使用 UntypedAgent，
     * 每个子 Agent 专注于单一职责，通过 Scope 共享上下文数据。
     */
    @Bean
    UntypedAgent storyWorkflow(CreativeWriter writer,
                               AudienceEditor audienceEditor,
                               StyleEditor styleEditor) {
        return AgenticServices.sequenceBuilder()
                .subAgents(writer, audienceEditor, styleEditor)
                .outputKey("story")
                .build();
    }
}
