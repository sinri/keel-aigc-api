package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


/**
 * @since 1.3.3
 */
public class GptImageResourceUsage extends UnmodifiableJsonifiableEntityImpl {

    public GptImageResourceUsage(JsonObject jsonObject) {
        super(jsonObject);
    }

    /*
    "usage": {
        "input_tokens": 364,
        "input_tokens_details": {
          "image_tokens": 323,
          "text_tokens": 41
        },
        "output_tokens": 408,
        "total_tokens": 772
      }
     */

    public @Nullable Integer getInputTokens() {
        return readInteger("input_tokens");
    }

    public @Nullable Integer getInputTokensForImage() {
        return readInteger("input_tokens_details", "image_tokens");
    }

    public @Nullable Integer getInputTokensForText() {
        return readInteger("input_tokens_details", "text_tokens");
    }

    public @Nullable Integer getOutputTokens() {
        return readInteger("output_tokens");
    }

    public @Nullable Integer getTotalTokens() {
        return readInteger("total_tokens");
    }

}
