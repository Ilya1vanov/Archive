package server.spring.rest.dispatcher;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import server.spring.rest.exception.UnprocessableEntityException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Ilya Ivanov
 */
@Component
@Scope("prototype")
public class ResponseEntitySerializer extends HttpSerializer<ResponseEntity> {
    private transient HttpStatus status;

    @Autowired
    private ResponseEntitySerializer(ResponseEntity response) throws UnprocessableEntityException {
        super(response.getHeaders(), response.getBody());
        this.status = response.getStatusCode();
    }

    @Override
    ResponseEntity getEntityInner() throws UnprocessableEntityException {
        return new ResponseEntity(body, headers, status);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(status);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        status = (HttpStatus) in.readObject();
    }
}
