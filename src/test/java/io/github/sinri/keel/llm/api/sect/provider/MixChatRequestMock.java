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
                                     .setTextContent(
                                             """
                                             賣炭翁，伐薪燒炭南山中。
                                             滿面塵灰煙火色，兩鬢蒼蒼十指黑。
                                             賣炭得錢何所營？身上衣裳口中食。
                                             可憐身上衣正單，心憂炭賤願天寒！
                                             夜來城外一尺雪，曉駕炭車輾冰轍。
                                             牛困人飢日已高，市南門外泥中歇。
                                             翩翩兩騎來是誰？黃衣使者白衫兒。
                                             手把文書口稱敕，迴車叱牛牽向北。
                                             一車炭重千余斤，宮使驅將惜不得！
                                             半匹紅紗一丈綾，繫向牛頭充炭直！
                                             """
                                     )
                             );
    }
}
