package server.spring.rest.dispatcher;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import server.spring.rest.exception.HttpException;
import server.spring.rest.exception.UnprocessableEntityException;
import server.spring.rest.session.SessionManager;

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
