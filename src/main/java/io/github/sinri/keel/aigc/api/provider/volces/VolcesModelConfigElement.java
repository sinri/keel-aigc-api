package io.github.sinri.keel.aigc.api.provider.volces;

import io.github.sinri.keel.aigc.api.provider.config.ModelConfigElement;
import io.github.sinri.keel.aigc.api.provider.config.ProviderPassport;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import org.jspecify.annotations.Nullable;

public class VolcesModelConfigElement extends ModelConfigElement {
    public VolcesModelConfigElement(ConfigElement another, @Nullable ProviderPassport providerPassport) {
        super(another, providerPassport);
    }

    public String generateUrlForChatCompletionsApi() throws NotConfiguredException {
        String url = getModelApiSchema() + "://" + getModelApiHost();
        try {
            Integer modelApiPort = getModelApiPort();
            url += ":" + modelApiPort;
        } catch (NotConfiguredException ignored) {
        }
        url += getModelApiPath();
        return url;
    }
}
