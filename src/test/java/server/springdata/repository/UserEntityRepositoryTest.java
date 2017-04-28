package server.springdata.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import server.springdata.config.TestConfig;
import server.springdata.model.UserEntity;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.fest.assertions.Assertions;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:META-INF/database.xml")
public class UserEntityRepositoryTest {
    @Autowired
    private UserEntityRepository userEntityRepository;

    @Test
    public void testFindAll() {
        final List<UserEntity> all = userEntityRepository.findAll();
        Assertions.assertThat(all).isNotNull().isNotEmpty();
    }

    @Test
    public void testFindOne() {
        final UserEntity one = userEntityRepository.findOne(2L);
        assertThat("Cannot find record by ID", one.getLogin(), is("ilya"));
    }

    @Test
    public void testSave() {
        UserEntity user = new UserEntity("ololo", "1234");
        UserEntity savedUser = userEntityRepository.save(user);

        UserEntity userFromDb = userEntityRepository.findOne(savedUser.getId());

        assertThat(user, is(savedUser));
        assertThat(userFromDb.getId(), is(user.getId()));
    }

    @Test
    public void testDelete() {
        UserEntity user = new UserEntity("trololo", "12345");

        userEntityRepository.save(user);
        UserEntity userFromDb = userEntityRepository.findOne(user.getId());
        userEntityRepository.delete(userFromDb);

        assertNull(userEntityRepository.findOne(userFromDb.getId()));
    }

    @Test
    public void findByLoginAndPassword() {
        final List<UserEntity> entities = userEntityRepository.findByLoginAndPassword("ilya", "ilya");
        Assertions.assertThat(entities).isNotNull().isNotEmpty();
        assertThat(entities.size(), is(1));
        assertThat(entities.get(0).getLogin(), is("ilya"));
        assertThat(entities.get(0).getPassword(), is("ilya"));
    }
}