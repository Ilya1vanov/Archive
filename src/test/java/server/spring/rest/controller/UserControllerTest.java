package server.spring.rest.controller;

import junitparams.JUnitParamsRunner;
import org.fest.assertions.Assertions;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.UserEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.NotFoundException;
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
    private final UserEntity userEditor = new UserEntity("123", "123", Role.EDITOR);
    private final UserEntity userAdmin = new UserEntity("1", "1", Role.ADMIN);
    private final String token = "token";
    private UserController SUT;

    @Before
    public void setUp() throws UnauthorizedException {
        userAdmin.setId(1L);
        when(userEntityRepository.findOne(userAdmin.getId())).thenReturn(userAdmin);
        when(userEntityRepository.findAll()).thenReturn(Arrays.asList(userEditor, userAdmin));
        when(sessionManager.validateByToken(token)).thenReturn(userEditor);
        SUT = new UserController(sessionManager, userEntityRepository);
    }

    @Test
    public void findAll() throws Exception {
        final List<UserEntity> all = SUT.findAll(token);

        verify(sessionManager).validateByToken(token);
        verify(userEntityRepository).findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
        Assertions.assertThat(all).hasSize(2);
        Assertions.assertThat(all).startsWith(userEditor, userAdmin);
    }

    @Test(expected = UnauthorizedException.class)
    public void findAllUnauthorizedException() throws Exception {
        when(sessionManager.validateByToken(token)).thenThrow(UnauthorizedException.class);
        SUT.findAll(token);
    }

    @Test(expected = UnauthorizedException.class)
    public void findOneUnauthorizedException() throws Exception {
        when(sessionManager.validateByToken(token)).thenThrow(UnauthorizedException.class);
        SUT.findOne(1L, token);
    }

    @Test(expected = ForbiddenException.class)
    public void updateForbiddenException() throws Exception {
        when(sessionManager.validateByToken(token)).thenReturn(userEditor);
        SUT.update(1L, token, userAdmin);
    }

    @Test(expected = NotFoundException.class)
    public void updateNotFoundException() throws Exception {
        when(userEntityRepository.exists(userAdmin.getId())).thenReturn(false);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.update(1L, token, userAdmin);
    }

    @Test(expected = BadRequestException.class)
    public void updateBadRequestException() throws Exception {
        when(userEntityRepository.exists(userAdmin.getId())).thenReturn(true);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.update(1L, token, userEditor);
    }

    @Test(expected = ForbiddenException.class)
    public void deleteForbiddenException() throws Exception {
        when(sessionManager.validateByToken(token)).thenReturn(userEditor);
        SUT.delete(1L, token);
    }

    @Test(expected = NotFoundException.class)
    public void deleteNotFoundException() throws Exception {
        when(userEntityRepository.exists(userAdmin.getId())).thenReturn(false);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.delete(1L, token);
    }

    @Test(expected = BadRequestException.class)
    public void deleteBadRequestException() throws Exception {
        when(userEntityRepository.exists(userAdmin.getId())).thenReturn(true);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.delete(1L, token);
    }
}