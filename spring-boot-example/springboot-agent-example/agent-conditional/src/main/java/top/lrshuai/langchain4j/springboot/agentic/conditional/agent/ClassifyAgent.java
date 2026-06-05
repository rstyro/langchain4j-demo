package top.lrshuai.langchain4j.springboot.agentic.conditional.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 分类 Agent — 将用户问题分为医疗/法律/技术/未知。
 */
public interface ClassifyAgent {

    @Agent(value = "问题分类器", description = "将用户问题分类为 MEDICAL/LEGAL/TECH/UNKNOWN", outputKey = "category")
    @UserMessage("""
            分析以下用户请求，并将其归类为 “MEDICAL”、“LEGAL” 或 “TECH” 类别。
            如果请求不属于上述任何类别，则将其归类为 “UNKNOWN”。
            仅用上述词语中的一个进行回复，其他内容一概不答。
            用户请求为：\n{{request}}
            """)
    RequestCategory classify(@V("request") String request);
}
