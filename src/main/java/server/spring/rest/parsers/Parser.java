package server.spring.rest.parsers;

import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import java.io.Reader;
import java.io.StringReader;
import org.apache.log4j.Logger;

/**
 * @author Ilya Ivanov
 */
@Component
public abstract class Parser {
    /** log4j logger */
    static final Logger log = Logger.getLogger(Parser.class);

    public Parser() {}

    /**
     * {@code schema} is defaults to null
     * @see #parse(String, Class, Schema)
     */
    public final <T> T parse(String rawData, Class<T> tClass) throws Exception {
        return parse(rawData, tClass, null);
    }

    /**
     *
     * @param rawData raw string data
     * @param tClass class to instantiate
     * @param schema validation schema; may be null
     * @param <T> returned class
     * @return new instance of {@code tClass}
     * @throws Exception if any error occurred
     */
    public final <T> T parse(String rawData, Class<T> tClass, Schema schema) throws Exception {
        // create JAXBSettings context and initializing Unmarshaller
        JAXBContext jaxbContext = JAXBContext.newInstance(tClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new LogValidationEventHandler());
        jaxbUnmarshaller.setSchema(schema);

        final Source source = getSource(new StringReader(rawData));

        return (T) jaxbUnmarshaller.unmarshal(source);
    }

    /**
     * @param reader input raw source
     * @return structured source date to parse
     * @throws Exception if any IO error occurred
     */
    protected abstract Source getSource(Reader reader) throws Exception;

    /**
     * JAXB logger
     */
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
