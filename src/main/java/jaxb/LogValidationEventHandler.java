package jaxb;

import org.apache.log4j.Logger;

/**
 * @author Ilya Ivanov
 */
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class LogValidationEventHandler implements ValidationEventHandler {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(LogValidationEventHandler.class);

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