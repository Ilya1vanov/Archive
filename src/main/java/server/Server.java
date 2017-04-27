package server;

import org.apache.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Ilya Ivanov
 */
@ImportResource("classpath:spring/server-context.xml")
@SpringBootApplication
public class Server implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Server.class);

    /** connection port */
    private int port;

    /** requested maximum length of the queue of incoming connections */
    private int backlog;

    public Server(int port, int backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        System.out.println("server run");

    }

    public static void main(String[] args) {
        final ConfigurableApplicationContext context =
                SpringApplication.run(Server.class);
    }

    @Bean
    public CommandLineRunner demo(Server server) {
        return (args) -> {
            server.run();
        };
    }
}
