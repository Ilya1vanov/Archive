package server.spring.rest.parsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * @author Ilya Ivanov
 */
@Component
public abstract class Parser {
    static final Logger log = Logger.getLogger(Parser.class);

    private XMLValidator xmlValidator;

    Parser() {}

    @Autowired
    public Parser(XMLValidator xmlValidator) {
        this.xmlValidator = xmlValidator;
    }

    public final <T> T parse(String rawData, Class<T> tClass) throws Exception {
        Source xmlSource = new StreamSource(new StringReader(rawData));
        xmlValidator.validate(xmlSource);
        return parseInner(rawData, tClass);
    }

    private  <T> T parseInner(String rawData, Class<T> tClass) throws Exception {
        // create JAXBSettings context and initializing Unmarshaller
        JAXBContext jaxbContext = JAXBContext.newInstance(tClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new LogValidationEventHandler());

        final Source source = getSource(new StringReader(rawData));

        return (T) jaxbUnmarshaller.unmarshal(source);
    }

    protected abstract Source getSource(Reader reader) throws Exception;

    /**
     * @author Ilya Ivanov
     */
    private static class XMLValidator {
        private Schema schema;

        @Autowired
        public XMLValidator(File schema) throws SAXException {
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            this.schema = schemaFactory.newSchema(schema);
        }


        void validate(Source source) throws IOException, SAXException {
            Validator validator = schema.newValidator();
            validator.validate(source);
        }
    }

    public static class LogValidationEventHandler implements ValidationEventHandler {
        public boolean handleEvent(ValidationEvent event) {
            log.error("\nEVENT");
            log.error("SEVERITY:  " + event.getSeverity());
            log.error("MESSAGE:  " + event.getMessage());
            log.error("LINKED EXCEPTION:  " + event.getLinkedException());
            log.error("LOCATOR");
            log.error("    LINE NUMBER:  " + event.getLocator().getLineNumber());
            log.error("    COLUMN NUMBER:  " + event.getLocator().getColumnNumber());
            log.error("    OFFSET:  " + event.getLocator().getOffset());
            log.error("    OBJECT:  " + event.getLocator().getObject());
            log.error("    NODE:  " + event.getLocator().getNode());
            log.error("    URL:  " + event.getLocator().getURL());
            return true;
        }
    }
}
