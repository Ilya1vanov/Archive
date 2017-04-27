package springdata.repository;

import org.springframework.data.repository.CrudRepository;
import server.employee.Employee;

/**
 * @author Ilya Ivanov
 */
public interface EmployeeEntityRepository extends CrudRepository<Employee, Long> {
}
