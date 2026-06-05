# LangChain4j-Agentic 从入门到精通全教程
> 文档参考：LangChain4j 官方Agentic教程 https://docs.langchain4j.dev/tutorials/agents/
> `langchain4j-agentic`是LangChain4j官方推出的**Java原生多智能体编排框架**，区别于传统单AI Service，支持多Agent协同、多种工作流、自主规划（Supervisor）、自定义编排引擎，分为**确定性Workflow（固定流程）**与**Pure Agent（大模型自主规划）**两大体系，本文由浅入深，从环境、基础、常用工作流、高级特性、自定义编排、生产落地全链路讲解。

## 目录
1. 前置准备：环境与依赖（入门第一步）
2. 核心基础：Agent定义 + AgenticScope（智能体共享上下文）
3. 五大内置确定性工作流实战（Sequential/Loop/Parallel/ParallelMapper/Conditional）
4. Agent高级增强特性：可选/异步/流式/动态模型/异常处理/可观测监控
5. 声明式注解开发：告别Builder链式，注解定义工作流
6. 纯自主智能体：Supervisor监督Agent（Pure Agentic AI，LLM自主调度子Agent）
7. 拓展智能体：非AI Agent、人在回路(Human-in-the-loop)、TypedKey强类型变量
8. 自定义编排Planner：Goal/P2P/Voting三大自定义模式（精通核心）
9. 进阶集成：A2A远程Agent、MCP工具Agent、Scope持久化与故障恢复
10. 生产实战：全流程综合项目（银行转账智能系统）
11. 落地最佳实践与避坑指南

## 一、前置准备：环境与依赖
### 1.1 Maven依赖
`langchain4j-agentic`仍处于**实验版本**，版本跟随LangChain4j主版本，引入核心依赖（以1.15.1-beta25为例）：
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-agentic</artifactId>
    <version>1.15.1-beta25</version>
</dependency>
<!-- 按需引入模型：OpenAI/通义千问/星火等，示例用OpenAI -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>1.15.1</version>
</dependency>
```
### 1.2 基础模型初始化
```java
import dev.langchain4j.model.openai.OpenAiChatModel;
// 全局基础模型，后续所有Agent共用
public static final OpenAiChatModel BASE_MODEL = OpenAiChatModel.builder()
        .apiKey("your-api-key")
        .modelName("gpt-3.5-turbo")
        .temperature(0.1)
        .build();
// Supervisor专用高精度规划模型（推荐gpt4）
public static final OpenAiChatModel PLANNER_MODEL = OpenAiChatModel.builder()
        .apiKey("your-api-key")
        .modelName("gpt-4")
        .temperature(0)
        .build();
```

## 二、核心基础：Agent与AgenticScope（重中之重）
### 2.1 Agent是什么：@Agent注解定义智能体
Agent本质是**增强版AI Service**，区别普通AI Service：
- 支持`outputKey`：输出存入`AgenticScope`共享变量，跨Agent传参
- 可被任意工作流组合编排，实现多智能体协作
- 支持绑定工具、记忆、动态模型、监听等能力

#### 2.1.1 两种Agent定义方式
##### 方式1：接口+注解（推荐，强类型）
```java
import dev.langchain4j.agent.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

// 创意写作Agent：根据主题生成短篇故事
public interface CreativeWriter {
    @Agent(description = "根据指定主题生成不超过3句话的短篇故事", outputKey = "story")
    @UserMessage("你是创意作家，围绕{{topic}}写≤3句话故事，只返回故事正文，不要额外内容")
    String generateStory(@V("topic") String topic);
}
```
- `@V("topic")`：从`AgenticScope`读取key为`topic`的变量；编译加`-parameters`可省略`@V`自动映射参数名
- `outputKey = "story"`：Agent执行结果存入Scope的`story`变量，供下游Agent读取
- `description`：Supervisor自主调度时用来识别Agent能力（Pure Agent必备）

##### 方式2：无类型UntypedAgent（动态定义，无接口）
```java
UntypedAgent untypedWriter = AgenticServices.agentBuilder()
        .chatModel(BASE_MODEL)
        .description("生成短篇故事")
        .userMessage("你是创意作家，围绕{{topic}}写≤3句话故事")
        .inputKey("topic")
        .outputKey("story")
        .build();
