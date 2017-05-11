package server.spring.rest.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.Employee;
import server.spring.data.model.EmployeeEntity;
import server.spring.data.model.EmployeeMeta;
import server.spring.data.model.UserEntity;
import server.spring.data.repository.EmployeeEntityRepository;
import server.spring.rest.parsers.Parser;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.ForbiddenException;
import server.spring.rest.exception.HttpException;
import server.spring.rest.exception.NotFoundException;
import server.spring.rest.session.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeEntityRepository employeeEntityRepository;

    private final ApplicationContext context;

    private final SessionManager sessionManager;

    @Autowired
    public EmployeeController(EmployeeEntityRepository employeeEntityRepository, ApplicationContext context, SessionManager sessionManager) {
        this.employeeEntityRepository = employeeEntityRepository;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<EmployeeMeta> findAll() {
        final List<EmployeeEntity> all = employeeEntityRepository.findAll();
        return all.stream().map(EmployeeEntity::getEmployeeMeta).collect(Collectors.toList());
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public Employee findOne(
            @PathVariable("id") Long id,
            @RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        if (!userEntity.hasReadPermission())
            throw new ForbiddenException();

        final EmployeeEntity employeeEntity = employeeEntityRepository.findOne(id);
        if (employeeEntity == null)
            throw new NotFoundException();

        Employee employee;
        try {
            employee = parse("SAX", employeeEntity.getData());
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException("Cannot retrieve employee from DB", e);
        }

        return employee;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.POST, produces = "application/json", consumes = "application/xml")
    public Employee save(
            @PathVariable("id") Long id,
            @RequestBody String rawEmployeeEntity,
            @RequestHeader("Token") String token,
            @RequestHeader("Alternates") String parser) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        if (!userEntity.hasWritePermission())
            throw new ForbiddenException();

        Employee employee = parse(parser, rawEmployeeEntity);

        final EmployeeEntity employeeEntity;
        try {
            employeeEntity = new EmployeeEntity(employee, rawEmployeeEntity);
        } catch (IOException e) {
            throw new RuntimeException("Cannot instantiate employee", e);
        }
        employeeEntityRepository.save(employeeEntity);
        employee.setId(employeeEntity.getId());
        return employee;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/xml")
    public Employee update(
            @RequestBody String rawEmployeeEntity,
            @RequestHeader("Token") String token,
            @RequestHeader("Alternates") String parser) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        if (!userEntity.hasEditPermission())
            throw new ForbiddenException();

        Employee employee = parse(parser, rawEmployeeEntity);

        final EmployeeEntity employeeEntity;
        try {
            employeeEntity = new EmployeeEntity(employee, rawEmployeeEntity);
        } catch (IOException e) {
            throw new RuntimeException("Cannot instantiate employee", e);
        }
        employeeEntityRepository.save(employeeEntity);
        employee.setId(employeeEntity.getId());
        return employee;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/xml")
    public void delete(
            @RequestBody String rawEmployeeEntity,
            @RequestHeader("Token") String token,
            @RequestHeader("Alternates") String parser) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        if (!userEntity.hasWritePermission())
            throw new ForbiddenException();

        final Employee employee = parse(parser, rawEmployeeEntity);

        employeeEntityRepository.delete(employee.getId());
    }

    private Employee parse(String parserType, String rawData) throws HttpException {
        try {
            Parser parser = (Parser) context.getBean(parserType + "Parser");
            return parser.parse(rawData, Employee.class);
        } catch (BeansException e) {
            throw new BadRequestException();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate employee", e);
        }
    }
}
