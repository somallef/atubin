import picocli.CommandLine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@CommandLine.Command(name = "update")
public class UpdateCommand implements Runnable {

    @CommandLine.ParentCommand
    private AtuBin atuBin;
    @CommandLine.Option(names = {"-t", "--ambientes"}, split = ";")
    List<EnvironmentUpdate> ambientes;

    @Override
    public void run() {
        try {
            updateEnvironments();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateEnvironments() throws MalformedURLException {

        Map<URL, Set<Path>> mapDownloads = new HashMap<>();

        ambientes.stream().forEach(conjuntoAmbientes -> {

            if (conjuntoAmbientes.updateAppServer()) {
                mapDownloads.computeIfAbsent(atuBin.appserverUrlDownload, k -> new HashSet<>())
                        .addAll(conjuntoAmbientes.environments().stream()
                                .map(p -> Paths.get(p.toString(), "tec", "appserver"))
                                .collect(Collectors.toSet()));
            }

            if (conjuntoAmbientes.updateDBAccess()) {
                mapDownloads.computeIfAbsent(atuBin.dbaccessUrlDownload, k -> new HashSet<>())
                        .addAll(conjuntoAmbientes.environments().stream()
                                .map(p -> Paths.get(p.toString(), "tec", "dbaccess"))
                                .collect(Collectors.toSet()));
            }

            if (conjuntoAmbientes.updateSmartClient()) {
                mapDownloads.computeIfAbsent(atuBin.smartclientUrlDownload, k -> new HashSet<>())
                        .addAll(conjuntoAmbientes.environments().stream()
                                .map(p -> Paths.get(p.toString(), "tec", "smartclient"))
                                .collect(Collectors.toSet()));
            }

        });

        downloadAndExtract(mapDownloads);
    }

    public static void downloadAndExtract(Map<URL, Set<Path>> mapDownloads) {
        // criar um objeto para realizar os downloads e extrações
        Downloader downloader = new Downloader();
        for (Map.Entry<URL, Set<Path>> entry : mapDownloads.entrySet()) {
            URL url = entry.getKey();
            Set<Path> paths = entry.getValue();

            try {
                var tempDir = downloader.download(url);
                for (Path path : paths) {
                    downloader.extract(tempDir, path);
                }
                Files.delete(tempDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}