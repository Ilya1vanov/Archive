package server.springdata.repository;

//import com.github.springtestdbunit.DbUnitTestExecutionListener;
//import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class/*, DbUnitTestExecutionListener.class*/})
//@DatabaseSetup("classpath:META-INF/database.xml")
public class EmployeeEntityRepositoryTest {
    @Autowired
    private EmployeeEntityRepository employeeEntityRepository;

    @Test
    public void testFindAll() {

    }

    @Test
    public void testFindOne() {
    }

    @Test
    public void testSave() {
    }

    @Test
    public void testDelete() {
    }


}