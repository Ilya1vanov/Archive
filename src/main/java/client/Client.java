package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jndi.toolkit.url.Uri;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import server.spring.data.model.UserEntity;
import server.spring.rest.protocol.RequestEntity;
import server.spring.rest.protocol.ResponseEntity;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ilya Ivanov
 */
//@SpringBootApplication
//@EnableAutoConfiguration(exclude = {
//                DataSourceAutoConfiguration.class,
//                DataSourceTransactionManagerAutoConfiguration.class,
//                HibernateJpaAutoConfiguration.class,
//                WebSocketAutoConfiguration.class,
//                WebSocketMessagingAutoConfiguration.class,
//                WebServicesAutoConfiguration.class,
//                WebMvcAutoConfiguration.class,
//                WebClientAutoConfiguration.class,
//                EmbeddedServletContainerAutoConfiguration.class})
@ComponentScan
//@ImportResource("classpath:spring/client-context.xml")
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
        final UserEntity entity = new UserEntity("ilya", "ilya");
        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RequestEntity request =
                RequestEntity.get(URI.create("/signin"))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Token", "archive-token")
                        .header("Alternates", "DOM")
                        .header("Authorization", entity.getLogin() + ":" + entity.getPassword()).build();
        System.out.println(request);


        ResponseEntity response = null;
        try (Socket self = new Socket(host, port);
             final ObjectOutputStream out = new ObjectOutputStream(self.getOutputStream());
             final ObjectInputStream in = new ObjectInputStream(self.getInputStream())
        ) {
            out.writeObject(request);
            self.shutdownOutput();

            System.out.println("Waiting for response...");

            response = (ResponseEntity) in.readObject();
            self.shutdownInput();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Client socket stream error: ", e);
        }
        System.out.println(response);
    }

    public static void main(String[] args) {
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/client-context.xml");
        final Client client = context.getBean(Client.class);
        client.run();
    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(Client.class);
//    }
//
//    @Bean
//    public CommandLineRunner demo(Client client) {
//        return (args) -> {
//            client.run();
//        };
//    }
}
