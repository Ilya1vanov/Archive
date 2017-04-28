package server.springdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import server.springdata.model.UserEntity;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@Repository
public interface UserEntityRepository extends CrudRepository<UserEntity, Long> {
//    Boolean countByLogin(String login);
    List<UserEntity> findAll();

    List<UserEntity> findByLoginAndPassword(String login, String password);
}
