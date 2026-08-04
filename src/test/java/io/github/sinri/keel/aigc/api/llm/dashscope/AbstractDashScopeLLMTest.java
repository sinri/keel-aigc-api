package io.github.sinri.keel.aigc.api.llm.dashscope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractDashScopeLLMTest {

    private static final String TEXT_PATH = "/api/v1/services/aigc/text-generation/generation";
    private static final String MULTIMODAL_PATH = "/api/v1/services/aigc/multimodal-generation/generation";

    @Test
    void appendsCompleteApiPathToHostOnlyBaseUrl() {
        assertEquals(
            "https://gateway.example.com/api/v1/services/aigc/text-generation/generation",
            AbstractDashScopeLLM.resolveEndpoint("https://gateway.example.com", TEXT_PATH)
        );
        assertEquals(
            "https://gateway.example.com/api/v1/services/aigc/multimodal-generation/generation",
            AbstractDashScopeLLM.resolveEndpoint("https://gateway.example.com/", MULTIMODAL_PATH)
        );
    }

    @Test
    void doesNotDuplicateApiPrefixForLegacyBaseUrl() {
        assertEquals(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation",
            AbstractDashScopeLLM.resolveEndpoint("https://dashscope.aliyuncs.com/api/v1", TEXT_PATH)
        );
        assertEquals(
            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation",
            AbstractDashScopeLLM.resolveEndpoint("https://dashscope.aliyuncs.com/api/v1/", MULTIMODAL_PATH)
        );
    }
}
