package io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter;

import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.ContentFilterChoiceResultsImpl;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.ContentFilterDetectedResultImpl;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core.ContentFilterDetectedWithCitationResultImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;


public interface OpenAIContentFilterChoiceResults extends OpenAIContentFilterResultsBase {
    static OpenAIContentFilterChoiceResults wrap(JsonObject json) {
        return new ContentFilterChoiceResultsImpl(json);
    }

    // protected_material_text	contentFilterDetectedResult		No
    // protected_material_code	contentFilterDetectedWithCitationResult		No

    @Nullable
    default OpenAIContentFilterDetectedResult getProtectedMaterialText() {
        JsonObject protectedMaterialText = readJsonObject("protected_material_text");
        if (protectedMaterialText == null) {
            return null;
        }
        return new ContentFilterDetectedResultImpl(protectedMaterialText);
    }

    @Nullable
    default OpenAIContentFilterDetectedWithCitationResult getProtectedMaterialCode() {
        JsonObject protectedMaterialText = readJsonObject("protected_material_code");
        if (protectedMaterialText == null) {
            return null;
        }
        return new ContentFilterDetectedWithCitationResultImpl(protectedMaterialText);
    }
}
