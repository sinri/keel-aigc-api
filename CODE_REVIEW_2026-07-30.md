# Keel AIGC API 代码审查（2026-07-30）

## 审查概要

- 审查对象：`dev-5.0.0` 分支，当前已对齐改写后的远端历史
- 项目定位：Java 17 / Vert.x 5 / Keel 的多 LLM 提供商统一异步 API，支持流式响应、工具调用、Agent 与 Agent Skills
- 验证结果：`./gradlew test`，170 个测试全部通过
- 当前状态：R-01、R-02 已快速修复；R-03 经用户决定暂不处理

## 问题清单

### R-01 [P0] 版本库跟踪的测试配置疑似包含真实 API Key

**状态：快速修复完成（凭据轮换待人工确认）**

已将示例配置中的硬编码值替换为占位符，并重写 GitHub 可达历史：

- `dev-5.0.0`：`52f2a4f` → `a0fa910`
- `local-vibe`：`7652fb2` → `eb66bf1`
- `main`：`aa6a4a2` → `cf3a080`
- `dev` 无需改写，仍为 `3fde2f7`

远端没有标签；pull refs 扫描未发现同类敏感内容。改写后的 `dev-5.0.0` 已通过完整测试。
仍需在 Provider 控制台撤销或轮换原凭据；Git 历史清理不能替代凭据轮换。

### R-02 [P1] 三个 Provider 的 SSE 解析会静默吞掉畸形 JSON

**状态：快速修复完成，待用户同意提交**

**处理结果：**

- 三个 handler 对已识别且非 `[DONE]` 的畸形 JSON 抛出 `IllegalArgumentException`
- 异常消息只包含 Provider 上下文，不包含原始 SSE 数据；解码异常保留为 cause
- 注释、非数据行、未知字段和未知事件类型仍保持原有兼容行为
- 三个 handler 的定向测试通过，完整 `./gradlew test` 的 170 个测试全部通过

OpenAI Chat Completions、OpenAI Responses 和 Anthropic 的流处理器在已识别的 `data:` 行
JSON 解析失败时直接返回 `null`。上层已经能传播 handler 异常，但解析器提前吞掉错误，因此
网络截断、兼容网关损坏事件或协议异常可能被当作正常的空事件。

**证据：**

- `OpenAIChatCompletionsStreamHandler.java:48-53`
- `OpenAIResponsesStreamHandler.java:45-50`
- `AnthropicStreamHandler.java:49-54`
- `DashScopeStreamHandler` 已采用抛出专用异常的相反处理方式

**影响：**

- 文本 chunk 损坏时，聚合调用可能返回缺字但表面成功的回复
- 工具参数 chunk 损坏时，错误根因可能丢失或留下不完整 JSON
- 完成事件损坏时，Future 可能成功但响应并未完成

**建议方向：**

- 对已识别且非 `[DONE]` 的 `data:` 事件，将 JSON 解码失败作为流失败传播
- 继续容忍注释、未知字段和未知事件类型
- 为三个 handler 补充畸形及截断 JSON 的回归测试

### R-03 [P2] 多个公开值对象泄漏可变集合和 `JsonObject`

**状态：暂不处理**

**决策记录：** 当前不修改代码，也不创建 GitHub Issue。保留以下风险说明，后续若公开模型需要
更强的不可变契约、出现异步修改问题或准备调整 API 兼容性时再重新评估。

以下公开模型未做完整的防御性复制：

- `CatholicAssistantMessage` 直接保存并返回 `toolCalls`
- `CatholicLLMResponseChunkImpl` 直接保存并返回 `deltaToolCalls`
- `CatholicLLMRequestOptionsImpl` 直接保存并公开 `stop` 和可变的 `extra`

**影响：**

- 构建后的请求或响应可能被原集合、返回集合或复用 Builder 意外改写
- 异步序列化结果可能随并发修改的时序变化
- Observer、重试与实际请求可能看到不同内容

**建议方向：**

- 列表在构造边界统一使用 `List.copyOf`
- `JsonObject extra` 在构造和访问边界复制
- 增加修改原集合与复用 Builder 不影响已构建对象的契约测试

## 建议处理顺序

1. R-01：已完成远端历史清理，待人工轮换凭据
2. R-02：修复静默数据损坏风险并补回归测试
3. R-03：暂不处理，后续按不可变契约需求重新评估
