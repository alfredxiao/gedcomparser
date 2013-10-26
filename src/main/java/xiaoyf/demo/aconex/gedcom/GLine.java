package xiaoyf.demo.aconex.gedcom;

import java.util.Scanner;

class GLine {
    
    public final static int MIN_TAG_LEN = 3;
    public final static int MAX_TAG_LEN = 4;
    public final static int MIN_ID_LEN = 3;

    private final int lineNumber;

    private final int level;
    private final String tag;
    private final String data;
    private final GLineType type;


    public GLineType getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getTag() {
        return tag;
    }
    
    public String getData() {
        return data;
    }

    private GLine(int lineNumber, int level, String tagName, String data, GLineType type) {
        this.lineNumber = lineNumber;
        this.level = level;
        this.tag = tagName;
        this.data = data;
        this.type = type;
    }
    
    static GLine fromStringLine(int lineNumber, String line) throws GParserException {
        if (null == line) throw new IllegalArgumentException("line cannot be null");
        
        Scanner lineScanner = new Scanner(line);
        
        // below field extraction steps must be done in sequence
        int level = scanLevel(lineScanner, lineNumber);
        String tag = scanTag(lineScanner, lineNumber);
        String data = scanData(lineScanner, lineNumber);
        GLineType type = null;
        
        if (0 == level && isValidId(tag)) {
            type = GLineType.GID;
        } else if (0 != level && isValidTagName(tag)) {
            type = GLineType.GTAG;
        } else {
            throw new GParserException("invalid/missing id/tag at line " + lineNumber);
        }

        return new GLine(lineNumber, level, tag, data, type);
    }

    private static boolean isValidTagName(String tagName) {
        return tagName != null && !tagName.startsWith("@") 
                && tagName.length() >= MIN_TAG_LEN && tagName.length() <= MAX_TAG_LEN;
    }

    private static boolean isValidId(String tagName) {
        return tagName != null && tagName.startsWith("@") 
                && tagName.endsWith("@") && tagName.length() >= MIN_ID_LEN;
    }
    
    static private int scanLevel(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNextInt()) {
            return scan.nextInt();
        } else {
            throw new GParserException("level not found or invalid at line " + lineNumber);
        }
    }
    
    static private String scanTag(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNext()) {
            return scan.next();
        } else {
            throw new GParserException("id/tag not found at line " + lineNumber);
        }
    }
    
    static private String scanData(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNext()) {
            return scan.nextLine().trim();
        } else {
            return null;
        }
    }
}

enum GLineType {
    GID,
    GTAG
}