package server.spring.rest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import server.spring.rest.protocol.RequestEntity;
import server.spring.rest.protocol.ResponseEntity;

import java.io.*;
import java.net.Socket;

/**
 * @author Ilya Ivanov
 */
public class SocketDispatcher implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(SocketDispatcher.class);

    private Socket self;

    private final HandlerMapping handlerMapping;

    public SocketDispatcher(Socket self, HandlerMapping handlerMapping) {
        this.self = self;
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void run() {
        try (final ObjectInputStream in = new ObjectInputStream(self.getInputStream());
             final ObjectOutputStream out = new ObjectOutputStream(self.getOutputStream())) {

            final RequestEntity request = (RequestEntity) in.readObject();
            System.out.println(request);
            self.shutdownInput();

            ResponseEntity response;
            try {
                response = handlerMapping.handle(request);
            } catch (RuntimeException e) {
                log.error(e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("I am so sorry");
            }

            out.writeObject(response);
            System.out.println(response);
            self.shutdownOutput();
        } catch (ClassNotFoundException e) {
            log.error(e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.warn(e);
        }finally {
            try {
                self.close();
            } catch (IOException e) {
                log.warn("Socket closing error: ", e);
            }
        }
    }
}
