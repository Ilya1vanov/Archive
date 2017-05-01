package server.spring.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import server.spring.rest.protocol.RequestEntity;
import server.spring.rest.protocol.ResponseEntity;
import server.spring.rest.controller.EmployeeController;
import server.spring.rest.controller.LoginController;
import server.spring.rest.controller.UserController;
import server.spring.rest.protocol.exception.HttpException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ilya Ivanov
 */
@Component
public class HandlerMapping {
    private static final Logger log = Logger.getLogger(HandlerMapping.class);

    private List<ControllerMethod> controllerMethods = Lists.newArrayList();

    @Autowired
    public HandlerMapping(LoginController loginController, UserController userController, EmployeeController employeeController) {
        for (Object controller : Arrays.asList(loginController, userController, employeeController)) {
            final Class<?> aClass = controller.getClass();

            final List<Method> annotatedMethods =
                    Arrays.stream(aClass.getMethods()).filter(method -> method.isAnnotationPresent(RequestMapping.class)).collect(Collectors.toList());

            for (Method annotatedMethod : annotatedMethods)
                controllerMethods.add(new ControllerMethod(controller, annotatedMethod));
        }
    }

    ResponseEntity handle(RequestEntity request) {
        final String urlPath = request.getUrl().getPath();
        final HttpMethod httpMethod = request.getMethod();

        final List<ControllerMethod> convenienceMethods = controllerMethods.stream().filter(controllerMethod ->
                controllerMethod.hasHttpMethod(httpMethod) && controllerMethod.hasPaths(urlPath))
                .collect(Collectors.toList());

        if (convenienceMethods.isEmpty())
            return new ResponseEntity(HttpStatus.NOT_FOUND);


        if (convenienceMethods.size() > 1)
            throw new RuntimeException("Controllers methods are not deterministic. Method: " + convenienceMethods);

        Object returnedValue = null;
        try {
            ControllerMethod convenienceMethod = convenienceMethods.get(0);
            final Object[] parameters = convenienceMethod.getControllerArgumentsResolver().resolve(request);
            final Method method = convenienceMethod.getMethod();
            final Object controller = convenienceMethod.getController();
            returnedValue = method.invoke(controller, parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | HttpException e) {
            final ResponseStatus annotation = e.getCause().getClass().getAnnotation(ResponseStatus.class);
            if (annotation != null) {
                final HttpStatus code = annotation.value();
                final String reason = e.getMessage();
                return ResponseEntity.status(code).body(reason);
            }
            return ResponseEntity.unprocessableEntity().build();
        }

        return new ResponseEntity(getBody(returnedValue), getHeaders(returnedValue, request.getHeaders()), getStatus(httpMethod));
    }

    private HttpStatus getStatus(HttpMethod method) {
        HttpStatus status;
        switch (method) {
            case GET:
                status = HttpStatus.OK;
                break;
            case PUT:
                status = HttpStatus.CREATED;
                break;
            case DELETE:
                status = HttpStatus.NO_CONTENT;
                break;
            default:
                status = HttpStatus.OK;
        }
        return status;
    }

    private String getBody(Object returnedValue) {
        if (returnedValue == null)
            return "";

        String body;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (returnedValue instanceof Pair) {
                Pair pair = (Pair) returnedValue;
                body = mapper.writeValueAsString(pair.getValue());

            } else
                body = mapper.writeValueAsString(returnedValue);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Jackson mapper error", e);
        }
        return body;
    }

    private MultiValueMap<String, String> getHeaders(Object returnedValue, HttpHeaders requestHeaders) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        if (returnedValue instanceof Pair) {
            Pair pair = (Pair) returnedValue;
            headers.add("Token", (String) pair.getKey());
        }

        return headers;
    }

    private class ControllerMethod {
        private Object controller;

        private List<HttpMethod> httpMethods = Lists.newArrayList();

        private final ControllerArgumentsResolver controllerArgumentsResolver;

        private Method method;

        private List<String> urlPaths = Lists.newArrayList();

        ControllerMethod(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
            this.controllerArgumentsResolver = new ControllerArgumentsResolver(method);
            initialize();
        }

        private void initialize() {
            final RequestMapping classAnnotation = controller.getClass().getAnnotation(RequestMapping.class);
            final List<String> classPaths = Lists.newArrayList(classAnnotation.value());
            if (classPaths.size() == 0)
                classPaths.add("");

            final RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
            final List<String> methodPaths = Lists.newArrayList(methodAnnotation.path());
            if (methodPaths.size() == 0)
                methodPaths.add("");

            for (String classPath : classPaths)
                for (String methodPath : methodPaths) {
                    final String quoted = methodPath.replaceAll("\\{.+}", ".+");
                    urlPaths.add(Pattern.quote(classPath) + quoted);
                }

            final RequestMethod[] requestMethods = methodAnnotation.method();
            for (RequestMethod requestMethod : requestMethods)
                httpMethods.add(HttpMethod.resolve(requestMethod.name()));
        }

        Object getController() {
            return controller;
        }

        boolean hasHttpMethod(HttpMethod httpMethod) {
            return httpMethods.isEmpty() || httpMethods.contains(httpMethod);
        }

        ControllerArgumentsResolver getControllerArgumentsResolver() {
            return controllerArgumentsResolver;
        }

        Method getMethod() {
            return method;
        }

        boolean hasPaths(String urlPath) {
            return urlPaths.stream().anyMatch(s -> Pattern.matches(s, urlPath));
        }
    }
}
