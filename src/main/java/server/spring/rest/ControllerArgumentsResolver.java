package server.spring.rest;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.SAXException;
import server.spring.rest.parsers.Parser;
import server.spring.rest.protocol.RequestEntity;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Ilya Ivanov
 */
@Component
public class ControllerArgumentsResolver {
    private final ApplicationContext context;

    @Autowired
    public ControllerArgumentsResolver(ApplicationContext context) {
        this.context = context;
    }

    List<Object> resolve(RequestEntity request, Method controllerMethod) throws IOException, SAXException {
        final List<Parameter> parameters = Lists.newArrayList(controllerMethod.getParameters());
        if (parameters.size() == 0)
            return null;

        List<Object> returnParameters = new ArrayList<>(parameters.size());

        final List<Parameter> requestBodyTypes =
                parameters.stream()
                        .filter(parameter -> parameter.isAnnotationPresent(RequestBody.class))
                        .collect(Collectors.toList());

        if (requestBodyTypes.size() > 1)
            throw new RuntimeException("More than one parameters annotated with @RequestBody. Method: " + controllerMethod);

        if (requestBodyTypes.size() == 1) {
            final Parameter bodyParameter = requestBodyTypes.get(0);
            final String body = request.getBody();

            final Parser parser = (Parser) context.getBean("Parser");
            final Object o = parser.parse(body, bodyParameter.getType());
            returnParameters.set(parameters.indexOf(bodyParameter), o);
        }

        final List<Parameter> requestedPathVariables =
                parameters.stream()
                        .filter(parameter -> parameter.isAnnotationPresent(PathVariable.class))
                        .collect(Collectors.toList());

        if (requestedPathVariables.size() > 0) {
            final String url = request.getUrl().getPath();

            for (Parameter parameter : requestedPathVariables) {
                if (!parameter.getType().equals(String.class) && !Number.class.isAssignableFrom(parameter.getType()))
                    throw new RuntimeException("Unknown controller method parameter " + parameter);

                final RequestMapping classAnnotation = controllerMethod.getDeclaringClass().getAnnotation(RequestMapping.class);
                final String classUrl = url.substring(classAnnotation.path()[0].length());
                final String requestUrl = url.replaceFirst(Pattern.quote(classUrl), "");

                final String methodUrl = controllerMethod.getAnnotation(RequestMapping.class).path()[0];
                final String variable = parameter.getAnnotation(PathVariable.class).value();

                final Pattern pattern = Pattern.compile(("([^/]*)\\{" + variable + "}(/|$)"), Pattern.CASE_INSENSITIVE);
                final Matcher matcher = pattern.matcher(methodUrl);

                if (!matcher.find())
                    throw new RuntimeException("Bug report: cannot find path variable " + variable + " in URL " + methodUrl);
                final int start = matcher.start();
                final String group1 = matcher.group(1);
                final String group2 = matcher.group(2);

                final Pattern pathVariablePattern = Pattern.compile(group1 + "(.*)" + group2);
                final Matcher pathVariableMatcher = pathVariablePattern.matcher(requestUrl);

                if (!pathVariableMatcher.find())
                    throw new RuntimeException("Bug report: cannot find path variable " + variable + " in URL " + requestUrl);

                final String extractedVariable = pathVariableMatcher.group(1);

                final Object castedParameterVariable = parameter.getType().cast(extractedVariable);
                returnParameters.set(parameters.indexOf(parameter), castedParameterVariable);
            }
        }

        return returnParameters;
    }
}
