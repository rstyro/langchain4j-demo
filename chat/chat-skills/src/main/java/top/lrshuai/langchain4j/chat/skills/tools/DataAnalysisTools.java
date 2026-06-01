package top.lrshuai.langchain4j.chat.skills.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 数据分析工具集
 */
@Slf4j
public class DataAnalysisTools {

    private final Random random = new Random();

    @Tool("统计指定指标的总量数据")
    public String queryStatistics(
            @P("指标名称，如 sales/users/orders") String metric,
            @P("时间范围，如 today/week/month") String timeRange) {
        log.info("查询统计: metric={}, timeRange={}", metric, timeRange);
        int value = 1000 + random.nextInt(9000);
        return String.format("%s 在 %s 的%s数据：总量 = %d，同比增长 12.5%%", metric, timeRange, metric, value);
    }

    @Tool("生成指定类型的数据报表摘要")
    public String generateReport(
            @P("报表类型，如 sales/performance/user_growth") String reportType) {
        log.info("生成报表: {}", reportType);
        return String.format("=== %s 报表摘要 ===\n日期：%s\n核心指标：\n- 总销售额：¥128,500\n- 订单数：1,230\n- 新用户：456\n- 活跃用户：3,210\n趋势：整体向好，较上月增长 8.3%%",
                reportType, java.time.LocalDate.now());
    }
}
