package top.lrshuai.langchain4j.mcp.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期时间工具
 */
@Slf4j
public class DateTimeTool {

    @Tool("获取当前日期和时间")
    public String currentDateTime(@P("日期时间格式，例如 yyyy-MM-dd HH:mm:ss，不填则使用默认格式") String format) {
        log.info("执行 currentDateTime, format={}", format);
        LocalDateTime now = LocalDateTime.now();
        if (format == null || format.isBlank()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        return now.format(DateTimeFormatter.ofPattern(format));
    }

    @Tool("获取今天是星期几")
    public String dayOfWeek(@P("日期，格式 yyyy-MM-dd，不填则使用今天") String date) {
        log.info("执行 dayOfWeek, date={}", date);
        LocalDate targetDate;
        if (date == null || date.isBlank()) {
            targetDate = LocalDate.now();
        } else {
            targetDate = LocalDate.parse(date);
        }
        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        return targetDate + " 是 " + getChineseDayOfWeek(dayOfWeek);
    }

    @Tool("计算两个日期之间相差多少天")
    public String daysBetween(
            @P("起始日期，格式 yyyy-MM-dd") String fromDate,
            @P("结束日期，格式 yyyy-MM-dd，不填则使用今天") String toDate) {
        log.info("执行 daysBetween, from={}, to={}", fromDate, toDate);
        LocalDate from = LocalDate.parse(fromDate);
        LocalDate to = (toDate == null || toDate.isBlank()) ? LocalDate.now() : LocalDate.parse(toDate);
        long days = ChronoUnit.DAYS.between(from, to);
        return from + " 到 " + to + " 相差 " + Math.abs(days) + " 天";
    }

    @Tool("计算N天之后的日期")
    public String plusDays(
            @P("基准日期，格式 yyyy-MM-dd，不填则使用今天") String date,
            @P("要加的天数") int days) {
        log.info("执行 plusDays, date={}, days={}", date, days);
        LocalDate baseDate = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);
        LocalDate result = baseDate.plusDays(days);
        return result.toString();
    }

    @Tool("获取当前年份")
    public int currentYear() {
        log.info("执行 currentYear");
        return LocalDate.now().getYear();
    }

    private String getChineseDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}
