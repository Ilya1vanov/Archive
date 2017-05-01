package server.spring.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import server.spring.data.model.UserEntity;

import java.util.List;

/**
 * @author Ilya Ivanov
 */
@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByLoginAndPassword(String login, String password);
}
