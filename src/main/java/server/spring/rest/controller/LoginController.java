package server.spring.rest.controller;

import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.protocol.exception.BadRequestException;
import server.spring.rest.protocol.exception.ForbiddenException;
import server.spring.rest.protocol.exception.HttpException;
import server.spring.rest.session.SessionManager;

import java.util.List;

@Controller
@RequestMapping("")
public class LoginController {
    private final SessionManager sessionManager;

    private final UserEntityRepository userEntityRepository;

    @Autowired
    public LoginController(SessionManager sessionManager, UserEntityRepository userEntityRepository) {
        this.sessionManager = sessionManager;
        this.userEntityRepository = userEntityRepository;
    }

    @RequestMapping(path = "/signin", method = RequestMethod.GET, produces = "application/json")
    public Pair<String, UserEntity> signIn(@RequestHeader("Authorization") String authorization) throws HttpException {
        final String[] split = authorization.split(":");
        if (split.length != 2)
            throw new BadRequestException("Wrong authorize header. Right format \"login:passwordInSHA-256\"");

        final UserEntity byLoginAndPassword = userEntityRepository.findByLoginAndPassword(split[0], split[1]);

        if (byLoginAndPassword != null) {
            final String token = sessionManager.getToken(byLoginAndPassword);
            return new Pair<>(token, byLoginAndPassword);
        }

        throw new ForbiddenException();
    }

    @RequestMapping(path = "/signout")
    public void signOut(@RequestHeader("Token") String token) throws HttpException {
        sessionManager.endSession(token);
    }

    @RequestMapping(path = "/signup", method = RequestMethod.POST, produces = "application/json")
    public Pair<String, UserEntity> signUp(@RequestHeader("Authorization") String authorization) throws HttpException {
        final String[] split = authorization.split(":");
        if (split.length != 2)
            throw new BadRequestException("Wrong authorize header. Right format \"login:passwordInSHA-256\"");

        final UserEntity byLoginAndPassword = userEntityRepository.findOne(Example.of(new UserEntity(split[0], null)));
        if (byLoginAndPassword != null)
            throw new BadRequestException("User with this login is already exists");

        userEntityRepository.save(UserEntity.createWithCalculatedPassword(split[0], split[1]));

        return signIn(authorization);
    }
}
