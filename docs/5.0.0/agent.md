# 5.0.0 Agent 指南

`CatholicAgent` 在 `CatholicLLM` 上提供无会话状态的流式 LLM 执行循环：每轮通过
`callStream(...)` 接收并聚合模型响应，随后由 Observer 判断、执行工具，再将工具结果
交给下一轮模型，直到完成或达到轮次上限。

## 基本 Agent

```java
CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .systemPrompt("You are a helpful assistant.")
    .maxRounds(8)
    .build();

return agent.interact("分析这份需求")
    .onSuccess(result -> {
        if (result.completed()) {
            System.out.println(result.text());
        } else {
            System.err.println(result.termination());
        }
    });
```

需要继续由上层保存的会话时，可按次传入此前的完整消息列表：

```java
return agent.interact(priorMessages, CatholicUserMessage.ofText("继续分析"));
```

请求消息依次由 Builder 配置的 system prompt、`priorMessages`、当前用户消息组成。
Agent 不会保存传入的历史，下一次交互仍需由调用方提供。

`maxRounds` 是最多发起的 LLM 请求次数，最小为 1、默认 32。达到限制是可观察的正常
终止，`interact(...)` 会成功返回 `ROUND_LIMIT_EXCEEDED`；模型、Observer 或工具失败
才会使 Future 失败。

## 注册工具

工具定义与处理器必须成对提供。已有 `NativeFunctionAdapter` 时，可使用内置注册器：

```java
var handler = CatholicToolInvocationHandler.createWithNativeFunctionAdapters();
handler.registerNativeFunctionAdapter(new QueryWeatherFunction());

CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .tools(handler.getRegisteredToolDefinitions())
    .toolHandler(handler)
    .maxRounds(8)
    .build();
```

一轮返回多个工具调用时会顺序执行；每个结果都会作为工具消息写入 transcript。处理器
必须返回非空的 `Future<String>`，失败应通过失败的 Future 传播。

## 自定义 Observer

`CatholicAgentObserver` 在每轮响应写入 transcript 后决定完成、执行工具，或追加机制
观察消息后继续：

```java
CatholicAgentObserver observer = context -> {
    if (isTaskComplete(context.response())) {
        return Future.succeededFuture(CatholicAgentDirective.complete());
    }
    if (context.response().hasToolCalls()) {
        return Future.succeededFuture(CatholicAgentDirective.continueExecution());
    }
    return Future.succeededFuture(CatholicAgentDirective.continueWith(
        CatholicUserMessage.ofText("答案缺少风险分析，请继续。")
    ));
};
```

没有工具调用却要求继续时，必须通过 `continueWith(...)` 增加上下文，否则交互会失败。
Observer 选择完成时，响应中尚未执行的工具调用不会执行。

## Agent Skills

```java
CatholicAgent agent = CatholicAgent.builder()
    .llm(llm)
    .model("model-name")
    .skillProvider(skillProvider)
    .build();
```

每次交互只将候选 Skill 的名称和描述放入 system context。模型调用保留工具
`activate_skill` 后，Agent 才通过 Provider 加载完整 instructions；同一交互内不会重复
加载同名 Skill。Skill 引用的脚本、参考资料和资源仍须由应用工具按需读取或执行。

## 首轮必须调用指定工具

```java
import io.github.sinri.keel.aigc.api.agent.reqtool.CatholicRequiredToolAgent;
import io.github.sinri.keel.aigc.api.agent.reqtool.CatholicRequiredToolNotCalledException;

CatholicRequiredToolAgent reportAgent =
    new CatholicRequiredToolAgent(baseAgent, "submit_report");

return reportAgent.interact(userMessage);
```

指定函数必须已经注册。约束只应用于首次 LLM 请求，并在 Observer 或工具处理器运行前
验证；服务若忽略 `tool_choice`，交互会以 `CatholicRequiredToolNotCalledException` 失败。

## 结果与会话

`CatholicAgentResult` 提供终止原因、最后响应、可空文本、只读 transcript、LLM 轮数、
工具轮数和轮次上限。一次 `interact(...)` 不继承其他交互的消息；跨请求会话应由上层
保存，并确保共享的 LLM、Observer 和工具处理器支持并发。
