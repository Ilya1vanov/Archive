package server.spring.rest.controller;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.*;
import server.spring.data.repository.EmployeeEntityRepository;
import server.spring.rest.exception.*;
import server.spring.rest.parsers.Parser;
import server.spring.rest.session.SessionManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    /** employee repository */
    private final EmployeeEntityRepository employeeEntityRepository;

    /** spring application context */
    private final ApplicationContext context;

    /** manager of HTTP sessions */
    private final SessionManager sessionManager;

    @Autowired
    public EmployeeController(EmployeeEntityRepository employeeEntityRepository, ApplicationContext context, SessionManager sessionManager) {
        this.employeeEntityRepository = employeeEntityRepository;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    /**
     * Returns list of meta-info of all existing employees
     * @param token validation token
     * @return list of meta-info of all existing employees
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<EmployeeMeta> findAll(@RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        validate(null, userEntity, Role.READER);
        final List<EmployeeEntity> all = employeeEntityRepository.findAll();
        return all.stream().map(EmployeeEntity::getEmployeeMeta).collect(Collectors.toList());
    }

    /**
     * Returns requested employee.
     * @param id requested id
     * @param token validation token
     * @return requested employee
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public Employee findOne(@PathVariable("id") Long id, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        validate(id, userEntity, Role.READER);
        final EmployeeEntity employeeEntity = employeeEntityRepository.findOne(id);
        Employee employee;
        try {
            employee = parse(employeeEntity.getData());
            employee.setId(id);
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException("Cannot retrieve employee from DB", e);
        }
        return employee;
    }

    /**
     * Creates and returns new employee with given spec.
     * @param employee new employee to persist
     * @param token validation token
     * @return new employee with given spec.
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", consumes = "application/xml")
    public Employee save(@RequestBody Employee employee, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        validate(null, userEntity, Role.WRITER);
        try {
            final EmployeeEntity employeeEntity = new EmployeeEntity(employee);
            final EmployeeEntity save = employeeEntityRepository.save(employeeEntity);
            employee.setId(save.getId());
            return employee;
        } catch (IOException | JAXBException e) {
            throw new RuntimeException("Cannot serialize in xml", e);
        }
    }

    /**
     * Returns updated employee.
     * @param id requested id
     * @param employee employee to edit
     * @param token validation token
     * @return updated employee.
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/xml")
    public Employee update(
            @PathVariable("id") Long id,
            @RequestBody Employee employee,
            @RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        validate(id, userEntity, Role.EDITOR);
        validateId(id, employee.getId());
        final EmployeeEntity one = employeeEntityRepository.findOne(id);
        one.setEmployeeMeta(employee.getEmployeeMeta());
        try {
            one.setData(employee);
        } catch (IOException | JAXBException e) {
            throw new RuntimeException("Cannot serialize in xml", e);
        }
        employeeEntityRepository.save(one);
        return employee;
    }

    /**
     * Delete specified employee
     * @param id requested id
     * @param token validation token
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/xml")
    public void delete(@PathVariable("id") Long id, @RequestHeader("Token") String token) throws HttpException {
        final UserEntity userEntity = sessionManager.validateByToken(token);
        validate(id, userEntity, Role.WRITER);
        employeeEntityRepository.delete(id);
    }

    private Employee parse(String rawData) throws HttpException {
        try {
            Parser parser = (Parser) context.getBean("SAX" + "Parser");
            return parser.parse(rawData, Employee.class);
        } catch (BeansException e) {
            throw new BadRequestException();
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate employee", e);
        }
    }

    private void validate(Long id, UserEntity user, Role role) throws ForbiddenException, NotFoundException {
        if (!user.hasPermission(role))
            throw new ForbiddenException("User must have " + role + " permissions to perform this operation");
        if (id != null)
            if (!employeeEntityRepository.exists(id))
                throw new NotFoundException("Employee with id " + id + " not found");
    }

    private void validateId(Long pathId, Long loginId) throws BadRequestException {
        if (!Objects.equals(pathId, loginId))
            throw new BadRequestException("ID mismatch: requested - " + pathId + " actual - " + loginId);
    }
}
