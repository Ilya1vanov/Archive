package server.spring.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.HttpException;
import server.spring.rest.session.SessionManager;

import java.util.List;
import java.util.Objects;

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
        final UserEntity validatedUser = sessionManager.validateByToken(token);

        if (!validatedUser.hasReadPermission())
            throw new ForbiddenException();

        return userEntityRepository.findAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public UserEntity findOne(@PathVariable("id") Long id, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);

        if (!validatedUser.hasReadPermission())
            throw new ForbiddenException();

        return userEntityRepository.findOne(id);
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public UserEntity add(@RequestHeader("Token") String token, @RequestBody UserEntity userEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);

        if (!validatedUser.hasAdminPermission())
            throw new ForbiddenException("User must have ADMIN permissions to perform this operation");

        return userEntityRepository.save(userEntity);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public UserEntity update(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token,
            @RequestBody UserEntity userEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validateId(id, userEntity.getId());

        if (!validatedUser.hasAdminPermission())
            throw new ForbiddenException("User must have ADMIN permissions to perform this operation");

        return userEntityRepository.save(userEntity);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token,
            @RequestBody UserEntity userEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validateId(id, userEntity.getId());

        if (!validatedUser.hasAdminPermission())
            throw new ForbiddenException();

        userEntityRepository.delete(userEntity);
    }

    private void validateId(Long pathId, Long loginId) throws BadRequestException {
        if (!Objects.equals(pathId, loginId))
            throw new BadRequestException("ID mismatch: requested - " + pathId + " actual - " + loginId);
    }
}
