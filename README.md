# Keel AIGC API

Keel AIGC API 是基于 Java 17、Vert.x 与 Keel 的异步大模型接入库。它用统一的
`CatholicLLM` 请求、响应、消息和工具定义屏蔽不同厂商协议，并提供轻量的 Agent
工具循环与 Agent Skills 渐进披露能力。

当前开发版本：`5.0.0-SNAPSHOT`。

## 能力概览

| 能力 | 实现 |
| --- | --- |
| OpenAI | Chat Completions、Responses |
| Anthropic | Messages |
| DashScope | Text Generation、Multimodal Generation |
| 调用方式 | 非流式、流式回调、流式聚合 |
| 消息 | 文本、图片 URL、Base64 图片 |
| 工具 | 通用函数定义、工具调用与结果 |
| Agent | 多轮工具执行、Observer、首轮必调工具、Agent Skills |

## 安装

正式版可从 Maven Central 获取：

```kotlin
dependencies {
    implementation("io.github.sinri:keel-aigc-api:<version>")
}
```

```xml
<dependency>
  <groupId>io.github.sinri</groupId>
  <artifactId>keel-aigc-api</artifactId>
  <version>${keel-aigc-api.version}</version>
</dependency>
```

`SNAPSHOT` 和候选版本是否可用取决于项目配置的制品仓库。5.0.0 要求 Java 17；模块化
项目可声明：

```java
requires io.github.sinri.keel.integration.llm.api;
```

## 快速开始

下面以 OpenAI Responses API 为例。`keel` 和 `httpClient` 由应用统一创建并管理，
不要为每次请求重复创建：

```java
CatholicLLM llm = OpenAIResponsesLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build();

CatholicLLMRequest request = CatholicLLMRequest.builder()
    .model("gpt-4.1-mini")
    .addMessage(CatholicUserMessage.ofText("用一句话解释事件循环"))
    .build();

return llm.call(request)
    .onSuccess(response -> System.out.println(response.text()));
```

流式调用既可以逐块消费，也可以由库聚合为完整响应：

```java
return llm.callStream(request, chunk -> {
    if (chunk.hasDeltaText()) {
        System.out.print(chunk.deltaText());
    }
    return Future.succeededFuture();
});

// 或：Future<CatholicLLMResponse> response = llm.callStream(request);
```

## 文档

- [版本文档索引](docs/README.md)
- [5.0.0 使用指南](docs/5.0.0/README.md)
- [5.0.0 快速开始](docs/5.0.0/getting-started.md)
- [5.0.0 Provider 配置](docs/5.0.0/providers.md)
- [5.0.0 Agent 指南](docs/5.0.0/agent.md)
- [迁移到 5.0.0](docs/5.0.0/migration.md)
- [源码 Javadoc](https://javadoc.io/doc/io.github.sinri/keel-aigc-api)

版本文档描述对应主版本的最新 API。使用固定版本时，请从相应 Git tag 查看文档，
避免开发分支文档与已发布制品不一致。

## 构建与测试

项目使用 Gradle Wrapper：

```shell
./gradlew test
./gradlew build
./gradlew javadoc
```

## License

[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)
