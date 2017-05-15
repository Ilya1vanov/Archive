package server.spring.rest.session;

import junitparams.JUnitParamsRunner;
import org.fest.assertions.Assertions;
import org.hamcrest.core.IsNull;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.springframework.http.RequestEntity;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.UnauthorizedException;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;
/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class SessionManagerTest {
    private final UserEntityRepository userEntityRepository = mock(UserEntityRepository.class);
    private final UserEntity userAdmin = new UserEntity("1", "1", Role.ADMIN);
    private final String validAuthorization = userAdmin.getLogin() + ":" + userAdmin.getPassword();
    private final SessionManager SUT = new SessionManager(userEntityRepository);
    private final URI signInUri = URI.create("/signin");
    private final URI signUpUri = URI.create("/signup");
    private final URI signOutUri = URI.create("/signout");
    private final URI wrongUri = URI.create("/wrong");
    private final String token = "token";
    private RequestEntity requestEntity;


    @Before
    public void setUp() {
        requestEntity = RequestEntity.get(signInUri).header("Authorization", validAuthorization).build();
        when(userEntityRepository.findByLogin(userAdmin.getLogin())).thenReturn(userAdmin);
        when(userEntityRepository.findByLoginAndPassword(userAdmin.getLogin(), userAdmin.getPassword())).thenReturn(userAdmin);
    }

    @Test(expected = UnauthorizedException.class)
    public void authorizeUnauthorizedException() throws Exception {
        requestEntity = RequestEntity.get(wrongUri).build();
        SUT.authorize(requestEntity);
    }

    @Test(expected = BadRequestException.class)
    public void authorizeBadRequestException() throws Exception {
        requestEntity = RequestEntity.get(signInUri).header("Authorization", "unsplitable").build();
        SUT.authorize(requestEntity);
    }

    @Test(expected = BadRequestException.class)
    public void authorizeBadRequestExceptionOnExtraColon() throws Exception {
        requestEntity = RequestEntity.get(signInUri).header("Authorization", "uns:pl:it:able").build();
        SUT.authorize(requestEntity);
    }

    @Test(expected = ForbiddenException.class)
    public void authorizeForbiddenException() throws Exception {
        requestEntity = RequestEntity.get(signInUri).header("Authorization", "splitable:a").build();
        SUT.authorize(requestEntity);
        verify(userEntityRepository).findByLoginAndPassword(userAdmin.getLogin(), userAdmin.getPassword());
    }

    @Test
    public void authorize() throws Exception {
        SUT.authorize(requestEntity);
        verify(userEntityRepository).findByLoginAndPassword(userAdmin.getLogin(), userAdmin.getPassword());
    }

    @Test
    public void validateByToken() throws Exception {
        SUT.authorize(requestEntity);
        final String tokenByAuthorization = SUT.getTokenByAuthorization(validAuthorization);
        final UserEntity userEntity = SUT.validateByToken(tokenByAuthorization);
        assertThat(userEntity, is(userAdmin));
    }

    @Test
    public void validateByAuthorization() throws Exception {
        SUT.authorize(requestEntity);
        final UserEntity userEntity = SUT.validateByAuthorization(validAuthorization);
        assertThat(userEntity, is(userAdmin));
    }

    @Test
    public void getTokenByAuthorization() throws Exception {
        SUT.authorize(requestEntity);
        final String tokenByAuthorization = SUT.getTokenByAuthorization(validAuthorization);
        Assertions.assertThat(tokenByAuthorization).isNotNull().isNotEmpty();
    }
}