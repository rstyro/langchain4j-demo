package top.lrshuai.langchain4j.common.config;

/**
 * LangChain4j LLM 统一配置常量
 * <p>
 * 所有 chat/mcp/spring-boot-example 模块共享的 LLM 连接配置，
 * 避免各模块重复定义。
 */
public final class LlmConfig {

    private LlmConfig() {
        // 工具类，禁止实例化
    }

    // =====================================================
    // API Key
    // =====================================================

    /** API Key 环境变量名 */
    public static final String API_KEY_ENV = "AI_API_KEY";

    // =====================================================
    // LLM 服务地址
    // =====================================================

    /** LLM 服务基础地址（火山引擎豆包 / DeepSeek API 的 OpenAI 兼容端点） */
    public static final String LLM_BASE_URL = "https://ark.cn-beijing.volces.com/api/coding/v3";

    // =====================================================
    // 模型名称
    // =====================================================

    /** 默认对话模型 */
    public static final String LLM_MODEL_DEEPSEEK = "deepseek-v3.2";

    /** 豆包模型（适合工具调用等场景） */
    public static final String LLM_MODEL_DOUBAO = "doubao-seed-2.0-pro";

    /** 豆包代码模型（适合代码生成场景） */
    public static final String LLM_MODEL_DOUBAO_CODE = "doubao-seed-2.0-code";

    /** 嵌入模型（向量生成） */
    public static final String EMBEDDING_MODEL_NAME = "doubao-embedding-vision";

}
