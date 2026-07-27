package io.github.sinri.keel.aigc.api.llm.catholic.observation;

/**
 * The stage at which an observed LLM exchange failed.
 */
public enum CatholicLLMObservationStage {
    REQUEST_CONVERSION,
    HTTP_REQUEST,
    HTTP_RESPONSE,
    RESPONSE_BODY,
    STREAM_READING,
    RESPONSE_CONVERSION
}
