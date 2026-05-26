package top.lrshuai.langchain4j.chat.tool.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 订单查询工具
 * <p>
 * 模拟订单系统 API，演示 LLM 如何根据用户意图调用不同工具获取数据。
 * 实际项目中可替换为真实的订单服务调用。
 */
@Slf4j
public class OrderTool {

    // 模拟订单数据库
    private static final Map<String, String> ORDER_DB = Map.of(
            "ORD001", "订单号: ORD001, 商品: Java编程思想, 金额: 89.00元, 状态: 已发货, 预计明天送达",
            "ORD002", "订单号: ORD002, 商品: 机械键盘, 金额: 299.00元, 状态: 配送中, 预计今天送达",
            "ORD003", "订单号: ORD003, 商品: LangChain4j实战, 金额: 69.00元, 状态: 待发货, 预计3天内发货"
    );

    @Tool("根据订单号查询订单详情，包括商品名称、金额、物流状态等") // @Tool LLM可调用工具标记
    public String queryOrder(
            @P("订单号，如：ORD001") String orderId // @P 参数说明
    ) {
        log.info("工具调用: queryOrder(orderId={})", orderId);
        return ORDER_DB.getOrDefault(orderId, "未找到订单号为 " + orderId + " 的订单，请确认订单号是否正确");
    }

    @Tool("根据订单号查询物流信息")
    public String queryLogistics(
            @P("订单号") String orderId
    ) {
        log.info("工具调用: queryLogistics(orderId={})", orderId);
        return switch (orderId) {
            case "ORD001" -> "物流单号: SF1234567890, 当前位置: 北京转运中心, 预计明天送达";
            case "ORD002" -> "物流单号: YT9876543210, 当前位置: 上海派送站, 快递员正在派送";
            case "ORD003" -> "该订单尚未发货，暂无物流信息";
            default -> "未找到订单号为 " + orderId + " 的物流信息";
        };
    }

    @Tool("取消订单")
    public String cancelOrder(
            @P("需要取消的订单号") String orderId,
            @P("取消原因") String reason
    ) {
        log.info("工具调用: cancelOrder(orderId={}, reason={})", orderId, reason);
        if ("ORD003".equals(orderId)) {
            return "订单 " + orderId + " 已成功取消，取消原因：" + reason + "，退款将在1-3个工作日内原路返回";
        }
        return "订单 " + orderId + " 已发货，无法取消，请联系客服处理";
    }
}
