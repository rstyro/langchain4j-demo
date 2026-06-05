package top.lrshuai.langchain4j.springboot.agentic.loop.agent;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 故事评分 Agent — 对故事的风格匹配度打分（0~1）。
 * <p>
 * 输出存入 Scope 的 {@code score} 变量，供 Loop 退出条件判断使用。
 */
public interface StyleScorer {

    @Agent(value = "风格评分师", description = "对故事的风格匹配度进行 0~1 打分", outputKey = "score")
    @UserMessage("请对以下「{{style}}」风格的故事进行评分（0~1，只输出数字）：\n{{story}}")
    Double score(@V("story") String story, @V("style") String style);
}