```

##### 实例化Agent
```java
CreativeWriter writer = AgenticServices.agentBuilder(CreativeWriter.class)
        .chatModel(BASE_MODEL)
        .build();
// 单独调用Agent
String res = writer.generateStory("巨龙与巫师");
```

### 2.2 AgenticScope：多Agent共享数据容器（框架核心）
`AgenticScope`是**每个Agentic系统独立的共享上下文存储空间**，生命周期绑定单次任务/用户会话：
1. **作用**：所有Agent的入参、输出全部存放在Scope，Agent通过`outputKey`写入、`@V`读取，实现数据互通
2. **生命周期规则**
   | 场景 | Scope生命周期 |
   |------|-------------|
   | 无ChatMemory（默认） | 单次任务执行完毕自动销毁，临时数据 |
   | 开启ChatMemory | 存入内存注册表，需手动`evict`释放，支持多轮对话 |
3. **内置能力**：自动记录全链路Agent调用日志、参数、返回值，是可观测、故障恢复的数据来源

> 所有工作流底层都是操作同一个`AgenticScope`，数据流转全靠Scope实现。

## 三、五大内置确定性工作流实战（固定编排，入门必练）
LangChain4j内置**5种开箱即用工作流**，全部通过`AgenticServices.xxxBuilder()`构建，工作流本身也是一个Agent，可嵌套组合。

### 3.1 Sequential顺序工作流（串行执行，前输出做后输入）
**场景**：故事生成→受众改编→风格润色，线性依赖
```java
// 1.定义三个子Agent：写作+受众编辑+风格编辑
public interface AudienceEditor {
    @Agent(outputKey = "story", description = "根据受众改写故事")
    @UserMessage("按照{{audience}}受众优化故事：{{story}}，仅返回优化内容")
    String editByAudience(@V("story") String story, @V("audience") String audience);
}
public interface StyleEditor {
    @Agent(outputKey = "story", description = "按照指定风格优化故事")
    @UserMessage("按照{{style}}风格改写故事：{{story}}，仅返回优化内容")
    String editByStyle(@V("story") String story, @V("style") String style);
}

// 2.实例化Agent
CreativeWriter writer = AgenticServices.agentBuilder(CreativeWriter.class).chatModel(BASE_MODEL).build();
AudienceEditor audienceEdit = AgenticServices.agentBuilder(AudienceEditor.class).chatModel(BASE_MODEL).build();
StyleEditor styleEdit = AgenticServices.agentBuilder(StyleEditor.class).chatModel(BASE_MODEL).build();

// 3.组装串行工作流（强类型接口）
public interface NovelCreator {
    @Agent
    String create(@V("topic") String topic, @V("audience") String audience, @V("style") String style);
}
NovelCreator novelAgent = AgenticServices.sequenceBuilder(NovelCreator.class)
        .subAgents(writer, audienceEdit, styleEdit) // 串行顺序：写作→受众→风格
        .outputKey("story")
        .build();

// 调用
String finalStory = novelAgent.create("巨龙与巫师", "青少年", "奇幻");
```

### 3.2 Loop循环工作流（迭代优化，满足条件退出）
**场景**：故事打分→迭代优化，分数≥0.8停止，最大5轮防死循环
```java
// 评分Agent
public interface StyleScorer {
    @Agent(outputKey = "score", description = "0~1打分故事风格匹配度")
    @UserMessage("给{{style}}风格的故事{{story}}打分，只输出小数")
    Double score(@V("story") String story, @V("style") String style);
}

StyleScorer scorer = AgenticServices.agentBuilder(StyleScorer.class).chatModel(BASE_MODEL).build();
// 构建循环：先打分再优化，分数≥0.8退出，最多迭代5次
UntypedAgent loopAgent = AgenticServices.loopBuilder()
        .subAgents(scorer, styleEdit)
        .maxIterations(5) // 最大循环次数
        .exitCondition(scope -> scope.readState("score",0.0)>=0.8) // 退出条件
        .testExitAtLoopEnd(true) // 循环结束后再判断退出
        .build();

