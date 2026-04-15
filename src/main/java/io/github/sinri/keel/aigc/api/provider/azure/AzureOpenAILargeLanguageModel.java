package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.aigc.api.provider.config.ProvidersConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.aigc.api.llm.catholic.LLMService;
import io.github.sinri.keel.aigc.api.llm.catholic.LargeLanguageModel;
import io.github.sinri.keel.aigc.api.provider.ProviderConfigElement;

public class AzureOpenAILargeLanguageModel extends LargeLanguageModel {
    public static final String MODEL_CODE_GPT_5_CHAT = "gpt-5-chat";

    private final String code;

    public AzureOpenAILargeLanguageModel(String code) {
        this.code = code;
    }

    @Override
    public String getRegisterCode() {
        return code;
    }

    private AzureOpenAIProviderConfigElement getOpenAIConfigElement() throws NotConfiguredException {
        return ProvidersConfigElement.load().azure().openai();
    }

    @Override
    public LLMService getService() throws NotConfiguredException {
        return new AzureOpenAIChatCompletionsService(getOpenAIConfigElement(), getLogger());
    }
}
