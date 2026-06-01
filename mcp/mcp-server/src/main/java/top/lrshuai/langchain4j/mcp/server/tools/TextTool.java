package top.lrshuai.langchain4j.mcp.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

/**
 * 文本处理工具
 */
@Slf4j
public class TextTool {

    @Tool("获取文本的长度（字符数）")
    public int countChars(@P("要计算的文本") String text) {
        log.info("执行 countChars, text length={}", text != null ? text.length() : 0);
        return text != null ? text.length() : 0;
    }

    @Tool("反转文本")
    public String reverse(@P("要反转的文本") String text) {
        log.info("执行 reverse, text length={}", text != null ? text.length() : 0);
        if (text == null) return "";
        return new StringBuilder(text).reverse().toString();
    }

    @Tool("文本转为大写")
    public String toUpperCase(@P("要转换的文本") String text) {
        log.info("执行 toUpperCase");
        return text != null ? text.toUpperCase() : "";
    }

    @Tool("文本转为小写")
    public String toLowerCase(@P("要转换的文本") String text) {
        log.info("执行 toLowerCase");
        return text != null ? text.toLowerCase() : "";
    }

    @Tool("去除首尾空白字符（trim）")
    public String trim(@P("要去除空白的文本") String text) {
        log.info("执行 trim");
        return text != null ? text.trim() : "";
    }

    @Tool("统计文本中某个子串出现的次数")
    public int countSubstring(
            @P("源文本") String text,
            @P("要查找的子串") String substring) {
        log.info("执行 countSubstring, substring='{}'", substring);
        if (text == null || substring == null || substring.isEmpty()) return 0;
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    @Tool("用分隔符连接多个字符串")
    public String join(
            @P("分隔符") String delimiter,
            @P("要连接的字符串数组") String[] parts) {
        log.info("执行 join, delimiter={}, parts count={}", delimiter, parts != null ? parts.length : 0);
        if (parts == null) return "";
        return String.join(delimiter, parts);
    }
}
