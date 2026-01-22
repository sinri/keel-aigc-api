package io.github.sinri.keel.llm.api.sect.provider.dashscope;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.QwenConfigElement;

public class DashscopeConfigElement extends ConfigElement {
    public DashscopeConfigElement(ConfigElement another) {
        super(another);
    }

    public QwenConfigElement qwen() throws NotConfiguredException {
        ConfigElement x = extract("qwen");
        if (x == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "qwen");
        }
        return new QwenConfigElement(x);
    }
}
