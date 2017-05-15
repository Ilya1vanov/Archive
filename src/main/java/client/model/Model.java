package client.model;

import client.http.Session;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author Ilya Ivanov
 */
@Component
public class Model {
    private ObjectProperty<Session> session = new SimpleObjectProperty<>(this, "session");

    private ObjectProperty<ResponseEntity> lastResponse = new SimpleObjectProperty<>(this, "lastResponse");

    private ObjectProperty<RequestEntity> lastGetRequest = new SimpleObjectProperty<>(this, "lastGetRequest");

    private RequestEntity previousGetRequest;

    {
        final Session fakeSession = new Session(null, null);
        setSession(fakeSession);
        final ResponseEntity<Object> fakeResponse = new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
        setLastResponse(fakeResponse);
    }

    public Session getSession() {
        return session.get();
    }

    public boolean isSessionPresent() {
        return getSession() != null;
    }

    public ObjectProperty<Session> sessionProperty() {
        return session;
    }

    public void setSession(Session session) {
        this.session.set(session);
    }

    public void resetSession() {
        this.setSession(null);
    }

    public ResponseEntity getLastResponse() {
        return lastResponse.get();
    }

    public ObjectProperty<ResponseEntity> lastResponseProperty() {
        return lastResponse;
    }

    public void setLastResponse(ResponseEntity lastResponse) {
        this.lastResponse.set(lastResponse);
    }

    public RequestEntity getLastGetRequest() {
        return lastGetRequest.get();
    }

    public ObjectProperty<RequestEntity> lastGetRequestProperty() {
        return lastGetRequest;
    }

    public void setLastGetRequest(RequestEntity lastGetRequest) {
        previousGetRequest = getLastGetRequest();
        this.lastGetRequest.set(lastGetRequest);
    }

    public RequestEntity getPreviousGetRequest() {
        return previousGetRequest;
    }
}
