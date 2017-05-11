package server.spring.data.model;

import junitparams.JUnitParamsRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.fest.assertions.Assertions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class EmployeeEntityTest {
    private static final EmployeeMeta meta = new EmployeeMeta("Ilya", "Petrovich", "Ivanov");

    private static String data;

    @BeforeClass
    public static void setUp() throws IOException {
        final String pathname = "src/test/resources/testEmp.xml";
        data = new String(Files.readAllBytes(Paths.get(pathname)));
    }

    @Test
    public void getData() throws Exception {
        EmployeeEntity employeeEntity = new EmployeeEntity(meta, data);

        final String data = employeeEntity.getData();

        assertThat(data, is(EmployeeEntityTest.data));
    }

}