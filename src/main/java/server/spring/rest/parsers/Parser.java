package server.spring.rest.parsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Ilya Ivanov
 */
public abstract class Parser {
    private XMLValidator xmlValidator;

    Parser() {}

    @Autowired
    public Parser(XMLValidator xmlValidator) {
        this.xmlValidator = xmlValidator;
    }

    public final <T> T parse(String rawData, Class<T> tClass) throws IOException, SAXException {
        Source xmlSource = new StreamSource(new StringReader(rawData));
        xmlValidator.validate(xmlSource);
        return parseInner(rawData, tClass);
    }

    protected abstract <T> T parseInner(String rawData, Class<T> tClass);
}
