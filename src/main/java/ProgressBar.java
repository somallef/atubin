import picocli.CommandLine;

public class ProgressBar {

    private long total;
    private String prefixMessage = "";
    private String suffixMessage = "";
    private int sizeBar = 10;


    public ProgressBar(long total) {
        this.total = total;
    }

    public void setPrefixMessage(String prefixMessage) {
        this.prefixMessage = CommandLine.Help.Ansi.AUTO.string("@|bold,blue " + prefixMessage + "|@");
    }

    public void setSuffixMessage(String suffixMessage) {
        this.suffixMessage = suffixMessage;
    }

    public void setSizeBar(int sizeBar) {
        this.sizeBar = sizeBar;
    }

    public void stepTo(long done) {

        String iconLeftBoundary = "[";
        String iconDone = "=";
        String iconRemain = ".";
        String iconRightBoundary = "]";

        if (done > total) {
            throw new IllegalArgumentException();
        }
        long donePercents = (100 * done) / total;
        long doneLength = sizeBar * donePercents / 100;

        StringBuilder bar = new StringBuilder(iconLeftBoundary);
        for (int i = 0; i < sizeBar; i++) {
            if (i < doneLength) {
                bar.append(iconDone);
            } else {
                bar.append(iconRemain);
            }
        }
        bar.append(iconRightBoundary);

        System.out.print("\r" + prefixMessage + " " +  bar + " " + donePercents + "%\t" + suffixMessage);
        System.out.flush();

        if (done == total) {
            System.out.print("\n");
        }
    }
}
