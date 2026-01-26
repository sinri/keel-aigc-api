package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIConfigElement;

public class AzureConfigElement extends ConfigElement {
    public AzureConfigElement(ConfigElement another) {
        super(another);
    }

    public OpenAIConfigElement openai() throws NotConfiguredException {
        ConfigElement openai = extract("openai");
        if (openai == null) {
            throw new NotConfiguredException(getAbsoluteKeyChain(), "openai");
        }
        return new OpenAIConfigElement(openai);
    }
}
