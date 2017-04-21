package client;

import jaxb.LogValidationEventHandler;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;

/**
 * @author Ilya Ivanov
 */
@XmlRootElement
public class Client implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Client.class);

    /** connection port */
    @XmlAttribute(name = "port", required = true)
    private int PORT;

    /** connection host */
    @XmlAttribute(name = "host", required = true)
    private String HOST;

    @Override
    public void run() {

    }

    public static void main(String[] args) {
        Client client = null;
        try {
            File schema = new File("./src/main/resources/xml/client.xsd");
            // create XML schema for validation
            SchemaFactory sf = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            Schema XMLSchema = sf.newSchema( schema );

            // create JAXB context and initializing Marshaller
            JAXBContext jaxbContext = JAXBContext.newInstance(Client.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(XMLSchema);
            jaxbUnmarshaller.setEventHandler(new LogValidationEventHandler());

            // specify the location and name of xml file to be read
            File XMLFile = new File("./src/main/resources/xml/client.xml");

            // this will create Java object - port from the XML file
            client = (Client) jaxbUnmarshaller.unmarshal(XMLFile);
        } catch (JAXBException | SAXException e) {
            log.fatal("Unable to instantiate client", e);
            e.printStackTrace();
        }
        client.run();
//        new Thread(client).start();
        // start GUI
    }
}
