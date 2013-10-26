package xiaoyf.demo.aconex.gedcom

import javax.xml.stream.XMLStreamWriter

class GProcessorTest extends spock.lang.Specification {

    def "Process with a few lines with mock processor and xml writer"() {
        given: "There are a few lines where the last line starts a new node"
        def gline0 = GLine.fromStringLine(0, "0 @ID01@ INDI")
        def gline1 = GLine.fromStringLine(1, "1 NAME xiaoyf")
        def gline2 = GLine.fromStringLine(2, "2 GIVN Alfred")
        def gline3 = GLine.fromStringLine(3, "2 FAMN Xiao")
        def gline4 = GLine.fromStringLine(4, "0 @ID02@ INDI")
        def gline5 = GLine.fromStringLine(5, "1 DATE 2012")
        def mock_writer = Mock(XMLStreamWriter)
        def processor = Spy(GProcessor, constructorArgs: [mock_writer])
        
        when: "parse the 1st line"
        processor.startDocument()
        processor.processLine(gline0)
        
        then: "start an element with attr"
        with (processor) {
            0 * finishPreviousContent()
            0 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeStartElement("indi")
            1 * writeAttribute("id", "@ID01@")
            0 * writeCharacters(_)
            0 * writeEndElement()
        }
        
        expect: "pushed to stack"
        with (processor) {
            // don't put in then:with() block
            prevLine == gline0
            levelStack.size() == 2
            levelStack.peek() == 0
        }

        and:
        when: "parse the 2nd line"
        processor.processLine(gline1)

        then: "starts new sub-element name"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeStartElement("name")
            0 * writeAttribute(_, _)
            0 * writeCharacters(_)
            0 * writeEndElement()
        }

        expect: "stack increases"
        with (processor) {
            levelStack.peek() == 1
            levelStack.size() == 3
            prevLine == gline1
        }

        and:
        when: "parse the 3rd line"
        processor.processLine(gline2)

        then: "complete previous element content and then start a new sub-element givn"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeStartElement("givn")
            1 * writeAttribute("value", "xiaoyf")
            0 * writeCharacters(_)
            0 * writeEndElement()
        }

        expect: "stack continue to increase"
        with (processor) {
            levelStack.peek() == 2
            levelStack.size() == 4
            prevLine == gline2
        }
        
        and:
        when: "parse the 4th line"
        processor.processLine(gline3)

        then: "closes previous element with text data, and starts a new sibbling element famn"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeCharacters("Alfred")
            1 * writeEndElement()
            1 * writeStartElement("famn")
            0 * writeAttribute(_, _)
        }

        expect: "stack looks the same"
        with (processor) {
            levelStack.peek() == 2
            levelStack.size() == 4
            prevLine == gline3
        }
        
        and:
        when: "parse the 5th line"
        processor.processLine(gline4)

        then: "closes all previous unclosed elements, and start a new node with id"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeCharacters("Xiao")
            3 * writeEndElement()
            1 * writeStartElement("indi")
            1 * writeAttribute("id", "@ID02@")
        }

        expect: "stack decreases"
        with (processor) {
            levelStack.peek() == 0
            levelStack.size() == 2
            prevLine == gline4
        }
        
        and:
        when: "parse the 6th line"
        processor.processLine(gline5)

        then: "starts a new sub-element date"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            1 * beginCurrentElement()
        }
        with (mock_writer) {
            0 * writeCharacters(_)
            0 * writeEndElement()
            1 * writeStartElement("date")
            0 * writeAttribute(_, _)
        }

        expect: "stack increases"
        with (processor) {
            levelStack.peek() == 1
            levelStack.size() == 3
            prevLine == gline5
        }

        
        and:
        when: "finish parsing all lines"
        processor.endDocument()

        then: "multiple elements are closed"
        with (processor) {
            1 * finishPreviousContent()
            1 * closePreviousElements()
            0 * beginCurrentElement()
        }
        with (mock_writer) {
            1 * writeCharacters("2012")
            3 * writeEndElement()
            0 * writeStartElement(_)
            0 * writeAttribute(_, _)
        }

        expect: "stack is empty"
        with (processor) {
            levelStack.size() == 0
            prevLine == null
        }

    }
}
