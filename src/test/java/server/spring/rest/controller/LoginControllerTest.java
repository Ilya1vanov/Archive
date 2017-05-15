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
import server.spring.rest.exception.UnauthorizedException;
import server.spring.rest.session.SessionManager;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class LoginControllerTest {
    private final SessionManager sessionManager = mock(SessionManager.class);;
    private final UserEntity user = new UserEntity("123", "123");
    private final String authorization = user.getLogin() + "/" + user.getPassword();
    private LoginController SUT;

    @Before
    public void setUp() throws UnauthorizedException {
        SUT = new LoginController(sessionManager);
        when(sessionManager.validateByAuthorization(authorization)).thenThrow(UnauthorizedException.class);
    }


    @Test(expected = UnauthorizedException.class)
    public void signInForbiddenException() throws Exception {
        SUT.signUp(user.getLogin() + "/" + user.getPassword());
        verify(sessionManager).validateByAuthorization(authorization);
    }

    @Test
    public void signOut() throws Exception {
        SUT.signOut();
    }

    @Test(expected = UnauthorizedException.class)
    public void signUpBadRequestException() throws Exception {
        SUT.signUp(authorization);
        verify(sessionManager).validateByAuthorization(authorization);
    }
}