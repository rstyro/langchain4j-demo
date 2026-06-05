package top.lrshuai.langchain4j.springboot.agentic.helloworld.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 创意写作 Agent — 根据主题生成短篇故事。
 * <p>
 * 流水线第一步：接收 {@code topic} 参数，输出存入 Scope 的 {@code story} 变量。
 */
public interface CreativeWriter {

    /**
     * 根据指定主题生成短篇故事。
     * 
     * LangChain4j Agentic 注解说明：
     * 
     * @Agent - 定义智能代理的元信息：
     *   value      - 代理名称，用于标识和日志记录
     *   description - 代理功能描述，帮助 LLM 理解其角色
     *   outputKey  - 输出键名，指定结果存入工作流上下文的变量名
     * 
     * @UserMessage - 用户消息模板，定义发送给 LLM 的提示词：
     *   使用 {{variableName}} 语法引用方法参数
     *   模板会在运行时动态填充参数值
     * 
     * @V("topic") - 变量绑定注解：
     *   将方法参数映射到提示词模板中的变量
     *   括号内的字符串对应模板中的变量名
     * 
     * @param topic 故事主题，将被注入到提示词模板的 {{topic}} 位置
     * @return 生成的短篇故事内容
     */
    @Agent(value = "创意作家", description = "根据指定主题生成不超过3句话的短篇故事", outputKey = "story")
    @UserMessage("你是创意作家，围绕「{{topic}}」写一段不超过3句话的短篇故事。只返回故事正文，不要额外内容。")
    String generateStory(@V("topic") String topic);
}
