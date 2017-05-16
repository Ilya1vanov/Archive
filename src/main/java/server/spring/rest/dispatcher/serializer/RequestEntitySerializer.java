package server.spring.rest.dispatcher.serializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import server.spring.rest.exception.HttpException;
import server.spring.rest.exception.UnprocessableEntityException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.URI;

/**
 * @author Ilya Ivanov
 */
@Component
@Scope("prototype")
public class RequestEntitySerializer extends HttpSerializer<RequestEntity> {
    private transient HttpMethod method;
    private transient URI url;
    private transient Type type;

    @Autowired
    private RequestEntitySerializer(RequestEntity request) throws UnprocessableEntityException {
        super(request.getHeaders(), request.getBody());
        this.method = request.getMethod();
        this.url = request.getUrl();
        this.type = request.getType();
    }

    /**
     *
     * @param requestEntity entity to write to stream
     * @return new Instance of {@code RequestEntitySerializer}, that is created to
     * be written in output stream
     * @throws UnprocessableEntityException if an IO error occurred
     */
    public static RequestEntitySerializer writer(RequestEntity requestEntity) throws UnprocessableEntityException {
        return new RequestEntitySerializer(requestEntity);
    }

    @Override
    RequestEntity getEntityInner() throws HttpException {
        final RequestEntity requestEntity = new RequestEntity(body, headers, method, url, type);
        return requestEntity;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(method);
        out.writeObject(url);
        out.writeObject(type);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        method = (HttpMethod) in.readObject();
        url = (URI) in.readObject();
        type = (Type) in.readObject();
    }
}
