

import picocli.CommandLine;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Downloader {
    public Path download(URL url) throws IOException {
        // realizar a conexão e retornar um InputStream para os bytes do conteúdo

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        String auth = "allef.souza:_Alf@042023_";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestMethod("GET");

        String fileName = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
        var tempFile = Files.createTempFile(fileName, ".zip");

        try (BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(tempFile.toFile())) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            long fileSize = conn.getContentLengthLong();
            ProgressBar pb = new ProgressBar(fileSize);
            pb.setPrefixMessage("\u2B07 Baixando");
            pb.setSuffixMessage(fileName);

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                out.write(dataBuffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                pb.stepTo(totalBytesRead);

            }

        } catch (IOException e) {
            // tratar o erro de download/extracão
        }

        return tempFile;
    }

    public void extract(Path fromPath, Path path) throws IOException {

        File zipFile = fromPath.toFile();
        FileInputStream fileInputStream = new FileInputStream(zipFile.getCanonicalFile());

        try (ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fileInputStream))) {

            FileChannel channel = fileInputStream.getChannel();
            ZipEntry zipEntry = zipIn.getNextEntry();
            byte[] buffer = new byte[1024];
            long fileSize = zipFile.length();


//            ProgressBar pb = new ProgressBar(100);
//            pb.setPrefixMessage("Extraindo");

            while (zipEntry != null) {
                File newFile = newFile(path.toFile(), zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {

                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int read;

                    while ((read = zipIn.read(buffer)) > 0) {

                        fos.write(buffer, 0, read);
                        System.out.print("\r\u29D6 Extraindo " + zipEntry.getName());
                        System.out.flush();
                    }

                    fos.close();
                }

                zipEntry = zipIn.getNextEntry();
                //pb.stepTo(channel.position() * 100 / fileSize);

            }

            zipIn.closeEntry();
            zipIn.close();
            //pb.stepTo(100);
            var doneMessage = CommandLine.Help.Ansi.AUTO.string("@|bold,yellow \r✓ Ambiente %s foi atualizado|@");
            System.out.printf(doneMessage, path.toString()).println();

        }

    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {

        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}

