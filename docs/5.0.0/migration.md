# 迁移到 5.0.0

本页汇总当前源码中可确认的 5.0.0 使用变化。升级前仍应运行应用自己的编译、测试和真实
Provider 集成测试。

## 运行基线

- Java toolchain 与字节码目标均为 Java 17；
- Maven 坐标保持 `io.github.sinri:keel-aigc-api`；
- Java 模块名为 `io.github.sinri.keel.integration.llm.api`；
- 异步 API 使用 Vert.x `Future`。

## 显式注入 Keel

Provider Builder 现在要求 `.keel(keel)`。不带 `Keel` 的构造器仍暂时保留，但已标记
`@Deprecated` 并退回 `Keel.shared()`：

```java
// 旧写法（废弃）
new OpenAIResponsesLLM(httpClient, apiKey);

// 5.0.0 推荐写法
OpenAIResponsesLLM.builder()
    .keel(keel)
    .httpClient(httpClient)
    .apiKey(apiKey)
    .build();
```

OpenAI Chat Completions、OpenAI Responses、Anthropic 和两个 DashScope Provider 均应
采用显式注入方式，便于控制运行时生命周期。

## Agent 轮次语义

新代码应调用 `interact(...)` 并使用 `CatholicAgentResult` 判断终止状态：

```java
return agent.interact(message).compose(result -> {
    if (result.completed()) {
        return consume(result);
    }
    return handleRoundLimit(result);
});
```

旧 `chat(...)` 只能返回最后一轮响应，并在轮次超限时以
`CatholicAgentTooManyToolRoundsException` 失败，无法表达完整结果。

`maxToolRounds(int)` 已废弃；它临时映射为 `maxRounds(maxToolRounds + 1)`。迁移时应按
“最多允许多少次 LLM 请求”重新设置：

```java
// 旧：最多 N 批工具调用
.maxToolRounds(n)

// 新：最多 N + 1 次 LLM 请求（保持旧配置的大致边界）
.maxRounds(n + 1)
```

## 内部包不可依赖

5.0.0 的 `module-info.java` 不导出 `io.github.sinri.keel.aigc.api.internal`。若旧代码直接
使用其中的协议转换器或流处理器，应改用公开的 `CatholicLLM`、Provider、请求和响应
API；内部类型可能在不另行通知的情况下变化。

## 升级检查清单

- 将运行和构建 JDK 升至 17+；
- 给所有 Provider Builder 增加 `.keel(keel)`；
- 用 `interact(...)` 和 `maxRounds(...)` 替换 Agent 旧 API；
- 不再导入 `internal` 包；
- 对流式响应检查 `finished()`，并验证中断与处理器失败可以向上传播；
- 对目标模型复测工具调用、多模态内容及厂商专有参数。
