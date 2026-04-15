package io.github.sinri.keel.aigc.api.llm.catholic.message;

import java.util.List;

/**
 * 消息内容的sealed接口，支持文本和图片两种类型。
 * 用于构建多模态消息。
 */
public sealed interface CatholicChatContent
    permits CatholicTextContent, CatholicImageContent {
}