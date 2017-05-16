package server.spring.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import server.spring.data.model.UserEntity;
import server.spring.rest.exception.HttpException;
import server.spring.rest.session.SessionManager;

@Controller
@RequestMapping("")
public class LoginController {
    /** manager of HTTP sessions */
    private final SessionManager sessionManager;

    /** url to sign in */
    public static final String signInPath = "/signin";

    /** url to sign out */
    public static final String signOutPath = "/signout";

    /** url to sign up */
    public static final String signUpPath = "/signup";

    @Autowired
    public LoginController(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     *
     * @param authorization HTTP header to validate session
     * @return user, associated with current session
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = signInPath, produces = "application/json")
    public UserEntity signIn(@RequestHeader("Authorization") String authorization) throws HttpException {
        return sessionManager.validateByAuthorization(authorization);
    }

    /**
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = signOutPath)
    public void signOut() throws HttpException {
    }

    /**
     * Do registration and {@link #signIn(String)}
     * @param authorization HTTP header to validate session
     * @return user, associated with current session
     * @throws HttpException if some validation error occurred
     */
    @RequestMapping(path = signUpPath, produces = "application/json")
    public UserEntity signUp(@RequestHeader("Authorization") String authorization) throws HttpException {
        return signIn(authorization);
    }
}
