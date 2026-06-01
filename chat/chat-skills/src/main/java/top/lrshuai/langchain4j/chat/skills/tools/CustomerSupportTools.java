package top.lrshuai.langchain4j.chat.skills.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 客服支持工具集
 */
@Slf4j
public class CustomerSupportTools {

    @Tool("在知识库中搜索指定关键词")
    public String searchKnowledgeBase(@P("搜索关键词") String keyword) {
        log.info("搜索知识库: {}", keyword);
        return String.format("关于 '%s' 的知识库结果：\n1. 退款政策：7天内无理由退款\n2. 发货时间：下单后48小时内发货\n3. 售后热线：400-123-4567", keyword);
    }

    @Tool("创建一个客户工单")
    public String createTicket(
            @P("客户姓名") String customerName,
            @P("问题描述") String issue) {
        log.info("创建工单: customer={}, issue={}", customerName, issue);
        return String.format("工单已创建，编号：TK-%d，客户：%s，问题：%s，状态：处理中",
                System.currentTimeMillis() % 100000, customerName, issue);
    }

    @Tool("将工单升级到高级支持团队")
    public String escalateTicket(
            @P("工单编号") String ticketId,
            @P("升级原因") String reason) {
        log.info("升级工单: ticketId={}, reason={}", ticketId, reason);
        return String.format("工单 %s 已升级，原因：%s，已分配高级工程师跟进", ticketId, reason);
    }
}
