package top.lrshuai.langchain4j.mcp.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 文件系统工具
 */
@Slf4j
public class FileSystemTool {

    @Tool("获取文件的大小（字节数）")
    public String fileSize(@P("文件路径") String filePath) {
        log.info("执行 fileSize, path={}", filePath);
        Path path = Paths.get(filePath);
        try {
            long size = Files.size(path);
            return formatSize(size);
        } catch (IOException e) {
            return "读取文件大小失败：" + e.getMessage();
        }
    }

    @Tool("获取文件的行数")
    public String lineCount(@P("文件路径") String filePath) {
        log.info("执行 lineCount, path={}", filePath);
        Path path = Paths.get(filePath);
        try {
            long lines = Files.lines(path).count();
            return "文件 " + path.getFileName() + " 共有 " + lines + " 行";
        } catch (IOException e) {
            return "读取文件行数失败：" + e.getMessage();
        }
    }

    @Tool("检查文件或目录是否存在")
    public String exists(@P("文件或目录路径") String path) {
        log.info("执行 exists, path={}", path);
        Path p = Paths.get(path);
        if (Files.exists(p)) {
            return Files.isDirectory(p) ? "目录存在：" + p.toAbsolutePath() : "文件存在：" + p.toAbsolutePath();
        }
        return "不存在：" + p.toAbsolutePath();
    }

    @Tool("获取文件的修改时间")
    public String lastModified(@P("文件路径") String filePath) {
        log.info("执行 lastModified, path={}", filePath);
        Path path = Paths.get(filePath);
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return "最后修改时间：" + attrs.lastModifiedTime().toString();
        } catch (IOException e) {
            return "读取文件属性失败：" + e.getMessage();
        }
    }

    @Tool("列出目录下的文件和子目录")
    public String listDir(@P("目录路径") String dirPath) {
        log.info("执行 listDir, path={}", dirPath);
        Path path = Paths.get(dirPath);
        if (!Files.isDirectory(path)) {
            return "不是一个目录：" + dirPath;
        }
        try {
            StringBuilder sb = new StringBuilder("目录 " + path.getFileName() + " 的内容：\n");
            Files.list(path).forEach(p -> {
                String prefix = Files.isDirectory(p) ? "[DIR]  " : "[FILE] ";
                sb.append(prefix).append(p.getFileName()).append("\n");
            });
            return sb.toString();
        } catch (IOException e) {
            return "读取目录失败：" + e.getMessage();
        }
    }

    @Tool("读取文本文件内容（最多5000字符）")
    public String readFile(@P("文件路径") String filePath) {
        log.info("执行 readFile, path={}", filePath);
        Path path = Paths.get(filePath);
        try {
            String content = Files.readString(path);
            if (content.length() > 5000) {
                return content.substring(0, 5000) + "\n\n... (文件过长，仅显示前5000字符)";
            }
            return content;
        } catch (IOException e) {
            return "读取文件失败：" + e.getMessage();
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}
