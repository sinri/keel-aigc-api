package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.provider.config.ModelConfigElement;
import io.github.sinri.keel.aigc.api.provider.config.ProviderPassport;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class OpenAIChatCompletionsModelConfigElement extends ModelConfigElement {

    public OpenAIChatCompletionsModelConfigElement(ConfigElement another, @Nullable ProviderPassport providerPassport) {
        super(another, providerPassport);
    }

    @Deprecated
    public static OpenAIChatCompletionsModelConfigElement create(String modelCode, String apiKey, String resourceName, String deployment, String apiVersion) {
        ConfigElement configElement = new ConfigElement(modelCode);
        configElement.ensureChild("apiKey").setElementValue(apiKey);
        configElement.ensureChild("resourceName").setElementValue(resourceName);
        configElement.ensureChild("deployment").setElementValue(deployment);
        configElement.ensureChild("apiVersion").setElementValue(apiVersion);
        return new OpenAIChatCompletionsModelConfigElement(configElement, null);
    }

    public String apiKey() throws NotConfiguredException {
        return readString(List.of("apiKey"));
    }

    public String resourceName() throws NotConfiguredException {
        return readString(List.of("resourceName"));
    }

    public String deployment() throws NotConfiguredException {
        return readString(List.of("deployment"));
    }

    public String apiVersion() throws NotConfiguredException {
        return readString(List.of("apiVersion"));
    }

    public String getModelApiSchema() throws NotConfiguredException {
        return "https";
    }

    public Integer getModelApiPort() throws NotConfiguredException {
        return 443;
    }

    public String getModelApiHost() throws NotConfiguredException {
        return resourceName() + ".openai.azure.com";
    }

    public String getModelApiPath() throws NotConfiguredException {
        return "/openai/deployments/" + deployment() + "/chat/completions";
    }

    public String generateUrlForChatCompletionsApi() throws NotConfiguredException {
        String url = getModelApiSchema() + "://" + getModelApiHost();
        try {
            Integer modelApiPort = getModelApiPort();
            if (getModelApiSchema().equals("https") && modelApiPort != 443) {
                url += ":" + modelApiPort;
            }
        } catch (NotConfiguredException ignored) {
        }
        url += getModelApiPath();
        try {
            String apiVersion = apiVersion();
            url += "?api-version=" + apiVersion();
        } catch (NotConfiguredException ignored) {
        }
        return url;
    }
}
