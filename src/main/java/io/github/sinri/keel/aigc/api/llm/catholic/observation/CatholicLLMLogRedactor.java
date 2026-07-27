package io.github.sinri.keel.aigc.api.llm.catholic.observation;

/**
 * Removes secrets from provider-native data before it is written to a log.
 */
public interface CatholicLLMLogRedactor {
    String redactHeader(String name, String value);

    String redactBody(String body);

    String redactText(String text);

    static CatholicLLMLogRedactor secureDefault() {
        return new DefaultCatholicLLMLogRedactor();
    }
}
