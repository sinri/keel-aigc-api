# Agent 失败现场 Trace 设计

状态：已在当前分支实现并完成本地验证，用户已确认验收并授权提交。对应 [GitHub Issue #16](https://github.com/sinri/keel-aigc-api/issues/16)。以下保留原设计；实际接口和限制见 [使用指南](../5.0.0/error-tracing.md)。背景：rc23judge 某候选的 Round-K 在 `CatholicFunctionToolCall.parseArguments()` 发生括号不匹配异常，业务层只能记录堆栈并跳过候选，无法判断模型输出、流式转换或参数聚合是否有误。

## 目标

接入一次后，正常调用只在有界内存中保留诊断证据；异常时自动保存现场，业务错误日志通过 traceId 查到对应记录。不能依赖事故发生前临时打开 DEBUG，也不能在 catch 时尝试重新获得已经消失的 SSE 数据。

一次失败应能回答：哪个候选、哪一轮、哪个工具失败；收到什么参数；参数来自哪些事件；在哪个阶段损坏；同轮哪些工具已经执行；证据是否完整。

本设计不自动修复 JSON、不自动重跑工具，也不改变当前 Agent 的失败传播语义。内存方案无法恢复进程崩溃前尚未保存的现场；进程崩溃取证需要另行启用持续持久化。

## 当前接口与缺口

- `CatholicLLMObserver` 已提供原始请求、响应、已切分 SSE event、传输诊断及失败回调，可复用采集位置。
- `CatholicToolInvocationObserver` 在解析参数前收到包含 arguments 的观察对象，可记录失败工具实际使用的参数。
- `CatholicAgentObserver` 是控制流程的策略接口，返回继续或完成指令，不适合作为被动 trace 生命周期接口。
- LLM exchangeId 与工具 invocationId 独立生成，没有 agentRunId / round 的显式关联。
- 参数解析失败发生在 LLM 成功之后；在 HTTP 完成时销毁缓存会丢掉最关键的上游证据。
- 原始 SSE 回调位于事件切分之后：CRLF 边界错误时甚至不会触发，必须补充切分前的有界传输证据。
- 现有脱敏器可能重新序列化 JSON、改变长度和换行，脱敏后的文本不能直接充当字节级重放原件。

## 生命周期与关联

新增独立的、默认关闭的 `CatholicTraceRecorder`（拟议名称）。应用启动时配置一次 recorder 和 sink；共享 Agent、LLM 和工具处理器不保存可变的“当前 trace”。

每次 `interact` 创建独立 TraceSession，显式传递不可变 TraceContext：

```text
traceId / agentRunId              一次 interact
  business tags                  flow=rc23judge、candidateId、roundLabel=K
  llmRound / exchangeId / attempt 一次模型请求；重试必须独立编号
    eventSequence                收到的 SSE 事件
    choiceIndex / toolIndex       参数归属
    toolCallId / invocationId     模型工具 ID / 本地执行 ID
```

business tags 采用白名单、数量和长度限制。业务侧传入候选标识，不由库依赖 IssueDialogService。不要把任意业务对象整个塞入 tags。

新增带 TraceContext 的调用重载，保留现有签名。Provider 和工具 Handler 的新重载默认委托旧接口，保证已有实现可继续运行；未接入新采集点的第三方实现记录 `capture_capability=partial`。内置实现完整传递上下文。上下文不写入 Provider 请求正文或认证头，不通过 ThreadLocal、MDC 或时间相近来猜测归属。

完整状态机：`ACTIVE → FREEZING → QUEUED → SAVED / SAVE_FAILED`；正常完成为 `ACTIVE → DISCARDED`。多个失败回调只能触发一次封存，原始错误作为主原因，后续观察事件作为关联信息。

缓存持有到 Agent 成功或失败。中间一轮 HTTP 成功只关闭该 exchange，不销毁其证据。失败时冻结当前 run 中已经收集的证据，晚到事件不能覆盖冻结内容，且不混入其他并发 run。

独立 LLM 调用以 exchange 为生命周期，另提供显式 session scope 供调用方将“LLM 返回后自行解析”的阶段纳入保护。

## 采集内容

| 层次 | 内容 | 用途 |
| --- | --- | --- |
| 运行 | 时间、版本/构建标识、Provider、模型、脱敏 endpoint、tags、轮次、最终状态 | 定位具体调用和代码版本 |
| 请求 | Provider 实际请求、工具 schema、采样及 token 配置；正文受内容策略与预算约束 | 还原输入与参数约束 |
| HTTP | 状态码、允许保留的响应头、服务端 request ID、字节计数、EOF/异常 | 区分协议失败和传输失败 |
| 切分前 | 当前轮响应 Buffer 的有界证据、顺序、换行统计、结束时未切分尾部 | 诊断事件边界、尾部丢失 |
| SSE | 顺序、事件类型、原始事件的长度与摘要、内容策略允许的载荷 | 还原服务返回的增量 |
| 转换 | 源事件序号、choice/output/tool index、call ID、参数 delta 长度与摘要、采集策略允许的 delta | 判断转换或路由是否有误 |
| 聚合 | 参数追加顺序、追加前后长度、最终 arguments 长度与摘要；最终文本或结构副本 | 对照解析器实际收到的字符串 |
| 工具 | invocation ID、开始/成功/失败、耗时、结果摘要、是否已进入业务函数 | 防止误把解析失败当作工具执行失败 |
| 异常 | 阶段、异常类型、cause 链、堆栈、原始参数中的行列/字符位置 | 给出可以调查的失败位置 |

参数必须按字符串处理，不能先成功解析 JSON 才能留存。诊断文件将非法 arguments 放在合法 JSON 的字符串字段或独立文本文件中。

`finish_reason` / response status / incomplete details 原样保留；“收到了结束事件”与“参数是合法 JSON”分别记录，不用一个 finished 布尔值替代。

## 内容策略与证据可信度

提供两档内容策略，并在接入配置中明确选择：

1. **默认结构诊断**：认证头从不采集；请求 prompt 和工具结果默认只留大小、摘要及结构元数据。工具参数保存结构副本：原始字符串中的 JSON 结构符号、换行保留，字符串值与键名内容、其他标量内容用等 UTF-16 长度占位符替换；保留引号、反斜杠位置以诊断转义。工具名和索引单独记录。扫描器不依赖 JSON 能被解析，遇到无法可靠判定的区域采用保守遮蔽并标注置信度。该副本用于定位括号和长度问题，不能承诺精确重放或隐藏所有结构信息。
2. **受限原文取证**：显式启用，允许保存指定范围的原始请求、响应和参数。认证信息仍不保存；载荷写入独立受控目录或存储，限制访问与保留时间。只有原文证据完整且未截断，才可标为可精确重放。加密与密钥管理由部署环境或 sink 提供，不把密钥写入 trace。

错误附近的文本窗口也服从同一策略，禁止因诊断错误而绕过脱敏。原始位置与脱敏后位置分开标识。结构副本若发生截断，则另外记录片段起始偏移，不能让文件第 4 行冒充原始第 4 行。

原始流的增量摘要在采集时计算，帮助判断持久化后丢失或拼接差异；受限场景使用部署密钥的 HMAC，避免对低熵业务值做裸哈希形成猜测接口。不把摘要当作内容已完整保留的证明。

对跨 delta 的敏感值，不能仅逐块正则替换就宣称安全。默认模式使用每个工具独立的跨片段状态扫描；原文模式按原文敏感级别存储。

## 内存、保存与失败语义

以下仅为初始可配置预算，实施后需按真实并发和工具参数大小测量调整：单 run 2 MiB；其中失败定位所需的当前轮参数/结构信息优先，给摘要与阶段事件预留空间；进程总 trace 缓存 64 MiB；保存队列同时限制 32 条及 32 MiB。按 UTF-8 字节及对象开销保守计费，并限制事件数、单字段长度与活跃 session 数，不能只按 Java 字符数计算。

总预算包含活跃 run、冻结记录和待保存队列，不在 freeze 时复制一份无预算约束的大对象。达到上限时优先丢弃早期轮次载荷、保留元数据和当前轮工具参数的首尾片段。单个参数仍必须有上限；任何丢失都写入 `truncated`、`dropped_events`、原始长度、缺失区间及原因。

正常路径的 callback 只做有界采集和轻量记录，禁止在 Vert.x event loop 上执行文件写入、压缩、加密、完整 JSON 重解析或大型脱敏。重计算交给有界工作队列；采集成本过高时降级并标注，不无限堆积任务。

Agent 失败后将快照交给异步 sink，立即保留原有 failed Future。保存是异步 best effort，不承诺业务日志出现时文件已经落盘：

```text
Agent failed trace_id=... stage=TOOL_ARGUMENT_PARSE capture_status=queued
Trace saved trace_id=... location=... evidence_complete=false
```

业务日志不需要改写异常消息；业务在调用前拿到 session 的 traceId，或订阅 recorder 的失败摘要事件。维持原异常类型和 cause，避免包装异常破坏既有判断。

队列满、磁盘满、权限错误、脱敏异常必须产生独立的限频告警和指标 `trace_capture_dropped` / `trace_save_failed`，不能被现有 Observer 的静默 fail-open 吞掉。trace 故障不替代原始业务异常。应用正常关闭提供限时 flush；如业务要求离开失败处理前已持久化，可显式等待 session 的保存 Future 并设置超时，不阻塞事件循环。

session 在成功、失败、显式取消、调用超时和 recorder 关闭时释放；悬挂调用另设 trace 保留 TTL，超时仅清除诊断资源并标注，不私自改变业务调用结果。

文件 sink 采用临时文件写入后原子改名，文件名只用内部生成 ID。按总容量、文件数和保留期限回收，例如初始设置 1 GiB / 7 天；回收操作在工作线程执行。部署可替换为对象存储 sink。

## 针对本次 JSON 错误的处理

在 NativeFunctionAdapter 参数解析处添加明确阶段边界：

```text
tool_arguments_ready → parse_started → parse_failed
                                      ↘ trace freeze/save
```

解析失败时记录 `TOOL_ARGUMENT_PARSE`、工具名、call ID、arguments 长度、原始位置、结构窗口与所有可用增量关联。`native_function_entered=false`。此前工具是否完成逐一记录。

排障时依次核对：

1. 事件层收到的参数 delta，按同一工具原始顺序拼接是否有效；
2. 转换后的 delta 是否与事件一致；
3. 收集器最终 arguments 是否与转换后的拼接结果一致；
4. 终止原因是否表示 token 上限或其他非正常完成。

仅当保存了完整原文时才执行精确对照；默认结构诊断可定位括号失配和部分长度差异，但不能证明所有文本语义一致。离线重放仅执行 SSE 解析、转换、聚合与 JSON 解析，绝不调用 NativeFunctionAdapter 或访问真实 Provider。

## 实施拆分与验收

**第一阶段：参数失败可定位。** 完成 TraceSession 生命周期、业务关联、最终参数结构快照、轮次/工具执行记录、有界 sink 与保存失败可见性。即使没有 SSE，也能知道实际解析了什么结构、哪个工具尚未执行。覆盖所有 Agent 失败出口，包括请求转换、首轮验证、策略回调、工具解析/执行、轮次耗尽。

**第二阶段：定位上游损坏。** 接入 Provider exchange、切分前传输、原始事件与参数 delta；支持受限原文、证据完整性标志及离线重放。

验收至少覆盖：

- 非法 JSON 带原始多行位置，工具业务函数调用次数为 0，产生一份可关联 trace。
- LLM 已成功而稍后工具解析失败，模型事件证据仍在。
- 两个候选复用同一 Agent/LLM 并发运行，trace 完全隔离；重复错误回调只保存一次。
- CRLF 未切出事件也有切分前证据；正常结束但 arguments 非法可区分。
- 大载荷/大量事件/长轮次/高并发不突破 recorder 预算，截断和降级明确可见。
- 跨片段秘密、无效 JSON、引号和转义、Unicode 代理对经过内容策略后，位置映射与保护行为符合声明。
- 保存失败不替换业务异常，也不报告保存成功；关闭 flush 与 TTL 释放可验证。
- 离线重放不执行任何真实工具；原文不完整时拒绝标记为精确重放。
- 成功路径无载荷落盘，已有 Observer/自定义 Provider 兼容，执行项目完整构建与测试，并测量 event loop 延迟与并发内存。

2026-09-22 用户确认先建立 GitHub Issue，已创建 [#16](https://github.com/sinri/keel-aigc-api/issues/16)。尚未修改运行代码或提交。


## 2026-09-22 实施与验证记录

用户确认完整方案后，在 `dev-5.0.0` 实施；2026-09-22 用户验收并授权提交及更新 Issue，push 由用户执行。

- 新增 `CatholicTraceRecorder`、Session、Context、文件 Sink 和离线 Replay；显式上下文贯通 Agent、RequiredToolAgent、工具处理器和五种 Provider。
- 采集切分前 Buffer、SSE、转换 delta、聚合长度/结果、解析失败和工具执行进度；LLM 成功后保留证据直至整个 Agent 收尾。
- 默认 STRUCTURE、显式 RESTRICTED_RAW；原始业务异常保持，保存异步执行，正常成功不落盘。
- 实现有界缓存/队列、首尾截断、全局计费、TTL、关闭 drain、保存失败指标和限频告警、文件权限/原子发布/后台保留期维护。
- 为原有 Native Handler 子类保留旧 protected 方法分派，该路径明确标为 partial，避免默默绕过应用自定义处理。
- 制品 manifest 增加 Implementation-Version，用于记录发布库版本；源码运行标为 unpackaged。

实现细节相对提案的收敛：结构脱敏使用保守逐字符遮蔽，保留结构字符和 UTF-16 位置，不依赖可能失效的 JSON 词法状态；其跨片段处理与整体处理一致，文档说明其结构信息暴露边界。HMAC 使用 recorder 生命周期随机密钥；精确重放仅支持完整原文流式证据。传输单 Buffer 采集上限 8192 字节，超过则明确标为不完整。相关限制均在使用指南中列明。

验证命令：`./gradlew build javadoc`，BUILD SUCCESSFUL。共 200 项测试，0 失败、0 错误、0 跳过，其中新增 trace 测试 14 项。覆盖五种 Provider 参数失败/离线重放、CRLF 切分前证据、并发候选隔离、此前工具执行进度、验证/策略/轮次终止、旧 Handler 子类、脱敏、缓存/队列限制、TTL、保存故障及文件权限/保留。

本机测试中的性能观察（非生产容量承诺）：

- event loop 连续采集 16 × 1 KiB 参数事件：100 组样本 p50 14 µs、p95 26 µs、最大 117 µs。
- 人为阻塞写入线程时，事件循环定时器仍能完成，验证磁盘 sink 不在事件循环上执行。
- 8 个线程并发采集：配置全局计费上限 4,194,304 字节，观察计费峰值 4,141,232 字节，释放后归零。
- 同次测试 JVM 已用 heap 差值 7,437,480 字节，包含临时分配和未回收对象，不能等同于 recorder 保留数据；计费预算并非 JVM heap 硬上限。

`git diff --check` 通过。用户已确认验收，按技能流程提交并更新 Issue。
