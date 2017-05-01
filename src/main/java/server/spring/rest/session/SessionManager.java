package server.spring.rest.session;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import server.spring.data.model.UserEntity;
import server.spring.rest.protocol.exception.UnauthorizedException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author Ilya Ivanov
 */
@Component
public class SessionManager {
    private Map<String, UserEntity> sessions = Maps.newConcurrentMap();

    public UserEntity authorize(String token) throws UnauthorizedException {
        final UserEntity userEntity = sessions.get(token);
        if (userEntity == null)
            throw new UnauthorizedException("Unknown token. Please, authorize first");
        return userEntity;
    }

    public String getToken(UserEntity userEntity) {
        String token = generateToken(userEntity);
        sessions.put(token, userEntity);
        return token;
    }

    public void endSession(String token) {
        sessions.remove(token);
    }

    private String generateToken(Object obj) {
        try {
            BigInteger hash = new BigInteger(1, MessageDigest.getInstance("SHA-256").digest(obj.toString().getBytes()));
            return hash.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new RuntimeException("Wrong crypto algorithm", e);
        }
    }
}
