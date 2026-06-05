package top.lrshuai.langchain4j.springboot.agentic.parallel.controller;

import dev.langchain4j.agentic.UntypedAgent;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.lrshuai.langchain4j.common.resp.R;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper.BatchHoroscopeAgent;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parallel 并行工作流 REST 接口。
 * <p>
 * 提供约会方案（并行推荐）和批量运势（ParallelMapper）两个端点。
 */
@RestController
@RequestMapping("/agent/parallel")
public class ParallelController {

    @Resource
    private UntypedAgent eveningPlanner;
    @Resource
    private BatchHoroscopeAgent batchHoroscopeAgent;

    /**
     * 约会方案：美食 + 电影并行推荐 → 聚合。
     *
     * @param mood 情绪关键词
     */
    @GetMapping("/plan")
    public R<?> eveningPlan(@RequestParam(defaultValue = "浪漫") String mood) {
        return R.ok(eveningPlanner.invoke(Map.of("mood", mood)));
    }

    /**
     * 批量星座运势：为用户列表并行生成运势（ParallelMapper）。
     *
     * @param names 用户名列表（逗号分隔）
     * @param signs 星座列表（逗号分隔）
     */
    @GetMapping("/horoscope")
    public R<List<String>> batchHoroscope(@RequestParam(defaultValue = "刘备,关羽,张飞") String names,
                                           @RequestParam(defaultValue = "白羊,金牛,双子") String signs) {
        String[] nameArr = names.split(",");
        String[] signArr = signs.split(",");
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < Math.min(nameArr.length, signArr.length); i++) {
            persons.add(new Person(nameArr[i].trim(), signArr[i].trim()));
        }
        return R.ok(batchHoroscopeAgent.generateHoroscopes(persons));
    }
}
