package server.spring.rest.parsers;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.transform.JDOMSource;
import org.springframework.stereotype.Component;

import javax.xml.transform.Source;
import java.io.Reader;

/**
 * @author Ilya Ivanov
 */
@Component
public class JDOMParser extends Parser {
    @Override
    protected Source getSource(Reader reader) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(reader);
        return new JDOMSource(document);
    }
}
