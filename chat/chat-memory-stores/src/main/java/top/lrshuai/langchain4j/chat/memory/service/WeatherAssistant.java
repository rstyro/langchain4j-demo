package top.lrshuai.langchain4j.chat.memory.service;

import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface WeatherAssistant {

    // 天气提问
    @SystemMessage("""
            你是一个天气助手，只能返回标准JSON格式，不要返回任何多余文字、解释、markdown。
            返回JSON格式如下：
            {
                "cityName": "城市名称",
                "weather": "天气描述",
                "temperature": "温度",
                "details": "天气详情"
            }
            只返回JSON，不要加任何其他内容！
            """)
    WeatherResultVo chat(@MemoryId String userId, @UserMessage String message);

    // 添加结果
    record WeatherResultVo(@Description("城市名称")String cityName,
                           @Description("天气描述")String weather,
                           @Description("温度")String temperature,
                           @Description("天气详情")String details){}
}
