package server.spring.rest.parsers;

import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
import java.io.Reader;

/**
 * @author Ilya Ivanov
 */
@Component
public class StAXParser extends Parser {
    @Override
    protected Source getSource(Reader reader) throws Exception {
        // create STAXSource
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(reader);

        return new StAXSource(xmlStreamReader);
    }
}
