package server.spring.rest;

import com.google.common.collect.Lists;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import server.spring.rest.protocol.RequestEntity;
import server.spring.rest.protocol.exception.BadRequestException;
import server.spring.rest.protocol.exception.HttpException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ilya Ivanov
 */
class ControllerArgumentsResolver {
    private final Method method;

    private List<Parameter> parameters;

    private Parameter requestBodyParameter;

    private List<Parameter> pathVariableParameters = Lists.newArrayList();

    private List<Parameter> requestHeaderParameters = Lists.newArrayList();;

    private int count;

    private String[] classUrlPaths;

    private String[] methodUrlPaths;

    ControllerArgumentsResolver(Method method) {
        this.method = method;
        this.count = method.getParameterCount();
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
        if (count == 0)
            return null;

        Object[] returnParameters = new Object[count];

        if (requestBodyParameter != null)
            returnParameters[parameters.indexOf(requestBodyParameter)] = request.getBody();

        for (Parameter parameter : requestHeaderParameters) {
            final HttpHeaders headers = request.getHeaders();
            final String name = parameter.getAnnotation(RequestHeader.class).value();

            final List<String> values = headers.get(name);

            if (values == null || values.isEmpty())
                throw new BadRequestException();

            returnParameters[parameters.indexOf(parameter)] = values.get(0);
        }

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


        return returnParameters;
    }
}
