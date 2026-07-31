# LLM 协议兼容性与易用性优化计划

## 1. 目标与边界

本计划用于把当前的“标准协议可用”提升为“标准协议稳定、常见兼容变体可配置、
供应商扩展不丢失、接入问题可诊断”。

支持范围分为三层：

1. **标准协议**：OpenAI Chat Completions、OpenAI Responses、Anthropic Messages、
   WHATWG Server-Sent Events。
2. **已验证变体**：阿里云百炼 OpenAI 兼容接口、DashScope 原生接口、火山引擎方舟
   Chat Completions。
3. **可扩展变体**：Azure 类部署路径、企业网关、自定义鉴权头、查询参数和签名鉴权。
   这类变体通过扩展点支持，不在核心代码中不断增加厂商条件分支。

“兼容”必须由离线协议样本测试或供应商集成测试证明；只允许设置 `baseUrl` 或能透传
某个参数，不视为已经兼容。

## 2. 信息来源政策

协议行为只引用以下可信来源：

- 正式标准，例如 WHATWG HTML 标准；
- 服务商官方 API Reference 或官方帮助中心；
- 本仓库中由上述资料固化并经过测试的协议样本。

博客、论坛、第三方 SDK 行为只能用于发现问题，不能作为实现或 Javadoc 的唯一依据。
官方资料发生变化时，先更新协议样本和本文的“来源矩阵”，再修改实现。

### 来源矩阵

以下链接已于 **2026-07-31** 核对；日期表示文档可访问且相关事实仍存在，不代表厂商
承诺接口永久不变。

