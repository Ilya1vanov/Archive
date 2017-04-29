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
    List<UserEntity> findAll();

    List<UserEntity> findByLoginAndPassword(String login, String password);

    long countByLogin(String login);
}
