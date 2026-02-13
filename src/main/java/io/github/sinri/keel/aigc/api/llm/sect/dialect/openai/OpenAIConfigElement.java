package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai;

import io.github.sinri.keel.aigc.api.provider.azure.AzureConfigElement;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class OpenAIConfigElement extends ConfigElement {
    public static final String CONFIG_ELEMENT_NAME = "openai";
    public OpenAIConfigElement(ConfigElement another) {
        super(another);
    }

    public OpenAIModelConfigElement model(String modelName) throws NotConfiguredException {
        var x = extract(modelName);
        return new OpenAIModelConfigElement(x);
    }

    public Map<String, OpenAIModelConfigElement> getModelMap() {
        return this.getChildNames().stream().map(k -> {
                       try {
                           return this.model(k);
                       } catch (NotConfiguredException e) {
                           return null;
                       }
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toMap(ConfigElement::getElementName, x -> x));
    }

    public static OpenAIConfigElement create(List<OpenAIModelConfigElement> models) {
        ConfigElement configElement=new ConfigElement(CONFIG_ELEMENT_NAME);
        models.forEach(configElement::addChild);
        return new OpenAIConfigElement(configElement);
    }
}
