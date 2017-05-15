package server.spring.rest.controller;

import junitparams.JUnitParamsRunner;
import org.assertj.core.util.Lists;
import org.fest.assertions.Assertions;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import server.spring.data.model.*;
import server.spring.data.repository.EmployeeEntityRepository;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.NotFoundException;
import server.spring.rest.exception.UnauthorizedException;
import server.spring.rest.session.SessionManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class EmployeeControllerTest {
    private final SessionManager sessionManager = mock(SessionManager.class);;
    private final EmployeeEntityRepository employeeEntityRepository = mock(EmployeeEntityRepository.class);
    private final ApplicationContext context = mock(ApplicationContext.class);
    private final UserEntity userReader = new UserEntity("123", "123", Role.READER);
    private final UserEntity userWriter = new UserEntity("123", "123", Role.WRITER);
    private final UserEntity userEditor = new UserEntity("123", "123", Role.EDITOR);
    private final UserEntity userAdmin = new UserEntity("1", "1", Role.ADMIN);
    private final EmployeeMeta meta = new EmployeeMeta("first", "middle", "last");
    private EmployeeEntity employeeEntity1;
    private EmployeeEntity employeeEntity2;
    private Employee anyEmployee = new Employee();
    private Employee employee = new Employee(meta, 18L, Sex.male, "somewhere",2);
    private final String token = "token";
    private EmployeeController SUT;

    @Before
    public void setUp() throws UnauthorizedException, IOException {
        userAdmin.setId(1L);
        userEditor.setId(2L);
        userWriter.setId(3L);

        String data = "datadatadatadatadatadtadtadtadat";

        employeeEntity1 = new EmployeeEntity(meta, data);
        employeeEntity2 = new EmployeeEntity(meta, data);

        when(employeeEntityRepository.findOne(userAdmin.getId())).thenReturn(employeeEntity1);
        when(employeeEntityRepository.findAll()).thenReturn(Arrays.asList(employeeEntity1, employeeEntity2));
        when(sessionManager.validateByToken(token)).thenReturn(userEditor);
        SUT = new EmployeeController(employeeEntityRepository, context, sessionManager);
    }

    @Test
    public void findAll() throws Exception {
        final List<EmployeeMeta> all = SUT.findAll(token);

        verify(sessionManager).validateByToken(token);
        verify(employeeEntityRepository).findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
        Assertions.assertThat(all).hasSize(2);
        Assertions.assertThat(all).startsWith(employeeEntity1.getEmployeeMeta(), employeeEntity2.getEmployeeMeta());
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

    @Test
    public void save() throws Exception {
        when(employeeEntityRepository.save(any(EmployeeEntity.class))).thenReturn(employeeEntity1);
        SUT.save(employee, token);
        verify(employeeEntityRepository).save(any(EmployeeEntity.class));
    }

    @Test(expected = ForbiddenException.class)
    public void updateForbiddenException() throws Exception {
        when(sessionManager.validateByToken(token)).thenReturn(userWriter);
        SUT.update(1L, anyEmployee, token);
    }

    @Test(expected = NotFoundException.class)
    public void updateNotFoundException() throws Exception {
        when(employeeEntityRepository.exists(userAdmin.getId())).thenReturn(false);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.update(1L, anyEmployee, token);
    }

    @Test(expected = BadRequestException.class)
    public void updateBadRequestException() throws Exception {
        when(employeeEntityRepository.exists(userAdmin.getId())).thenReturn(true);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.update(1L, anyEmployee, token);
    }

    @Test(expected = ForbiddenException.class)
    public void deleteForbiddenException() throws Exception {
        when(sessionManager.validateByToken(token)).thenReturn(userReader);
        SUT.delete(1L, token);
    }

    @Test(expected = NotFoundException.class)
    public void deleteNotFoundException() throws Exception {
        when(employeeEntityRepository.exists(userAdmin.getId())).thenReturn(false);
        when(sessionManager.validateByToken(token)).thenReturn(userAdmin);
        SUT.delete(1L, token);
    }
}