package io.github.sinri.keel.llm.api.sect.dashscope.qwen.request;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dashscope.QwenRequestImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.input.QwenRequestInput;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.parameters.QwenRequestParameters;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface QwenRequest extends JsonifiableDataUnit {
    static QwenRequest create() {
        return new QwenRequestImpl();
    }

    static QwenRequest wrap(JsonObject jsonObject) {
        return new QwenRequestImpl(jsonObject);
    }

    default QwenRequest model(String model) {
        ensureEntry("model", model);
        return this;
    }

    default @Nullable String model() {
        return readString("model");
    }

    default QwenRequest input(QwenRequestInput input) {
        ensureEntry("input", input.toJsonObject());
        return this;
    }

    default QwenRequest input(Handler<QwenRequestInput> inputHandler) {
        QwenRequestInput x = input();
        inputHandler.handle(x);
        return input(x);
    }

    default QwenRequestInput input() {
        JsonObject x = readJsonObject("input");
        if (x == null) {
            x = new JsonObject();
        }
        return QwenRequestInput.wrap(x);
    }

    default QwenRequest parameters(QwenRequestParameters parameters) {
        ensureEntry("parameters", parameters.toJsonObject());
        return this;
    }

    default QwenRequest parameters(Handler<QwenRequestParameters> parametersHandler) {
        QwenRequestParameters x = parameters();
        parametersHandler.handle(x);
        return parameters(x);
    }

    default QwenRequestParameters parameters() {
        JsonObject x = readJsonObject("parameters");
        if (x == null) {
            x = new JsonObject();
        }
        return QwenRequestParameters.wrap(x);
    }
}
