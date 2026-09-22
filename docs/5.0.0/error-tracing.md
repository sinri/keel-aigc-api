# Agent 失败现场 Trace

Trace 默认关闭。配置后，每次 Agent 交互在有界内存中暂存证据；正常完成即释放，异常或轮次耗尽时异步保存。无需预先打开 DEBUG。LLM 调用成功不会提前清除本轮证据，工具参数解析随后失败时仍可调查上游响应。

## 接入

Recorder 和 Sink 在应用启动时创建并复用，不要每次请求创建线程池：

```java
var recorder = new CatholicTraceRecorder(
    new CatholicFileTraceSink(Path.of("runtime/agent-traces"))
);

var agent = CatholicAgent.builder()
    .llm(llm)
    .model(model)
    .tools(toolHandler.getRegisteredToolDefinitions())
    .toolHandler(toolHandler)
    .traceRecorder(recorder)
    .build();

// 自动管理 Session；失败摘要和保存结果默认通过 System.Logger 输出 trace_id。
return agent.interact(userText);
```

业务需要候选标识、提前取得 traceId 时，显式创建 Session：

```java
var session = recorder.start(Map.of(
    "flow", "rc23judge",
    "candidateId", candidateId,
    "roundLabel", "K"
));

return agent.interact(userText, session).onFailure(cause ->
    System.getLogger("issue-dialog").log(System.Logger.Level.ERROR,
        "Round-K agent failed, trace_id=" + session.traceId(), cause)
);
```

`CatholicRequiredToolAgent.interact(userText, session)` 同样可用。默认允许 `flow`、`candidateId`、`roundLabel` 三种 tag，最多 16 项，键和值分别限制为 64、128 个 UTF-16 单元。只传业务标识，不传完整候选正文。

Agent 管理传入 Session 的成功/失败收尾。一个 Session 只能用于一次交互；不要在异步调用返回后立即用 try-with-resources 关闭它，否则会主动丢弃现场。

业务失败 Future 不等待写盘，保留原始异常类型与 cause。`session.persistence()` 返回只读 CompletionStage：保存成功得到位置，正常成功丢弃得到 null，保存失败/过期/取消/无法采集时异常完成。必要时可在应用层设置有界异步等待，不在 Vert.x event loop 上调用 join/get。

```text
Agent failed trace_id=... stage=TOOL_ARGUMENT_PARSE capture_status=queued
Trace saved trace_id=... location=...
```

`queued` 不表示已经落盘。队列满、落盘失败会输出限频告警，可查询 `captureDroppedCount()`、`saveFailureCount()`。自定义状态回调必须快速返回；回调失败不会替代业务异常。

## 证据与内容策略

内置五种 Provider 均支持上下文传播：Chat Completions、Responses、Anthropic Messages、DashScope Text Generation 和 Multimodal Generation。采集请求、响应元数据、切分前 Buffer、SSE、转换后的工具参数 delta、聚合长度与最终参数、工具解析及执行状态。

关联字段包括 trace ID、LLM 轮次、exchange ID、事件/片段序号、工具索引和工具调用 ID。`response_built` 与工具事件中的 response ID、轮次把聚合结果和本地执行关联起来。`provider_terminal` 保留 Provider 的结束原因/状态。

默认 `STRUCTURE`：

