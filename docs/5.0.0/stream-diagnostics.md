# 流式调用诊断与后续措施

针对 PR #13 后的流式响应 ID 未初始化问题。当前证据尚不能确认线上根因；缺少 `[DONE]` 本身不会阻止有效 chunk 的收集。

## 启用与关联

给 LLM builder 配置 `LoggingCatholicLLMObserver`，启用 DEBUG 日志。已有 observer 实现可覆盖新增的默认方法 `onStreamDiagnostic`。默认 noop observer 不输出日志。所有诊断均带 `exchange_id`、`provider`、`elapsed_ms`；原始事件日志仍经过现有脱敏逻辑。

诊断 phase：

- `collector_started`：OpenAI 聚合接口收集第一片后的状态。
- `parse_failed` / `processor_failed`：失败事件的 sequence；原始异常通过 `onFailure` 报告并传给调用方。
- `transport_completed`：收到的字节数、HTTP 是否正常 EOF、切片器残留字节数、残留是否包含 CRLF。
- `stream_completed`：实际开始处理的事件数、解析成功 chunk 数、处理成功 chunk 数、协议终结 chunk 数（terminal_chunks）、是否见到 `[DONE]`、处理是否成功。
- `response_build`：各协议聚合接口组装前的 collector 标识、已收集片数、ID 初始化状态、文本长度、工具数量和 finished 状态。

collector 标识为实例 identity hash，仅用于同一进程同一 exchange 内关联，不是跨进程唯一 ID。诊断不包含生成文本或工具参数。失败后尚未处理的事件会被跳过，因此 events 不是网络接收事件总数。

## 判断顺序

1. 按 exchange 关联，不能只按日志相邻或线程相同推断同一次调用。
2. `pending_bytes > 0` 表示 EOF 时有未形成完整事件的尾部；`pending_has_crlf=true` 可提示现有 cutter 的换行兼容问题，但不是独立根因证明。
3. 查找 parse_failed/processor_failed 的 sequence，再对照对应 raw event 与 LLM failure。
4. 对比 collector_started 与 response_build 的 collector 标识及 collected_chunks，区分未收集、实例不一致和状态异常。
5. 正常 HTTP EOF 不代表模型响应完整；对照 finished 与 done_seen。模型结束片完整时允许缺少 DONE；空响应或没有 finish_reason 的聚合响应明确失败。
6. 若日志仍矛盾，核对部署实际加载的 keel-aigc-api、keel-core、keel-logger-api 版本及 JAR 来源，不能只看编译依赖声明。

## 已采取措施

- 在异步部署 cutter 之前暂停 HTTP response，避免快速响应结束后才安装 handler。
- 保存首个解析/回调异常，阻止后续业务回调，并在流结束后传播原始异常，避免底层 recover 吞掉错误。
- 解析与异步回调放入同一串行链；OpenAI 两种收集入口复用一份解析实现。
- OpenAI 聚合接口在 build 前校验 ID 与 finish_reason，替代静默返回空或未完成响应。这个 RC 行为变更可能暴露此前被隐藏的服务端不完整输出。

## 后续方案

当前仍使用 keel-core 的 cutter。CRLF/CR、无空行 EOF、无空格 data:、多行 data 以及跨 Buffer UTF-8 边界应作为单独的 SSE 分帧改造处理，避免仅为本次故障加入未经验证的宽松解析。

严格 SSE 在 EOF 丢弃未以空行结束的事件；若要兼容服务商的非标准尾片，应采用明确的可配置策略，并仅在完整 JSON 校验通过后接纳。不能补齐截断的工具参数。

首个处理错误当前在 HTTP 结束、队列排空后传播；如果服务端随后一直不关闭连接，仍需调用方超时或后续取消/超时机制。这一改动尚未引入自动重试，以免重复工具副作用。

验证：本地 HTTP 复现覆盖缺少 DONE、尾部缺少空行、CRLF、非法 JSON、延迟回调、异步回调失败、observer 抛错、快速响应和聚合完整性；不调用外部模型。

## 其他协议检查

所有聚合接口现在共用 `buildCollectedResponse`，记录 collector 状态、校验有效 ID 与协议终结片，并将组装错误归入 RESPONSE_CONVERSION。

| 协议 | 聚合完成条件 | 错误处理 |
| --- | --- | --- |
| Anthropic Messages | message_stop | error 事件传播；缺失终结片明确失败 |
| OpenAI Responses | response.completed | error、response.failed、response.incomplete 明确失败，保留错误或 incomplete_details |
| DashScope 文本／多模态 | 有效 finish_reason | event:error 与错误 payload 传播；字符串 null 不是完成条件 |

参考：[Anthropic 流式事件](https://platform.claude.com/docs/en/build-with-claude/streaming)、[Responses 流式事件](https://platform.openai.com/docs/api-reference/responses-streaming)。

DashScope 与另外两类协议现共用事件诊断和串行处理实现；每个完整事件末尾补空行交给 DashScope handler。原先 handler.flush() 无法读取底层 cutter 尚未切出的字节，不能据此认定 EOF 残留已被处理。现统一通过 transport_completed 报告这些残留。

`done_seen=false` 对 Anthropic、Responses、DashScope 通常没有故障含义，应看 terminal_chunks 和对应完成条件。新增本地 HTTP 测试矩阵覆盖四个客户端的正常结束、缺失终结片、空流、服务端错误；正常路径均不需要 DONE。

本轮完整性校验针对返回聚合响应的 callStream(request)。回调重载继续负责传递 chunk 与处理异常，调用方仍需依据终结 chunk 判断业务完成。非流式请求转换和完整协议功能覆盖不在本轮流结束诊断修复范围内。
