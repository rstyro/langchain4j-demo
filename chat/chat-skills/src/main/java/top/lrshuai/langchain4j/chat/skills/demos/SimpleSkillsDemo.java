package top.lrshuai.langchain4j.chat.skills.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.skills.Skill;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;
import top.lrshuai.langchain4j.chat.skills.tools.CustomerSupportTools;
import top.lrshuai.langchain4j.chat.skills.tools.OrderTools;

/**
 * 简单 Skills 演示
 * <p>
 * 演示 Skills 核心流程：
 * 1. 把工具集分组为 Skill（客服技能、订单处理技能）
 * 2. LLM 根据用户问题自动激活对应 Skill
 * 3. Skill 激活后，该 Skill 内的工具才可见，避免工具泛滥
 */
@Slf4j
public class SimpleSkillsDemo {

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请先设置环境变量 {}", LlmConfig.API_KEY_ENV);
            return;
        }

        // =====================================================
        // 步骤1：定义 Skill —— 把工具集和用法说明打包在一起
        // =====================================================
        Skill customerSupportSkill = Skill.builder()
                .name("customer-support")
                .description("处理客户咨询、搜索知识库、创建和升级工单")
                .content("""
                        客服支持流程：
                        1. 先用 searchKnowledgeBase(keyword) 搜索知识库看看有没有现成答案
                        2. 如果知识库没有满意的答案，用 createTicket(customerName, issue) 创建工单
                        3. 如果问题比较严重，用 escalateTicket(ticketId, reason) 升级工单
                        """)
                .tools(new CustomerSupportTools())
                .build();

        Skill orderSkill = Skill.builder()
                .name("order-processing")
                .description("验证订单、执行付款、查询订单状态")
                .content("""
                        订单处理流程：
                        1. 先用 validateOrder(orderId) 验证订单
                        2. 验证通过后调用 chargePayment(orderId) 扣款
                        3. 随时可用 checkOrderStatus(orderId) 查订单状态
                        """)
                .tools(new OrderTools())
                .build();

        // =====================================================
        // 步骤2：创建 Skills —— 管理所有 Skill 的容器
        // =====================================================
        Skills skills = Skills.from(customerSupportSkill, orderSkill);

        // =====================================================
        // 步骤3：构建 Assistant —— 注入 Skills
        // =====================================================
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .baseUrl(LlmConfig.LLM_BASE_URL)
                        .modelName(LlmConfig.LLM_MODEL_DEEPSEEK)
                        .temperature(0.3)
                        .logRequests(true)
                        .logResponses(true)
                        .build())
                .toolProvider(skills.toolProvider())
                .systemMessage("""
                        你有一个 AI 助手，可以激活以下技能来解决用户问题：
                        %s
                        当用户的问题与某个技能相关时，先激活该技能（activate_skill），再调用技能里的工具。
                        """.formatted(skills.formatAvailableSkills()))
                .build();

        // =====================================================
        // 步骤4：测试 —— 观察 LLM 如何选择并激活 Skill
        // =====================================================
        log.info("========== Simple Skills 演示 ==========\n");

        log.info(">>> 测试1：客服问题（应激活 customer-support）");
        String answer1 = assistant.chat("我想退款，怎么操作？");
        log.info("回答：{}\n", answer1);

        log.info(">>> 测试2：订单问题（应激活 order-processing）");
        String answer2 = assistant.chat("帮我查一下订单 ORDER-12345 的状态");
        log.info("回答：{}\n", answer2);

        log.info(">>> 测试3：涉及升级（激活 customer-support → escalateTicket）");
        String answer3 = assistant.chat("客户张三反馈产品质量有问题，要退款但客服一直没回复，帮我创建工单并升级");
        log.info("回答：{}\n", answer3);

        log.info("Simple Skills 演示完成！");
    }
}
