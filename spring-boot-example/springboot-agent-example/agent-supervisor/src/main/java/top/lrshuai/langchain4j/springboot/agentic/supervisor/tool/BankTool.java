package top.lrshuai.langchain4j.springboot.agentic.supervisor.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 银行账户工具 — 提供存取款操作。
 * <p>
 * 使用内存 Map 模拟账户余额，绑定到取款/存款 Agent。
 */
public class BankTool {

    private final Map<String, Double> accounts = new ConcurrentHashMap<>();

    public BankTool() {
        // 初始化两个测试账户
        accounts.put("Mario", 1000.0);
        accounts.put("Georgios", 1000.0);
    }

    /**
     * 存款操作
     *
     * @param user  用户名
     * @param amount 存款金额（USD）
     * @return 操作后余额
     */
    @Tool("给用户账户存入 USD 金额")
    public Double credit(@P("用户名") String user, @P("存款金额(USD)") Double amount) {
        double newBalance = accounts.getOrDefault(user, 0.0) + amount;
        accounts.put(user, newBalance);
        System.out.printf("[BankTool] 存款: %s +%.2f USD, 余额: %.2f%n", user, amount, newBalance);
        return newBalance;
    }

    /**
     * 取款操作
     *
     * @param user  用户名
     * @param amount 取款金额（USD）
     * @return 操作后余额
     */
    @Tool("从用户账户扣除 USD 金额")
    public Double withdraw(@P("用户名") String user, @P("取款金额(USD)") Double amount) {
        double current = accounts.getOrDefault(user, 0.0);
        if (current < amount) {
            throw new RuntimeException("账户余额不足: " + user + " 当前余额 " + current + " USD");
        }
        double newBalance = current - amount;
        accounts.put(user, newBalance);
        System.out.printf("[BankTool] 取款: %s -%.2f USD, 余额: %.2f%n", user, amount, newBalance);
        return newBalance;
    }

    /**
     * 查询余额
     */
    @Tool("查询用户账户 USD 余额")
    public Double getBalance(@P("用户名") String user) {
        return accounts.getOrDefault(user, 0.0);
    }

    /**
     * 获取所有账户余额快照
     */
    public Map<String, Double> getAccounts() {
        return Map.copyOf(accounts);
    }
}
