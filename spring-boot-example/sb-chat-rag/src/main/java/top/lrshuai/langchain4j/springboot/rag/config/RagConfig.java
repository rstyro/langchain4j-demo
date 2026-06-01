package top.lrshuai.langchain4j.springboot.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 配置：知识库初始化、向量存储、检索增强器。
 * <p>
 * {@code ChatModel} 和 {@code EmbeddingModel} 由 langchain4j-spring-boot4-starter
 * 根据 application.yml 自动配置，无需手动创建。
 * {@code @AiService} 注解自动创建 RagAssistant 代理 Bean。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RagConfig {

    @Bean
    EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore,
                                          EmbeddingModel embeddingModel) {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(
                        EmbeddingStoreContentRetriever.builder()
                                .embeddingStore(embeddingStore)
                                .embeddingModel(embeddingModel)
                                .maxResults(3)
                                .minScore(0.5)
                                .build()
                )
                .build();
    }
}
