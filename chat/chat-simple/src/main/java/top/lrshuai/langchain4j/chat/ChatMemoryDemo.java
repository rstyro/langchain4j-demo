package top.lrshuai.langchain4j.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LangChain4j 对话记忆（Chat Memory）完整示例
 *
 * @author rstyro
 */
@Slf4j
public class ChatMemoryDemo {

    public static void main(String[] args) {
        // 第一步：获取并校验 API Key
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("环境变量 " + LlmConfig.API_KEY_ENV + " 未配置，请在运行前设置：");
            log.error("  Windows PowerShell: $env:" + LlmConfig.API_KEY_ENV + "=\"your-api-key\"");
            log.error("  Linux/Mac: export " + LlmConfig.API_KEY_ENV + "=\"your-api-key\"");
            System.exit(1);
        }

        // 第二步：构建 LLM 聊天模型
        log.info("正在初始化 LLM 模型...");
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(LlmConfig.LLM_BASE_URL)
                .modelName(LlmConfig.LLM_MODEL_DOUBAO_CODE)
                .logRequests(true)
                .logResponses(true)
                .build();

        log.info("\n========== 演示 1：基础对话记忆 ==========");
        demoBasicChatMemory(chatModel);

        log.info("\n========== 演示 2：消息窗口记忆 ==========");
        demoMessageWindowMemory(chatModel);

        log.info("\n========== 演示 3：多用户会话隔离 ==========");
        demoMultiUserSessions(chatModel);

        log.info("\n========== 演示 4：对话历史管理 ==========");
        demoMemoryOperations(chatModel);

