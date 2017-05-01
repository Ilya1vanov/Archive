package server.spring.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.protocol.exception.BadRequestException;
import server.spring.rest.protocol.exception.ForbiddenException;
import server.spring.rest.protocol.exception.HttpException;
import server.spring.rest.session.SessionManager;

import java.io.IOException;
import java.util.List;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/users")
public class UserController {
    private final SessionManager sessionManager;

    private final UserEntityRepository userEntityRepository;

    @Autowired
    public UserController(SessionManager sessionManager, UserEntityRepository userEntityRepository) {
        this.sessionManager = sessionManager;
        this.userEntityRepository = userEntityRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<UserEntity> findAll(@RequestHeader("Token") String token) throws HttpException {
        final UserEntity validatedUser = sessionManager.authorize(token);

        if (!validatedUser.hasReadPermission())
            throw new ForbiddenException();

        return userEntityRepository.findAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public UserEntity update(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token,
            @RequestBody String rawUserEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.authorize(token);

        if (!validatedUser.hasAdminPermission())
            throw new ForbiddenException("User must have ADMIN permissions to perform this operation");

        final UserEntity userEntity = parse(rawUserEntity);
        if (userEntity.getId().equals(id))
            throw new BadRequestException("ID mismatch");

        return userEntityRepository.save(userEntity);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token,
            @RequestBody String rawUserEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.authorize(token);

        if (!validatedUser.hasAdminPermission())
            throw new ForbiddenException();

        final UserEntity userEntity = parse(rawUserEntity);
        if (userEntity.getId().equals(id))
            throw new BadRequestException();

        userEntityRepository.delete(userEntity);
    }

    private UserEntity parse(String rawUserEntity) throws BadRequestException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(rawUserEntity, UserEntity.class);
        } catch (IOException e) {
            throw new BadRequestException("Invalid request body");
        }
    }
}
