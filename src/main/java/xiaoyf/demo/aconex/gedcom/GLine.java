package xiaoyf.demo.aconex.gedcom;

import java.util.Scanner;

class GLine {
    
    public final static int MIN_TAG_LEN = 3;
    public final static int MAX_TAG_LEN = 4;
    public final static int MIN_ID_LEN = 3;

    private final int lineNumber;

    private final int level;
    private final String itag;
    private final String data;
    private final TagType tagType;

    public int getLineNumber() {
        return lineNumber;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getITag() {
        return itag;
    }
    
    public String getData() {
        return data;
    }
    
    public TagType getTagType() {
        return tagType;
    }
    
    public boolean typeIsId() {
        return tagType == TagType.ID;
    }
    
    public boolean typeIsTagName() {
        return tagType == TagType.TAGNAME;
    }
    
    public boolean dataIsEmpty() {
        return data == null;
    }
    
    public boolean isAncestralLevelOf(GLine another) {
        return this.level < another.level;
    }

    private GLine(int lineNumber, int level, String tagName, String data, TagType tagType) {
        this.lineNumber = lineNumber;
        this.level = level;
        this.itag = tagName;
        this.data = data;
        this.tagType = tagType;
    }
    
    static GLine fromStringLine(int lineNumber, String line) throws GParserException {
        if (null == line) throw new IllegalArgumentException("line cannot be null");
        
        Scanner lineScanner = new Scanner(line);
        
        // below field steps must be done in sequence to scan diff parts
        int level = scanLevel(lineScanner, lineNumber);
        String itag = scanITag(lineScanner, lineNumber);
        String data = scanOptionalData(lineScanner, lineNumber);

        // scanITag() makes sure itag is either valid ID or valid tagname
        TagType tagType = itag.startsWith("@") ? TagType.ID : TagType.TAGNAME;

        return new GLine(lineNumber, level, itag, data, tagType);
    }
    
    private static boolean isValidTagName(String tagName) {
        return tagName != null && !tagName.startsWith("@") && !tagName.endsWith("@")
                && tagName.length() >= MIN_TAG_LEN && tagName.length() <= MAX_TAG_LEN;
    }

    private static boolean isValidId(String tagName) {
        return tagName != null && tagName.startsWith("@") 
                && tagName.endsWith("@") && tagName.length() >= MIN_ID_LEN;
    }
    
    static private int scanLevel(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNext()) {
            String candidate = scan.next();
            try {
                return Integer.parseInt(candidate);
            } catch(NumberFormatException e) {
                throw new GParserException(formatErrorMsg(Errors.INVALID_LEVEL, lineNumber));
            }
        } else {
            throw new GParserException(formatErrorMsg(Errors.LEVEL_NOT_FOUND, lineNumber));
        }
    }
    
    static private String scanITag(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNext()) {
            String candidate = scan.next();

            if (candidate.startsWith("@")) {
                if (isValidId(candidate)) {
                    return candidate;
                } else {
                    throw new GParserException(formatErrorMsg(Errors.INVALID_ID, lineNumber));
                }
            } else {
                if (isValidTagName(candidate)) {
                    return candidate;
                } else {
                    throw new GParserException(formatErrorMsg(Errors.INVALID_TAG, lineNumber));
                }
            }
        } else {
            throw new GParserException(formatErrorMsg(Errors.ITAG_NOT_FOUND, lineNumber));
        }
    }
    
    static private String scanOptionalData(Scanner scan, int lineNumber) throws GParserException {
        if (scan.hasNext()) {
            return scan.nextLine().trim();
        } else {
            return null;
        }
    }
    
    static private String formatErrorMsg(Errors error, int lineNumber) {
        return error.toString() + " at line " + lineNumber;
    }
    
    enum Errors {
        LEVEL_NOT_FOUND,
        INVALID_LEVEL,
        ITAG_NOT_FOUND,
        INVALID_TAG,
        INVALID_ID
    }
    
    enum TagType {
        ID,
        TAGNAME
    }
}