        log.info("所有演示完成！");
    }

    /**
     * 消息窗口记忆类（模拟 LangChain4j 的 MessageWindowChatMemory）
     */
    static class MessageWindowMemory {
        private final LinkedList<ChatMessage> messages = new LinkedList<>();
        private final int maxMessages;
        private SystemMessage systemMessage;

        public MessageWindowMemory(int maxMessages) {
            this.maxMessages = maxMessages;
        }

        /**
         * 添加消息到记忆中
         */
        public void add(ChatMessage message) {
            if (message instanceof SystemMessage) {
                this.systemMessage = (SystemMessage) message;
                // SystemMessage 放在最前面
                if (!messages.isEmpty() && messages.getFirst() instanceof SystemMessage) {
                    messages.set(0, message);
                } else {
                    messages.addFirst(message);
                }
            } else {
                messages.add(message);
                // 如果超过最大数量，移除最早的非 SystemMessage 消息
                while (messages.size() > maxMessages) {
                    // 找到第一个非 SystemMessage 的消息
                    for (int i = 0; i < messages.size(); i++) {
                        if (!(messages.get(i) instanceof SystemMessage)) {
                            messages.remove(i);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * 获取所有消息
         */
        public List<ChatMessage> getMessages() {
            List<ChatMessage> result = new ArrayList<>();
            // 如果有 SystemMessage，先添加
            if (systemMessage != null) {
                result.add(systemMessage);
            }
            // 添加其他消息
            for (ChatMessage msg : messages) {
                if (!(msg instanceof SystemMessage)) {
                    result.add(msg);
                }
            }
            return result;
        }

        /**
         * 获取当前消息数量
         */
        public int size() {
            return messages.size();
        }

        /**
         * 清空记忆
         */
        public void clear() {
            messages.clear();
            // 保留 SystemMessage
        }

        /**
         * 获取消息预览
         */
        public String getPreview(ChatMessage msg) {
            try {
                // 尝试通用的 toString 方式
                String text = msg.toString();
                // 移除可能的类名前缀
                if (text.contains("text=")) {
                    text = text.substring(text.indexOf("text=") + 5);
                    // 去掉末尾的 } 或其他符号
                    text = text.replaceAll("[})$]", "").trim();
                }
                return text.length() > 50 ? text.substring(0, 50) + "..." : text;
            } catch (Exception e) {
                return msg.type().name();
            }
        }
    }

    // 演示 1：基础对话记忆
    private static void demoBasicChatMemory(OpenAiChatModel chatModel) {
        // 创建对话记忆实例（最大容量 20 条消息）
        MessageWindowMemory chatMemory = new MessageWindowMemory(20);

        // 添加系统提示（定义 AI 角色）
        chatMemory.add(SystemMessage.from("你是一个专业的 Java 编程助手。"));

        // 第一轮对话
        String userMessage1 = "什么是多态？";
        log.info("用户: {}", userMessage1);
        chatMemory.add(UserMessage.from(userMessage1));

        // 从记忆中获取所有消息，构建请求
        List<ChatMessage> messages1 = chatMemory.getMessages();
        ChatRequest request1 = ChatRequest.builder()
                .messages(messages1)
                .temperature(0.3)
                .maxOutputTokens(1024)
                .build();

        ChatResponse response1 = chatModel.chat(request1);
        String answer1 = response1.aiMessage().text();

        log.info("AI: {}", answer1);
        // 将 AI 回复添加到记忆中
        chatMemory.add(response1.aiMessage());

        // 第二轮对话（携带第一轮上下文）
        String userMessage2 = "能举个 Java 代码示例吗？";
        log.info("\n用户: {}", userMessage2);
        chatMemory.add(UserMessage.from(userMessage2));

        // 从记忆中获取所有消息（包括第一轮的问题和回答）
        List<ChatMessage> messages2 = chatMemory.getMessages();
        log.info("当前记忆中的消息数量: {}", messages2.size());

        ChatRequest request2 = ChatRequest.builder()
                .messages(messages2)
                .temperature(0.3)
                .maxOutputTokens(1024)
                .build();

        ChatResponse response2 = chatModel.chat(request2);
        String answer2 = response2.aiMessage().text();

        log.info("AI: {}", answer2);
        chatMemory.add(response2.aiMessage());

        // 打印完整的对话历史
        log.info("\n--- 当前对话历史 ---");
        List<ChatMessage> finalMessages = chatMemory.getMessages();
        for (int i = 0; i < finalMessages.size(); i++) {
            ChatMessage msg = finalMessages.get(i);
            String role = msg.type().name();
            String content = chatMemory.getPreview(msg);
            log.info("  {}. [{}]: {}", i + 1, role, content);
        }
    }

    // 演示 2：消息窗口记忆（滑动窗口）
    private static void demoMessageWindowMemory(OpenAiChatModel chatModel) {
        // 创建一个只保留最近 6 条消息的记忆
        int maxMessages = 6;
        MessageWindowMemory chatMemory = new MessageWindowMemory(maxMessages);

        // 添加系统提示
        chatMemory.add(SystemMessage.from("你是一个乐于助人的 AI 助手。"));

        // 模拟多轮对话
        String[] userQuestions = {
                "我叫小明",
                "我刚才说我叫什么名字？",  // 应该能记住
                "今天天气怎么样？",        // 这个话题可以忘记
                "我叫什么名字？",          // 应该还记得
                "再问一次，我叫什么？"      // 应该还记得
        };

        for (int i = 0; i < userQuestions.length; i++) {
            String question = userQuestions[i];
            log.info("\n--- 第 {} 轮对话 ---", i + 1);
            log.info("用户: {}", question);

            // 添加用户消息
            chatMemory.add(UserMessage.from(question));

            // 获取当前记忆中的消息数量
            int currentSize = chatMemory.getMessages().size();
            log.info("记忆中的消息数量: {} (最大: {})", currentSize, maxMessages);

            // 构建请求并发送
            ChatRequest request = ChatRequest.builder()
                    .messages(chatMemory.getMessages())
                    .temperature(0.3)
                    .maxOutputTokens(512)
                    .build();

            ChatResponse response = chatModel.chat(request);
            String answer = response.aiMessage().text();

            log.info("AI: {}", answer);
            chatMemory.add(response.aiMessage());

            // 展示滑动窗口效果
            if (currentSize >= maxMessages) {
                log.info("⚠️ 消息数量达到上限，最早的消息已被自动移除（滑动窗口）");
            }
        }

        // 展示最终的记忆内容
        log.info("\n--- 滑动窗口后的最终记忆 ---");
        List<ChatMessage> finalMessages = chatMemory.getMessages();
        log.info("剩余消息数量: {}", finalMessages.size());
        for (ChatMessage msg : finalMessages) {
            String type = msg.type().name();
            String preview = chatMemory.getPreview(msg);
            log.info("  [{}]: {}", type, preview);
        }
    }

    // 演示 3：多用户会话隔离
    private static void demoMultiUserSessions(OpenAiChatModel chatModel) {
        // 模拟多用户场景：使用 Map 存储不同用户的对话记忆
        Map<String, MessageWindowMemory> userMemories = new java.util.HashMap<>();

        // 获取或创建用户记忆的工具方法
        Consumer<String> ensureMemory = userId -> {
            if (!userMemories.containsKey(userId)) {
                userMemories.put(userId, new MessageWindowMemory(10));
                userMemories.get(userId).add(SystemMessage.from("你是一个友善的聊天机器人。"));
            }
        };

        // 获取用户 1 的记忆（首次自动创建）
        String userId1 = "user_001";
        ensureMemory.accept(userId1);
        MessageWindowMemory memory1 = userMemories.get(userId1);

        // 获取用户 2 的记忆
        String userId2 = "user_002";
        ensureMemory.accept(userId2);
        MessageWindowMemory memory2 = userMemories.get(userId2);

        // ========================================================================
        // 用户 1 的对话
        // ========================================================================
        log.info("\n--- 用户 {} 的对话 ---", userId1);
        String q1 = "我最喜欢的颜色是蓝色";
        memory1.add(UserMessage.from(q1));

        ChatResponse r1 = chatModel.chat(ChatRequest.builder()
                .messages(memory1.getMessages())
                .temperature(0.3)
                .maxOutputTokens(512)
                .build());
        log.info("用户1问: {}", q1);
        log.info("AI答: {}", r1.aiMessage().text());
        memory1.add(r1.aiMessage());

        // 用户 2 的对话（独立进行，不受用户 1 影响）
        log.info("\n--- 用户 {} 的对话 ---", userId2);
        String q2 = "今天星期几？";
        memory2.add(UserMessage.from(q2));

        ChatResponse r2 = chatModel.chat(ChatRequest.builder()
                .messages(memory2.getMessages())
                .temperature(0.3)
                .maxOutputTokens(512)
                .build());
        log.info("用户2问: {}", q2);
        log.info("AI答: {}", r2.aiMessage().text());
        memory2.add(r2.aiMessage());

        // 用户 1 继续对话（能记住之前说的蓝色）
        log.info("\n--- 用户 {} 继续对话 ---", userId1);
        String q3 = "我刚才说我最喜欢什么颜色？";
        memory1.add(UserMessage.from(q3));

        ChatResponse r3 = chatModel.chat(ChatRequest.builder()
                .messages(memory1.getMessages())
                .temperature(0.3)
                .maxOutputTokens(512)
                .build());
        log.info("用户1问: {}", q3);
        log.info("AI答: {}", r3.aiMessage().text());
        memory1.add(r3.aiMessage());

        // 验证会话隔离
        log.info("\n--- 会话隔离验证 ---");
        log.info("用户 {} 的记忆消息数: {}", userId1, memory1.getMessages().size());
        log.info("用户 {} 的记忆消息数: {}", userId2, memory2.getMessages().size());
        log.info("✅ 不同用户的对话记忆完全隔离，互不影响");
    }

    // 演示 4：对话历史的增删改查
    private static void demoMemoryOperations(OpenAiChatModel chatModel) {
        // 使用 ArrayList 模拟可变长的记忆
        List<ChatMessage> messages = new ArrayList<>();
        int maxCapacity = 20;

        // 添加系统提示
        messages.add(SystemMessage.from("你是一个 AI 助手。"));

        // ========================================================================
        // 添加消息
        // ========================================================================
        log.info("\n--- 添加消息 ---");
        messages.add(UserMessage.from("今天天气真好！"));
        messages.add(AiMessage.from("是啊，今天阳光明媚，适合出游。"));

        log.info("添加 2 条消息后，当前消息数: {}", messages.size());
        printMemory(messages, "添加后");

        // ========================================================================
        // 更新消息
        // ========================================================================
        log.info("\n--- 更新消息 ---");
        // 注意：索引 0 是 SystemMessage，所以用户消息从索引 1 开始
        int userMessageIndex = 1;
        messages.set(userMessageIndex, UserMessage.from("今天天气真不错！虽然有点热。"));

        log.info("更新索引 {} 的消息", userMessageIndex);
        printMemory(messages, "更新后");

        // ========================================================================
        // 删除消息
        // ========================================================================
        log.info("\n--- 删除消息 ---");
        int deleteIndex = 2; // 删除 AI 的回复
        messages.remove(deleteIndex);

        log.info("删除索引 {} 的消息", deleteIndex);
        printMemory(messages, "删除后");

        // ========================================================================
        // 清空记忆（保留 SystemMessage）
        // ========================================================================
        log.info("\n--- 清空记忆（保留系统提示） ---");
        // 保留 SystemMessage
        String systemPrompt = messages.get(0) instanceof SystemMessage
                ? ((SystemMessage) messages.get(0)).text()
                : "";
        messages.clear();
        if (!systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }

        log.info("清空所有消息后，当前消息数: {}", messages.size());

        // ========================================================================
        // 完整对话流程演示
        // ========================================================================
        log.info("\n--- 完整对话流程 ---");
        messages.clear();
        messages.add(SystemMessage.from("你是一个简洁的 AI 助手。"));

        String[] questions = {
                "用一句话介绍 Java",
                "再说一个 Java 的特点",
                "最后补充一点"
        };

        for (String question : questions) {
            log.info("\n用户: {}", question);
            messages.add(UserMessage.from(question));

            ChatResponse response = chatModel.chat(ChatRequest.builder()
                    .messages(messages)
                    .temperature(0.3)
                    .maxOutputTokens(256)
                    .build());

            String answer = response.aiMessage().text();
            log.info("AI: {}", answer);
            messages.add(response.aiMessage());

            TokenUsage usage = response.tokenUsage();
            if (usage != null) {
                log.info("Token 消耗: 输入 {} / 输出 {}",
                        usage.inputTokenCount(),
                        usage.outputTokenCount());
            }
        }

        printMemory(messages, "最终记忆");
    }

    /**
     * 打印当前记忆内容（用于调试）
     */
    private static void printMemory(List<ChatMessage> messages, String stage) {
        log.info("  === {} ===", stage);
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            String type = msg.type().name();
            String preview = getMessagePreview(msg);
            log.info("    {}. [{}]: {}", i, type, preview);
        }
    }

    /**
     * 获取消息的预览内容（用于日志输出）
     *
     * @param message 消息对象
     * @return 消息内容的预览字符串
     */
    private static String getMessagePreview(ChatMessage message) {
        try {
            // 尝试通用的 toString 方式
            String text = message.toString();
            // 移除可能的类名前缀
            if (text.contains("text=")) {
                text = text.substring(text.indexOf("text=") + 5);
                // 去掉末尾的 } 或其他符号
                text = text.replaceAll("[})$]", "").trim();
            }
            return text.length() > 50 ? text.substring(0, 50) + "..." : text;
        } catch (Exception e) {
            return message.type().name();
        }
    }
}
