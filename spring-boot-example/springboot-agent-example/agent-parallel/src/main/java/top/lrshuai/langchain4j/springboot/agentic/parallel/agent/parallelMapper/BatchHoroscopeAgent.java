package top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.planner.AgentInstance;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * 批量运势 Agent — ParallelMapper 声明式接口。
 * <p>
 * 框架遍历 persons 列表，为每个 Person 并发调用 HoroscopeAgent，
 */
public interface BatchHoroscopeAgent extends AgentInstance {

    @Agent
    List<String> generateHoroscopes(@V("persons") List<Person> persons);
}
