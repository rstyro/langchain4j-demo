package top.lrshuai.langchain4j.springboot.agentic.conditional.controller;

import dev.langchain4j.agentic.UntypedAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;

import java.util.Map;

/**
 * Conditional 条件路由 REST 接口。
 * <p>
 * 用户提问 → 自动分类 → 路由对应领域专家回答。
 */
@RestController
@RequestMapping("/agent/conditional")
public class ConditionalController {

    @Resource
    private UntypedAgent expertBot;

    /**
     * 专家问答：自动分类并路由。
     *
     * @param request 用户问题
     */
    @GetMapping("/ask")
    public R<String> ask(@RequestParam(defaultValue = "我腿摔破了怎么办") String request) {
        String result = (String) expertBot.invoke(Map.of("request", request));
        return R.ok(result);
    }
}
