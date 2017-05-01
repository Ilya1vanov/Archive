package server.spring.rest.parsers;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import java.io.Reader;

/**
 * @author Ilya Ivanov
 */
@Component
public class DOMParser extends Parser {
    @Override
    protected Source getSource(Reader reader) throws Exception {
        // create STAXSource and get the DOM Builder Factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //Get the DOM Builder
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Load and Parse the XML document
        //document contains the complete XML as a Tree.
        Document document = builder.parse(new InputSource(reader));

        return new DOMSource(document);
    }
}