// 组合：先生成故事再进入循环优化
UntypedAgent fullWorkflow = AgenticServices.sequenceBuilder()
        .subAgents(writer, loopAgent)
        .outputKey("story")
        .build();
String res = fullWorkflow.invoke(Map.of("topic","巨龙","style","喜剧"));
```

### 3.3 Parallel并行工作流（多Agent并发执行，结果聚合）
**场景**：根据情绪，美食专家+电影专家并行推荐，最后组合约会方案
```java
// 美食/电影Agent
public interface FoodExpert {
    @Agent(outputKey = "meals", description = "根据情绪推荐3个餐食")
    @UserMessage("根据{{mood}}推荐3个晚餐名称，仅列表")
    List<String> getFood(@V("mood") String mood);
}
public interface MovieExpert {
    @Agent(outputKey = "movies", description = "根据情绪推荐3部电影")
    @UserMessage("根据{{mood}}推荐3部电影名称，仅列表")
    List<String> getMovie(@V("mood") String mood);
}
// 实体存储组合结果
record EveningPlan(String movie, String meal){}

FoodExpert food = AgenticServices.agentBuilder(FoodExpert.class).chatModel(BASE_MODEL).build();
MovieExpert movie = AgenticServices.agentBuilder(MovieExpert.class).chatModel(BASE_MODEL).build();

// 并行编排，自定义结果聚合逻辑
UntypedAgent eveningAgent = AgenticServices.parallelBuilder()
        .subAgents(food, movie)
        .executor(Executors.newFixedThreadPool(2)) // 自定义线程池
        .output(scope->{ // 聚合两个Agent结果
            List<String> meals = scope.readState("meals", List.of());
            List<String> movies = scope.readState("movies", List.of());
            List<EveningPlan> plans = new ArrayList<>();
            for(int i=0;i<Math.min(meals.size(),movies.size());i++){
                plans.add(new EveningPlan(movies.get(i),meals.get(i)));
            }
            return plans;
        })
        .build();
List<EveningPlan> plans = (List<EveningPlan>) eveningAgent.invoke(Map.of("mood","浪漫"));
```

### 3.4 ParallelMapper批量并行（同一个Agent遍历集合多实例并发）
**场景**：批量给多人生成星座运势，列表入参，每个元素单独起Agent并发执行
```java
record Person(String name, String sign){}
public interface HoroscopeAgent {
    @Agent(outputKey = "horo")
    @UserMessage("给{{person}}生成星座运势")
    String gen(@V("person") Person person);
}
HoroscopeAgent horo = AgenticServices.agentBuilder(HoroscopeAgent.class).chatModel(BASE_MODEL).build();

// 批量Mapper：自动遍历persons列表，每个Person开一个Agent并行
UntypedAgent batchAgent = AgenticServices.parallelMapperBuilder()
        .subAgents(horo)
        .itemsProvider("persons") // 指定入参key为persons（List<Person>）
        .executor(Executors.newFixedThreadPool(3))
        .build();

List<Person> users = List.of(new Person("马里奥","白羊"),new Person("路易","双鱼"));
List<String> result = (List<String>) batchAgent.invoke(Map.of("persons",users));
```
> 限制：Mapper子Agent**禁止配置ChatMemory**，框架会抛异常（每个实例无独立会话）

### 3.5 Conditional条件路由工作流（分类后路由不同专家Agent）
**场景**：用户提问→分类（医疗/法律/技术/未知）→路由对应专家回答
```java
// 1.分类枚举
enum RequestCat{MEDICAL,LEGAL,TECH,UNKNOWN}
// 分类Agent
public interface ClassifyAgent {
    @Agent(outputKey = "category")
    @UserMessage("把{{request}}分为MEDICAL/LEGAL/TECH/UNKNOWN，只返回枚举名")
    RequestCat classify(@V("request") String req);
}
// 三类专家Agent
public interface MedicalExpert {
    @Agent(outputKey = "resp")
    @UserMessage("医疗专家回答：{{request}}")
    String med(@V("request") String req);
}
// LegalExpert、TechnicalExpert 代码同上

