package io.github.sinri.keel.aigc.api.provider.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.LargeLanguageModel;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;

abstract class AbstractDashscopeLargeLanguageModel extends LargeLanguageModel {
    private final String modelCode;

    protected AbstractDashscopeLargeLanguageModel(String modelCode) {
        this.modelCode = modelCode;
    }


    @Override
    public final String getRegisterCode() {
        return modelCode;
    }

    public final String getApiKey() throws NotConfiguredException {
        return ProviderConfigElement.load().dashscope().qwen().apiKey();
    }
}
