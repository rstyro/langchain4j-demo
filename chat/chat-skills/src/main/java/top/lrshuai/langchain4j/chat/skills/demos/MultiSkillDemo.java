package top.lrshuai.langchain4j.chat.skills.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.search.simple.SimpleToolSearchStrategy;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.chat.skills.consts.ConfigConst;
import top.lrshuai.langchain4j.chat.skills.tools.CustomerSupportTools;
import top.lrshuai.langchain4j.chat.skills.tools.DataAnalysisTools;
import top.lrshuai.langchain4j.chat.skills.tools.OrderTools;

/**
 * 多技能 + 工具搜索 演示
 * <p>
 * 当技能和工具数量增多时，用 ToolSearchStrategy 减少 token 消耗：
 * - 注册全局可见的"搜索工具"（tool_search_tool），LLM 先搜索再调用
 * - Skill 内部的工具只在 Skill 激活后才可见
 * - 避免了把所有工具一次性发给 LLM
 */
@Slf4j
public class MultiSkillDemo {

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(ConfigConst.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请先设置环境变量 {}", ConfigConst.API_KEY_ENV);
            return;
        }

        // =====================================================
        // 步骤1：定义三个 Skill
        // =====================================================
        Skill customerSupportSkill = Skill.builder()
                .name("customer-support")
                .description("处理客户咨询：搜索知识库、创建工单、升级工单")
                .content("""
                        1. searchKnowledgeBase(keyword) 搜索知识库
                        2. createTicket(customerName, issue) 创建工单
                        3. escalateTicket(ticketId, reason) 升级工单
                        """)
                .tools(new CustomerSupportTools())
                .build();

        Skill orderSkill = Skill.builder()
                .name("order-processing")
                .description("处理订单：验证订单、扣款、查询订单状态")
                .content("""
                        1. validateOrder(orderId) 验证订单
                        2. chargePayment(orderId) 扣款
                        3. checkOrderStatus(orderId) 查状态
                        """)
                .tools(new OrderTools())
                .build();

        Skill dataSkill = Skill.builder()
                .name("data-analysis")
                .description("数据分析：查询统计数据、生成报表")
                .content("""
                        1. queryStatistics(metric, timeRange) 查询统计
                        2. generateReport(reportType) 生成报表
                        """)
                .tools(new DataAnalysisTools())
                .build();

        Skills skills = Skills.from(customerSupportSkill, orderSkill, dataSkill);

        // =====================================================
        // 步骤2：配置工具搜索策略 —— 减少 token 消耗
        // =====================================================
        SimpleToolSearchStrategy searchStrategy = SimpleToolSearchStrategy.builder()
                .maxResults(5)
                .build();

        // =====================================================
        // 步骤3：构建 Assistant
        // =====================================================
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(ConfigConst.LLM_BASE_URL)
                        .modelName(ConfigConst.LLM_MODEL_NAME)
                        .temperature(0.3)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .toolProvider(skills.toolProvider())       // Skills 工具在激活后才可见
                .toolSearchStrategy(searchStrategy)         // 用搜索策略减少 token
                .systemMessage("""
                        你可以激活以下技能来处理用户问题：
                        %s
                        当问题涉及数据分析时激活 data-analysis，
                        涉及订单时激活 order-processing，
                        涉及客服咨询时激活 customer-support。
                        """.formatted(skills.formatAvailableSkills()))
                .build();

        // =====================================================
        // 步骤4：跨技能测试
        // =====================================================
        log.info("========== Multi-Skill 演示 ==========\n");

        log.info(">>> 测试1：数据分析");
        String answer1 = assistant.chat("帮我出一个本周的 sales 报表");
        log.info("回答：{}\n", answer1);

        log.info(">>> 测试2：订单处理");
        String answer2 = assistant.chat("帮我查一下订单 ORDER-888 到哪了");
        log.info("回答：{}\n", answer2);

        log.info(">>> 测试3：客服 + 升级");
        String answer3 = assistant.chat("客户王五说没收到货，帮我查下订单 ORDER-666 的状态，如果超时了创建一个投诉工单并升级");
        log.info("回答：{}\n", answer3);

        log.info(">>> 测试4：数据分析 + 客服联动");
        String answer4 = assistant.chat("查一下本周用户增长情况，如果增长率低于10%就帮我创建一个工单跟进");
        log.info("回答：{}\n", answer4);

        log.info("Multi-Skill 演示完成！");
    }
}
