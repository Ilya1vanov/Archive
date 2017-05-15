package server.spring.rest.session;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.controller.LoginController;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.HttpException;
import server.spring.rest.exception.UnauthorizedException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * @author Ilya Ivanov
 */
@Component
public class SessionManager {
    /** map: token -> userEntity */
    private Map<String, UserEntity> sessions = Maps.newConcurrentMap();
    private Map<String, String> authToken = Maps.newConcurrentMap();

    /** user repository */
    private final UserEntityRepository userEntityRepository;

    @Autowired
    public SessionManager(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    public void authorize(RequestEntity request) throws HttpException {
        final HttpHeaders headers = request.getHeaders();

        final String path = request.getUrl().getPath();
        final List<String> tokens = headers.get("Token");
        final List<String> authorizations = headers.get("Authorization");

        if (authorizations != null && !authorizations.isEmpty()) {
            final String authorization = authorizations.get(0);
            final String[] split = authorization.split(":");
            if (split.length != 2)
                throw new BadRequestException("Wrong authorization header. Right format \"login:passwordInSHA-256\"");

            switch (path) {
                case LoginController.signInPath:  // sign in
                    signIn(split[0], split[1]);
                    break;
                case LoginController.signUpPath:  // sign up
                    signUp(split[0], split[1]);
                    break;
                default:
            }
        } else if (tokens != null && !tokens.isEmpty()) { // existed session
            final String token = tokens.get(0);
            if (path.equals(LoginController.signOutPath)) {// sign out
                endSession(token);
            } else {
                final UserEntity userEntity = sessions.get(token);
                if (userEntity == null)
                    throw new UnauthorizedException("Unknown token. Please, validateByToken first");
            }
        } else
            throw new UnauthorizedException("Permission denied");
    }

    public UserEntity validateByToken(String token) throws UnauthorizedException {
        final UserEntity userEntity = sessions.get(token);
        if (userEntity == null)
            throw new UnauthorizedException("Unknown token " + token + ". Please, authorize with token first");
        return userEntity;
    }

    public UserEntity validateByAuthorization(String authorization) throws UnauthorizedException {
        final UserEntity userEntity = sessions.get(authToken.get(authorization));
        if (userEntity == null)
            throw new UnauthorizedException("Unknown token. Please, validateByToken first");
        return userEntity;
    }

    public String getTokenByAuthorization(String authorization) {
        return authToken.get(authorization);
    }

    private void endSession(String token) {
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

    private void signIn(String login, String password) throws ForbiddenException {
        final UserEntity byLoginAndPassword = userEntityRepository.findByLoginAndPassword(login, password);

        if (byLoginAndPassword != null) {
            final String token = generateToken(byLoginAndPassword);
            sessions.put(token, byLoginAndPassword);
            authToken.put(login + ":" + password, token);
        } else
            throw new ForbiddenException("No user found");
    }

    private void signUp(String login, String password) throws HttpException {
        final UserEntity byLoginAndPassword = userEntityRepository.findByLogin(login);
        if (byLoginAndPassword != null)
            throw new BadRequestException("User with this login is already exists");

        userEntityRepository.save(UserEntity.createWithCalculatedPassword(login, password));
        signIn(login, password);
    }
}
