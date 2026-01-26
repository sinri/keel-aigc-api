package io.github.sinri.keel.aigc.api.vgm.gptimage;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.multipart.MultipartForm;

import java.io.File;
import java.net.URLConnection;

/**
 * @since 5.0.0
 */
public class EditImageRequest {
    private final MultipartForm formDataParts;

    public EditImageRequest() {
        formDataParts = MultipartForm.create();
    }

    /**
     * The input image to edit.
     */
    public EditImageRequest setImage(String localImageFilePath) {
        File file = new File(localImageFilePath);
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        formDataParts.binaryFileUpload("image", file.getName(), file.getAbsolutePath(), mimeType);
        return this;
    }

    /**
     * The input image to edit.
     */
    public EditImageRequest setImage(Buffer buffer, String imageName, String mimeType) {
        formDataParts.binaryFileUpload("image", imageName, buffer, mimeType);
        return this;
    }

    /**
     * A mask image to define the area of the input image that the model should edit,
     * using fully transparent pixels (alpha of zero) in those areas.
     * Must be a valid image URL or base64-encoded image.
     */
    public EditImageRequest setMask(String localMaskImageFilePath) {
        File file = new File(localMaskImageFilePath);
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        formDataParts.binaryFileUpload("mask", file.getName(), file.getAbsolutePath(), mimeType);
        return this;
    }

    /**
     * A mask image to define the area of the input image that the model should edit,
     * using fully transparent pixels (alpha of zero) in those areas.
     * Must be a valid image URL or base64-encoded image.
     */
    public EditImageRequest setMask(Buffer buffer, String imageName, String mimeType) {
        formDataParts.binaryFileUpload("mask", imageName, buffer, mimeType);
        return this;
    }

    /**
     * A text description of how the input image should be edited.
     * The maximum length is 4000 characters.
     */
    public EditImageRequest setPrompt(String prompt) {
        formDataParts.attribute("prompt", prompt);
        return this;
    }

    /**
     * The number of images to generate.
     * By default, n is 1.
     */
    public EditImageRequest setNumber(int n) {
        formDataParts.attribute("n", "" + n);
        return this;
    }

    /**
     * There are three options for image quality: low, medium, and high.Lower quality images can be generated
     * faster.
     * The default value is high.
     *
     * @param quality "low", "medium", and "high"
     */
    public EditImageRequest setQuality(Quality quality) {
        formDataParts.attribute("quality", quality.name());
        return this;
    }

    /**
     * Specify the size of the generated images.
     * Must be one of 1024x1024, 1024x1536, or 1536x1024 for GPT-image-1 models.
     * Square images are faster to generate.
     *
     * @param size "1024x1024", "1024x1536", or "1536x1024"
     */
    public EditImageRequest setSize(Size size) {
        formDataParts.attribute("size", size.getSizeExpression());
        return this;
    }

    /**
     * Use the user parameter to specify a unique identifier for the user making the request.
     * This is useful for tracking and monitoring usage patterns.
     *
     * @param user The value can be any string, such as a user ID or email address.
     */
    public EditImageRequest setUser(String user) {
        formDataParts.attribute("user", user);
        return this;
    }

    /**
     * Use the output_compression parameter to specify the compression level for the generated image.
     * Input an integer between 0 and 100, where 0 is no compression and 100 is maximum compression.
     * The default is 100.
     *
     * @param output_compression [0,100]
     */
    public EditImageRequest setOutputCompression(int output_compression) {
        formDataParts.attribute("output_compression", "" + output_compression);
        return this;
    }


    public MultipartForm toMultipartForm() {
        return formDataParts;
    }
}
