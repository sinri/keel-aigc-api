package io.github.sinri.keel.llm.api.internal.catholic.message;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.message.MixChatVisionContentElement;
import io.vertx.core.json.JsonObject;

public class MixChatVisionContentElementImpl extends JsonifiableDataUnitImpl implements MixChatVisionContentElement {
    public MixChatVisionContentElementImpl() {
        super();
    }

    public MixChatVisionContentElementImpl(JsonObject jsonObject) {
        super(jsonObject);
    }


    @Override
    public ContentElementType getType() {
        return ContentElementType.valueOf(readString("type"));
    }

    @Override
    public MixChatVisionContentElement setType(ContentElementType contentElementType) {
        this.ensureEntry("type", contentElementType.name());
        return this;
    }

    @Override
    public String getText() {
        return readStringRequired("text");
    }

    @Override
    public MixChatVisionContentElement setText(String text) {
        setType(ContentElementType.text);
        this.ensureEntry("text", text);
        return this;
    }

    @Override
    public String getImage() {
        return readStringRequired("image");
    }

    @Override
    public MixChatVisionContentElement setImage(String image) {
        setType(ContentElementType.image);
        this.ensureEntry("image", image);
        return this;
    }
}
