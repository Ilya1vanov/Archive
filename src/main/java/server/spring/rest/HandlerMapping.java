package server.spring.rest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.xml.sax.SAXException;
import server.spring.rest.protocol.RequestEntity;
import server.spring.rest.protocol.ResponseEntity;
import server.spring.rest.controller.EmployeeController;
import server.spring.rest.controller.LoginController;
import server.spring.rest.controller.RestControllerMarker;
import server.spring.rest.controller.UserController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ilya Ivanov
 */
@Component
public class HandlerMapping {
    private Map<Method, RestControllerMarker> restControllerMap = new HashMap<>();

    private Multimap<String, Method> URLMap = HashMultimap.create();

    private Multimap<Method, HttpMethod> methodHttpMethodMap = HashMultimap.create();

    private final ControllerArgumentsResolver controllerArgumentsResolver;

    @Autowired
    public HandlerMapping(LoginController loginController, UserController userController, EmployeeController employeeController, ControllerArgumentsResolver controllerArgumentsResolver) {
        initialize(Arrays.asList(loginController, userController, employeeController));
        this.controllerArgumentsResolver = controllerArgumentsResolver;
    }

    private void initialize(List<? extends RestControllerMarker> controllers) {
        for (RestControllerMarker controller : controllers) {
            final Class<? extends RestControllerMarker> aClass = controller.getClass();
            final RequestMapping annotation = aClass.getAnnotation(RequestMapping.class);
            final String[] classPaths = annotation.value();
            if (classPaths.length == 0)
                continue;

            final List<Method> annotatedMethods =
                    Arrays.stream(aClass.getMethods()).filter(method -> method.isAnnotationPresent(RequestMapping.class)).collect(Collectors.toList());
            for (Method annotatedMethod : annotatedMethods) {
                final RequestMapping methodAnnotation = annotatedMethod.getAnnotation(RequestMapping.class);
                final String[] methodPaths = methodAnnotation.path();

                restControllerMap.put(annotatedMethod, controller);
                for (String classPath : classPaths) {
                    if (methodPaths.length == 0)
                        URLMap.put(classPath, annotatedMethod);
                    else
                        for (String methodPath : methodPaths) {
                            final String quoted = methodPath.replaceAll("\\{.+}", ".+");
                            URLMap.put(Pattern.quote(classPath) + quoted, annotatedMethod);
                        }
                }

                final RequestMethod[] requestMethods = methodAnnotation.method();
                for (RequestMethod requestMethod : requestMethods)
                    methodHttpMethodMap.put(annotatedMethod, HttpMethod.resolve(requestMethod.name()));
            }
        }
    }

    ResponseEntity handle(RequestEntity request) {
        final URI url = request.getUrl();
        final HttpMethod httpMethod = request.getMethod();

        final List<String> suitablePaths =
                URLMap.keySet().stream()
                        .filter(pattern -> Pattern.matches(pattern, url.getPath()))
                        .collect(Collectors.toList());

        if (suitablePaths.isEmpty())
            // throw
            return new ResponseEntity(HttpStatus.NOT_FOUND);

        // suitable paths are all the same
        final String suitablePath = suitablePaths.get(0);
        final Collection<Method> methods = URLMap.get(suitablePath);

        final Method suitableMethod = getSuitableMethod(methods, httpMethod);
        if (suitableMethod == null)
            return new ResponseEntity(HttpStatus.NOT_FOUND);

        Object returnedValue = null;
        try {
            List<Object> parameters = controllerArgumentsResolver.resolve(request, suitableMethod);
            final RestControllerMarker controller = restControllerMap.get(suitableMethod);
            returnedValue = suitableMethod.invoke(controller, parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | IOException | SAXException e) {
            // parse
            return ResponseEntity.notFound().build();
        }

//        returnedValue;
        ResponseEntity.ok();
        return new ResponseEntity(HttpStatus.OK);
    }

    private Method getSuitableMethod(Collection<Method> methods, HttpMethod httpMethod) {
        final List<Method> methodList = methods.stream().filter(method ->
                methodHttpMethodMap.get(method).stream().filter(requestMethod -> requestMethod.equals(httpMethod)).count() >= 1)
                .collect(Collectors.toList());
        if (methodList.isEmpty())
            return null;
        if (methodList.size() > 1)
            throw new RuntimeException("Controllers methods are not deterministic. Method: " + httpMethod);
        return methodList.get(0);
    }
}
