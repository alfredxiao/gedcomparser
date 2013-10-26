package xiaoyf.demo.aconex.gedcom

class GLineTest extends spock.lang.Specification {
    
    def "Parse a valid line string in gedcom format"() {
        when: "a valid line string is parsed"
        def gline = GLine.fromStringLine(lnum, line)
        
        then: "get valid fields and valid status via the result object"
        lnum        == gline.lineNumber
        level       == gline.level
        tag_name    == gline.tag
        data        == gline.data
        type        == gline.type
        
        where:
        line                    | lnum  | level | tag_name  | data          | type
        "0 @L2@ INDI"           | 0     | 0     | "@L2@"    | "INDI"        | GLineType.GID
        "  2  NAME"             | 12    | 2     | "NAME"    | null          | GLineType.GTAG
        " 3   DAT     @F0087@ " | 23    | 3     | "DAT"     | "@F0087@"     | GLineType.GTAG
        " 2  DATE  8 Mar 2004 " | 34    | 2     | "DATE"    | "8 Mar 2004"  | GLineType.GTAG
    }
    
    def "Parse line strings that are not valid in gedcom format" () {
        when: "invalid line strings are given to GLine parser"
        def gline = GLine.fromStringLine(lnum, line)
        
        then: "expect exceptions to be thrown with specific error message"
        def e = thrown(GParserException)
        e != null
        e.getMessage().contains(error_msg)
        
        where:
        lnum    | line              | error_msg
        1       | ""                | "level not found or invalid"
        2       | " 0 "             | "id/tag not found"
        3       | " 3 DA  "         | "invalid/missing id/tag"
        5       | " 882 mytag  "    | "invalid/missing id/tag"
        6       | " A BEA ORCL"     | "level not found or invalid"
        11      | " 10 @AL   QBE"   | "invalid/missing id/tag"
        13      | " 0 INDI"         | "invalid/missing id/tag"
    }

}