// 构建条件路由
ClassifyAgent classifier = AgenticServices.agentBuilder(ClassifyAgent.class).chatModel(BASE_MODEL).build();
MedicalExpert med = AgenticServices.agentBuilder(MedicalExpert.class).chatModel(BASE_MODEL).build();
LegalExpert legal = AgenticServices.agentBuilder(LegalExpert.class).chatModel(BASE_MODEL).build();

UntypedAgent router = AgenticServices.conditionalBuilder()
        .subAgents(scope->scope.readState("category",RequestCat.UNKNOWN)==RequestCat.MEDICAL, med)
        .subAgents(scope->scope.readState("category",RequestCat.LEGAL), legal)
        // 其他分支...
        .build();
// 串行：先分类再路由
Untyped expertBot = AgenticServices.sequenceBuilder()
        .subAgents(classifier,router)
        .outputKey("resp")
        .build();
String ans = (String) expertBot.invoke(Map.of("request","我腿摔破了怎么办"));
```

## 四、Agent高级增强特性
### 4.1 Optional可选Agent：缺参自动跳过不报错
部分Agent参数可选，无对应Scope变量时跳过执行，避免`MissingArgumentException`
```java
AudienceEditor optionalEdit = AgenticServices.agentBuilder(AudienceEditor.class)
        .chatModel(BASE_MODEL)
        .optional(true) // 缺失audience参数自动跳过
        .build();
// 入参不带audience，audienceEdit直接跳过，执行剩余Agent
Map input = Map.of("topic","巨龙","style","奇幻");
```
注解写法：`@Agent(optional = true)`

### 4.2 Async异步Agent：不阻塞主线程，需要结果时阻塞等待
```java
FoodExpert asyncFood = AgenticServices.agentBuilder(FoodExpert.class)
        .chatModel(BASE_MODEL)
        .async(true) // 异步执行
        .build();
// 串行中两个异步Agent会并行跑，无需parallel
```

### 4.3 Streaming流式Agent（返回TokenStream，实时输出）
```java
public interface StreamWriter {
    @Agent
    @UserMessage("写故事{{topic}}")
    TokenStream write(@V("topic") String topic);
}
StreamWriter streamAgent = AgenticServices.agentBuilder(StreamWriter.class)
        .streamingChatModel(streamOpenAiModel) // 流式模型
        .outputKey("story")
        .build();
TokenStream stream = streamAgent.write("巨龙");
// 消费流式数据
stream.onNext(chunk->System.out.print(chunk));
```
> 流式仅最后一个Agent可以实时返回流，前置Agent会等全量生成再往下执行

### 4.4 动态选择ChatModel（运行时根据Scope切换模型）
```java
StoryEditor dynamicEditor = AgenticServices.agentBuilder(StoryEditor.class)
        // 根据打分动态选模型：高分用高精度GPT4，低分用廉价GPT3.5
        .chatModel(scope->{
            Double score = scope.readState("score",0.0);
            return score>0.8 ? PLANNER_MODEL : BASE_MODEL;
        })
        .build();
```

### 4.5 异常错误处理ErrorHandler（重试/兜底返回/抛异常三策略）
```java
UntypedAgent workflow = AgenticServices.sequenceBuilder()
        .subAgents(writer,styleEdit)
        .errorHandler(ctx->{
            // 缺失topic自动填充默认值并重试
            if(ctx.exception() instanceof MissingArgumentException ex && ex.argumentName().equals("topic")){
                ctx.agenticScope().writeState("topic","默认：巨龙传说");
                return ErrorRecoveryResult.retry();
            }
            return ErrorRecoveryResult.throwException(); // 默认抛出
        })
        .build();
```
三种恢复策略：
1. `throwException`：向上抛异常（默认）
2. `retry()`：修复Scope数据后重试
3. `result(obj)`：直接返回兜底结果，终止报错

### 4.6 可观测与AgentMonitor监控、HTML报表
内置`AgentMonitor`全链路埋点，自动记录耗时、token、入参出参，支持生成HTML可视化报告：
```java
AgentMonitor monitor = new AgentMonitor();
// 顶层工作流注册监听器，自动继承所有子Agent调用日志
UntypedAgent workflow = AgenticServices.sequenceBuilder()
        .subAgents(writer,loopAgent)
        .listener(monitor)
        .build();
