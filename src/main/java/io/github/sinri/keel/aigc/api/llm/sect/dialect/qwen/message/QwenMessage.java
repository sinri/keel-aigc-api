package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.message;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import org.jspecify.annotations.Nullable;

/**
 * QwenMessage 接口，定义了 Qwen 系列消息对象的基本结构和 JSON 序列化能力。
 * 提供消息角色的读取与设置方法，便于消息的统一处理。
 * @since 2.0.0
 */
public interface QwenMessage extends JsonifiableDataUnit {

    default @Nullable String role() {
        return readString("role");
    }

    default QwenMessage role(String role) {
        ensureEntry("role", role);
        return this;
    }
}
