package io.github.sinri.keel.llm.api.sect.provider;

import io.github.sinri.keel.llm.api.catholic.request.MixChatRequest;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MixChatRequestMock {
    String getModelForMixChatRequest();

    default MixChatRequest createPlainMixChatRequest() {
        return MixChatRequest.create()
                             .setModel(getModelForMixChatRequest())
                             .addMessage(msg -> msg
                                     .setRole("system")
                                     .setTextContent("你是一个同声传译，用户说中文，你翻译成日文")
                             )
                             .addMessage(msg -> msg
                                     .setRole("user")
                                     .setTextContent("太君，这是我们找到的八路的情报")
                             );
    }
}
