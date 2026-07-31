# Keel AIGC API 5.0.0

5.0.0 提供统一的 `CatholicLLM` 抽象、四类 Provider 实现，以及建立在该抽象之上的
非流式 Agent 执行框架。

## 阅读路径

1. [快速开始](getting-started.md)：依赖、统一请求、非流式和流式调用；
2. [Provider 配置](providers.md)：OpenAI、Anthropic、DashScope 的选择和构造；
3. [Agent 指南](agent.md)：工具循环、Observer、必调工具和 Agent Skills；
4. [迁移到 5.0.0](migration.md)：5.0.0 的运行要求和废弃 API 替代方式；
5. [LLM 协议兼容性与易用性优化计划](llm-compatibility-plan.md)：可信来源、分阶段
   改造、`@see` 规则和兼容性发布门槛。

## 公开 API 分层

- `llm.catholic`：厂商无关的请求、响应和调用接口；
- `llm.catholic.message`：文本、多模态和工具消息；
- `llm.catholic.tool`：工具定义、调用及原生函数适配；
- `llm.openai.*`、`llm.anthropic`、`llm.dashscope.*`：Provider；
- `agent`：多轮执行与观察机制。

`io.github.sinri.keel.aigc.api.internal` 及其子包属于内部实现，未由 Java 模块导出，
应用不应直接依赖。

## 5.0.0 当前边界

- 所有调用都以 Vert.x `Future` 表达；
- Agent 当前只使用非流式 LLM 调用；
- Agent 的工具按模型返回顺序执行，不并行；
- 一次 `interact(...)` 是独立交互，长期会话、重试、超时和持久化由应用负责；
- `allowed-tools` 可随 Skill 披露，但不会绕过应用自己的权限控制。
