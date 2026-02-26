package io.github.sinri.keel.aigc.api.internal.llm.catholic.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.skill.Skill;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillProvider;
import io.github.sinri.keel.aigc.api.llm.catholic.skill.SkillStub;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link SkillProvider} 的基于本地文件系统目录的实现。
 * <p>
 * 给定一个存放各 Skill 子目录的根目录路径，该实现通过 Vert.x {@link FileSystem} 异步扫描子目录，
 * 读取每个子目录中的 {@code SKILL.md} 文件以解析 frontmatter 并提供 Skill 实例。
 *
 * @see <a href="https://agentskills.io/what-are-skills">What are skills</a>
 */
public class LocalSkillProvider implements SkillProvider {
    private static final String SKILL_MD = "SKILL.md";

    private final Vertx vertx;
    private final String skillsDirPath;

    /**
     * 通过给定的一个存放各种 Skill 目录的目录来初始化。
     *
     * @param vertx         Vert.x 实例，用于异步文件系统操作
     * @param skillsDirPath Skills 根目录路径，其子目录各自代表一个 Skill
     */
    public LocalSkillProvider(Vertx vertx, String skillsDirPath) {
        this.vertx = vertx;
        this.skillsDirPath = skillsDirPath;
    }

    /**
     * 从指定目录（{@link #skillsDirPath}）扫描所有包含 {@code SKILL.md} 的子目录，
     * 解析各自的 frontmatter 并构建 {@link SkillStub} 列表返回。
     * <p>
     * 不含 {@code SKILL.md} 的子目录将被跳过。
     *
     * @return 异步返回 Skill 存根列表
     */
    @Override
    public Future<List<SkillStub>> getSkillStubs() {
        FileSystem fs = vertx.fileSystem();
        return fs.readDir(skillsDirPath)
                .compose(entries -> collectStubs(fs, entries, 0, new ArrayList<>()));
    }

    /**
     * 递归地逐个处理目录条目：检查是否存在 SKILL.md，存在则读取并解析 frontmatter 后加入结果列表。
     */
    private Future<List<SkillStub>> collectStubs(FileSystem fs, List<String> entries, int index, List<SkillStub> accumulated) {
        if (index >= entries.size()) {
            return Future.succeededFuture(accumulated);
        }
        String entryPath = entries.get(index);
        String skillMdPath = entryPath + File.separator + SKILL_MD;

        return fs.exists(skillMdPath)
                .compose(exists -> {
                    if (!exists) {
                        return collectStubs(fs, entries, index + 1, accumulated);
                    }
                    return fs.readFile(skillMdPath)
                            .compose(buf -> {
                                LocalSkillFrontmatter fm = LocalSkillFrontmatter.parseFromContent(buf.toString());
                                accumulated.add(new SkillStub(fm.getName(), fm.getDescription(), entryPath));
                                return collectStubs(fs, entries, index + 1, accumulated);
                            });
                });
    }

    /**
     * 根据 Skill 名称，在 {@link #skillsDirPath} 下查找对应子目录，
     * 构建 {@link LocalSkill} 实例返回。
     * <p>
     * {@link LocalSkill} 在构造时会同步读取并解析 SKILL.md 的 frontmatter，
     * 因此此处通过 {@link Vertx#executeBlocking} 在工作线程上执行，避免阻塞事件循环。
     *
     * @param name Skill 名称，应与子目录名一致
     * @return 异步返回对应的 {@link Skill} 实例
     */
    @Override
    public Future<Skill> getSkill(String name) {
        String skillRootPath = skillsDirPath + File.separator + name;
        return vertx.executeBlocking(() -> (Skill) new LocalSkill(vertx, skillRootPath));
    }
}
