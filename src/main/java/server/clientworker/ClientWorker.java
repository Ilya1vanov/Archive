package server.clientworker;

import org.apache.log4j.Logger;

import java.net.Socket;

/**
 * @author Ilya Ivanov
 */
public class ClientWorker implements Runnable {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(ClientWorker.class);

    /** communication socket */
    Socket socket;

    public ClientWorker(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
