package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.provider.config.ProviderConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

public class AzureProviderConfigElement extends ProviderConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "azure";

    public AzureProviderConfigElement(ConfigElement another) {
        super(another);
    }

    public AzureOpenAIProviderConfigElement openai() throws NotConfiguredException {
        ConfigElement openai = extract(AzureOpenAIProviderConfigElement.CONFIG_ELEMENT_NAME);
        return new AzureOpenAIProviderConfigElement(openai,this);
    }
}
