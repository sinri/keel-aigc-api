package io.github.sinri.keel.aigc.api.llm.catholic.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatholicAssistantMessageTest {

    @Test
    void acceptsNullTextAndToolCalls() {
        CatholicAssistantMessage message = new CatholicAssistantMessage(null, null);

        assertNull(message.text());
        assertTrue(message.toolCalls().isEmpty());
        assertTrue(message.contents().isEmpty());
        assertFalse(message.hasText());
        assertFalse(message.hasToolCalls());
    }

    @Test
    void factoryMethodsAcceptNullArguments() {
        assertFalse(CatholicAssistantMessage.ofText(null).hasText());
        assertFalse(CatholicAssistantMessage.ofToolCalls(null).hasToolCalls());
        assertFalse(CatholicAssistantMessage.ofMixed(null, null).hasToolCalls());
    }
}
