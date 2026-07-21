# 5.0.0 快速开始

## 环境与依赖

- Java 17 或更高版本；
- 应用已初始化 `Keel`；
- 复用一个由应用管理的 Vert.x `HttpClient`；
- 至少一个模型服务的 API Key。

Gradle Kotlin DSL：

```kotlin
dependencies {
    implementation("io.github.sinri:keel-aigc-api:5.0.0")
}
```

## 创建 Provider

```java
CatholicLLM llm = OpenAIResponsesLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();
```

Builder 必须设置 `keel`、`httpClient` 和非空 `apiKey`。私有网关或兼容服务可通过
`baseUrl(...)` 覆盖默认地址。

## 构造统一请求

```java
CatholicLLMRequestOptions options = CatholicLLMRequestOptions.builder()
    .temperature(0.2)
    .maxTokens(800)
    .build();

CatholicLLMRequest request = CatholicLLMRequest.builder()
    .model("gpt-4.1-mini")
    .addMessage(CatholicSystemMessage.of("回答要准确、简洁。"))
    .addMessage(CatholicUserMessage.ofText("解释什么是背压。"))
    .options(options)
    .build();
```

图片消息可使用 URL 或 Base64：

```java
CatholicUserMessage byUrl =
    CatholicUserMessage.ofTextAndImage("描述图片", "https://example.com/image.png");

CatholicUserMessage byBase64 =
    CatholicUserMessage.ofTextAndBase64Image("描述图片", "image/png", base64Data);
```

具体模型是否接受图片、参数范围及模型名由 Provider 服务决定。

## 发起调用

非流式：

```java
Future<CatholicLLMResponse> future = llm.call(request);

return future.onSuccess(response -> {
    if (response.hasText()) {
        System.out.println(response.text());
    }
    System.out.println(response.usage().totalTokens());
});
```

逐块消费流：

```java
return llm.callStream(request, chunk -> {
    if (chunk.hasDeltaText()) {
        System.out.print(chunk.deltaText());
    }
    return Future.succeededFuture();
});
```

聚合流：

```java
return llm.callStream(request)
    .onSuccess(response -> {
        System.out.println(response.text());
        if (!response.finished()) {
            System.err.println("流在完成标记前中断");
        }
    });
```

传给逐块处理器的 `Future<Void>` 会参与背压和错误传播：应在异步处理真正完成后再令
Future 成功；处理失败则令 Future 失败。

## 错误处理

Provider 对非 2xx 响应、网络失败、无效响应或流处理失败返回失败的 `Future`。API Key
不应写入源码或日志；推荐由环境变量或应用的密钥管理设施注入。

下一步可查看 [Provider 配置](providers.md) 或 [Agent 指南](agent.md)。