- 不持久化原始 prompt、工具结果、原始 SSE 或传输 Buffer，只保留长度、元数据及带密钥摘要。
- 参数保存保守结构副本：只保留 `{ } [ ] , : " \` 和空格、制表符、换行，其他每个 UTF-16 单元变为 `x`。不依赖 JSON 能成功解析；独立片段遮蔽与合并后遮蔽一致，因此跨片段的秘密也不会原样保留。
- 这是原始长度/位置保持的字符掩码，不是 JSON token 解析，也不保证输出仍是合法 JSON。可能保留字符串内部标点等结构信息。不能用于精确重放。
- 异常记录类型、有限 cause 链、有限堆栈和消息中的行列位置，不保存可能含业务内容的异常消息。

原文取证必须显式选择：

```java
var recorder = new CatholicTraceRecorder(
    new CatholicFileTraceSink(Path.of("runtime/private-agent-traces")),
    CatholicTraceRecorder.Limits.defaults(),
    CatholicTraceRecorder.ContentMode.RESTRICTED_RAW,
    Set.of("flow", "candidateId", "roundLabel"),
    message -> System.getLogger("agent.trace").log(System.Logger.Level.INFO, message)
);
```

`RESTRICTED_RAW` 会保存范围内的原始业务载荷。认证请求头从不进入 recorder，endpoint 去除 user-info、query 和 fragment，响应头仅保留 request ID / content-type。它不是对任意业务正文的脱敏模式；敏感目录、存储加密和访问控制由部署方配置。POSIX 文件 Sink 使用目录 0700、文件 0600；建议使用专用目录。

摘要是使用 recorder 生命周期内随机密钥的 HMAC-SHA256，密钥不写盘，避免低熵内容被裸哈希猜测。摘要可以在同一 recorder 的记录之间比较，不能跨重启重新计算。截断时标明 `digest_scope=head_only`，并保存独立尾部摘要，不冒充完整内容的摘要。

## 预算、丢失与保留

默认限制：单 run 2 MiB、全局 64 MiB、每 run 最多 1024 个事件、每个载荷最多 16384 个 UTF-16 单元、活跃 run 最多 128、写入等待队列最多 32 条、执行中和排队快照共最多 32 MiB、活跃现场 TTL 15 分钟。

预算按保守对象/字符串开销收费，`retainedBytes()` 是 recorder 的计费值，不是 JVM 实际 heap 指标。全局预算包含活跃、冻结与排队数据；序列化/脱敏空间在单事件计费中预留，sink 在 `save` 返回后自行保留的对象不再由 recorder 管理。

超限时淘汰旧事件或拒绝新事件；单个大载荷保留首尾并标注偏移。切分前 Buffer 最多复制前 8192 字节，记录完整字节长度、样本 CR/LF 数及是否截断。元数据超长也标为截断。字段 `dropped_events`、`truncated`、`capture_capability`、`evidence_complete` 明确说明证据是否齐全，丢失不会伪装为完整现场。

FIFO 淘汰优先保留最新轮次，但预算特别小时，失败事件也可能挤掉参数事件；顶层失败阶段/类型仍在。可据实际请求长度调大预算。结构模式即使 `evidence_complete=true`，也只表示采集范围未丢失，不表示保存了原文。

文件 Sink 用临时文件加原子移动发布，默认最多 1 GiB、10000 个文件、保留 7 天。在保存前和 recorder 每分钟的后台维护中回收。异步写入不能保证进程崩溃时保存成功；崩溃遗留的 `.trace-*.tmp` 文件不作为完整 trace，需部署方清理。自定义 sink 的阻塞操作必须有超时，否则只能保证队列有界，不能保证正常 drain 完成。

TTL、显式 `session.close()`、recorder 关闭会释放未完成的采集，不取消业务调用。应用关闭流程：

```java
recorder.close(); // 停止接收，释放活跃现场，排队写入继续执行
// 只在关闭流程的普通线程中等待：
boolean drained = recorder.awaitClosed(Duration.ofSeconds(5));
```

## 独立 LLM 调用及兼容性

独立调用显式传递 Context，并用 Session 管理生命周期：

```java
var session = recorder.start(Map.of("flow", "standalone"));
return session.track(llm.callStream(request, session.context()));
```

若返回响应后还要解析参数，把后处理也放进 `session.track(...)` 包裹的 Future 链，或自行调用 `session.fail(stage, cause)` / `session.succeed()`。HTTP 成功不等于应用处理成功。

已有 LLM/Handler/Observer 签名保留；新 LLM 重载通过携带上下文的请求适配器调用旧方法。自定义 LLM 默认 `supportsTrace=false`，标为 partial；接入原生采集点后才应声明支持。已有 Native Handler 子类的旧 protected hook 保持分派，子类走 partial capture；需要完整解析阶段跟踪时覆盖带 TraceContext 的 hook。上下文不写进 Provider JSON，不依赖 ThreadLocal 或 MDC。

## 离线重放

```java
JsonObject trace = new JsonObject(Files.readString(traceFile));
JsonObject result = CatholicTraceReplay.replayStream(trace, exchangeId);
```

只接受 schema version 1、完整的 RESTRICTED_RAW 流式证据，要求传输序号连续、长度匹配、HTTP EOF 已到达。仅调用本地 SSE handler、聚合器和 JSON 解析器，不创建 HTTP 客户端、不执行 NativeFunctionAdapter。结果给出参数是否合法，并与已记录聚合参数对照。

重放使用当前运行库的解析代码，复现原版本问题应使用相同制品版本。当前保留生产代码的 LF 分帧语义，因此 Issue #14 的 CRLF 输入会重现零片段及待处理尾部，而不会被重放器悄悄修正。该接口不提供非流式响应的精确重放。
