package io.github.sinri.keel.aigc.api.llm.catholic.request;

import io.github.sinri.keel.aigc.api.llm.catholic.request.MixChatRequest;
import io.github.sinri.keel.tesuto.KeelJUnit5Test;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;
@NullMarked
class MixChatRequestTest extends KeelJUnit5Test {

    @Test
    void testCreate1() {
        MixChatRequest request = MixChatRequest.create()
                                               .addMessage(msg -> msg
                                                       .setTextContent("Tell me a story about giant panda and red panda."));
        getUnitTestLogger().info("request: \n"+request.toJsonObject().encodePrettily());
    }

}