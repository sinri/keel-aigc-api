# Catholic Agent

本包在 `CatholicLLM` 之上提供通用、流式 LLM 的 Agent 执行框架。它面向一次用户交互完成以下流程：

```text
用户需求
  → LLM 分析
  → Observer 判断任务已完成或需要继续
  → 必要时顺序执行工具，或追加机制观察消息
  → 再次交给 LLM
  → 完成，或达到 LLM 轮次上限后强制中断
  → 返回包含终止原因和完整 transcript 的结果
```

`CatholicAgent` 是无会话状态的配置和执行器。每次 `interact(...)` 都会建立独立 transcript，不会继承其他交互的消息。同一个 Agent 可以用于彼此独立的请求；并发使用时，其所依赖的 LLM、Observer 和工具 handler 也必须支持并发。跨用户交互的长期会话历史应由上层管理，并可通过 `interact(priorMessages, userMessage)` 按次传入。消息顺序为 Builder 配置的 system prompt、`priorMessages`、当前用户消息。

## 基本使用

没有配置工具时，默认 Observer 会在 LLM 返回文本结果后结束交互：

```java
CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .systemPrompt("You are a helpful assistant.")
    .maxRounds(8)
    .build();

return agent.interact("分析这份需求")
    .compose(result -> {
        if (result.completed()) {
            @Nullable String text = result.text(); // 例如模型只返回非文本内容时为 null
            // 将结果返回给用户
        } else {
            // ROUND_LIMIT_EXCEEDED：可向用户展示最后一轮输出或记录 transcript
        }
        return Future.succeededFuture();
    });
```

`maxRounds` 限制一次交互最多发起的 LLM 请求次数，最小值为 1。达到上限时，如果 Observer 仍要求继续，框架不会再执行下一批工具或发起下一次 LLM 请求，而会返回 `ROUND_LIMIT_EXCEEDED`。

## 交互结果

`interact(...)` 返回 `Future<CatholicAgentResult>`。结果包含：

- `termination()`：`COMPLETED` 或 `ROUND_LIMIT_EXCEEDED`；
- `completed()`：是否正常完成；
- `lastResponse()`：最后一轮非空 LLM 响应；
- `text()`：最后一轮响应的可空文本；
- `transcript()`：本次交互的只读完整消息快照；
- `llmRounds()`：已完成的 LLM 请求轮数；
- `toolRounds()`：已执行的工具调用批次数；
- `maxRounds()`：本次 Agent 配置的 LLM 轮次上限。

轮次超限是正常且可观察的终止状态，因此 `interact(...)` 会成功返回结果。LLM 调用失败、Observer 失败、工具失败或违反执行契约时，返回的 `Future` 失败。

## 工具调用

工具定义和工具执行器必须成对配置：

```java
CatholicToolInvocationHandler handler = toolCall -> {
    return queryWeather(toolCall.parseArguments())
        .map(JsonObject::encode);
};

CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .tools(toolDefinitions)
    .toolHandler(handler)
    .maxRounds(8)
    .build();
```

默认 Observer 的行为是：

- LLM 响应包含工具调用：返回“继续”；
- LLM 响应不包含工具调用：返回“完成”。

一次响应包含多个工具调用时，框架按模型给出的顺序逐一执行。每个结果都会转换成 `CatholicToolCallMessage` 写入本次 transcript，然后进入下一轮 LLM 分析。一批工具调用计为一个 `toolRounds`。

配置了非空工具列表却没有配置 `toolHandler` 时，`build()` 会失败。工具处理器必须返回非空的 `Future<String>` 和非空结果字符串。

### NativeFunctionAdapter 注册器

已有 `NativeFunctionAdapter` 时，可以使用内置注册器同时生成工具定义和路由工具调用：

```java
var handler = CatholicToolInvocationHandler.createWithNativeFunctionAdapters();
handler.registerNativeFunctionAdapter(new QueryWeatherFunction());

CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .tools(handler.getRegisteredToolDefinitions())
    .toolHandler(handler)
    .build();
```

模型请求未注册的函数时，注册器会返回包含函数名的失败 `Future`，而不是产生空指针异常。

### 工具执行记录与审计

任意工具处理器都可以通过 `CatholicToolInvocationObserver` 记录执行开始、成功结果、失败原因
和耗时。每次执行会生成独立的 `invocationId`，并保留模型提供的 `toolCallId`、函数名和原始
参数用于关联审计记录：

```java
var auditLogger = LoggerFactory.getShared().createLogger("agent.tool.audit");
var toolObserver = new LoggingCatholicToolInvocationObserver(auditLogger);

CatholicToolInvocationHandler observedHandler =
    handler.observedBy(toolObserver);
```

使用 `NativeFunctionAdapter` 注册器时也可以在创建时直接配置：

```java
var handler =
    CatholicToolInvocationHandler.createWithNativeFunctionAdapters(toolObserver);
```

默认日志实现使用结构化 `LogContext`，参数、结果和异常消息在写入 context 前会脱敏。执行开始
和成功使用 `INFO`，失败使用 `WARNING`。Observer 采用 fail-open 语义，审计 Logger 故障
不会改变工具调用结果；要求审计失败时禁止执行工具的场景，应由应用提供显式的 fail-closed
包装器。

## 自定义观察机制

`CatholicAgentObserver` 在每一轮 LLM 响应进入 transcript 后执行。它可以根据响应内容、完整 transcript 和当前轮数决定完成或继续：

