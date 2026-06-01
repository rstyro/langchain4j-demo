package top.lrshuai.langchain4j.mcp.client.consts;

/**
 * MCP Client 通用配置常量
 */
public final class ConfigConst {

    private ConfigConst() {}

    /**
     * API Key 环境变量名
     */
    public static final String API_KEY_ENV = "AI_API_KEY";

    /**
     * LLM 服务基础地址（火山引擎 豆包/DeepSeek）
     */
    public static final String LLM_BASE_URL = "https://ark.cn-beijing.volces.com/api/coding/v3";

    /**
     * 模型名称
     */
    public static final String LLM_MODEL_NAME = "deepseek-v3.2";

    /**
     * 默认 MCP Server HTTP 地址
     */
    public static final String MCP_SERVER_URL = "http://localhost:8080";

}
