package server;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.ImportResource;
import server.spring.data.model.Employee;
import server.spring.data.model.EmployeeEntity;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.EmployeeEntityRepository;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.mapping.HandlerMapping;
import server.spring.rest.dispatcher.SocketDispatcher;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
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
@ImportResource("classpath:spring/network-context.xml")
@SpringBootApplication
public class Server implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(Server.class);

    /** connection port */
    private Integer port;

    /** requested maximum length of the queue of incoming connections */
    private Integer backlog;

    @Autowired private ApplicationContext context;

    private ExecutorService clients = Executors.newCachedThreadPool();

    public Server(Integer port, Integer backlog) {
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        log.info("Server run");
        try (ServerSocket serverSocket = new ServerSocket(port, backlog)) {
            while (!Thread.currentThread().isInterrupted()) {
                final Socket client = serverSocket.accept();
                clients.submit(context.getBean(SocketDispatcher.class, client));
            }
        } catch (IOException e) {
            log.error("Server socket closed: ", e);
        }
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Server.class, args);
    }

    @Bean
    public CommandLineRunner application(Server server, UserEntityRepository userEntityRepository, EmployeeEntityRepository employeeEntityRepository) {
        return (String... args) -> {
            userEntityRepository.save(new UserEntity("ilya", "ilya", Role.ADMIN));
            final JAXBContext jaxbContext = JAXBContext.newInstance(Employee.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final Employee employee = (Employee) unmarshaller.unmarshal(new File("H:\\Dropbox\\CSaN\\4th-semester\\CPP\\epam\\archive\\src\\main\\resources\\testEmp.xml"));
            employeeEntityRepository.save(new EmployeeEntity(employee));

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
