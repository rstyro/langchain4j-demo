package top.lrshuai.langchain4j.springboot.agentic.advanced.controller;

import dev.langchain4j.agentic.observability.AgentMonitor;
import dev.langchain4j.agentic.observability.MonitoredExecution;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;
import top.lrshuai.langchain4j.springboot.agentic.advanced.agent.StoryAssistant;

import java.util.List;

/**
 * Agent 高级特性 REST 接口。
 * <p>
 * 演示 ChatMemory 多轮会话、Agent Tool、Optional Agent、ErrorHandler、AgentMonitor。
 */
@RestController
@RequestMapping("/agent/advanced")
public class AdvancedController {

    @Resource
    private StoryAssistant storyAssistant;
    @Resource
    private AgentMonitor agentMonitor;

    /**
     * 故事创作 / 对话。
     * <p>
     * 同一 userId 共享会话记忆：
     * <ul>
     *   <li>说「写一个关于龙的故事」→ 调用 Writer 创作</li>
     *   <li>追问「我上一个写了什么」→ 从 ChatMemory 回忆</li>
     *   <li>不同 userId 完全隔离</li>
     * </ul>
     *
     * @param userId 用户标识（不传默认 "default"）
     * @param topic  用户输入（创作主题 / 追问 / 闲聊）
     * @param style  可选风格（不传空字符串）
     */
    @GetMapping("/story")
    public R<String> story(@RequestParam(required = false) String userId,
                            @RequestParam(required = false) String topic,
                            @RequestParam(required = false) String style) {
        String uid = userId != null && !userId.isBlank() ? userId : "default";
        String stl = style != null && !style.isBlank() ? style : "";
        // 空 topic 默认 "写个故事"，让 LLM 触发 writeStory 工具（空主题 → ErrorHandler → 默认主题）
        String tpc = topic != null && !topic.isBlank() ? topic : "写个故事";
        // 把风格嵌入用户消息，让 LLM 在工具调用时能传递给 StoryTool
        String message = stl.isEmpty() ? tpc : "以" + stl + "风格：" + tpc;
        String result = storyAssistant.chat(uid, message);
        return R.ok(result);
    }

    /**
     * 查看 AgentMonitor 执行监控记录。
     */
    @GetMapping("/monitor")
    public R<List<String>> monitor() {
        List<MonitoredExecution> executions = agentMonitor.successfulExecutions();
        List<String> summary = executions.stream()
                .map(e -> String.format("[%s] %s",
                        e.memoryId(),
                        e.topLevelInvocations().done() ? "已完成" : "执行中"))
                .toList();
        return R.ok(summary);
    }
}
