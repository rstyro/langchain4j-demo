# LangChain4j Demo

基于 LangChain4j 的大语言模型应用示例项目。

## 技术栈

- **Java**: 17
- **框架**: Spring Boot 4.0.6
- **LLM SDK**: LangChain4j 1.15.0
- **构建工具**: Maven

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 配置 API Key

在运行项目前，需要设置环境变量：

```bash
# Windows (PowerShell)
$env:AI_API_KEY="your-api-key"

# Linux/Mac
export AI_API_KEY="your-api-key"
```

### 运行项目

```bash
# 进入项目目录
cd langchain4j-demo

# 编译项目
mvn clean compile -DskipTests

# 运行 HelloWorld 示例
cd hello-world
mvn exec:java -Dexec.mainClass="top.lrshuai.langchain4j.HelloWorld"
```


## 模块说明

### hello-world

基础的 LLM 调用示例，演示如何使用 LangChain4j 调用大语言模型。

**核心代码示例**：

```java
OpenAiChatModel model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .baseUrl("https://ark.cn-beijing.volces.com/api/coding/v3")
        .modelName("deepseek-v3.2")
        .temperature(0.7)
        .logRequests(true)
        .logResponses(true)
        .build();

String answer = model.chat("你好呀，介绍一下你自己");
```


