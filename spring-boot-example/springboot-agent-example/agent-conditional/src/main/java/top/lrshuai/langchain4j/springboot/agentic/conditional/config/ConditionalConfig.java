package top.lrshuai.langchain4j.springboot.agentic.conditional.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.conditional.agent.*;

/**
 * Conditional 条件路由工作流配置。
 * <p>
 * 组装：分类 Agent → 条件路由 → 对应领域专家 Agent。
 */
@Configuration
public class ConditionalConfig {

    @Bean
    ClassifyAgent classifyAgent(ChatModel chatModel) {
        return AgenticServices.agentBuilder(ClassifyAgent.class)
                .chatModel(chatModel)
                .outputKey("category")
                .build();
    }

    @Bean
    MedicalExpert medicalExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(MedicalExpert.class)
                .chatModel(chatModel)
                .outputKey("resp")
                .build();
    }

    @Bean
    LegalExpert legalExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(LegalExpert.class)
                .chatModel(chatModel)
                .outputKey("resp")
                .build();
    }

    @Bean
    TechnicalExpert technicalExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(TechnicalExpert.class)
                .chatModel(chatModel)
                .outputKey("resp")
                .build();
    }

    @Bean
    DefaultExpert defaultExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(DefaultExpert.class)
                .chatModel(chatModel)
                .outputKey("resp")
                .build();
    }

    /**
     * 条件路由：根据分类结果路由到对应专家。
     */
    @Bean
    UntypedAgent conditionRouter(MedicalExpert med, LegalExpert legal, TechnicalExpert tech,
                                   DefaultExpert def) {
        return AgenticServices.conditionalBuilder()
                .subAgents(scope -> {
                    RequestCategory cat = scope.readState("category", RequestCategory.UNKNOWN);
                    return cat == RequestCategory.MEDICAL;
                }, med)
                .subAgents(scope -> {
                    RequestCategory cat = scope.readState("category", RequestCategory.UNKNOWN);
                    return cat == RequestCategory.LEGAL;
                }, legal)
                .subAgents(scope -> {
                    RequestCategory cat = scope.readState("category", RequestCategory.UNKNOWN);
                    return cat == RequestCategory.TECH;
                }, tech)
                .subAgents(scope -> {
                    RequestCategory cat = scope.readState("category", RequestCategory.UNKNOWN);
                    return cat == RequestCategory.UNKNOWN;
                }, def)
                .build();
    }

    /**
     * 完整专家路由工作流：Classify → conditionRouter。
     */
    @Bean
    UntypedAgent expertBot(ClassifyAgent classifier, UntypedAgent conditionRouter) {
        return AgenticServices.sequenceBuilder()
                .subAgents(classifier, conditionRouter)
                .outputKey("resp")
                .build();
    }
}
