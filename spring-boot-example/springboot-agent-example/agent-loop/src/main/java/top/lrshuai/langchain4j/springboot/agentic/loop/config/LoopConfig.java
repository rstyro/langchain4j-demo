package top.lrshuai.langchain4j.springboot.agentic.loop.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.loop.agent.StoryRefiner;
import top.lrshuai.langchain4j.springboot.agentic.loop.agent.StyleScorer;

/**
 * Loop 循环工作流配置。
 * <p>
 * 组装：风格评分 → 故事优化（循环直到分数 ≥ 0.8 或达到最大迭代次数 5）。
 */
@Configuration
public class LoopConfig {

    @Bean
    StyleScorer styleScorer(ChatModel chatModel) {
        return AgenticServices.agentBuilder(StyleScorer.class)
                .chatModel(chatModel)
                .outputKey("score")
                .build();
    }

    @Bean
    StoryRefiner storyRefiner(ChatModel chatModel) {
        return AgenticServices.agentBuilder(StoryRefiner.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
    }

    /**
     * Loop 循环工作流：先评分 → 再优化 → 评分达标退出，最多循环 5 次。
     */
    @Bean
    UntypedAgent loopWorkflow(StyleScorer scorer, StoryRefiner refiner) {
        return AgenticServices.loopBuilder()
                .subAgents(scorer, refiner)
                .maxIterations(5)
                .exitCondition(scope -> scope.readState("score", 0.0) >= 0.8)
                .testExitAtLoopEnd(true)
                .outputKey("story")
                .build();
    }
}
