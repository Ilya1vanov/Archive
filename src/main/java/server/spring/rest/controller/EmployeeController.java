package server.spring.rest.controller;

import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.EmployeeEntity;
import server.spring.data.repository.EmployeeEntityRepository;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController implements RestControllerMarker {
    private final EmployeeEntityRepository employeeEntityRepository;

    @Autowired
    public EmployeeController(EmployeeEntityRepository employeeEntityRepository) {
        this.employeeEntityRepository = employeeEntityRepository;
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public List<EmployeeEntity> findAll() {
        return employeeEntityRepository.findAll();
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public EmployeeEntity findOne(@PathVariable("id") Long id) throws NotFoundException {
        EmployeeEntity employeeEntity = employeeEntityRepository.findOne(id);
        if (employeeEntity == null)
            // 404 Not Found
            throw new NotFoundException("");

        return employeeEntity;
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.POST, produces = "application/json", consumes = "application/xml")
    public EmployeeEntity save(@PathVariable("id") Long id, @RequestBody EmployeeEntity employeeEntity) {
        // validate representation
        // 403 Forbidden
        return employeeEntityRepository.save(employeeEntity);
        // 201 Created
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT, produces = "application/json", consumes = "application/xml")
    public EmployeeEntity update(@PathVariable("id") Long id, @RequestBody EmployeeEntity employeeEntity) {
        // validate representation
        return employeeEntityRepository.save(employeeEntity);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
    public void delete(@PathVariable("id") Long id, @RequestBody EmployeeEntity employeeEntity) {
        // validate representation
        employeeEntityRepository.delete(employeeEntity);
    }
}
