package io.github.sinri.keel.aigc.api.vgm.gptimage;

/**
 * @since 1.3.1
 */
public enum Size {
    SQUARE("1024x1024"),
    PORTRAIT("1024x1536"),
    LANDSCAPE("1536x1024"),
    ;

    private final String sizeExpression;

    Size(String sizeExpression) {
        this.sizeExpression = sizeExpression;
    }

    public String getSizeExpression() {
        return sizeExpression;
    }
}
