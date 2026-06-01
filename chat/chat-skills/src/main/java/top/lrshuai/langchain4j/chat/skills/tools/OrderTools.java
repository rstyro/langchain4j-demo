package top.lrshuai.langchain4j.chat.skills.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 订单处理工具集
 */
@Slf4j
public class OrderTools {

    @Tool("根据订单号验证订单是否有效")
    public String validateOrder(@P("订单号") String orderId) {
        log.info("验证订单: {}", orderId);
        return String.format("订单 %s 验证通过：已支付，待发货", orderId);
    }

    @Tool("对指定订单执行扣款操作")
    public String chargePayment(@P("订单号") String orderId) {
        log.info("执行扣款: {}", orderId);
        return String.format("订单 %s 扣款成功，金额 ¥299.00", orderId);
    }

    @Tool("查询订单当前状态")
    public String checkOrderStatus(@P("订单号") String orderId) {
        log.info("查询订单状态: {}", orderId);
        return String.format("订单 %s 状态：已发货，物流单号：SF%d，预计3天内送达",
                orderId, System.currentTimeMillis() % 1000000000);
    }
}