```java
CatholicAgentObserver observer = context -> {
    if (isTaskComplete(context.response())) {
        return Future.succeededFuture(CatholicAgentDirective.complete());
    }

    if (context.response().hasToolCalls()) {
        return Future.succeededFuture(CatholicAgentDirective.continueExecution());
    }

    return Future.succeededFuture(CatholicAgentDirective.continueWith(
        CatholicUserMessage.ofText("观察结果：答案缺少风险分析，请继续完善。")
    ));
};

CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .observer(observer)
    .maxRounds(4)
    .build();
```

可用指令如下：

- `CatholicAgentDirective.complete()`：任务完成，立即返回；
- `continueExecution()`：继续执行当前响应里的工具调用；
- `continueWith(message)`：追加一条机制观察消息后继续。

如果当前响应没有工具调用，Observer 又要求继续，则必须通过 `continueWith(...)` 提供观察消息。否则下一轮没有任何新增上下文，框架会以失败 `Future` 报告非法指令。

Observer 返回完成时，即使响应包含工具调用，工具也不会执行。业务 Observer 因此应明确决定工具调用是否仍有必要。

## Agent Skills

可通过 `CatholicSkillProvider` 为 Agent 提供符合 [Agent Skills](https://agentskills.io/specification)
规范的 Skill：

```java
CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .skillProvider(skillProvider)
    .build();
```

框架采用渐进披露：每次交互开始时调用 `getSkillCandidates()`，只把候选 Skill 的
`name` 和 `description` 放入本次 system context；存在候选时才注册内置
`activate_skill` 工具，且其 `name` 参数枚举限定为本次候选名称。模型调用该工具后，
框架再通过 `loadSkillByName(...)` 加载完整正文，并将 `instructions()` 作为工具结果送回下一轮。
同一次 `interact(...)` 内已成功激活的 Skill 会被记录；模型重复激活时不会再次访问 Provider
或重复注入正文，只会收到“已在当前上下文中”的简短工具结果。该记录不会跨交互共享。

`CatholicSkillFrontmatter` 对应规范的 frontmatter：`name`、`description` 为必填，
`license`、`compatibility`、`metadata`、`allowedTools` 为可选；其中 `allowed-tools`
仍是实验字段，框架目前仅表达和披露其值，不据此绕过应用自身的权限控制。

候选为空时不会注入空目录或注册激活工具。候选名称重复、字段违反规范、Provider 返回
null、加载结果名称不一致或正文为空时，当前交互会失败。`activate_skill` 是保留函数名，
不能作为业务工具注册。Skill 中引用的 scripts、references、assets 仍需由应用已有工具
按 Skill 指令按需读取或执行；CatholicAgent 本身不隐式取得文件系统或命令执行能力。

## 首轮强制调用指定工具

“本次交互的首次 LLM 请求必须选择某个函数”属于特殊策略，不由通用 `CatholicAgent` 保存状态。使用独立的 `CatholicRequiredToolAgent`：

```java
CatholicAgent baseAgent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .tools(handler.getRegisteredToolDefinitions())
    .toolHandler(handler)
    .maxRounds(2)
    .build();

CatholicRequiredToolAgent reportAgent =
    new CatholicRequiredToolAgent(baseAgent, "submit_report");

return reportAgent.interact(userMessage);
```

构造 `CatholicRequiredToolAgent` 时会验证指定函数已经配置。其 `tool_choice` 覆盖只存在于本次交互的首次 LLM 请求中；后续请求恢复基础 options，且不会修改或污染共享的 `CatholicLLMRequestOptions`。

实际的工具选择仍由模型服务根据 `tool_choice` 参数完成，框架不会绕过模型自行调用工具。首次响应返回后，框架会在 Observer 和任何工具 handler 执行前验证响应中包含指定函数调用。如果模型服务忽略参数、没有调用工具或改为调用其他工具，交互会以 `CatholicRequiredToolNotCalledException` 失败，错误的工具调用不会产生副作用。

该约束只验证首次响应至少包含一次指定函数调用。后续请求恢复基础 options；如果业务还要求整个交互中该函数最多执行一次，应由 Observer 或工具 handler 另外限制。

## Builder 约束

- `llm` 和非空白 `model` 必填；
- `maxRounds` 必须至少为 1，默认值为 32；
- 工具列表非空时必须提供 `toolHandler`；
- `options`、Observer、工具定义及其集合元素均不可为 null；
- `systemPrompt(null)` 和空白 system prompt 会被忽略；
- 本包使用包级 `@NullMarked`，仅显式标注 `@Nullable` 的值允许为 null。

## 兼容 API

旧的 `chat(...)` 暂时保留。它在完成时仅返回最后一轮 `CatholicLLMResponse`，轮次超限时以 `CatholicAgentTooManyToolRoundsException` 结束失败，因此无法完整表达新的终止状态。新代码应使用 `interact(...)`。

`maxToolRounds(int)` 同样仅用于兼容旧代码，并映射为 `maxRounds(maxToolRounds + 1)`。新代码应直接以 LLM 请求轮数配置 `maxRounds(int)`。

## 当前边界

- Agent 每轮使用流式 LLM 调用，并在响应聚合完成后执行 Observer 和工具；
- 一次 `interact(...)` 是独立用户交互，不负责跨交互的会话存储；
- 工具调用当前顺序执行，不并行执行；
- 工具重试、超时、错误转换以及长期记忆应由 handler、Observer 或上层业务实现。
