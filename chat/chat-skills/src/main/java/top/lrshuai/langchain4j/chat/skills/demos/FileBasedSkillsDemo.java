package top.lrshuai.langchain4j.chat.skills.demos;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.skills.ClassPathSkillLoader;
import dev.langchain4j.skills.Skills;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.chat.skills.consts.ConfigConst;

/**
 * 文件驱动 Skills 演示 — 纯 Skills 能力，不依赖外部工具
 * 与 SimpleSkillsDemo 的区别：
 * - 无需注册任何 Java 工具类（无 {@code .tools()} 调用）
 * - LLM 读到 SKILL.md 后，按照其中的「工作流程」和「输出格式」直接响应
 * - 纯 LLM 原生能力驱动，适用于写作、审查、翻译等文本处理场景
 */
@Slf4j
public class FileBasedSkillsDemo {

    interface Assistant {
        String chat(String userMessage);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv(ConfigConst.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请先设置环境变量 {}", ConfigConst.API_KEY_ENV);
            return;
        }

        // 步骤1：一行代码从 classpath 加载所有 Skills
        Skills skills = Skills.from(ClassPathSkillLoader.loadSkills("skills"));
        log.info("已加载 Skills：\n{}\n", skills.formatAvailableSkills());

        // =====================================================
        // 步骤2：构建 Assistant（纯 Skills，无外部工具）
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
                .toolProvider(skills.toolProvider())
                .systemMessage("""
                        你有一个 AI 助手，可以激活以下技能来解决用户问题：
                        %s
                        当用户的问题与某个技能相关时，先激活该技能（activate_skill），
                        然后严格按照该技能的「工作流程」和「输出格式」来响应用户。
                        """.formatted(skills.formatAvailableSkills()))
                .build();

        // --- 写作助手 ---
        log.info(">>> 测试1：写作助手 — 润色、校对");
        String answer1 = assistant.chat(
                "帮我润色这段话：今天开会讨论了下季度的计划，大家觉得应该重点搞海外市场，然后产品那边说要先把bug修一修，不然客户都在投诉。");
        log.info("回答：\n{}\n", answer1);

        // --- 代码审查 ---
        log.info(">>> 测试2：代码审查 — 安全+性能");
        String answer2 = assistant.chat("""
                帮我 review 这段代码：
                ```java
                public User getUser(String userId) {
                    String sql = "SELECT * FROM users WHERE id = " + userId;
                    return jdbcTemplate.queryForObject(sql, new UserRowMapper());
                }
                ```
                """);
        log.info("回答：\n{}\n", answer2);

        // --- 会议纪要 ---
        log.info(">>> 测试3：会议纪要 — 对话→结构化纪要");
        String answer3 = assistant.chat("""
                帮我整理一下今天的周会内容：

                张三：上周的客户反馈看完了，用户对搜索速度不太满意，平均响应要3秒多。
                李四：我这边已经定位到问题了，是索引没建好，加上缓存就好了，预计周三能上线。
                王五：产品这边下周要出一个新版的PRD，主要加多语言支持和暗黑模式。
                张三：多语言优先级高，新加坡那边催了好几次了。王五你周五前把PRD给我看下。
                李四：那我修复搜索的同时也评估一下多语言对索引的影响。
                """);
        log.info("回答：\n{}\n", answer3);

        // --- 跨技能 ---
        log.info(">>> 测试4：写作助手 + 会议纪要联动");
        String answer4 = assistant.chat(
                "这是下午技术评审的录音转文字，帮我整理成会议纪要，然后把关键结论润色成一段正式的对外公告：" +
                "「张三说微服务拆分方案过了，下个月开始搞。李四觉得网关层还需要加个限流，不然大促扛不住。" +
                "大家讨论了下觉得限流方案就用令牌桶，简单好用。王五负责出详细方案，两周内搞定。」");
        log.info("回答：\n{}\n", answer4);

        log.info("纯 Skills 演示完成！无工具、零注册、全靠 SKILL.md 驱动。");
    }
}
