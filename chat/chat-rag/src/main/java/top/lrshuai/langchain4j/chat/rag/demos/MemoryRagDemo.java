package top.lrshuai.langchain4j.chat.rag.demos;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import top.lrshuai.langchain4j.chat.rag.consts.ConfigConst;
import top.lrshuai.langchain4j.chat.rag.service.MemoryRagAssistant;

import java.util.List;

/**
 * 带记忆的RAG示例
 * 流程：文本分段 -> 向量生成 -> 存储 -> 召回 -> 生成回答的完整流程
 * 演示：将对话记忆和RAG检索结合，实现上下文关联的问答
 */
public class MemoryRagDemo {

    public static void main(String[] args) {
        // 1. 初始化模型
        String apiKey = System.getenv(ConfigConst.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("请先设置环境变量 " + ConfigConst.API_KEY_ENV);
            return;
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(ConfigConst.BASE_URL)
                .modelName(ConfigConst.MODEL_NAME)
                .temperature(0.7)
//                .logRequests(true)
                .logResponses(true)
                .build();

        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(ConfigConst.BASE_URL)
                .modelName(ConfigConst.EMBEDDING_MODEL_NAME)
//                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. 准备知识库（模拟电商知识库）
        Document doc1 = Document.document("商品信息：华为Mate60 Pro，价格6999元，处理器是麒麟9000S，支持卫星通话，内存有8G+256G、12G+512G、12G+1T三个版本可选，颜色有雅川青、白沙银、南糯紫、雅丹黑四种。");
        Document doc2 = Document.document("商品信息：苹果iPhone 15 Pro，价格7999元，处理器是A17 Pro，支持USB-C接口，内存有8G+128G、8G+256G、8G+512G、8G+1T四个版本可选，颜色有黑色钛金属、白色钛金属、蓝色钛金属、原色钛金属四种。");
        Document doc3 = Document.document("售后政策：7天无理由退换货，1年全国联保，非人为质量问题免费维修，人为损坏维修需要收取成本费，支持延保服务，延保1年需要加299元。");
        Document doc4 = Document.document("配送政策：普通快递免费配送，3-5天到货，顺丰加急需要加20元，1-2天到货，支持货到付款，支持全国配送，偏远地区需要加10元运费。");

        // 3. 文档处理 （按300字符分割，重叠50字符）
        DocumentSplitter splitter = DocumentSplitters.recursive(300, 50);
        List<TextSegment> segments = splitter.splitAll(List.of(doc1, doc2, doc3, doc4));
        // 内存向量库
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        System.out.println("文档分割完成，共 " + segments.size() + " 个片段");
        System.out.println("向量存储完成，共存储 " + embeddings.size() + " 个向量");

        // 4. 创建检索器（默认召回最相关的2个片段）
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.7)  // 相似度最低阈值，低于这个值的片段不会被召回
                .build();

        // 5. 构建检索增强器
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        // 6. 创建带记忆的RAG助手（每个记忆ID对应10条消息的记忆窗口）
        MemoryRagAssistant assistant = AiServices.builder(MemoryRagAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .build())
                .build();

        // 7. 测试多轮对话（用户ID为user_001）
        System.out.println("=== 用户001对话 ===");
        System.out.println("用户：华为Mate60 Pro多少钱？");
        String answer1 = assistant.chat("user_001", "华为Mate60 Pro多少钱？");
        System.out.println("助手：" + answer1);

        System.out.println("\n用户：有什么颜色可选？");
        String answer2 = assistant.chat("user_001", "有什么颜色可选？"); // 这里不需要重复说商品名，会根据上下文自动识别
        System.out.println("助手：" + answer2);

        System.out.println("\n用户：如果买的话多久能送到？");
        String answer3 = assistant.chat("user_001", "如果买的话多久能送到？");
        System.out.println("助手：" + answer3);

        System.out.println("\n用户：支持退换货吗？");
        String answer4 = assistant.chat("user_001", "支持退换货吗？");
        System.out.println("助手：" + answer4);

        // 8. 测试另一个用户（用户ID为user_002，记忆是独立的）
        System.out.println("\n=== 用户002对话 ===");
        System.out.println("用户：iPhone 15 Pro有多大内存的？");
        String answer5 = assistant.chat("user_002", "iPhone 15 Pro有多大内存的？");
        System.out.println("助手：" + answer5);

        System.out.println("\n用户：可以加急配送吗？加多少钱？");
        String answer6 = assistant.chat("user_002", "可以加急配送吗？加多少钱？");
        System.out.println("助手：" + answer6);
    }
}