| 协议/能力 | 规范性来源 | 本计划采用的事实 |
| --- | --- | --- |
| SSE | [WHATWG Server-Sent Events](https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream) | 按事件而非物理行解析；`data:` 后的一个空格可选；多条 `data` 字段以换行拼接 |
| OpenAI Chat | [OpenAI Chat API Reference](https://developers.openai.com/api/reference/resources/chat) | 响应包含 choices、finish reason、message、usage；`n` 可产生多个 choice |
| OpenAI Responses 流 | [OpenAI Responses streaming events](https://platform.openai.com/docs/api-reference/responses-streaming) | 流由带 `type` 的具名事件组成，包含 completed、failed、incomplete 等终态 |
| 阿里云百炼 OpenAI Chat | [百炼 OpenAI Chat API](https://help.aliyun.com/zh/model-studio/qwen-api-via-openai-chat-completions) | 使用 OpenAI 兼容消息、工具与流式结构，但能力依模型而异 |
| 阿里云深度思考 | [百炼深度思考](https://help.aliyun.com/zh/model-studio/deep-thinking) | Chat Completions 通过 `reasoning_content` 返回思考内容，并支持 `enable_thinking` 等扩展 |
| 阿里云 Responses | [百炼 Responses API](https://help.aliyun.com/zh/model-studio/qwen-api-via-openai-responses) | reasoning 是可回传的输出项，另有非 OpenAI 标准扩展参数 |
| DashScope 原生 | [DashScope API Reference](https://help.aliyun.com/en/model-studio/qwen-api-via-dashscope) | 请求包装在 `input`/`parameters`，流式响应包含 DashScope 事件字段和 token 用量 |
| 火山方舟 Chat | [方舟 ChatCompletions API](https://api.volcengine.com/api-docs/view?action=ChatCompletions&serviceCode=ark&version=2024-01-01) | Bearer API Key、`/api/v3/chat/completions`、SSE `[DONE]`、`thinking`、`reasoning_content` 和 `stream_options` |
| Anthropic Messages | [Anthropic Messages API](https://platform.claude.com/docs/en/api/messages) | Messages 的请求、内容块、版本头、工具和 token 用量语义 |
| Anthropic 流 | [Anthropic streaming Messages](https://platform.claude.com/docs/en/build-with-claude/streaming) | 流由 `message_*`、`content_block_*`、`ping`、`error` 等事件组成 |

## 3. 设计原则

### 3.1 统一语义，不统一丢失

`CatholicLLMResponse` 保留跨供应商稳定的便捷字段，同时提供结构化内容项和只读扩展
数据。不能为了得到一个 `String text()` 而静默丢弃 reasoning、拒答、引用、finish
reason、token 明细或供应商请求 ID。

### 3.2 协议与传输解耦

Provider 负责请求/响应映射；HTTP endpoint、header、query、鉴权和请求拦截由共享传输
配置负责。Provider 不应通过枚举穷举所有云厂商鉴权。

### 3.3 严格输出，宽容输入

请求生成应稳定且可验证；响应解析应接受标准允许的等价表示，例如 `data:x` 与
`data: x`，但不能吞掉无法识别的终态或错误事件。

### 3.4 预设只是配置

`dashScopeCompatible()`、`volcArk()` 等预设只填充官方 endpoint、鉴权和协议能力，
底层仍使用同一套公开配置对象。预设不得形成独立、难以测试的 Provider 分支。

## 4. 分阶段实施

## P0：建立协议基线和防回归测试

### P0.1 协议样本目录

新增 `src/test/resources/llm-contracts/`，按以下层级保存去敏后的官方响应样本：

```text
openai/chat-completions/
openai/responses/
aliyun/openai-chat/
aliyun/dashscope/
volcengine/ark-chat/
anthropic/messages/
sse/
```

每个样本旁保存 `source.json`，至少包含官方 URL、采集/核对日期、协议类型、是否经过
人工裁剪。不得保存 API Key、用户 prompt 或供应商签名。

### P0.2 契约测试矩阵

必须覆盖：

- 非流式文本、图片、工具调用；
- 流式文本、多工具并行及 arguments 分片；
- `reasoning_content` 与普通 `content` 交错或分阶段出现；
- usage 独立尾帧和空 `choices`；
- `data:{...}`、`data: {...}`、CRLF、多 `data` 行、注释、`event` 和无最终空行；
- `[DONE]`、finish reason、failed/incomplete/error、连接提前结束；
- 缺少可选 `id`、未知字段、未知事件类型；
- `extra` 不能意外覆盖框架保护字段。

### P0.3 完成标准

- 所有样本可离线执行；
- 每个“已验证变体”至少有请求序列化、非流响应和流响应测试；
- 测试名称直接表达供应商和协议行为，不能只命名为 `testCompatibility`。

## P1：规范化 SSE 解析

### P1.1 新增共享事件模型

新增：

- `SseEvent`：`event`、`data`、`id`、`retry`；
- `SseEventParser`：严格实现 WHATWG 行解析和事件派发；
- `SseProtocolException`：包含 provider、事件类型、序号和去敏后的片段摘要。

`SSE2Chunk.processSSEStream` 改为输出 `SseEvent`。OpenAI Chat、Responses、Anthropic 和
DashScope handler 消费事件，不再分别判断字符串是否以 `"data: "` 开头。

### P1.2 结束语义

流式聚合结果必须区分：

- `COMPLETED`：收到协议终态；
- `INCOMPLETE`：供应商明确返回不完整；
- `FAILED`：收到错误事件；
- `TRUNCATED`：连接结束但未收到终态。

保留现有 `finished()` 作为兼容便捷方法，但新增明确的 `status()`。

### P1.3 `@see`

`SseEvent`、`SseEventParser`、`SSE2Chunk.processSSEStream` 及所有直接解析 SSE 字段的
方法必须包含：

```java
@see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html#parsing-an-event-stream">
    WHATWG HTML: Parsing an event stream</a>
```

Provider handler 还应链接对应供应商的流式协议页，不能只链接 WHATWG。

## P2：扩展统一请求与响应语义

### P2.1 内容模型

将 assistant 输出扩展为有序内容项：

- `CatholicTextContent`
- `CatholicReasoningContent`
- `CatholicRefusalContent`
- `CatholicToolCallContent`
- 后续可增加 citation、file、audio 等内容

`CatholicAssistantMessage.text()` 保留并聚合文本项；新增 `reasoningText()` 作为便捷
访问器。原始 reasoning 若供应商只允许回传 opaque item，应保留其 ID 和扩展字段，
不能伪造为普通文本。

### P2.2 响应元数据

`CatholicLLMResponse` 和 chunk 增加：

- `status`、`finishReason`；
- 只读 `metadata`/`extensions`；
- token usage details，包括 cached/reasoning token；
- provider request ID；
- 可选 raw response，仅能通过显式诊断配置启用。

默认 API 仍返回首个候选；如果请求设置 `n > 1`，必须返回 choices 集合或在调用前拒绝，
不能静默丢弃其余候选。

### P2.3 请求参数

把高频跨供应商能力做成类型化选项：

- `maxOutputTokens`
- `responseFormat`
- `toolChoice`
- `parallelToolCalls`
- `streamUsage`
- `reasoning`

供应商专有参数继续放在 extensions 中。框架字段 `model`、`messages/input`、`tools` 和
`stream` 默认受保护；只有显式的 raw request customizer 可以覆盖。

### P2.4 `@see`

以下类和转换方法必须同时链接“定义公共语义的标准来源”和“实现映射的供应商来源”：

| 目标 | 必需 `@see` |
| --- | --- |
| `CatholicReasoningContent`、reasoning options | OpenAI Responses、百炼深度思考、方舟 Chat |
| `OpenAIChatCompletionsRequestConverter.convert` | OpenAI Chat Reference；涉及百炼/方舟扩展的方法另链官方供应商页 |
| `OpenAIChatCompletionsResponseConverter.convert` | OpenAI Chat Reference、百炼深度思考、方舟 Chat |
| `OpenAIResponsesRequestConverter.convert` 和 response/stream converter | OpenAI Responses Reference；百炼扩展映射另链百炼 Responses |
| DashScope request/response/stream converter | DashScope API Reference |
| Anthropic request/response/stream converter | Anthropic Messages 和 streaming Messages |

类级 `@see` 描述协议整体，字段或方法级 `@see` 指向定义该特殊行为的精确页面。不要在
每个普通 getter 上复制同一链接。

## P3：可组合 HTTP 传输配置

### P3.1 新增配置对象

用不可变 `LLMHttpTransportConfig` 取代 Provider 内散落的 `baseUrl + path` 和
`AuthMethod` 判断，至少包括：

- `endpoint` 或 `EndpointResolver`；
- default headers；
- query parameters；
- `LLMAuthentication`；
- `RequestCustomizer`；
- connect、request、idle timeout；
- 最大错误响应体；
- proxy/TLS 继续复用 Vert.x `HttpClient` 配置。

认证实现至少提供：

- Bearer；
- 固定 header API Key；
- NoAuth；
- callback/signer。

callback/signer 负责 AK/SK 或临时凭据，不在核心库内实现厂商签名算法。

### P3.2 URL 规则

- `baseUrl` 兼容方法保留，但在构建时规范化末尾 `/`；
- 新 API 接受完整 endpoint，不能再次追加固定 path；
- query 使用 URI builder 合并，禁止字符串拼接；
- observer 记录最终 endpoint，但默认去除敏感 query。

### P3.3 `@see`

供应商预设类必须链接其官方 endpoint/鉴权文档。通用 transport 类只链接 HTTP/SSE
标准，不把某个供应商规则写成通用事实。签名 callback 接口的 Javadoc 要明确核心库
不保证任何具体签名算法。

## P4：供应商预设与能力发现

新增 `LLMProviderProfile`：

```java
OpenAIChatCompletionsLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .profile(LLMProviderProfiles.volcArk(apiKey))
    .build();
```

首批预设：

- OpenAI；
- 阿里云百炼 OpenAI-compatible（地域/Workspace endpoint）；
- 火山方舟；
- DashScope 原生；
- Anthropic。

profile 提供 `LLMCapabilities`，包括协议、多模态类型、tools、reasoning、structured
output、stream usage。能力来自官方文档，但应以保守默认值表达；随模型变化的能力允许
用户覆盖，不能硬编码成永久事实。

错误信息应包含：

- 最终 provider/profile 名；
- 使用的协议；
- endpoint（去敏）；
- 不支持能力及替代配置；
- provider request ID；
- HTTP 状态和结构化错误码。

所有 profile 工厂方法必须通过 `@see` 链接其 endpoint 和协议官方页面，并在 Javadoc
注明“核对日期”和可能随地域、模型变化的部分。

## P5：文档、迁移和发布

更新：

- `providers.md`：增加 profile 使用示例、兼容矩阵、reasoning 和 raw extensions；
- `migration.md`：说明 `AuthMethod`、`baseUrl` 和旧 response API 的迁移；
- API Javadoc：公开扩展点、线程安全性、敏感信息处理；
- release notes：明确二进制/源码兼容性和弃用周期。

旧接口至少保留一个次版本并标为 `@Deprecated`，其 Javadoc 使用 `@see` 指向替代类型或
方法，例如：

```java
@deprecated 使用 {@link LLMHttpTransportConfig} 配置认证与 endpoint。
@see LLMHttpTransportConfig
```

## 5. 推荐提交拆分

1. `test(llm): add provider contract fixtures`
2. `refactor(sse): parse WHATWG events centrally`
3. `feat(llm): preserve reasoning and response metadata`
4. `feat(llm): add composable HTTP transport configuration`
5. `feat(llm): add verified provider profiles`
6. `docs(llm): publish compatibility matrix and migration guide`

每个提交都应能独立编译和执行相关测试。不要把公共 API 重构、供应商 profile 和文档
一次性混在同一提交中。

## 6. 发布门槛

达到以下条件才可以宣称某供应商“兼容”：

- 来源矩阵存在仍可访问的官方链接和最近核对日期；
- 请求、非流式响应、流式响应均有离线契约测试；
- tools/reasoning/multimodal 等宣称支持的能力分别有测试；
- 未识别字段保留在 extensions，未识别错误或终态不会被吞掉；
- `./gradlew test` 和 `./gradlew javadoc` 通过；
- 所有新增协议类、转换入口和特殊字段映射方法均有对应 `@see`；
- 至少一次使用真实供应商凭据的可选集成测试通过，测试结果只记录时间、模型、地域和
  request ID，不提交凭据或完整内容。

## 7. 优先级结论

实施顺序必须是 **契约样本 → SSE → 响应无损化 → HTTP 扩展点 → profile**。
如果先增加 profile，当前的 reasoning 丢失和严格 SSE 行解析会被包装成看似易用但不
可靠的接口，无法满足“已验证兼容”的发布门槛。
