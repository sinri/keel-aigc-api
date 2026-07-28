package io.github.sinri.keel.aigc.api.agent;

import java.time.Instant;
import java.util.Objects;

/**
 * 表示一次工具调用的不可变审计快照。
 * 它在工具实际执行前创建，保存本次调用的关联标识、模型工具调用标识、函数名、
 * 原始参数和开始时间，并作为同一次调用开始、成功及失败事件之间的关联实体。
 *
 * @param invocationId 执行前生成的审计关联标识
 * @param toolCallId LLM 服务生成的工具调用标识
 * @param functionName 请求调用的函数名
 * @param arguments LLM 服务提供的原始 JSON 参数字符串
 * @param startedAt 工具调用开始的墙上时钟时间
 */
public record CatholicToolInvocationObservation(
    String invocationId,
    String toolCallId,
    String functionName,
    String arguments,
    Instant startedAt
) {
    public CatholicToolInvocationObservation {
        Objects.requireNonNull(invocationId, "invocationId");
        Objects.requireNonNull(toolCallId, "toolCallId");
        Objects.requireNonNull(functionName, "functionName");
        Objects.requireNonNull(arguments, "arguments");
        Objects.requireNonNull(startedAt, "startedAt");
    }
}
