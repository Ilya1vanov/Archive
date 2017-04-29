package server.springwebsock.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyHandler extends TextWebSocketHandler {
    private List<WebSocketSession> peers = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        final HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
        System.out.println(handshakeHeaders.getConnection());
        System.out.println(handshakeHeaders.getAccessControlRequestHeaders());
        peers.add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println(message);
        session.sendMessage(message);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println(status);
        peers.remove(session);
    }
}