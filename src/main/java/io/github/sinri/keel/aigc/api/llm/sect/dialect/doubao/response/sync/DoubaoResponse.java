package io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao.DoubaoResponseImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface DoubaoResponse extends UnmodifiableJsonifiableEntity {


    static DoubaoResponse wrap(JsonObject jsonObject) {
        return new DoubaoResponseImpl(jsonObject);
    }

    /**
     * 本次请求的唯一标识。
     */
    default @Nullable String getId() {
        return readString("id");
    }

    /**
     * doubao 1.5 代模型的模型名称格式为 doubao-1-5-**。
     * 如调用部署doubao-1.5-pro-32k 250115模型的推理接入点，返回model字段信息{@code doubao-1-5-pro-32k-250115}。
     *
     * @return 本次请求实际使用的模型名称和版本。
     */
    default @Nullable String getModel() {
        return readString("model");
    }

    /**
     * 本次请求是否使用了TPM保障包。
     *
     * @return {@code scale}：本次请求使用TPM保障包额度。 {@code default}：本次请求未使用TPM保障包额度。
     */
    default @Nullable String getServiceTier() {
        return readString("service_tier");
    }

    /**
     * @return 本次请求创建时间的 Unix 时间戳（秒）。
     */
    default @Nullable Integer getCreated() {
        return readInteger("created");
    }

    /**
     * @return 固定为 {@code chat.completion}。
     */
    default @Nullable String getObject() {
        return readString("object");
    }

    /**
     * 本次请求的模型输出内容。
     */
    default List<DoubaoResponseChoice> getChoices() {
        List<JsonObject> choices = readJsonObjectArray("choices");
        if (choices == null) return List.of();
        return choices.stream().map(DoubaoResponseChoice::wrap).toList();
    }

    /**
     * @return 本次请求的 token 用量。
     */
    default @Nullable JsonObject getUsage() {
        return readJsonObject("usage");
    }


}
