package io.github.sinri.keel.llm.api.catholic;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.vertx.core.json.JsonObject;

/**
 * 需要针对具体的模型生成具体的实现类。
 */
public abstract class LargeLanguageModel implements JsonObjectConvertible {
    public abstract String getCode();

    public abstract LLMService getService();

    @Override
    public final JsonObject toJsonObject() {
        return new JsonObject().put("code", getCode());
    }

    @Override
    public final String toJsonExpression() {
        return toJsonObject().encode();
    }

    @Override
    public final String toFormattedJsonExpression() {
        return toJsonObject().encodePrettily();
    }
}
