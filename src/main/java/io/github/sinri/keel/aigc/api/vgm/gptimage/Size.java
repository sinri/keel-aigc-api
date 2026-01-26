package io.github.sinri.keel.aigc.api.vgm.gptimage;

/**
 * 图像尺寸枚举。
 * <p>
 * 用于指定生成图像的尺寸。
 *
 * @since 5.0.0
 */
public enum Size {
    /**
     * 正方形（1024x1024）。
     */
    SQUARE("1024x1024"),
    /**
     * 纵向（1024x1536）。
     */
    PORTRAIT("1024x1536"),
    /**
     * 横向（1536x1024）。
     */
    LANDSCAPE("1536x1024"),
    ;

    private final String sizeExpression;

    Size(String sizeExpression) {
        this.sizeExpression = sizeExpression;
    }

    /**
     * 获取尺寸表达式。
     *
     * @return 尺寸表达式字符串
     */
    public String getSizeExpression() {
        return sizeExpression;
    }
}
