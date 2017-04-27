package springdata.repository;

import org.springframework.data.repository.CrudRepository;
import springdata.model.UserEntity;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
public interface UserEntityRepository extends CrudRepository<UserEntity, Long> {
//    Boolean countByLogin(String login);

    List<UserEntity> findByLoginAndPassword(String login, String password);
}
