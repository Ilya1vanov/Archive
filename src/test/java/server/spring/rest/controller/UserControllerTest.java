package server.spring.rest.controller;

import junitparams.JUnitParamsRunner;
import org.fest.assertions.Assertions;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.UnauthorizedException;
import server.spring.rest.session.SessionManager;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class UserControllerTest {
    private final SessionManager sessionManager = mock(SessionManager.class);;
    private final UserEntityRepository userEntityRepository = mock(UserEntityRepository.class);
    private final UserEntity user1 = new UserEntity("123", "123");
    private final UserEntity user2 = new UserEntity("1", "1");
    private final String token = "token";
    private UserController SUT;

    @Before
    public void setUp() throws UnauthorizedException {
        when(userEntityRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(sessionManager.validateByToken(token)).thenReturn(user1);
        SUT = new UserController(sessionManager, userEntityRepository);
    }

    @Test
    public void findAll() throws Exception {
        final List<UserEntity> all = SUT.findAll(token);

        verify(sessionManager).validateByToken(token);
        verify(userEntityRepository).findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
        Assertions.assertThat(all).hasSize(2);
        Assertions.assertThat(all).startsWith(user1, user2);
    }

    @Test(expected = ForbiddenException.class)
    public void findAllForbiddenException() throws Exception {
        when(sessionManager.validateByToken(token)).thenReturn(new UserEntity("1", "2", Role.READER));
         SUT.findAll(token);
    }

//    @Test(expected = ForbiddenException.class)
//    public void updateForbiddenException() throws Exception {
//        when(sessionManager.validateByToken(token)).thenReturn(new UserEntity("1", "2", UserEntity.EDIT));
//        SUT.update(1L, token, "req");
//    }
//
//    @Test(expected = BadRequestException.class)
//    public void updateBadRequestException() throws Exception {
//        when(sessionManager.validateByToken(token)).thenReturn(new UserEntity("1", "2", UserEntity.ADMIN));
//        SUT.update(1L, token, "req");
//    }
//
//    @Test(expected = ForbiddenException.class)
//    public void deleteForbiddenException() throws Exception {
//        when(sessionManager.validateByToken(token)).thenReturn(new UserEntity("1", "2", UserEntity.EDIT));
//        SUT.delete(1L, token, "req");
//    }
//
//    @Test(expected = BadRequestException.class)
//    public void deleteBadRequestException() throws Exception {
//        when(sessionManager.validateByToken(token)).thenReturn(new UserEntity("1", "2", UserEntity.ADMIN));
//        SUT.delete(1L, token, "req");
//    }

}