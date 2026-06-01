package top.lrshuai.langchain4j.springboot.tool.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface ToolAssistant {

    @SystemMessage("""
            你是一个实用的 AI 助手，可以查询天气信息来帮助用户。
            当用户询问天气时，使用 getWeather 或 getWeatherForecast 工具获取实时数据后再回答。
            回答要简洁实用。
            """)
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    TokenStream chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
