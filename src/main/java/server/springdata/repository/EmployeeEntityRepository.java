package server.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import server.springdata.model.EmployeeEntity;

/**
 * @author Ilya Ivanov
 */
@Repository
public interface EmployeeEntityRepository extends CrudRepository<EmployeeEntity, Long> {

}
