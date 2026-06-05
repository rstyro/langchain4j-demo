package top.lrshuai.langchain4j.springboot.agentic.loop.controller;

import dev.langchain4j.agentic.UntypedAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;

import java.util.Map;

/**
 * Loop 循环工作流 REST 接口。
 * <p>
 * 接收初始故事和风格，循环评分+优化直到达标。
 */
@RestController
@RequestMapping("/agent/loop")
public class LoopController {

    @Resource
    private UntypedAgent loopWorkflow;

    /**
     * 循环优化故事
     *
     * @param story 初始故事文本
     * @param style 目标文学风格
     * @return 优化后的故事（分数 ≥ 0.8）
     */
    @GetMapping("/optimize")
    public R<String> optimize(@RequestParam(defaultValue = "从前有座山，山里有座庙") String story,
                               @RequestParam(defaultValue = "奇幻") String style) {
        String result = (String) loopWorkflow.invoke(Map.of(
                "story", story,
                "style", style
        ));
        return R.ok(result);
    }
}
