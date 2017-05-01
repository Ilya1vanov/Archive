package server.spring.rest.parsers;

import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.Reader;

/**
 * @author Ilya Ivanov
 */
@Component
public class SAXParser extends Parser {
    @Override
    protected Source getSource(Reader reader) throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);

        // create SAXSource
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        return new SAXSource(xmlReader, new InputSource(reader));
    }
}
