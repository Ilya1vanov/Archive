package server.spring.rest.controller;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.springframework.data.domain.Example;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.session.SessionManager;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class LoginControllerTest {
    private final SessionManager sessionManager = mock(SessionManager.class);;
    private final UserEntityRepository userEntityRepository = mock(UserEntityRepository.class);
    private final UserEntity user = new UserEntity("123", "123");
    private final String token = "token";
    private LoginController SUT;

    @Before
    public void setUp() {
//        SUT = new LoginController(sessionManager, userEntityRepository);
    }

//    @Test
//    public void signIn() throws Exception {
//        when(sessionManager.getToken(user)).thenReturn(token);
//        when(userEntityRepository.findByLoginAndPassword(user.getLogin(), user.getPassword())).thenReturn(user);
//
//        final Pair<String, UserEntity> stringUserEntityPair = SUT.signIn(user.getLogin() + ":" + user.getPassword());
//
//        assertThat(stringUserEntityPair.getKey(), is(token));
//        assertThat(stringUserEntityPair.getValue(), is(user));
//        verify(sessionManager).getToken(user);
//        verify(userEntityRepository).findByLoginAndPassword(user.getLogin(), user.getPassword());
//    }

    @Test(expected = ForbiddenException.class)
    public void signInForbiddenException() throws Exception {
        when(userEntityRepository.findByLoginAndPassword(user.getLogin(), user.getPassword())).thenReturn(null);
        SUT.signIn(user.getLogin() + ":" + user.getPassword());
    }

    @Test(expected = BadRequestException.class)
    public void signInBadRequestException() throws Exception {
        SUT.signIn(user.getLogin() + "/" + user.getPassword());
    }
//
//    @Test
//    public void signOut() throws Exception {
//        doNothing().when(sessionManager).endSession(token);
//        SUT.signOut(token);
//        verify(sessionManager).endSession(token);
//    }

//    @Test
//    public void signUp() throws Exception {
//        when(userEntityRepository.save(user)).thenReturn(user);
//        when(sessionManager.getToken(user)).thenReturn(token);
//        when(userEntityRepository.findByLoginAndPassword(user.getLogin(), user.getPassword())).thenReturn(user);
//
//        final Pair<String, UserEntity> pair = SUT.signUp(user.getLogin() + ":" + user.getPassword());
//
//        assertThat(pair.getKey(), is(token));
//        assertThat(pair.getValue(), is(user));
//        verify(sessionManager).getToken(user);
//        verify(userEntityRepository).findOne((Example<UserEntity>) any());
//        verify(userEntityRepository).save((UserEntity)any());
//    }

    @Test(expected = BadRequestException.class)
    public void signUpBadRequestException() throws Exception {
        SUT.signUp(user.getLogin() + "/" + user.getPassword());
    }

    @Test(expected = BadRequestException.class)
    public void signUpAlreadyExists() throws Exception {
        when(userEntityRepository.findOne((Example<UserEntity>) any())).thenReturn(user);
        SUT.signUp(user.getLogin() + ":" + user.getPassword());
    }

}