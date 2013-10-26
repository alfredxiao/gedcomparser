package xiaoyf.demo.aconex.gedcom;

public class GParserException extends Exception {

    private static final long serialVersionUID = -214349028361610127L;

    public GParserException() {
    }

    public GParserException(String msg) {
        super(msg);
    }

    public GParserException(Throwable t) {
        super(t);
    }
    
    public GParserException(String msg, Throwable t) {
        super(msg, t);
    }
}
