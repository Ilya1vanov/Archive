package server.springwebsock.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import server.springdata.model.UserEntity;
import server.springdata.repository.UserEntityRepository;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@Controller
public class UserController {
    @Autowired
    private UserEntityRepository userEntityRepository;

    @MessageMapping("/users")
    @SendTo("/topic/greetings")
    public List<UserEntity> greeting() throws Exception {
        final List<UserEntity> all = userEntityRepository.findAll();
        return all;
    }
}
