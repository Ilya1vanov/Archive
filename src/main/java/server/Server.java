package server;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.HandlerMapping;
import server.spring.rest.SocketDispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ilya Ivanov
 */
@ImportResource("classpath:spring/server-context.xml")
@SpringBootApplication
//@EnableAutoConfiguration(exclude = {
//        DataSourceAutoConfiguration.class,
//        WebSocketAutoConfiguration.class,
//        WebSocketMessagingAutoConfiguration.class,
//        WebServicesAutoConfiguration.class,
//        WebMvcAutoConfiguration.class,
//        WebClientAutoConfiguration.class,
//        EmbeddedServletContainerAutoConfiguration.class})
public class Server implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Server.class);

    /** connection port */
    private int port;

    /** requested maximum length of the queue of incoming connections */
    private int backlog;

    @Autowired
    ApplicationContext context;

    private ExecutorService clients = Executors.newCachedThreadPool();

    public Server(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        log.info("Server run");
        final HandlerMapping bean = context.getBean(HandlerMapping.class);

        try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
            while (!Thread.currentThread().isInterrupted()) {
                final Socket client = serverSocket.accept();
                clients.submit(new SocketDispatcher(client, bean));
            }
        } catch (IOException e) {
            log.error("Server socket closed: ", e);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(Server.class, args);
    }

    @Bean
    public CommandLineRunner application(Server server, UserEntityRepository repository) {
        return (String... args) -> {
            repository.save(new UserEntity("ilya", "ilya", UserEntity.ADMIN));
            final List<UserEntity> all = repository.findAll();
            log.debug(all);
            Thread serverThread = null;
            try {
                serverThread = new Thread(server);
                serverThread.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    log.info("Enter \"stop\" to stop the server: ");
                    String answer = reader.readLine();
                    if (answer.equalsIgnoreCase("stop"))
                        break;
                }
            } finally {
                if (serverThread != null)
                    serverThread.interrupt();
                Runtime.getRuntime().halt(0);
            }
        };
    }
}
