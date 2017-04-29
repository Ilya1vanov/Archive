package jaxb;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Ilya Ivanov
 */
public class JAXBSettings {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(JAXBSettings.class);

    /***
     * Instantiate specified class via XML setting.
     * @param clazz class object
     * @param xsdSchema schema to validate; may be null to avoid validation
     * @param xmlFile file to load from
     * @param <T> class
     * @return new instance of specified class
     * @throws SAXException if a SAX error occurs during xsd schema parsing
     * @throws JAXBException if any unexpected errors occur while unmarshalling
     * @throws FileNotFoundException if the file does not exist, is a directory rather than a
     * regular file, or for some other reason cannot be opened for reading.
     */
    public static <T> T loadWithSettings(Class<T> clazz, File xsdSchema, File xmlFile)
            throws SAXException, JAXBException, FileNotFoundException {
        Schema XMLSchema = null;
        if (xsdSchema != null) {
            // create XML schema for validation
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            XMLSchema = sf.newSchema(xsdSchema);
        }

        // create JAXBSettings context and initializing Unmarshaller
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        jaxbUnmarshaller.setEventHandler(new LogValidationEventHandler());

        // create SAX parser
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setSchema(XMLSchema);

        // create SAXSource
        XMLReader xmlReader = null;
        try {
            xmlReader = spf.newSAXParser().getXMLReader();
        } catch (ParserConfigurationException e) {
            log.error("Bug report", e);
            throw new RuntimeException("Bug report", e);
        }
        SAXSource source = new SAXSource(xmlReader, new InputSource(new FileInputStream(xmlFile)));

        // this will create Java object from the XML file
        return (T) jaxbUnmarshaller.unmarshal(source);
    }

    /**
     * Save specified object to XML file.
     * @param obj object to marshall
     * @param xsdSchema schema to validate; may be null to avoid validation
     * @param xmlFile file to save to
     * @param <T> class of {@code obj}
     * @throws JAXBException if any unexpected errors occur while marshalling
     * @throws SAXException if a SAX error occurs during xsd schema parsing
     */
    public static <T> void saveSettings(T obj, File xsdSchema, File xmlFile) throws JAXBException, SAXException {
        // create JAXBSettings context and initializing Marshaller
        JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();

        // create XML schema for validation
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        if (xsdSchema != null) {
            Schema XMLSchema = sf.newSchema(xsdSchema);
            marshaller.setSchema(XMLSchema);
        }

        final XmlSchema annotation = obj.getClass().getPackage().getAnnotation(XmlSchema.class);
        if (annotation != null)
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, annotation.location());

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setEventHandler(new LogValidationEventHandler());

        marshaller.marshal(obj, xmlFile);
    }

    private static class LogValidationEventHandler implements ValidationEventHandler {
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
