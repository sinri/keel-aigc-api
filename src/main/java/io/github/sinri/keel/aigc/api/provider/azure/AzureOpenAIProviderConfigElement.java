package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.provider.config.ModelConfigElement;
import io.github.sinri.keel.aigc.api.provider.config.ProviderConfigElement;
import io.github.sinri.keel.aigc.api.provider.config.ProviderPassport;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AzureOpenAIProviderConfigElement extends ProviderConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "openai";

    public AzureOpenAIProviderConfigElement(ConfigElement another, @Nullable ProviderPassport parent) {
        super(another, parent);
    }

    @Deprecated
    public static AzureOpenAIProviderConfigElement create(List<OpenAIChatCompletionsModelConfigElement> models) {
        ConfigElement configElement = new ConfigElement(CONFIG_ELEMENT_NAME);
        models.forEach(configElement::addChild);
        return new AzureOpenAIProviderConfigElement(configElement, null);
    }

    public OpenAIChatCompletionsModelConfigElement model(String modelIdentity) throws NotConfiguredException {
        ModelConfigElement modelConfigElement = super.getModelConfigElement(modelIdentity);
        return new OpenAIChatCompletionsModelConfigElement(modelConfigElement, this);
    }

    /**
     *
     * @return a map of modelIdentity to ModelConfigElement
     */
    public Map<String, OpenAIChatCompletionsModelConfigElement> getModelMap() {
        return this.getChildNames().stream().map(k -> {
                       try {
                           return this.model(k);
                       } catch (NotConfiguredException e) {
                           return null;
                       }
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toMap(OpenAIChatCompletionsModelConfigElement::getModelIdentity, x -> x));
    }
}
