package server.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.spring.data.model.EmployeeEntity;

/**
 * @author Ilya Ivanov
 */
@Repository
public interface EmployeeEntityRepository extends JpaRepository<EmployeeEntity, Long> {
}
