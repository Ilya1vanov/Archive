package client.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import server.spring.data.model.UserEntity;

/**
 * @author Ilya Ivanov
 */
public class Session {
    private UserEntity userEntity;

    private String token;

    public Session(UserEntity userEntity, String token) {
        this.userEntity = userEntity;
        this.token = token;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public String getToken() {
        return token;
    }
}
