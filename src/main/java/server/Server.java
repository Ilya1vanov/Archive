package server;

import jaxb.LogValidationEventHandler;
import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * @author Ilya Ivanov
 */
@XmlRootElement
public class Server implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Server.class);

    /** connection port */
    @XmlAttribute(name = "port", required = true)
    private int PORT;

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Server server = null;
        try {
            File schema = new File("./src/main/resources/xml/server.xsd");
            // create XML schema for validation
            SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            Schema XMLSchema = sf.newSchema( schema );

            // create JAXB context and initializing Marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(Server.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(XMLSchema);
            jaxbUnmarshaller.setEventHandler(new LogValidationEventHandler());

            // specify the location and name of xml file to be read
            File XMLFile = new File("./src/main/resources/xml/server.xml");

            // this will create Java object - port from the XML file
            server = (Server) jaxbUnmarshaller.unmarshal(XMLFile);
        } catch (JAXBException | SAXException e) {
            log.fatal("Unable to instantiate server", e);
            throw new RuntimeException("Unable to instantiate server", e);
        }
        server.run();
    }
}