// 执行后生成html报表
HtmlReportGenerator.generateReport(monitor, Path.of("./agent-exec.html"));
// 查看执行记录
MonitoredExecution exec = monitor.successfulExecutions().get(0);
System.out.println(exec);
```
快捷方式：接口继承`MonitoredAgent`，自动内置Monitor无需手动注册：
```java
public interface StyledWriter extends MonitoredAgent {
    @Agent String write(String topic,String style);
}
StyledWriter agent = AgenticServices.sequenceBuilder(StyledWriter.class).subAgents(...).build();
AgentMonitor monitor = agent.agentMonitor(); // 直接获取监控实例
```

## 五、声明式注解开发（告别Builder，注解定义工作流）
框架提供`@SequenceAgent/@ParallelAgent/@ConditionalAgent`等注解，直接在接口定义工作流，极简代码：
```java
// 并行工作流声明式定义
public interface EveningPlanner {
    @ParallelAgent(subAgents = {FoodExpert.class,MovieExpert.class},outputKey = "plans")
    List<EveningPlan> plan(@V("mood") String mood);
    // 自定义线程池
    @ParallelExecutor
    static Executor pool(){return Executors.newFixedThreadPool(2);}
    // 结果聚合逻辑
    @Output
    static List<EveningPlan> combine(@V("meals") List<String> meals,@V("movies") List<String> movies){
        // 同之前聚合代码
    }
    // 子Agent自定义模型
    @ChatModelSupplier
    static ChatModel foodModel(){return BASE_MODEL;}
}
// 一行创建Agent
EveningPlanner planner = AgenticServices.createAgenticSystem(EveningPlanner.class,BASE_MODEL);
```
条件路由注解：`@ConditionalAgent + @ActivationCondition`，无需手动构建conditionalBuilder。

## 六、Pure Agentic AI：Supervisor监督自主智能体（LLM动态调度）
> **区别固定Workflow**：不再硬编码执行顺序，Supervisor（大模型）理解用户需求，**自主拆解任务、动态选择子Agent、编排调用链路**，类似AutoGPT/ReAct智能体，是Pure Agent核心。

### 6.1 实战：银行转账Supervisor
需求：用户指令「把100欧元从Mario转到Georgios」，Supervisor自动：欧元→美元兑换→Mario扣款→Georgios入账
```java
// 1.工具类：银行账户操作、汇率转换
public class BankTool {
    Map<String,Double> account = new HashMap<>();
    @Tool("给用户账户存入USD") Double credit(@P("user")String user,@P("amt")Double num){
        account.put(user,account.getOrDefault(user,0.0)+num);
        return account.get(user);
    }
    @Tool("从用户账户扣除USD") Double withdraw(@P("user")String user,@P("amt")Double num){
        account.put(user,account.get(user)-num);
        return account.get(user);
    }
}
public class ExchangeTool{
    @Tool("币种换算") Double exchange(@P("ori")String ori,@P("tar")String tar,@P("amt")Double amt){
        if(ori.equals("EUR")&&tar.equals("USD")) return amt*1.15;
        return amt;
    }
}

// 2.三个业务Agent（绑定工具）
public interface WithdrawAgent{@Agent("美元取款") String withdraw(@V("user")String u,@V("amount")Double a);}
public interface CreditAgent{@Agent("美元存款") String credit(@V("user")String u,@V("amount")Double a);}
public interface ExchangeAgent{@Agent("币种转换") Double exchange(@V("ori")String o,@V("tar")String t,@V("amt")Double a);}

// 实例化Agent并绑定工具
BankTool bank = new BankTool();
bank.account.put("Mario",1000d);
bank.account.put("Georgios",1000d);
WithdrawAgent wd = AgenticServices.agentBuilder(WithdrawAgent.class).chatModel(BASE_MODEL).tools(bank).build();
CreditAgent cr = AgenticServices.agentBuilder(CreditAgent.class).chatModel(BASE_MODEL).tools(bank).build();
ExchangeAgent ex = AgenticServices.agentBuilder(ExchangeAgent.class).chatModel(BASE_MODEL).tools(new ExchangeTool()).build();

