package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.vertx.core.json.JsonObject;

/**
 * @since 1.3.1
 */
public class GenerateImageRequest extends JsonifiableDataUnitImpl {
    public GenerateImageRequest(JsonObject jsonObject) {
        super(jsonObject);
    }

    public GenerateImageRequest() {
        this(new JsonObject());
    }

    public GenerateImageRequest setPrompt(String prompt) {
        this.ensureEntry("prompt", prompt);
        return this;
    }

    /**
     * Specify the size of the generated images.
     * Must be one of 1024x1024, 1024x1536, or 1536x1024 for GPT-image-1 models.
     * Square images are faster to generate.
     *
     * @param size "1024x1024", "1024x1536", or "1536x1024"
     */
    public GenerateImageRequest setSize(Size size) {
        this.ensureEntry("size", size.getSizeExpression());
        return this;
    }

    /**
     * There are three options for image quality: low, medium, and high.Lower quality images can be generated faster.
     * The default value is high.
     *
     * @param quality "low", "medium", and "high"
     */
    public GenerateImageRequest setQuality(Quality quality) {
        this.ensureEntry("quality", quality.name());
        return this;
    }

    /**
     * You can generate between one and 10 images in a single API call.
     * The default value is 1.
     *
     * @param n [1,10]
     */
    public GenerateImageRequest setNumber(int n) {
        this.ensureEntry("n", n);
        return this;
    }

    /**
     * Use the user parameter to specify a unique identifier for the user making the request.
     * This is useful for tracking and monitoring usage patterns.
     *
     * @param user The value can be any string, such as a user ID or email address.
     */
    public GenerateImageRequest setUser(String user) {
        this.ensureEntry("user", user);
        return this;
    }

    /**
     * Use the output_format parameter to specify the format of the generated image.
     * Supported formats are PNG and JPEG.
     * The default is PNG.
     *
     * @param outputFormat PNG or JPEG
     */
    public GenerateImageRequest setOutputFormat(OutputFormat outputFormat) {
        this.ensureEntry("output_format", outputFormat.name());
        return this;
    }

    /**
     * Use the output_compression parameter to specify the compression level for the generated image.
     * Input an integer between 0 and 100, where 0 is no compression and 100 is maximum compression.
     * The default is 100.
     *
     * @param output_compression [0,100]
     */
    public GenerateImageRequest setOutputCompression(int output_compression) {
        this.ensureEntry("output_compression", output_compression);
        return this;
    }


}
