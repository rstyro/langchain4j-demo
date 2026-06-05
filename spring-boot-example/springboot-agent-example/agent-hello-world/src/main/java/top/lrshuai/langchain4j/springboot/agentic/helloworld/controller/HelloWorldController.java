package top.lrshuai.langchain4j.springboot.agentic.helloworld.controller;

import dev.langchain4j.agentic.UntypedAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;

import java.util.Map;

/**
 * 故事创作 REST 接口。
 * <p>
 * 提供两个端点：单步创作（仅写作 Agent）和全流水线（写作 → 受众 → 风格）。
 */
@RestController
@RequestMapping("/agent/hello")
public class HelloWorldController {

    @Resource
    private UntypedAgent storyWorkflow;

    /**
     * 全流水线创作：主题 → 写作 → 受众改编 → 风格润色
     *
     * @param topic    故事主题
     * @param audience 目标受众（如：儿童、青少年、成人）
     * @param style    文学风格（如：奇幻、科幻、现实、幽默）
     * @return 最终润色后的故事
     */
    @GetMapping("/story")
    public R<String> createStory(@RequestParam(defaultValue = "妖王与凡人") String topic,
                                  @RequestParam(defaultValue = "成人") String audience,
                                  @RequestParam(defaultValue = "奇幻") String style) {
        String result = (String) storyWorkflow.invoke(Map.of(
                "topic", topic,
                "audience", audience,
                "style", style
        ));
        return R.ok(result);
    }
}