// 3.构建Supervisor
SupervisorAgent bankSuper = AgenticServices.supervisorBuilder()
        .chatModel(PLANNER_MODEL) // 规划用高精度模型
        .subAgents(wd,cr,ex) // 交给supervisor管理的子Agent
        .responseStrategy(SupervisorResponseStrategy.SUMMARY) // 返回任务摘要
        // 全局约束上下文：优先内部工具、仅USD结算
        .supervisorContext("规则：所有资金最终使用USD结算，优先内置工具，禁止外部接口")
        .build();

// 调用，supervisor自主规划执行链路
String res = bankSuper.invoke("将100欧元从Mario转账到Georgios");
```
### 6.2 Supervisor三大返回策略
```java
public enum SupervisorResponseStrategy{
    LAST, // 默认：返回最后一个子Agent输出
    SUMMARY, // 大模型总结全流程结果（转账场景首选）
    SCORED // 双结果打分择优返回：摘要/最后输出二选一
}
```
### 6.3 运行时动态传入约束上下文
```java
// 单次调用覆盖默认规则
Map input = Map.of("request","100EUR转Mario→Georgios",
        "supervisorContext","汇率固定1.2EUR=USD");
String res = (String) bankSuper.invoke(input);
```

## 七、拓展智能体：非AI Agent、人在回路、强类型TypedKey
### 7.1 非AI Agent（纯Java逻辑，无需LLM）
不需要大模型，直接Java执行业务（接口单@Agent方法即可）：
```java
// 非AI：REST接口查汇率
public class RestExchangeAgent{
    @Agent(outputKey = "result",description = "调用第三方接口汇率转换")
    public Double calc(@V("ori")String o,@V("tar")String t,@V("amt")Double a){
        // 调用http接口，无LLM
        return 1.15*a;
    }
}
// 直接放入supervisor子Agent列表
```
快捷工具`AgenticServices.agentAction()`：Scope数据预处理
```java
// 字符串分数转double
UntypedAgent castAgent = AgenticServices.agentAction(scope->{
    String strScore = scope.readState("score","0");
    scope.writeState("score",Double.parseDouble(strScore));
});
```

### 7.2 Human-In-The-Loop人在回路（运行中人工介入补全参数）
```java
// 人机交互Agent：控制台询问星座
HumanInTheLoop human = AgenticServices.humanInTheLoopBuilder()
        .outputKey("sign")
        .description("向用户询问星座")
        .responseProvider(scope->{
            String name = scope.readState("name","");
            System.out.printf("请输入%s的星座：",name);
            return new Scanner(System.in).nextLine();
        })
        .async(true) // 配置异步，不阻塞其他并行任务
        .build();
// 串行：先问星座再生成运势
UntypedAgent horoFlow = AgenticServices.sequenceBuilder()
        .subAgents(human,horoAgent)
        .outputKey("res")
        .build();
horoFlow.invoke(Map.of("name","马里奥"));
```

### 7.3 TypedKey强类型变量（告别字符串key拼写错误）
自定义`TypedKey`替代字符串key，编译期类型校验：
```java
// 定义强类型Key
static class UserReq implements TypedKey<String>{}
static class Category implements Typed<RequestCat>{
    @Override public RequestCat defaultValue(){return RequestCat.UNKNOWN;}
}
// Agent注解使用@K绑定
@UserMessage("分类{{UserReq}}")
@Agent(typedOutputKey = Category.class)
RequestCat classify(@K(UserReq.class) String req);
```

## 八、精通核心：自定义Planner自定义编排模式
框架所有内置工作流底层都是实现`Planner`接口，可自定义任意编排逻辑，四大官方扩展Planner：**GoalOriented目标驱动、P2P去中心化、Voting投票**。

### 8.1 GoalOriented目标驱动Planner（依赖图自动推导执行顺序）
**特点**：仅指定最终输出目标，框架根据各Agent输入输出依赖，自动推导最短执行链路，无需手动写顺序。
> 场景：输入prompt→提取人名→提取星座→生成运势→联网找故事→汇总文案，Goal=writeup，自动排序调用

### 8.2 P2P对等分布式Planner（去中心化，状态触发执行）
无中心调度，**Scope新增变量自动触发满足入参的Agent**，循环迭代直到分数达标退出（科研迭代场景）：
1. scope存入topic→文献Agent触发→生成researchFindings
2. findings入scope→假说Agent触发→hypothesis
3. hypothesis→评审Agent→critique，循环迭代打分≥0.85结束

### 8.3 Voting投票Planner（多Agent并行投票，结果聚合）
同任务多个Agent并行执行，通过**多数/平均值/最大值**聚合结果，适合风控、情感分类、审核场景：
```java
// 3个情感分类Agent并行投票，majority多数胜出
UntypedAgent voteAgent = AgenticServices.plannerBuilder()
        .subAgents(c1,c2,c3)
        .planner(() -> new VotingPlanner(VotingStrategy.majority()))
        .outputKey("result")
        .build();
