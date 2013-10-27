package xiaoyf.demo.aconex.gedcom;

import java.io.IOException;
import java.util.Stack;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

// some fields/methods are set as public/default for the purpose of testing
class GProcessor {

    public Stack<Integer> levelStack = new Stack<Integer>();
    public XMLStreamWriter writer;
    public GLine prevLine = null;
    private GLine curLine = null;

    GProcessor(XMLStreamWriter writer) {
        this.writer = writer;
    }

    void processLine(GLine curLine) throws IOException, GParserException, XMLStreamException {
        if (levelStack.isEmpty()) throw new GParserException("startGDoc() must be called before processing");
        
        this.curLine = curLine;
        
        if (prevLine != null) postProcessPreviousLines();

        beginCurrentElement();
        
        this.prevLine = this.curLine;
    }
    
    void startDocument() throws XMLStreamException {
        initLevelStackWithMarker();
        writer.writeStartDocument();
        writer.writeStartElement("gedcom");
    }
    
    // no more lines left that need processing
    void endDocument() throws XMLStreamException {
        curLine = null;
        postProcessPreviousLines();

        writer.writeEndElement();
        writer.writeEndDocument();

        // reset member variables such that this process can be used again
        writer = null;
        prevLine = null;
        levelStack.clear();
    }

    // before converting current element, see if the line above is a parent element 
    // and has value (if both, write an @value)
    void postProcessPreviousLines() throws XMLStreamException {
        finishPreviousContent();

        closePreviousElements();
    }

    void closePreviousElements() throws XMLStreamException {
        int lastLevel = levelStack.peek();
        if (curLine != null) {
            if (curLine.getLevel() > lastLevel + 1) {
                System.err.println("WARNING at line " + curLine.getLineNumber() + ", level gap larger than 1");
            }
            
            // complete last element (not necessary previous line, but 
            // some parent nodes above current line that haven't been 
            // properly ended
            while (curLine.getLevel() <= lastLevel) {
                writer.writeEndElement();
                levelStack.pop();
                lastLevel = levelStack.peek();
            }
        } else {
            // This implies there is no lines left
            while (-1 != levelStack.peek()) {
                writer.writeEndElement();
                levelStack.pop();
            }
        }
    }

    // complete previous line's data, be it should be written as content or value attribute
    void finishPreviousContent() throws XMLStreamException {
        if (prevLine != null && prevLine.typeIsTagName() && !prevLine.dataIsEmpty()) {
            if (curLine != null && prevLine.isAncestralLevelOf(curLine)) {
                writer.writeAttribute("value", prevLine.getData());
            } else {
                writer.writeCharacters(prevLine.getData());
            }
        }
    }
    
    void beginCurrentElement() throws IOException, GParserException, XMLStreamException {

        switch (curLine.getTagType()) {
        case ID:
            writer.writeStartElement(curLine.getData().toLowerCase());
            writer.writeAttribute("id", curLine.getITag());
            break;
        case TAGNAME:
            writer.writeStartElement(curLine.getITag().toLowerCase());
            break;
        }

        levelStack.push(curLine.getLevel());
    }
    
    private void initLevelStackWithMarker() {
        levelStack.push(-1);
    }

}