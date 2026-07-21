# 5.0.0 Provider 配置

所有 Provider 都实现 `CatholicLLM`，因此应用可以在不改变上层请求与响应处理逻辑的
情况下切换实现。

| 服务 | 实现类 | 默认端点 | 流式 | 多模态 |
| --- | --- | --- | --- | --- |
| OpenAI Responses | `OpenAIResponsesLLM` | `https://api.openai.com/v1/responses` | 是 | 是 |
| OpenAI Chat Completions | `OpenAIChatCompletionsLLM` | `https://api.openai.com/v1/chat/completions` | 是 | 是 |
| Anthropic Messages | `AnthropicLLM` | `https://api.anthropic.com/v1/messages` | 是 | 是 |
| DashScope Text Generation | `DashScopeTextGenerationLLM` | `/services/aigc/text-generation/generation` | 是 | 文本 |
| DashScope Multimodal Generation | `DashScopeMultimodalGenerationLLM` | `/services/aigc/multimodal-generation/generation` | 是 | 是 |

“多模态”表示库可转换图片消息；实际支持范围仍取决于所选模型。

## OpenAI Responses

新接入 OpenAI 时优先评估 Responses API：

```java
CatholicLLM llm = OpenAIResponsesLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(apiKey)
    .build();
```

## OpenAI Chat Completions

兼容仍以 Chat Completions 暴露的服务或网关：

```java
CatholicLLM llm = OpenAIChatCompletionsLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(apiKey)
    .baseUrl("https://gateway.example.com/v1")
    .build();
```

默认认证是 `Authorization: Bearer ...`。兼容服务需要其他认证形式时可设置
`authMethod(...)`。

## Anthropic Messages

```java
CatholicLLM llm = AnthropicLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(apiKey)
    .anthropicVersion("2023-06-01")
    .build();
```

默认 API 版本为 `2023-06-01`，可按目标服务要求覆盖。

## DashScope

纯文本模型：

```java
CatholicLLM llm = DashScopeTextGenerationLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(apiKey)
    .build();
```

视觉模型使用 `DashScopeMultimodalGenerationLLM.builder()`，配置方式相同。

## Provider 无关参数

`CatholicLLMRequestOptions` 提供 `temperature`、`maxTokens`、`topP` 和 `stop`。
厂商专有参数可放入 `extra`：

```java
CatholicLLMRequestOptions options = CatholicLLMRequestOptions.builder()
    .putExtra("provider_specific_option", value)
    .build();
```

专有参数是否透传以及其语义由相应转换器和服务端决定。切换 Provider 时应重新核对
`model`、参数范围、工具调用和多模态支持。

