package server.spring.rest.session;

import junitparams.JUnitParamsRunner;
import org.assertj.core.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.Test;
import server.spring.data.model.UserEntity;
import server.spring.rest.exception.UnauthorizedException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class SessionManagerTest {
//    SessionManager SUT = new SessionManager(userEntityRepository, loginController);
//
//    @Test
//    public void authorize() throws Exception {
//        final UserEntity userEntity = new UserEntity("1", "1");
//        final String token = SUT.getToken(userEntity);
//        final UserEntity authorize = SUT.validateByToken(token);
//        assertThat(authorize, is(userEntity));
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void authorizeUnauthorizedException() throws Exception {
//        SUT.validateByToken("");
//    }
//
//    @Test
//    public void getToken() throws Exception {
//        final String token = SUT.getToken(new UserEntity("1", "1"));
//        Assertions.assertThat(token).isNotNull().isNotEmpty();
//    }
//
//    @Test(expected = UnauthorizedException.class)
//    public void endSession() throws Exception {
//        final UserEntity userEntity = new UserEntity("1", "1");
//        final String token = SUT.getToken(userEntity);
//        SUT.endSession(token);
//        SUT.validateByToken(token);
//    }

}