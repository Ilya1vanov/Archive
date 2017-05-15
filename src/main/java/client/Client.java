package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Ilya Ivanov
 */
@ComponentScan(basePackages = {"client", "server.spring.rest.dispatcher.serializer", "server.spring.rest.parsers"})
@ImportResource("classpath:spring/network-context.xml")
public class Client {
    /** connection port */
    private Integer port;

    /** connection host */
    private String host;

    @Autowired
    public Client(Integer port, String host) {
        this.port = port;
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(Client.class);
    }
}
