package top.lrshuai.langchain4j.chat.rag.demos;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import top.lrshuai.langchain4j.chat.rag.consts.ConfigConst;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.net.URI;
import java.net.URL;

/**
 * 文件加载RAG示例
 * 演示：从本地目录加载文档 -> 解析 -> 分段 -> 存储 -> 检索 -> 回答
 * 支持 TXT、PDF、DOCX、Markdown 等多种格式
 */
public class FileLoadRagDemo {

    interface CustomerServiceAssistant {
        String answer(String question);
    }

    public static void main(String[] args) throws Exception {
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
                .temperature(0.1) // 客服场景需要更低的温度，回答更准确
                .logRequests(true)
                .logResponses(true)
                .build();

        OpenAiEmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(ConfigConst.BASE_URL)
                .modelName(ConfigConst.EMBEDDING_MODEL_NAME)
                .logRequests(true)
                .logResponses(true)
                .build();

        // 2. 加载本地文档（这里演示加载TXT文件，PDF/DOCX使用对应parser即可）
        URL docsUrl = FileLoadRagDemo.class.getClassLoader().getResource("docs");
        if (docsUrl == null) {
            System.err.println("找不到文档目录，请确认资源目录配置正确");
            return;
        }
        Path docsDir = Paths.get(docsUrl.toURI());
        List<Document> documents = FileSystemDocumentLoader.loadDocuments(docsDir, new TextDocumentParser());
        System.out.println("加载文档完成，共 " + documents.size() + " 个文档");
        documents.forEach(doc -> System.out.println("- " + doc.metadata().getString("file_name")));

        // 3. 文档分段
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.splitAll(documents);
        System.out.println("\n文档分割完成，共 " + segments.size() + " 个片段");

        // 4. 生成向量并存储
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        embeddingStore.addAll(embeddings, segments);
        System.out.println("向量存储完成，共存储 " + embeddings.size() + " 个向量");

        // 5. 创建检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.5)
                .build();

        // 调试：测试检索器是否能找到相关文档
        System.out.println("\n=== 测试检索器 ===");
        List<Content> testResults = contentRetriever.retrieve(Query.from("专业版价格"));
        System.out.println("检索到 " + testResults.size() + " 个相关片段");
        for (Content testResult : testResults) {
            System.out.println("===:"+testResult.textSegment().text());
        }

        // 6. 构建检索增强器（添加查询压缩优化，提升多轮对话检索准确性）
        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryTransformer(new CompressingQueryTransformer(chatModel)) // 压缩查询，自动结合上下文
                .contentRetriever(contentRetriever)
                .build();

        // 7. 创建客服助手
        CustomerServiceAssistant assistant = AiServices.builder(CustomerServiceAssistant.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();

        // 8. 测试客服问答
        System.out.println("\n=== 测试1：询问产品价格 ===");
        String answer1 = assistant.answer("你们的专业版多少钱一个月？包含什么功能？");
        System.out.println("回答：" + answer1);

        System.out.println("\n=== 测试2：询问企业版服务 ===");
        String answer2 = assistant.answer("企业版有什么售后服务？支持私有部署吗？");
        System.out.println("回答：" + answer2);

        System.out.println("\n=== 测试3：询问退款政策 ===");
        String answer3 = assistant.answer("买了之后不满意可以退款吗？退款政策是怎样的？");
        System.out.println("回答：" + answer3);

        System.out.println("\n=== 测试4：询问接入渠道 ===");
        String answer4 = assistant.answer("你们的产品支持接入企业微信吗？");
        System.out.println("回答：" + answer4);

        System.out.println("\n=== 测试5：超范围问题 ===");
        String answer5 = assistant.answer("你们的产品可以用来开发游戏吗？");
        System.out.println("回答：" + answer5);
    }
}
