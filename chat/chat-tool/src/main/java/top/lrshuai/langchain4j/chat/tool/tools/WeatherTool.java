package top.lrshuai.langchain4j.chat.tool.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 天气查询工具
 * <p>
 * 模拟天气查询 API，实际项目中可替换为真实天气服务调用。
 * 使用 @Tool 注解标记方法，LLM 会根据用户问题自动决定是否调用。
 */
@Slf4j
public class WeatherTool {

    @Tool("查询指定城市的当前天气信息，包括温度、天气状况和风力") // @Tool 标记方法为LLM可调用工具，参数为工具功能描述
    public String queryWeather(
            @P("城市名称，如：北京、上海、广州") String cityName // @P 描述参数含义，指导LLM生成正确参数值
    ) {
        log.info("工具调用: queryWeather(cityName={})", cityName);
        // 模拟天气数据，实际项目应调用真实天气 API
        String weather = switch (cityName) {
            case "北京" -> "晴，温度 18°C，北风 3 级，空气质量良好";
            case "上海" -> "多云，温度 22°C，东南风 2 级，湿度 65%";
            case "广州" -> "阵雨，温度 28°C，南风 1 级，湿度 85%";
            case "深圳" -> "雷阵雨，温度 30°C，南风 2 级，湿度 90%";
            case "成都" -> "阴，温度 16°C，微风，湿度 70%";
            default -> "晴，温度 20°C，微风";
        };
        return weather;
    }

    @Tool("查询指定城市未来3天的天气预报")
    public String queryWeatherForecast(
            @P("城市名称") String cityName
    ) {
        log.info("工具调用: queryWeatherForecast(cityName={})", cityName);
        return switch (cityName) {
            case "北京" -> """
                    北京未来3天天气预报：
                    明天：晴转多云，12~22°C
                    后天：多云，10~20°C
                    大后天：阴转小雨，8~16°C""";
            case "上海" -> """
                    上海未来3天天气预报：
                    明天：多云转晴，18~25°C
                    后天：晴，17~26°C
                    大后天：多云，16~24°C""";
            default -> """
                    %s未来3天天气预报：
                    明天：多云，15~23°C
                    后天：晴，14~22°C
                    大后天：多云转阴，13~20°C""".formatted(cityName);
        };
    }
}
