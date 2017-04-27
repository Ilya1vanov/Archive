package client;

import jaxb.LogValidationEventHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.USER_AGENT;

/**
 * @author Ilya Ivanov
 */
@ImportResource("classpath:spring/client-context.xml")
@SpringBootApplication
public class Client implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Client.class);

    /** connection port */
    private int port;

    /** connection host */
    private String host;

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void run() {
        HttpGet request = new HttpGet();
        // add request header
        request.addHeader("UserEntity-Agent", USER_AGENT);
        request.addHeader("application/xml", ACCEPT);
        try {
            request.setURI(new URI("/persons"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println(request);
    }

    public static void main(String[] args) {
        SpringApplication.run(Client.class);
    }

    @Bean
    public CommandLineRunner demo(Client client) {
        return (args) -> {
            client.run();
        };
    }
}
