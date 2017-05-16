package server.spring.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.HttpException;
import server.spring.rest.exception.NotFoundException;
import server.spring.rest.session.SessionManager;

import java.util.List;
import java.util.Objects;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/users")
public class UserController {
    /** manager of HTTP sessions */
    private final SessionManager sessionManager;

    /** user repository */
    private final UserEntityRepository userEntityRepository;

    @Autowired
    public UserController(SessionManager sessionManager, UserEntityRepository userEntityRepository) {
        this.sessionManager = sessionManager;
        this.userEntityRepository = userEntityRepository;
    }

    /**
     * Returns list of all user entities
     * @param token validation token
     * @return list of all user entities
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<UserEntity> findAll(@RequestHeader("Token") String token) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validate(null, validatedUser, Role.READER);
        return userEntityRepository.findAll();
    }

    /**
     * Identificate user and return it
     * @param token validation token
     * @param userEntity new user
     * @return identificate user and return it
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public UserEntity add(@RequestHeader("Token") String token, @RequestBody UserEntity userEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validate(null, validatedUser, Role.ADMIN);
        return userEntityRepository.save(userEntity);
    }

    /**
     * Returns requested user entity
     * @param id requested id
     * @param token validation token
     * @return requested user entity
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public UserEntity findOne(@PathVariable("id") Long id, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validate(id, validatedUser, Role.READER);
        return userEntityRepository.findOne(id);
    }

    /**
     *
     * @param id requested id
     * @param token validation token
     * @param userEntity user to modify
     * @return modified user
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/json")
    public UserEntity update(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token,
            @RequestBody UserEntity userEntity) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validateId(id, userEntity.getId());
        validate(id, validatedUser, Role.ADMIN);
        final UserEntity one = userEntityRepository.findOne(id);
        if (validatedUser.getId().equals(one.getId()))
            throw new BadRequestException("Cannot edit yourself");
        return userEntityRepository.save(userEntity);
    }

    /**
     *
     * @param id requested id
     * @param token validation token
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, consumes = "application/json")
    public void delete(@PathVariable("id") Long id, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity validatedUser = sessionManager.validateByToken(token);
        validate(id, validatedUser, Role.ADMIN);
        final UserEntity one = userEntityRepository.findOne(id);
        if (validatedUser.getId().equals(one.getId()))
            throw new BadRequestException("Cannot delete yourself");
        if (one.hasAdminPermission())
            throw new ForbiddenException("Cannot delete ADMIN");
        userEntityRepository.deleteById(id);
    }

    private void validate(Long id, UserEntity user, Role role) throws ForbiddenException, NotFoundException {
        if (!user.hasPermission(role))
            throw new ForbiddenException("User must have " + role + " permissions to perform this operation");
        if (id != null)
            if (!userEntityRepository.exists(id))
                throw new NotFoundException("User with id " + id + " not found");
    }

    private void validateId(Long pathId, Long loginId) throws BadRequestException {
        if (!Objects.equals(pathId, loginId))
            throw new BadRequestException("ID mismatch: requested - " + pathId + " actual - " + loginId);
    }
}
