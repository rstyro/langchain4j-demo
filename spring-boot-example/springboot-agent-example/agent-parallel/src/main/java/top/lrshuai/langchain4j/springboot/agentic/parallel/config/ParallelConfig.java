package top.lrshuai.langchain4j.springboot.agentic.parallel.config;

import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.FoodExpert;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.MovieExpert;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper.BatchHoroscopeAgent;
import top.lrshuai.langchain4j.springboot.agentic.parallel.agent.parallelMapper.HoroscopeAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Parallel 并行工作流配置。
 */
@Configuration
public class ParallelConfig {

    @Bean
    FoodExpert foodExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(FoodExpert.class)
                .chatModel(chatModel)
                .outputKey("meals")
                .async(true) // 可以开启异步
                .build();
    }

    @Bean
    MovieExpert movieExpert(ChatModel chatModel) {
        return AgenticServices.agentBuilder(MovieExpert.class)
                .chatModel(chatModel)
                .outputKey("movies")
                .async(true)
                .build();
    }

    /**
     * 约会方案 — 电影+餐食的组合实体。
     */
    public record EveningPlan(String movie, String meal) {}

    /**
     * 并行工作流：美食 + 电影并行推荐 → 聚合为 Map。
     */
    @Bean
    UntypedAgent eveningPlanner(FoodExpert food, MovieExpert movie) {
        return AgenticServices.parallelBuilder()
                .subAgents(food, movie)
                .executor(Executors.newFixedThreadPool(2))
                .output(scope -> {
                    List<String> movies = scope.readState("movies", List.of());
                    List<String> meals = scope.readState("meals", List.of());
                    List<EveningPlan> moviesAndMeals = new ArrayList<>();
                    for (int i = 0; i < movies.size(); i++) {
                        if (i >= meals.size()) {
                            break;
                        }
                        moviesAndMeals.add(new EveningPlan(movies.get(i), meals.get(i)));
                    }
                    return moviesAndMeals;
                })
                .outputKey("plans")
                .build();
    }



    @Bean
    HoroscopeAgent horoscopeAgent(ChatModel chatModel) {
        return AgenticServices.agentBuilder(HoroscopeAgent.class)
                .chatModel(chatModel)
                .outputKey("horo")
                .build();
    }



    /**
     * ParallelMapper 批量工作流：遍历 Person 列表并发生成运势。
     * <p>
     * 使用声明式接口 {@link BatchHoroscopeAgent} 配合 {@code parallelMapperBuilder(Class)}。
     */
    @Bean
    BatchHoroscopeAgent batchHoroscopeAgent(HoroscopeAgent horo) {
        return AgenticServices.parallelMapperBuilder(BatchHoroscopeAgent.class)
                .subAgents(horo)
                .itemsProvider("persons")
                .executor(Executors.newFixedThreadPool(3))
                .build();
    }
}
