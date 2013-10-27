package xiaoyf.demo.aconex.gedcom

import xiaoyf.demo.aconex.gedcom.GParser
import javax.xml.stream.XMLStreamWriter
import groovy.xml.XmlUtil

class GParserTest extends spock.lang.Specification {
    
    def static USER_DIR = System.getProperty("user.dir")
    def static INPUT_DIR = USER_DIR + File.separator + "input" + File.separator
    def static OUTPUT_DIR = USER_DIR + File.separator + "output" + File.separator
    static {
        def output_dir = new File(OUTPUT_DIR);
        if (!output_dir.exists()) {
            output_dir.mkdir();
        }
    }

    def "Reports error and line number where data format is invalid"() {
        given: "A piece of INVALID data is given to parser"
        def in_valid_input = "0 @I1@ INDI\n\
                            1 NAME Jamis Gordon /Buck/\n\
                            2 SU Buck\n\
                            2 GIVN Jamis Gordon\n\
                            1 SEX M"
        
        when: "parse this piece of string"
        def input_reader = GParser.createReader(in_valid_input)
        def output_writer = GParser.createStringWriter()
        new GParser().parse(input_reader, output_writer)
        
        then: "expect exceptions to be thrown"
        def e = thrown(GParserException)
        e.getMessage().contains(GLine.Errors.INVALID_TAG.toString())
        e.getMessage().contains("at line 3")
    }

    def "Parse a complete node in gedcom format"() {
        given: "a piece of valid input as string"
        def valid_input = "0 @I1@ INDI\n\
        1 NAME Jamis Gordon /Buck/\n\
        2 SURN Buck\n\
        2 GIVN Jamis Gordon\n\
        1 SEX M"
        def expected_pretty_xml = '\
<?xml version="1.0" encoding="UTF-8"?><gedcom>\n\
  <indi id="@I1@">\n\
    <name value="Jamis Gordon /Buck/">\n\
      <surn>Buck</surn>\n\
      <givn>Jamis Gordon</givn>\n\
    </name>\n\
    <sex>M</sex>\n\
  </indi>\n\
</gedcom>\n'
        def parser = new GParser()

        when: "parse this piece of string"
        def input_reader = GParser.createReader(valid_input)
        def output_writer = GParser.createStringWriter()
        new GParser().parse(input_reader, output_writer)
        def output_xml = output_writer.toString()
        
        then: "expect output to be valid xml string like above expected one"
        def actual_pretty_xml = XmlUtil.serialize(output_xml)
        actual_pretty_xml == expected_pretty_xml

        and: "resulted xml can be correctly parsed and accessed"
        def gedcom = new XmlSlurper().parseText(output_xml)
        gedcom.name() == "gedcom"
        '@I1@'                  == gedcom.indi.@id.text()
        'Jamis Gordon /Buck/'   == gedcom.indi.name.@value.text()
        'Buck'                  == gedcom.indi.name.surn.text() 
        'Jamis Gordon'          == gedcom.indi.name.givn.text()
        'M'                     == gedcom.indi.sex.text()
    }
    
