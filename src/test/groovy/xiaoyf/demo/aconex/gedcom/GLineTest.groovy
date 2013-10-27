package xiaoyf.demo.aconex.gedcom

class GLineTest extends spock.lang.Specification {

    def "Parse a valid line string in gedcom format"() {
        when: "a valid line string is parsed"
        def gline = GLine.fromStringLine(lnum, line)

        then: "get valid fields and valid status via the result object"
        lnum        == gline.lineNumber
        level       == gline.level
        itag        == gline.itag
        data        == gline.data
        tagType     == gline.tagType

        where:
        line                    | lnum  | level | itag      | data          | tagType
        "0 @L2@ INDI"           | 0     | 0     | "@L2@"    | "INDI"        | GLine.TagType.ID
        "  2  NAME"             | 1     | 2     | "NAME"    | null          | GLine.TagType.TAGNAME
        " 3   DAT     @F0087@ " | 2     | 3     | "DAT"     | "@F0087@"     | GLine.TagType.TAGNAME
        " 2  DATE  8 Mar 2004 " | 3     | 2     | "DATE"    | "8 Mar 2004"  | GLine.TagType.TAGNAME
        "0 TRLR"                | 4     | 0     | "TRLR"    | null          | GLine.TagType.TAGNAME
        "0 @L3@ FAM"            | 5     | 0     | "@L3@"    | "FAM"         | GLine.TagType.ID
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
        1       | ""                | GLine.Errors.LEVEL_NOT_FOUND.toString()
        2       | " 2A "            | GLine.Errors.INVALID_LEVEL.toString()
        3       | " A BEA ORCL"     | GLine.Errors.INVALID_LEVEL.toString()
        4       | "0"               | GLine.Errors.ITAG_NOT_FOUND.toString()
        5       | " 2 "             | GLine.Errors.ITAG_NOT_FOUND.toString()
        6       | " 3 DA  "         | GLine.Errors.INVALID_TAG.toString()
        7       | " 2 mytag  "      | GLine.Errors.INVALID_TAG.toString()
        8       | " 10 @AL   QBE"   | GLine.Errors.INVALID_ID.toString()
        9       | " 11 AL@   QBE"   | GLine.Errors.INVALID_TAG.toString()
    }
}
