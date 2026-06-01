package top.lrshuai.langchain4j.springboot.tool.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WeatherTools {

    @Tool("查询指定城市的天气信息")
    public String getWeather(@P("城市名称") String city) {
        log.info("查询天气: {}", city);
        // 模拟天气数据
        return String.format("%s 今天晴，气温 22°C ~ 30°C，东南风 2-3 级，空气质量良", city);
    }

    @Tool("查询指定城市的未来天气预报")
    public String getWeatherForecast(
            @P("城市名称") String city,
            @P("未来天数，最多7天") int days) {
        log.info("查询 {} 未来 {} 天天气", city, days);
        return String.format("%s 未来 %d 天天气：明天晴 20~28°C，后天多云 18~25°C，大后天小雨 16~22°C",
                city, Math.min(days, 7));
    }
}
