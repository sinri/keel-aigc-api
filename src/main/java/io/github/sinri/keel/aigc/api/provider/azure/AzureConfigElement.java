package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.OpenAIConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

public class AzureConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "azure";

    public AzureConfigElement(ConfigElement another) {
        super(another);
    }

    public OpenAIConfigElement openai() throws NotConfiguredException {
        ConfigElement openai = extract(OpenAIConfigElement.CONFIG_ELEMENT_NAME);
        return new OpenAIConfigElement(openai);
    }

    public static AzureConfigElement create(OpenAIConfigElement openai) {
        ConfigElement configElement = new ConfigElement(CONFIG_ELEMENT_NAME);
        configElement.addChild(openai);
        return new AzureConfigElement(configElement);
    }
}
