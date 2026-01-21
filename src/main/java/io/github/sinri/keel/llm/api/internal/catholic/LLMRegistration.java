package io.github.sinri.keel.llm.api.internal.catholic;

import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;

public interface LLMRegistration {
    static LLMRegistration shared() {
        return LLMRegistrationImpl.getInstance();
    }

    LargeLanguageModel getModelWithCode(String code);

    void registerModel(LargeLanguageModel largeLanguageModel);
}
