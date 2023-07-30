import picocli.CommandLine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

@CommandLine.Command(
        name = "atubin"
//        ,subcommands = {
//                UpdateCommand.class
//        }
)
public class AtuBin implements Runnable {
    String environmentsFolder;
    URL dbaccessUrlDownload;
    URL appserverUrlDownload;
    URL smartclientUrlDownload;

    @CommandLine.Option(names = {"-u", "--update"})
    private boolean update;

    public static void main(String[] args) {
        //new CommandLine(new AtuBin()).execute(args);
        var atuBin = new AtuBin();
        CommandLine commandLine = new CommandLine(atuBin);
        var converter = new EnvironmentUpdateConverter(atuBin.environmentsFolder);
        var colorScheme = createColorScheme();

        commandLine.addSubcommand("update", new UpdateCommand()).registerConverter(EnvironmentUpdate.class, converter);
        commandLine.setColorScheme(colorScheme);
        commandLine.execute(args);

    }

    private static CommandLine.Help.ColorScheme createColorScheme() {
        return new CommandLine.Help.ColorScheme.Builder()
                .commands(CommandLine.Help.Ansi.Style.bold, CommandLine.Help.Ansi.Style.underline) // combine multiple styles
                .options(CommandLine.Help.Ansi.Style.fg_yellow) // yellow foreground color
                .parameters(CommandLine.Help.Ansi.Style.fg_yellow)
                .optionParams(CommandLine.Help.Ansi.Style.italic)
                .errors(CommandLine.Help.Ansi.Style.fg_red, CommandLine.Help.Ansi.Style.bold)
                .stackTraces(CommandLine.Help.Ansi.Style.italic)
                .build();
    }

    public AtuBin() {
        try (InputStream input = UpdateCommand.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            //load a properties file from class path, inside static method
            prop.load(input);

            environmentsFolder = prop.getProperty("environments.folder");
            dbaccessUrlDownload = new URL(prop.getProperty("download.url.dbaccess"));
            appserverUrlDownload = new URL(prop.getProperty("download.url.appserver"));
            smartclientUrlDownload = new URL(prop.getProperty("download.url.smartclient"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void run() {

        BufferedImage image = new BufferedImage(144, 32, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.drawString("AtuBin", 6, 24);
        try {
            ImageIO.write(image, "png", new File("text.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int y = 0; y < 32; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < 144; x++)
                sb.append(image.getRGB(x, y) == -16777216 ? " " : image.getRGB(x, y) == -1 ? "#" : "*");
            if (sb.toString().trim().isEmpty()) continue;
            System.out.println(sb);
        }

        System.out.println("Bem-vindo ao AtuBin!");

    }
}
