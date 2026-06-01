package top.lrshuai.langchain4j.chat.skills.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Word 文档处理工具集
 */
@Slf4j
public class DocxTools {

    @Tool("查看文档中的修订记录（跟踪更改）")
    public String trackChanges(@P("文档路径") String filePath) {
        log.info("查看修订记录: {}", filePath);
        List<String> changes = List.of(
                "第3行：将「客户满意度达到95%」→ 修改为「客户满意度达到98%」",
                "第8行：新增段落「2025年Q1新增华东大区客户23家」",
                "第12行：删除「旧版合同编号 CT-20240001」",
                "第15行：格式调整 — 正文行距从1.5倍改为1.25倍"
        );
        return String.format("""
                        === %s 修订记录 ===
                        %s
                        共 %d 处修订，修改者：张三
                        """,
                filePath,
                String.join("\n", changes),
                changes.size());
    }

    @Tool("批量接受文档中的所有修订")
    public String acceptAllChanges(@P("文档路径") String filePath) {
        log.info("接受所有修订: {}", filePath);
        return String.format("文档 %s 的所有修订（4处）已接受，生成新版本 v2.1", filePath);
    }

    @Tool("批量拒绝文档中的所有修订")
    public String rejectAllChanges(@P("文档路径") String filePath) {
        log.info("拒绝所有修订: {}", filePath);
        return String.format("文档 %s 的所有修订（4处）已拒绝，回退到原始版本 v2.0", filePath);
    }

    @Tool("查看文档中的所有注释")
    public String listComments(@P("文档路径") String filePath) {
        log.info("查看注释: {}", filePath);
        List<String> comments = List.of(
                "[张三] 建议此处补充2025年数据",
                "[李四] 请确认客户名称是否正确",
                "[王五] PENDING — 需要法务确认合同条款"
        );
        return String.format("""
                        === %s 注释列表 ===
                        %s
                        共 %d 条注释（1条待处理）
                        """,
                filePath,
                String.join("\n", comments),
                comments.size());
    }
}
