package io.github.sinri.keel.aigc.api.llm.skill;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @see <a href="https://agentskills.io/specification#frontmatter-required">Frontmatter (required)</a>
 * @since 5.0.0
 */
public interface SkillFrontmatter {
    /**
     * Skill 的命名，对应文件夹名称应一致。
     * <ul>
     * <li>Must be 1-64 characters</li>
     * <li>May only contain unicode lowercase alphanumeric characters and hyphens ({@code a-z} and {@code -})</li>
     * <li>Must not start or end with {@code -}</li>
     * <li>Must not contain consecutive hyphens ({@code --})</li>
     * <li>Must match the parent directory name</li>
     * </ul>
     *
     * @return the name of the mapped skill
     */
    String getName();

    /**
     * Skill 的描述，定义其用途和操作方式。
     * <ul>
     * <li>Must be 1-1024 characters</li>
     * <li>Should describe both what the skill does and when to use it</li>
     * <li>Should include specific keywords that help agents identify relevant tasks</li>
     * </ul>
     *
     * @return the description of the mapped skill
     */
    String getDescription();

    /**
     * Skill 的许可证，可选。
     * <ul>
     * <li>Specifies the license applied to the skill</li>
     * <li>We recommend keeping it short (either the name of a license or the name of a bundled license file)</li>
     * </ul>
     *
     * @return the optional license
     */
    @Nullable String getLicense();

    /**
     * Skill 的兼容性，可选。
     * <ul>
     * <li>Must be 1-500 characters if provided</li>
     * <li>Should only be included if your skill has specific environment requirements</li>
     * <li>Can indicate intended product, required system packages, network access needs, etc.</li>
     * </ul>
     *
     * @return the optional compatibility
     */
    @Nullable String getCompatibility();

    /**
     * Skill 的元数据，可选。
     * <p>
     * Arbitrary key-value mapping for additional metadata.
     * <p>
     * <ul>
     * <li>A map from string keys to string values</li>
     * <li>Clients can use this to store additional properties not defined by the Agent Skills spec</li>
     * <li>We recommend making your key names reasonably unique to avoid accidental conflicts</li>
     * </ul>
     *
     * @return the optional metadata
     */
    @Nullable Map<String, String> getMetadata();

    /**
     * Skill 的可用 tool，可选，实验性。
     * <ul>
     * <li>A space-delimited list of tools that are pre-approved to run</li>
     * <li>Experimental. Support for this field may vary between agent implementations</li>
     * </ul>
     *
     * @return the optional allowed-tools
     */
    @TechnicalPreview
    @Nullable String getAllowedTools();
}
