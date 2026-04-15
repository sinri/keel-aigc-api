package io.github.sinri.keel.aigc.api.llm.catholic.message;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall;

import java.util.Collections;
import java.util.List;

/**
 * 助手消息，包含LLM生成的回复内容。
 * 可能是纯文本回复，也可能是工具调用请求。
 */
public class CatholicAssistantMessage implements CatholicChatMessage {

    public static final String ROLE = "assistant";

    private final String text;
    private final List<CatholicToolCall> toolCalls;

    /**
     * 构造助手消息
     *
     * @param text      文本内容（可能为null或空）
     * @param toolCalls 工具调用列表（可能为null或空）
     */
    public CatholicAssistantMessage(String text, List<CatholicToolCall> toolCalls) {
        this.text = text;
        this.toolCalls = toolCalls != null ? toolCalls : Collections.emptyList();
    }

    /**
     * 构造纯文本助手消息
     */
    public CatholicAssistantMessage(String text) {
        this(text, Collections.emptyList());
    }

    /**
     * 构造工具调用助手消息
     */
    public CatholicAssistantMessage(List<CatholicToolCall> toolCalls) {
        this(null, toolCalls);
    }

    @Override
    public String role() {
        return ROLE;
    }

    @Override
    public List<CatholicChatContent> contents() {
        if (text != null && !text.isEmpty()) {
            return List.of(new CatholicTextContent(text));
        }
        return Collections.emptyList();
    }

    /**
     * 获取文本内容
     */
    public String text() {
        return text;
    }

    /**
     * 获取工具调用列表
     */
    public List<CatholicToolCall> toolCalls() {
        return toolCalls;
    }

    /**
     * 是否包含工具调用
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    /**
     * 是否包含文本内容
     */
    public boolean hasText() {
        return text != null && !text.isEmpty();
    }

    // === 便捷构造方法 ===

    /**
     * 创建纯文本回复
     */
    public static CatholicAssistantMessage ofText(String text) {
        return new CatholicAssistantMessage(text);
    }

    /**
     * 创建工具调用回复
     */
    public static CatholicAssistantMessage ofToolCalls(List<CatholicToolCall> toolCalls) {
        return new CatholicAssistantMessage(toolCalls);
    }

    /**
     * 创建混合回复（文本+工具调用）
     */
    public static CatholicAssistantMessage ofMixed(String text, List<CatholicToolCall> toolCalls) {
        return new CatholicAssistantMessage(text, toolCalls);
    }
}