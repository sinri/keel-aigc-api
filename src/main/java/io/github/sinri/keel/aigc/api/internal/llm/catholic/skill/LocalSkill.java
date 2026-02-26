package io.github.sinri.keel.aigc.api.internal.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillFrontmatter;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于本地文件系统目录的 {@link Skill} 接口实现。
 * <p>
 * 构造时同步读取 Skill 目录中的 {@code SKILL.md} 文件并解析 YAML frontmatter；
 * {@link #getContent()} 则通过 Vert.x {@link io.vertx.core.file.FileSystem} 异步读取全文。
 *
 * @see <a href="https://agentskills.io/specification">The complete format specification for Agent Skills.</a>
 */
public class LocalSkill implements Skill {
    private static final String SKILL_MD = "SKILL.md";

    private final Vertx vertx;
    private final String rootPath;
    private final SkillFrontmatter frontmatter;
    private final LateObject<String> lateContent = new LateObject<>();

    /**
     * 通过本地文件系统中的一个 Skill 目录路径，构建一个 Skill 实例。
     * <p>
     * 构造过程中会同步读取 {@code rootPath/SKILL.md} 文件，解析其 YAML frontmatter。
     * <p>
     * A skill is a directory containing at minimum a {@code SKILL.md} file,
     * and optional directories:
     * {@code scripts/},
     * {@code references/},
     * and {@code assets/}.
     *
     * @param vertx    Vert.x 实例，用于后续异步文件读取
     * @param rootPath Skill 对应的目录路径
     * @throws UncheckedIOException     若 SKILL.md 文件不存在或读取失败
     * @throws IllegalArgumentException 若 frontmatter 格式不合法或缺少必填字段
     */
    public LocalSkill(Vertx vertx, String rootPath) {
        this.vertx = vertx;
        this.rootPath = rootPath;
        this.frontmatter = parseFrontmatter();
    }

    /**
     * 同步读取 SKILL.md 并解析其 YAML frontmatter。
     */
    private LocalSkillFrontmatter parseFrontmatter() {
        Path skillMdPath = Path.of(rootPath, SKILL_MD);
        try {
            var content = Files.readString(skillMdPath, StandardCharsets.UTF_8);
            return LocalSkillFrontmatter.parseFromContent(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + skillMdPath, e);
        }
    }

    @Override
    public SkillFrontmatter getFrontmatter() {
        return frontmatter;
    }

    @Override
    public final String getRootPath() {
        return rootPath;
    }

    /**
     * 异步读取该 Skill 目录下 {@code SKILL.md} 文件的完整内容。
     */
    @Override
    public Future<String> getContent() {
        if (lateContent.isInitialized()) {
            return Future.succeededFuture(lateContent.get());
        }
        String skillMdPath = rootPath + File.separator + SKILL_MD;
        return vertx.fileSystem().readFile(skillMdPath)
                    .compose(buffer -> Future.succeededFuture(buffer.toString()))
                    .compose(s -> {
                        this.lateContent.set(s);
                        return Future.succeededFuture(s);
                    });
    }

    @Override
    public SkillStub getSkillStub() {
        return new SkillStub(getName(), getDescription(), getRootPath());
    }
}
