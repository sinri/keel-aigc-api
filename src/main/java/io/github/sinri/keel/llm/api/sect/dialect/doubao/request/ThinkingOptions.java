package io.github.sinri.keel.llm.api.sect.dialect.doubao.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dialect.doubao.ThinkingOptionsImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface ThinkingOptions extends JsonifiableDataUnit {
    static ThinkingOptions create() {
        return new ThinkingOptionsImpl();
    }

    static ThinkingOptions wrap(JsonObject jsonObject) {
        return new ThinkingOptionsImpl(jsonObject);
    }

    /**
     * @param type 取值范围：{@code enabled}， {@code disabled}，{@code auto}。
     *             {@code enabled}：开启思考模式，模型一定先思考后回答。
     *             {@code disabled}：关闭思考模式，模型直接回答问题，不会进行思考。
     *             {@code auto}：自动思考模式，模型根据问题自主判断是否需要思考，简单题目直接回答。该取值只有doubao-1-5-thinking-pro-m-250428模型支持。
     */
    default ThinkingOptions type(String type) {
        ensureEntry("type", type);
        return this;
    }

    default @Nullable String type() {
        return readString("type");
    }
}
