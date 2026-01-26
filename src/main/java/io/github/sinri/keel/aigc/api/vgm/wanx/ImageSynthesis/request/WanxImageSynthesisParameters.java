package io.github.sinri.keel.aigc.api.vgm.wanx.ImageSynthesis.request;

import io.github.sinri.keel.aigc.api.internal.vgm.wanx.WanxImageSynthesisParametersImpl;
import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface WanxImageSynthesisParameters extends JsonifiableDataUnit {
    static WanxImageSynthesisParameters wrap(JsonObject x) {
        return new WanxImageSynthesisParametersImpl(x);
    }

    static WanxImageSynthesisParameters create() {
        return new WanxImageSynthesisParametersImpl();
    }

    default @Nullable Style getStyle() {
        var x = readString("style");
        if (x == null) {
            return null;
        }
        return Style.fromStyleCode(x);
    }

    default WanxImageSynthesisParameters setStyle(Style style) {
        ensureEntry("style", style.styleCode);
        return this;
    }

    default @Nullable Size getSize() {
        var x = readString("size");
        if (x == null) {
            return null;
        }
        return Size.fromSize(x);
    }

    default WanxImageSynthesisParameters setSize(Size size) {
        ensureEntry("size", size.size());
        return this;
    }

    default @Nullable Integer getN() {
        return readInteger("n");
    }

    default WanxImageSynthesisParameters setN(int n) {
        if (n < 1 || n > 4) throw new IllegalArgumentException("n must be between 1 and 4");
        ensureEntry("n", n);
        return this;
    }

    default @Nullable Long getSeed() {
        return readLong("seed");
    }

    /**
     * 图片生成时候的种子值，取值范围为(0, 4294967290) 。
     * 如果不提供，则算法自动用一个随机生成的数字作为种子，如果给定了，则根据 batch 数量分别生成 seed，seed+1，seed+2，seed+3为参数的图片。
     */
    default WanxImageSynthesisParameters setSeed(long seed) {
        ensureEntry("seed", seed);
        return this;
    }

    default @Nullable Float getRefStrength() {
        return readFloat("ref_strength");
    }

    /**
     * 期望输出结果与垫图（参考图）的相似度，取值范围[0.0, 1.0]，数字越大，生成的结果与参考图越相似
     */
    default WanxImageSynthesisParameters setRefStrength(float refStrength) {
        if (refStrength < 0.0 || refStrength > 1.0)
            throw new IllegalArgumentException("refStrength must be between 0.0 and 1.1");
        ensureEntry("ref_strength", refStrength);
        return this;
    }

    default @Nullable RefMode getRefMode() {
        var x = readString("ref_mode");
        if (x == null) {
            return null;
        }
        return RefMode.valueOf(x);
    }

    default WanxImageSynthesisParameters setRefMode(RefMode refMode) {
        ensureEntry("ref_mode", refMode.name());
        return this;
    }

    /**
     * 输出图像的风格
     */
    enum Style {
        Photography("<photography>"),// 摄影,
        Portrait("<portrait>"),//  人像写真,
        ThreeDimensionalCartoon("<3d cartoon>"),//  3D卡通,
        anime("<anime>"),//  动画,
        OilPainting("<oil painting>"),//  油画,
        Watercolor("<watercolor>"),// 水彩,
        Sketch("<sketch>"),//  素描,
        ChinesePainting("<chinese painting>"),//  中国画,
        FlatIllustration("<flat illustration>"),//  扁平插画,
        Auto("<auto>"),//  默认
        ;
        private final String styleCode;

        Style(String styleCode) {
            this.styleCode = styleCode;
        }

        public static Style fromStyleCode(String styleCode) {
            for (Style style : Style.values()) {
                if (style.getStyleCode().equals(styleCode)) {
                    return style;
                }
            }
            throw new IllegalArgumentException("Invalid styleCode: " + styleCode);
        }

        public String getStyleCode() {
            return styleCode;
        }
    }

    enum Size {
        LANDSCAPE("1280*720"),
        SQUARE("1024x1024"),
        PORTRAIT("720*1280");
        private final String size;

        Size(String size) {
            this.size = size;
        }

        public static Size fromSize(String size) {
            for (Size style : Size.values()) {
                if (style.size().equals(size)) {
                    return style;
                }
            }
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        public String size() {
            return size;
        }
    }

    /**
     * 垫图（参考图）生图使用的生成方式，可选值为'repaint' （默认） 和 'refonly'; 其中 repaint代表参考内容，refonly代表参考风格
     */
    enum RefMode {
        repaint, refonly
    }
}
