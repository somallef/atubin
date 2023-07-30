import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentUpdateConverter implements CommandLine.ITypeConverter<EnvironmentUpdate> {

    private String environmentsFolder;

    public EnvironmentUpdateConverter(String environmentsFolder) {
        this.environmentsFolder = environmentsFolder;
    }

    @Override
    public EnvironmentUpdate convert(String value) throws Exception {

        int pos = value.lastIndexOf(':');

        if (pos < 0) {
            throw new CommandLine.TypeConversionException("Formato inválido");
        }

        String ambi = value.substring(0, pos);
        String updates = value.substring(pos + 1);

        var environments = Arrays.stream(ambi.split(","))
                .peek(e -> {
                    if (!Files.isDirectory(Paths.get(environmentsFolder, e)) &&
                            !Files.isDirectory(Path.of(e))) {
                        var message = CommandLine.Help.Ansi.AUTO.string("@|bold,red \u26A0 %s não é um ambiente Protheus e será desconsiderado|@");
                        System.out.printf(message, e).println();
                    }

                })
                .map(e -> Files.isDirectory(Path.of(e)) ? Path.of(e) : Paths.get(environmentsFolder, e))
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        return new EnvironmentUpdate(environments,
                updates.toLowerCase().contains("a"),
                updates.toLowerCase().contains("d"),
                updates.toLowerCase().contains("s"));
    }
}
