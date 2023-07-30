import java.nio.file.Path;
import java.util.List;

public record EnvironmentUpdate(
        List<Path> environments,
        Boolean updateAppServer,
        Boolean updateDBAccess,
        Boolean updateSmartClient
) {}
