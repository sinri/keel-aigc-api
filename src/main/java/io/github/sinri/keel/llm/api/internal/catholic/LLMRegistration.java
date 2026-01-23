package io.github.sinri.keel.llm.api.internal.catholic;

import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class LLMRegistration {
    private static final LLMRegistration instance = new LLMRegistration();
    private final Map<String, LargeLanguageModel> map = new ConcurrentHashMap<>();

    public static LLMRegistration shared() {
        return instance;
    }

    public static LLMRegistration getInstance() {
        return instance;
    }

    public LargeLanguageModel getModelWithCode(String code) {
        return Objects.requireNonNull(map.get(code));
    }

    public void registerModel(LargeLanguageModel serviceSpecification) {
        this.map.put(serviceSpecification.getCode(), serviceSpecification);
    }
}
