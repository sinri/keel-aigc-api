package io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.request;

import io.github.sinri.keel.aigc.api.internal.vgm.wanx.WanxImageSynthesisRequestImpl;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface WanxImageSynthesisRequest extends JsonifiableDataUnit {
    static WanxImageSynthesisRequest wrap(JsonObject json) {
        return new WanxImageSynthesisRequestImpl(json);
    }

    static WanxImageSynthesisRequest create() {
        return new WanxImageSynthesisRequestImpl()
                .setModel(WanxImageSynthesisModel.WanxV1);
    }

    @Nullable
    default WanxImageSynthesisModel getModel() {
        var x = this.readString("model");
        if (x == null) return null;
        return WanxImageSynthesisModel.fromModelCode(x);
    }

    default WanxImageSynthesisRequest setModel(WanxImageSynthesisModel model) {
        ensureEntry("model", model.getModelCode());
        return this;
    }

    default WanxImageSynthesisRequest handleInput(Handler<WanxImageSynthesisInput> inputHandler) {
        WanxImageSynthesisInput input;

        var x = this.readJsonObject("input");
        if (x != null) {
            input = WanxImageSynthesisInput.wrap(x);
        } else {
            input = WanxImageSynthesisInput.create();
        }

        inputHandler.handle(input);
        this.setInput(input);
        return this;
    }

    @Nullable
    default WanxImageSynthesisInput getInput() {
        var x = this.readJsonObject("input");
        if (x == null) return null;
        return WanxImageSynthesisInput.wrap(x);
    }

    default WanxImageSynthesisRequest setInput(WanxImageSynthesisInput input) {
        ensureEntry("input", input.toJsonObject());
        return this;
    }

    default WanxImageSynthesisRequest handleParameters(Handler<WanxImageSynthesisParameters> parametersHandler) {
        WanxImageSynthesisParameters parameters;

        var x = this.readJsonObject("parameters");
        if (x != null) {
            parameters = WanxImageSynthesisParameters.wrap(x);
        } else {
            parameters = WanxImageSynthesisParameters.create();
        }

        parametersHandler.handle(parameters);
        this.setParameters(parameters);
        return this;
    }

    @Nullable
    default WanxImageSynthesisParameters getParameters() {
        var x = this.readJsonObject("parameters");
        if (x == null) return null;
        return WanxImageSynthesisParameters.wrap(x);
    }

    default WanxImageSynthesisRequest setParameters(WanxImageSynthesisParameters parameters) {
        ensureEntry("parameters", parameters.toJsonObject());
        return this;
    }
}
