package top.lrshuai.langchain4j.springboot.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAG 初始化知识库
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RagInitializer {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    // 产品知识库
    private static final String[] KNOWLEDGE_BASE = {
            "产品支持 Web、iOS、Android、企业微信、飞书、钉钉等多渠道接入。",
            "专业版 ¥299/月，包含 5 个坐席、知识库、基础报表；企业版 ¥999/月，无限坐席、私有部署、高级报表、API 接入。",
            "7 天内无理由退款，联系客服 400-123-4567 或在线提交退款申请。",
            "企业版支持私有部署，提供 Docker 镜像和 Kubernetes Helm Chart，也支持私有云、混合云部署。",
            "系统采用 AES-256 加密存储数据，通过 ISO 27001 认证，支持数据脱敏和审计日志。",
            "API 接口支持 RESTful 和 WebSocket 两种方式，提供 Java/Python/Go SDK。",
    };

    @PostConstruct
    public void init() {
        for (String text : KNOWLEDGE_BASE) {
            TextSegment segment = TextSegment.from(text);
            var embedding = embeddingModel.embed(segment).content();
            embeddingStore.add(embedding, segment);
        }
        log.info("知识库初始化完成");
    }
}