    def "Parse a complete gedcom document with two nodes"() {
        given: "There is a piece of valid input as string"
        def valid_input = "0 @I1@ INDI\n\
                            1 NAME Jamis Gordon /Buck/\n\
                            2 SURN Buck\n\
                            2 GIVN Jamis Gordon\n\
                            1 SEX M\n\
                            0 @I9@ INDI\n\
                            1 NAME L Messi"
        def expected_pretty_xml = '\
<?xml version="1.0" encoding="UTF-8"?><gedcom>\n\
  <indi id="@I1@">\n\
    <name value="Jamis Gordon /Buck/">\n\
      <surn>Buck</surn>\n\
      <givn>Jamis Gordon</givn>\n\
    </name>\n\
    <sex>M</sex>\n\
  </indi>\n\
  <indi id="@I9@">\n\
    <name>L Messi</name>\n\
  </indi>\n\
</gedcom>\n'
        def parser = new GParser()

        when: "parse this piece of string"
        def input_reader = GParser.createReader(valid_input)
        def output_writer = GParser.createStringWriter()
        new GParser().parse(input_reader, output_writer)
        def output_xml = output_writer.toString()
        
        then: "expect output to be valid xml string like the above expected one"
        def actual_pretty_xml = XmlUtil.serialize(output_xml)
        actual_pretty_xml == expected_pretty_xml

        and: "result xml be parsed and accessed"
        def gedcom = new XmlSlurper().parseText(output_xml)
        gedcom.name() == "gedcom"
        '@I1@'                  == gedcom.indi[0].@id.text()
        'Jamis Gordon /Buck/'   == gedcom.indi[0].name.@value.text()
        'Buck'                  == gedcom.indi[0].name.surn.text()
        'Jamis Gordon'          == gedcom.indi[0].name.givn.text()
        'M'                     == gedcom.indi[0].sex.text()
        '@I9@'                  == gedcom.indi[1].@id.text()
        'L Messi'               == gedcom.indi[1].name.text()
    }

    def "Parses a file as input and writes to an xml file as output"() {
        given: "There is a file with valid gedcom content"
        def input_filepath = INPUT_DIR + input_filename
        def output_filepath = OUTPUT_DIR + output_filename
        def expected_filepath = INPUT_DIR + expected_filename 
        def gparser = new GParser()
        def expected_output = new XmlSlurper().parse(new File(expected_filepath))

        when: "parse the file"
        def input_reader = GParser.createFileReader(input_filepath)
        def output_writer = GParser.createFileWriter(output_filepath)
        gparser.parse(input_reader, output_writer)
        
        then: "parsing output gets what is expected"
        def parser_output = new XmlSlurper().parse(new File(output_filepath))
        expected_output == parser_output
        
        where:
        input_filename      | output_filename   | expected_filename
        "input_1.txt"       | "output_1.txt"    | "expected_1.txt"
        "input_2.txt"       | "output_2.txt"    | "expected_2.txt"
    }

    def "Parse files where data is of invalid gedcom format"() {
        given: "There is a file with INVALID gedcom content"
        def invalid_input = INPUT_DIR + input_filename
        def invalid_output = OUTPUT_DIR + output_filename

        when: "parse the source file"
        def gparser = new GParser()
        def input_reader = GParser.createFileReader(invalid_input)
        def output_writer = GParser.createFileWriter(invalid_output)
        gparser.parse(input_reader, output_writer)
        
        then: "expects exception to be thrown with error messages"
        def e = thrown(GParserException)
        e != null
        e.getMessage().contains(error_msg_frag)

        where:
        input_filename      | output_filename   | error_msg_frag
        "invalid_1.txt"     | "invalid_o1.txt"  | GLine.Errors.INVALID_TAG.toString()
        "invalid_2.txt"     | "invalid_o2.txt"  | GLine.Errors.INVALID_LEVEL.toString()
        "invalid_3.txt"     | "invalid_o3.txt"  | GLine.Errors.INVALID_ID.toString()
    }

    def "Run main method without any arguments"() {
        given: "No argument is provided"
        def empty_args = [] as String[]
        def mock_stream = Mock(PrintStream)
        
        when: "Run Main class with empty args"
        def stdout = System.out
        System.setOut(mock_stream)
        GParser.main(empty_args)
        System.setOut(stdout)
        
        then: "some messages are printed to System.out"
        (1.._) * mock_stream.println(_)
    }
    

    def "Run main method with two arguments"() {
        given:
        def input_filepath = INPUT_DIR + "input_1.txt"
        def output_filepath = OUTPUT_DIR + "output_1.txt"
        def args = [input_filepath, output_filepath] as String[]
        def expected_file = INPUT_DIR + "expected_1.txt" 
        def expected_output = new XmlSlurper().parse(new File(expected_file))
        
        when:
        GParser.main(args)
        
        then: "some messages are printed to System.out"
        def output = new XmlSlurper().parse(new File(output_filepath))
        output == expected_output
    }
}
