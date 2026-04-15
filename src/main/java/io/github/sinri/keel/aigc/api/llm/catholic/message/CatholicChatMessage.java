package io.github.sinri.keel.aigc.api.llm.catholic.message;

import java.util.List;

/**
 * 聊天消息接口，支持多种角色（system、user、assistant、tool）。
 * 用于构建对话历史。
 */
public interface CatholicChatMessage {
    /**
     * 消息角色
     *
     * @return 角色名称：system / user / assistant / tool
     */
    String role();

    /**
     * 消息内容列表（支持多模态）
     *
     * @return 内容元素列表
     */
    List<CatholicChatContent> contents();
}