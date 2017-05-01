package server.spring.rest.session;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import server.spring.data.model.UserEntity;
import server.spring.rest.protocol.exception.UnauthorizedException;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Ilya Ivanov
 */
@RunWith(JUnitParamsRunner.class)
public class SessionManagerTest {
    SessionManager SUT = new SessionManager();

    @Test
    public void authorize() throws Exception {
        final UserEntity userEntity = new UserEntity("1", "1");
        final String token = SUT.getToken(userEntity);
        final UserEntity authorize = SUT.authorize(token);
        assertThat(authorize, is(userEntity));
    }

    @Test(expected = UnauthorizedException.class)
    public void authorizeUnauthorizedException() throws Exception {
        SUT.authorize("");
    }

    @Test
    public void getToken() throws Exception {
        final String token = SUT.getToken(new UserEntity("1", "1"));
        Assertions.assertThat(token).isNotNull().isNotEmpty();
    }

    @Test(expected = UnauthorizedException.class)
    public void endSession() throws Exception {
        final UserEntity userEntity = new UserEntity("1", "1");
        final String token = SUT.getToken(userEntity);
        SUT.endSession(token);
        SUT.authorize(token);
    }

}