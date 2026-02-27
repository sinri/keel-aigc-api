package io.github.sinri.keel.aigc.api.llm.skill;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.FunctionAdapter;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.FunctionParameterDefinition;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.core.utils.IOUtils;
import io.github.sinri.keel.core.utils.io.AsyncOutputReadStream;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.common.dsl.SchemaType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@NullMarked
public class RunCommandFunctionAdapter implements FunctionAdapter {
    private final Keel keel;

    public RunCommandFunctionAdapter(Keel keel) {
        this.keel = keel;
    }

    @Override
    public String getFunctionName() {
        return "runCommand";
    }

    @Override
    public String getFunctionDescription() {
        return "在 Shell 命令行运行指定的命令";
    }

    @Override
    public List<FunctionParameterDefinition> getParameters() {
        return List.of(
                new FunctionParameterDefinition(
                        SchemaType.STRING,
                        "command",
                        "需要执行的命令"
                ),
                new FunctionParameterDefinition(
                        SchemaType.STRING,
                        "dir",
                        "命令执行的所在工作目录"
                )
        );
    }

    @Override
    public Future<String> call(@Nullable JsonObject arguments, @Nullable JsonObject fixedArgument) {
        Objects.requireNonNull(arguments);
        String command = arguments.getString("command");
        String dir = arguments.getString("dir");
        Objects.requireNonNull(command);
        Objects.requireNonNull(dir);
        if (command.contains("rm")) {
            return Future.failedFuture("rm is not allowed");
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "-c", command);
        processBuilder.directory(new File(dir));

        //        return keel.executeBlocking((Callable<String>) () -> {
        //            Process process = processBuilder.start();
        //            InputStream inputStream = process.getInputStream();
        //            StringBuilder sb = new StringBuilder();
        //            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
        //                while (true) {
        //                    String line = bufferedReader.readLine();
        //                    if (line == null) {
        //                        break;
        //                    } else {
        //                        sb.append(line).append("\n");
        //                        System.out.println(line);
        //                    }
        //                }
        //                return sb.toString();
        //            } catch (IOException e) {
        //                throw new RuntimeException(e);
        //            }
        //        });

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        InputStream inputStream = process.getInputStream();
        Buffer buffer = Buffer.buffer();
        AsyncOutputReadStream readStream = IOUtils.toReadStream(keel, inputStream, rs -> {
            rs.handler(x->{
                System.out.println("STDOUT | "+x.toString());
                buffer.appendBuffer(x);
            });
        });
        return readStream.readOver()
                         .compose(bytesRead -> {
                             return Future.succeededFuture(buffer.toString());
                         });
    }

}