```

## 九、高阶集成：A2A远程Agent + MCP工具Agent + Scope持久化
### 9.1 A2A远程Agent：调用跨服务远端Agent
```java
// 对接远端A2A服务
UntypedAgent remoteWriter = AgenticServices.a2aBuilder("http://xxx/a2a")
        .inputKeys("topic")
        .outputKey("story")
        .build();
```

### 9.2 MCP工具Agent：封装MCP协议工具为Agent
```java
McpClient client = new DefaultMcpClient.Builder().transport(...).build();
UntypedAgent mcpTool = McpAgent.builder(client).toolName("genStory").outputKey("story").build();
```

### 9.3 AgenticScope持久化与故障断点续跑
实现`AgenticScopeStore`SPI，自定义Redis/数据库存储Scope，服务宕机重启后从持久化数据恢复任务、继续执行未完成步骤（人在长审批流程必备）：
```java
// 全局配置持久化存储
AgenticScopePersister.setStore(new RedisScopeStore());
// 带@MemoryId的Agent自动落地Scope
public interface OrderAgent extends AgenticScopeAccess{
    @Agent String process(@MemoryId String orderId,@V("order")String detail);
}
```

## 十、生产实战：综合项目：智能客服工单系统
**需求**
1. 用户输入工单→分类（售后/咨询/投诉）
2. 咨询→知识库RAG回答；售后→查询订单工具；投诉→人工排队（人在回路）
3. 全链路日志监控，异常自动重试兜底
   **技术栈**：Conditional路由+Tool工具+HumanInLoop+AgentMonitor+Scope持久化
> 代码结构参考前文各模块组合，核心：分类Agent+三个业务Agent+条件路由+人工Agent+全局监控。

## 十一、最佳实践&避坑指南
### ✅ 最佳实践
1. **Agent拆分原则：单一职责**，一个Agent只做一件事，Supervisor按需组合
2. Supervisor规划优先使用GPT4/通义千问超大规模模型，小模型规划能力不足
3. 生产环境全部接入`AgentMonitor`+Html报表，便于问题排查
4. 长流程（人审批）必须开启Scope持久化，防止进程重启丢失进度
5. 批量任务优先ParallelMapper，IO密集Agent配置async异步

### ❌ 常见踩坑
1. 忘记`outputKey`：Agent结果无法存入Scope，下游拿不到数据
2. ParallelMapper子Agent配置ChatMemory：框架直接抛异常
3. Supervisor子Agent缺少`@Agent(description)`：大模型无法识别能力，调度失败
4. 无状态工作flow大量数据：Scope内存膨胀，大任务拆分多段执行

## 十二、学习进阶路线
1. **入门（1~3天）**：顺序/循环/并行基础工作流，独立定义单个Agent
2. **进阶（1周）**：条件+Mapper+异步/流式/异常/监控，声明式开发
3. **精通（2~3周）**：Supervisor自主Agent、自定义Planner、P2P/Voting/Goal模式
4. **生产（长期）**：A2A/MCP集成、Scope持久化、SpringBoot项目落地、国产大模型适配（通义/星火/文心）

