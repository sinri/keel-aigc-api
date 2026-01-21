package io.github.sinri.keel.llm.api.internal.catholic;

import io.github.sinri.keel.llm.api.catholic.LargeLanguageModel;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LLMRegistrationImpl implements LLMRegistration {
    private static final LLMRegistrationImpl instance = new LLMRegistrationImpl();

    public static LLMRegistrationImpl getInstance() {
        return instance;
    }

    private final Map<String, LargeLanguageModel> map = new ConcurrentHashMap<>();

    @Override
    public LargeLanguageModel getModelWithCode(String code) {
        return Objects.requireNonNull(map.get(code));
    }

    @Override
    public void registerModel(LargeLanguageModel serviceSpecification) {
        this.map.put(serviceSpecification.getCode(), serviceSpecification);
    }
}
