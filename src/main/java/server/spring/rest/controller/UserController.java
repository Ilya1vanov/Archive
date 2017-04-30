package server.spring.rest.controller;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/users")
public class UserController implements RestControllerMarker {
    private final UserEntityRepository userEntityRepository;

    @Autowired
    public UserController(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<UserEntity> findAll() {
        return userEntityRepository.findAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public UserEntity findOne(@PathVariable("id") Long id) throws NotFoundException {
        UserEntity userEntity = userEntityRepository.findOne(id);
        if (userEntity == null)
            // 404 Not Found
            throw new NotFoundException("");

        return userEntity;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.POST, produces = "application/json", consumes = "application/xml")
    public UserEntity save(@PathVariable("id") Long id, @RequestBody UserEntity userEntity) {
        // validate representation
        // 403 Forbidden
        return userEntityRepository.save(userEntity);
        // 201 Created
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/xml")
    public UserEntity update(@PathVariable("id") Long id, @RequestBody UserEntity userEntity) {
        // validate representation
        return userEntityRepository.save(userEntity);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public void delete(@PathVariable("id") Long id, @RequestBody UserEntity userEntity) {
        // validate representation
        userEntityRepository.delete(userEntity);
    }
}
