package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters.QwenRequestOcrOptions;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters.QwenRequestParameters;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.request.parameters.QwenRequestSearchOptions;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * @since 2.0.0
 */
public class QwenRequestParametersImpl extends JsonifiableDataUnitImpl implements QwenRequestParameters {
    public QwenRequestParametersImpl() {
        this(new JsonObject());
    }

    public QwenRequestParametersImpl(JsonObject jsonObject) {
        super(jsonObject);
        this.resultFormat("message");
    }

    @Override
    public QwenRequestParameters getImplementation() {
        return this;
    }

    public static class QwenRequestOcrOptionsImpl extends JsonifiableDataUnitImpl implements QwenRequestOcrOptions {
        public QwenRequestOcrOptionsImpl() {
            super();
        }

        public QwenRequestOcrOptionsImpl(JsonObject jsonObject) {
            super(jsonObject);
        }

        public QwenRequestOcrOptionsImpl(String task, @Nullable JsonObject task_config) {
            super(new JsonObject().put("task", task));
            if (task_config != null) {
                ensureEntry("task_config", task_config);
            }
        }

    }

    public static class QwenRequestSearchOptionsImpl extends JsonifiableDataUnitImpl implements QwenRequestSearchOptions {

        public QwenRequestSearchOptionsImpl() {
            super();
        }

        public QwenRequestSearchOptionsImpl(JsonObject jsonObject) {
            super(jsonObject);
        }

    }
}
