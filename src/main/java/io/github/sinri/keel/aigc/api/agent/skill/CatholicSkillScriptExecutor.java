package io.github.sinri.keel.aigc.api.agent.skill;

import io.github.sinri.keel.aigc.api.internal.agent.skill.ProcessCatholicSkillScriptExecutor;
import io.vertx.core.Future;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/**
 * 由宿主应用提供的 Skill 脚本执行边界。
 * <p>
 * 实现负责选择运行时、隔离工作目录、限制时间和输出，并实施应用自身的授权策略。
 * CatholicAgent 不提供默认执行器。
 */
@FunctionalInterface
public interface CatholicSkillScriptExecutor {
    /**
     * 创建直接通过宿主操作系统进程执行脚本的执行器。
     *
     * <p>该执行器提供独立临时工作目录、超时和输出大小限制，但不提供网络、文件系统或
     * 子进程级别的安全隔离，不应直接用于执行不受信任的脚本。</p>
     */
    static CatholicSkillScriptExecutor createProcessExecutor() {
        return new ProcessCatholicSkillScriptExecutor();
    }

    /**
     * 创建直接通过宿主操作系统进程执行脚本的执行器。
     *
     * <p>该执行器提供独立临时工作目录、超时和输出大小限制，但不提供网络、文件系统或
     * 子进程级别的安全隔离，不应直接用于执行不受信任的脚本。</p>
     *
     * @param timeout        单次脚本执行的超时时间
     * @param maxOutputBytes 标准输出和标准错误各自最多保留的字节数
     */
    static CatholicSkillScriptExecutor createProcessExecutor(Duration timeout, int maxOutputBytes) {
        return new ProcessCatholicSkillScriptExecutor(timeout, maxOutputBytes);
    }

    /**
     * 创建直接通过宿主操作系统进程执行脚本的执行器。
     *
     * <p>该执行器提供独立临时工作目录、超时和输出大小限制，但不提供网络、文件系统或
     * 子进程级别的安全隔离，不应直接用于执行不受信任的脚本。</p>
     *
     * @param temporaryDirectory 用于创建脚本临时工作目录的根目录
     * @param timeout            单次脚本执行的超时时间
     * @param maxOutputBytes     标准输出和标准错误各自最多保留的字节数
     */
    static CatholicSkillScriptExecutor createProcessExecutor(
            Path temporaryDirectory,
            Duration timeout,
            int maxOutputBytes
    ) {
        return new ProcessCatholicSkillScriptExecutor(temporaryDirectory, timeout, maxOutputBytes);
    }

    Future<CatholicSkillScriptResult> execute(
            String skillName,
            CatholicSkillResource script,
            CatholicSkillResourceContent content,
            List<String> arguments
    );
}
