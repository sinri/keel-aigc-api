package io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.request;

public enum WanxImageSynthesisModel {
    WanxV1("wanx-v1");
    private final String modelCode;

    WanxImageSynthesisModel(String modelCode) {
        this.modelCode = modelCode;
    }

    public static WanxImageSynthesisModel fromModelCode(String modelCode) {
        for (WanxImageSynthesisModel model : values()) {
            if (model.getModelCode().equals(modelCode)) {
                return model;
            }
        }
        throw new IllegalArgumentException("Invalid modelCode: " + modelCode);
    }

    public String getModelCode() {
        return modelCode;
    }
}
