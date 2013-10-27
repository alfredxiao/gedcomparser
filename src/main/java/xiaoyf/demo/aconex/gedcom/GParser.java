package xiaoyf.demo.aconex.gedcom;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class GParser {
    private static final int FLUSH_INTERVAL = 10; 
    
    public GParser() {
    }

    public void parse(Reader reader, Writer writer) throws GParserException {
        LineNumberReader lnReader = ensureLineNumberReader(reader);
        parse(lnReader, writer);
    }
    
    private void parse(LineNumberReader reader, Writer aWriter) throws GParserException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        int lineNumber = 0;
        
        try {
            writer = factory.createXMLStreamWriter(aWriter);

            GProcessor processor = new GProcessor(writer);
            processor.startDocument();
            
            String line = null;
            while ( (line = reader.readLine()) != null) {
                lineNumber = reader.getLineNumber();

                GLine curGLine = GLine.fromStringLine(lineNumber, line);
                processor.processLine(curGLine);
                
                if (lineNumber % FLUSH_INTERVAL == 0) writer.flush();
            }
            
            processor.endDocument();
        } catch(IOException e) {
            throw new GParserException("error occurs when processing line " + lineNumber, e);
        } catch(XMLStreamException e) {
            throw new GParserException("error occurs when processing line " + lineNumber, e);
        } finally {
            try {
                writer.flush();
                writer.close();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    private LineNumberReader ensureLineNumberReader(Reader reader) {
        if (reader instanceof LineNumberReader) {
            return (LineNumberReader) reader;
        } else {
            return createLineNumberReader(reader);
        }
    }
    
    private static LineNumberReader createLineNumberReader(Reader reader) {
        return new LineNumberReader(reader);
    }

    public static Reader createReader(String str) {
        return createLineNumberReader(new StringReader(str));
    }
    
    public static Reader createFileReader(String filepath) throws FileNotFoundException {
        return createLineNumberReader(new FileReader(filepath));
    }
    
    public static Reader createFileReader(File inputFile) throws FileNotFoundException {
        return createLineNumberReader(new FileReader(inputFile));
    }
    
    public static Writer createStringWriter() {
        return new StringWriter();
    }
    
    public static Writer createFileWriter(String filepath) throws IOException {
        return new FileWriter(filepath);
    }

    public static void main( String[] args ) {
        if (2 != args.length) {
            System.out.println("Usage: java -jar gedcoms-1.0.jar input_file output_file");
            return;
        }
        
        String input_path = args[0];
        String output_path = args[1];
        
        File inputFile = new File(input_path);
        if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
            throw new RuntimeException("Please make sure file [" + input_path + "] exists and accessible.");
        } else {
            System.out.println("INFO input file: " + inputFile.getAbsolutePath());
        }
        
        try {
            GParser parser = new GParser();
            Reader sourceReader = GParser.createFileReader(inputFile);
            Writer targetWriter = GParser.createFileWriter(output_path);
            
            parser.parse(sourceReader, targetWriter);
            System.out.println("Parsing completed.");
            
            File outputFile = new File(output_path);
            System.out.println("INFO output file: " + outputFile.getAbsolutePath());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

