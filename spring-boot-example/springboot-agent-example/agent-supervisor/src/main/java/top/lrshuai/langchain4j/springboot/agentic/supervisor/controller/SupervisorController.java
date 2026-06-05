package top.lrshuai.langchain4j.springboot.agentic.supervisor.controller;

import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;
import top.lrshuai.langchain4j.springboot.agentic.supervisor.tool.BankTool;

import java.util.Map;

/**
 * Supervisor 自主智能体 REST 接口。
 * <p>
 * 提供银行转账和账户查询端点。
 */
@RestController
@RequestMapping("/agent/supervisor")
public class SupervisorController {

    @Resource
    private SupervisorAgent bankSupervisor;
    @Resource
    private BankTool bankTool;

    /**
     * 银行转账 — Supervisor 自主规划执行。
     * <p>
     * 示例：将100欧元从Mario转账到Georgios
     *
     * @param request 转账指令（自然语言）
     */
    @GetMapping("/transfer")
    public R<String> transfer(@RequestParam(defaultValue = "将100欧元从Mario转账到Georgios") String request) {
        String result = bankSupervisor.invoke(request);
        return R.ok(result);
    }

    /**
     * 查询所有账户余额。
     */
    @GetMapping("/accounts")
    public R<Map<String, Double>> accounts() {
        return R.ok(bankTool.getAccounts());
    }
}
