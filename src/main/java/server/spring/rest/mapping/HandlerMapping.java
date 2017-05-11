package server.spring.rest.mapping;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import server.spring.rest.controller.EmployeeController;
import server.spring.rest.controller.LoginController;
import server.spring.rest.controller.UserController;
import server.spring.rest.exception.BadRequestException;
import server.spring.rest.exception.HttpException;
import server.spring.rest.session.SessionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ilya Ivanov
 */
@Component
public class HandlerMapping {
    private List<ControllerMethod> controllerMethods = Lists.newArrayList();

    private final SessionManager sessionManager;

    @Autowired
    public HandlerMapping(LoginController loginController, UserController userController, EmployeeController employeeController, SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        for (Object controller : Arrays.asList(loginController, userController, employeeController)) {
            final Class<?> aClass = controller.getClass();

            final List<Method> annotatedMethods =
                    Arrays.stream(aClass.getMethods()).filter(method -> method.isAnnotationPresent(RequestMapping.class)).collect(Collectors.toList());

            for (Method annotatedMethod : annotatedMethods)
                controllerMethods.add(new ControllerMethod(controller, annotatedMethod));
        }
    }

    public ResponseEntity handle(RequestEntity<?> request) throws Throwable {
        final List<ControllerMethod> convenienceMethods =
                controllerMethods.stream().filter(controllerMethod -> controllerMethod.isAppropriate(request))
                .collect(Collectors.toList());

        if (convenienceMethods.isEmpty())
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        else if (convenienceMethods.size() > 1)
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
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause != null) throw cause;
            else throw new RuntimeException("Unknown error", e);
        }

        ResponseEntity responseEntity;
        if (returnedValue instanceof org.springframework.http.ResponseEntity)
            responseEntity = (ResponseEntity) returnedValue;
        else
            responseEntity = new ResponseEntity(returnedValue, getHeaders(request), getStatus(request));

        return responseEntity;
    }

    private HttpStatus getStatus(RequestEntity request) {
        final HttpMethod method = request.getMethod();
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

    private MultiValueMap<String, String> getHeaders(RequestEntity request) {
        final HttpHeaders requestHeaders = request.getHeaders();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

        final List<String> token = requestHeaders.get("Token");
        if (token != null && !token.isEmpty())
            headers.set("Token", token.get(0));

        final List<String> authorization = requestHeaders.get("Authorization");
        if (authorization != null && !authorization.isEmpty())
            headers.set("Token", sessionManager.getTokenByAuthorization(authorization.get(0)));

        final List<MediaType> accept = requestHeaders.getAccept();
        if (accept != null && !accept.isEmpty())
            headers.set("Content-Type", MediaType.toString(Lists.newArrayList(accept.get(0))));

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

        ControllerArgumentsResolver getControllerArgumentsResolver() {
            return controllerArgumentsResolver;
        }

        Method getMethod() {
            return method;
        }

        private boolean hasHttpMethod(HttpMethod httpMethod) {
            return httpMethods.isEmpty() || httpMethods.contains(httpMethod);
        }

        private boolean hasPaths(String urlPath) {
            return urlPaths.stream().anyMatch(s -> Pattern.matches(s, urlPath));
        }

        boolean isAppropriate(RequestEntity request) {
            final String urlPath = request.getUrl().getPath();
            final HttpMethod httpMethod = request.getMethod();

            return hasHttpMethod(httpMethod) && hasPaths(urlPath);
        }

        private class ControllerArgumentsResolver {
            private final Method method;

            private final List<Parameter> parameters;

            private Parameter requestBodyParameter;

            private final List<Parameter> pathVariableParameters = Lists.newArrayList();

            private final List<Parameter> requestHeaderParameters = Lists.newArrayList();;

            private final String[] classUrlPaths;

            private final String[] methodUrlPaths;

            ControllerArgumentsResolver(Method method) {
                this.method = method;
                this.parameters = Lists.newArrayList(method.getParameters());
                this.classUrlPaths = method.getDeclaringClass().getAnnotation(RequestMapping.class).value();
                this.methodUrlPaths = method.getAnnotation(RequestMapping.class).path();
                initialize();
            }

            private void initialize() {
                for (Parameter parameter : parameters) {
                    if (parameter.isAnnotationPresent(RequestBody.class)) {
                        if (requestBodyParameter != null)
                            throw new RuntimeException("More than one parameter in " + method + " annotated with @RequestBody annotation");
                        requestBodyParameter = parameter;
                        continue;
                    }

                    if (parameter.isAnnotationPresent(PathVariable.class)) {
                        final Class<?> type = parameter.getType();
                        if (!type.equals(String.class) && !Number.class.isAssignableFrom(type))
                            throw new RuntimeException("Unknown path variable type " + type + ", parameter " + parameter);

                        pathVariableParameters.add(parameter);
                        continue;
                    }

                    if (parameter.isAnnotationPresent(RequestHeader.class)) {
                        requestHeaderParameters.add(parameter);
                        continue;
                    }

                    throw new RuntimeException("Not annotated parameter of controller " + parameter);
                }
            }

            Object[] resolve(RequestEntity request) throws HttpException {
                if (parameters.size() == 0)
                    return null;

                Object[] returnParameters = new Object[parameters.size()];

                resolveRequestBody(request, returnParameters);
                resolveRequestHeader(request, returnParameters);
                resolvePathVariable(request, returnParameters);

                return returnParameters;
            }

            private void resolveRequestBody(RequestEntity request, Object[] returnParameters) {
                if (requestBodyParameter != null)
                    returnParameters[parameters.indexOf(requestBodyParameter)] = request.getBody();
            }

            private void resolveRequestHeader(RequestEntity request, Object[] returnParameters) throws HttpException {
                for (Parameter parameter : requestHeaderParameters) {
                    final HttpHeaders headers = request.getHeaders();
                    final String name = parameter.getAnnotation(RequestHeader.class).value();

                    final List<String> values = headers.get(name);

                    if (values == null || values.isEmpty())
                        throw new BadRequestException();

                    returnParameters[parameters.indexOf(parameter)] = values.get(0);
                }
            }

            private void resolvePathVariable(RequestEntity request, Object[] returnParameters) throws HttpException {
                for (Parameter parameter : pathVariableParameters) {
                    String variable = parameter.getAnnotation(PathVariable.class).value();
                    if (variable.isEmpty())
                        variable = parameter.getName();

                    final String requestUrl = request.getUrl().getPath();

                    Object castedParameterVariable = null;
                    for (String classUrlPath : classUrlPaths) {
                        for (String methodUrlPath : methodUrlPaths) {
                            final String fullMethodUrlPath = classUrlPath + methodUrlPath;

                            final Pattern methodUrlPathPattern = Pattern.compile(("(.*)\\{" + variable + "}(.*)"), Pattern.CASE_INSENSITIVE);
                            final Matcher methodUrlPathMatcher = methodUrlPathPattern.matcher(fullMethodUrlPath);

                            if (!methodUrlPathMatcher.find())
                                continue;

                            String group1 = methodUrlPathMatcher.group(1);
                            group1 = group1.replaceAll("\\{.*}", ".+");
                            String group2 = methodUrlPathMatcher.group(2);
                            group2 = group2.replaceAll("\\{.*}", ".+");

                            final Pattern pathVariablePattern = Pattern.compile(group1 + "(.+)" + group2);
                            final Matcher pathVariableMatcher = pathVariablePattern.matcher(requestUrl);

                            if (!pathVariableMatcher.find())
                                continue;

                            final String extractedVariable = pathVariableMatcher.group(1);

                            final Class<?> type = parameter.getType();
                            if (Short.class.isAssignableFrom(type))
                                castedParameterVariable = Short.valueOf(extractedVariable);
                            if (Integer.class.isAssignableFrom(type))
                                castedParameterVariable = Integer.valueOf(extractedVariable);
                            if (Long.class.isAssignableFrom(type))
                                castedParameterVariable = Long.valueOf(extractedVariable);
                            if (Float.class.isAssignableFrom(type))
                                castedParameterVariable = Float.valueOf(extractedVariable);
                            if (Double.class.isAssignableFrom(type))
                                castedParameterVariable = Double.valueOf(extractedVariable);

                            returnParameters[parameters.indexOf(parameter)] = castedParameterVariable;
                        }
                    }
                    if (castedParameterVariable == null)
                        throw new BadRequestException();
                }
            }
        }
    }
}
