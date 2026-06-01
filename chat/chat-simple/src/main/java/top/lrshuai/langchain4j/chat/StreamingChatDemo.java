package top.lrshuai.langchain4j.chat;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import top.lrshuai.langchain4j.common.config.LlmConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * LangChain4j 流式输出示例
 *
 * @author rstyro
 */
@Slf4j
public class StreamingChatDemo {

    public static void main(String[] args) {
        String apiKey = System.getenv(LlmConfig.API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            log.error("请设置环境变量 " + LlmConfig.API_KEY_ENV);
            System.exit(1);
        }

        // 使用 StreamingChatModel 接口
        StreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(LlmConfig.LLM_BASE_URL)
                .modelName(LlmConfig.LLM_MODEL_DOUBAO_CODE)
                .logRequests(true)
                .logResponses(true)
                .build();

        final int[] inputTokens = {0};
        final int[] outputTokens = {0};
        final int[] tokenCount = {0};
        long startTime = System.currentTimeMillis();

        CompletableFuture<Void> future = new CompletableFuture<>();
        ChatRequest request = ChatRequest.builder()
                .messages(
                        SystemMessage.from("你是一个专业的技术讲师。"),
                        UserMessage.from("解释什么是设计模式，由简入深")
                )
                .temperature(0.3)
                .maxOutputTokens(800)
                .build();

        streamingChatModel.chat(request, new StreamingChatResponseHandler() {

            /**
             * 部分响应回调
             * <p>
             * 当 LLM 返回部分（片段）响应时触发，通常是 1 个或几个 token。
             * 适用于实时显示流式输出，实现打字机效果。
             *
             * @param partialResponse 部分响应内容（通常是文本片段）
             */
            @Override
            public void onPartialResponse(String partialResponse) {
                tokenCount[0]++;
                System.out.print(partialResponse);
            }

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                // 思考过程，看模型是否支持
                System.out.println("onPartialThinking: " + partialThinking);
            }

            @Override
            public void onPartialToolCall(PartialToolCall partialToolCall) {
                System.out.println("onPartialToolCall: " + partialToolCall);
            }

            @Override
            public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                System.out.println("onCompleteToolCall: " + completeToolCall);
            }

            /**
             * 完整响应完成回调
             * <p>
             * 当 LLM 响应完全结束时触发，此时可以获取：
             * - 完整的 AI 回复内容
             * - Token 使用量统计
             * - 响应结束原因（正常结束/达到最大长度等）
             *
             * @param response 完整的响应对象，包含 AI 回复、Token 用量、结束原因等
             */
            @Override
            public void onCompleteResponse(ChatResponse response) {
                long duration = System.currentTimeMillis() - startTime;
                TokenUsage tokenUsage = response.tokenUsage();

                if (tokenUsage != null) {
                    inputTokens[0] = tokenUsage.inputTokenCount();
                    outputTokens[0] = tokenUsage.outputTokenCount();
                }

                String text = response.aiMessage().text();
                System.out.println("AI回复："+text);

                System.out.println();
                log.info("\n========== Token 统计 ==========");
                log.info("片段数量: {}", tokenCount[0]);
                log.info("输入 Token: {}", inputTokens[0]);
                log.info("输出 Token: {}", outputTokens[0]);
                log.info("总耗时: {}ms", duration);
                if (outputTokens[0] > 0) {
                    log.info("平均每 Token 耗时: {}ms", duration / outputTokens[0]);
                }

                future.complete(null);
            }

            /**
             * 错误回调
             * <p>
             * 当流式响应过程中发生错误时触发，常见错误场景：
             * - 网络连接失败
             * - API Key 无效或过期
             * - LLM 服务不可用
             * - 请求超时
             * - 模型不支持某些参数
             * @param error 错误信息
             */
            @Override
            public void onError(Throwable error) {
                log.error("流式响应出错: {}", error.getMessage());
                future.completeExceptionally(error);
            }
        });

        try {
            future.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("等待流式响应超时: {}", e.getMessage());
        }
    }
}
