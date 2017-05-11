package server.spring.rest.dispatcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import server.spring.data.model.Employee;
import server.spring.rest.exception.HttpException;
import server.spring.rest.parsers.Parser;
import server.spring.rest.exception.UnprocessableEntityException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.lang.reflect.Array;
import java.util.List;

/**
 * @author Ilya Ivanov
 */
@Component("httpSerializer")
@Scope("prototype")
abstract class HttpSerializer<T extends HttpEntity> implements Serializable, ApplicationContextAware {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(HttpSerializer.class);

    /** spring application context */
    @Autowired private ApplicationContext context;

    /** Jackson mapper */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** HTTP headers */
    transient HttpHeaders headers;

    /** serialized body */
    private transient String data;

    /** object, that represents HTTP body */
    transient Object body;

    /** body class */
    private transient Class<?> aClass;

    /** read flag */
    transient boolean read = false;

    private transient Schema employeeSchema;

    HttpSerializer(HttpHeaders headers, Object body) throws UnprocessableEntityException {
        this.headers = headers;
        this.body = body;
        if (body != null) {
            this.aClass = body.getClass();
            if (List.class.isAssignableFrom(aClass)) {
                final List list = (List) body;
                if (!list.isEmpty()) {
                    final Class<?> componentType = list.get(0).getClass();
                    this.aClass = Array.newInstance(componentType, 0).getClass();
                } else {
                    aClass = Object[].class;
                }
            }
        }
        try {
            this.serializeBody();
        } catch (UnprocessableEntityException e) {
            log.warn(e);
            throw e;
        }
    }

    public T getEntity() throws HttpException {
        if (read)
            try {
                deserializeBody();
            } catch (UnprocessableEntityException e) {
                log.warn(e);
                throw e;
            }
        else
            throw new RuntimeException("No data read");
        return getEntityInner();
    }

    abstract T getEntityInner() throws HttpException;

    private String serializeBodyToJSON() throws UnprocessableEntityException {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new UnprocessableEntityException("Error while serialization " + aClass + " to JSON with Jackson", e);
        }
    }

    private String serializeBodyToXML() throws UnprocessableEntityException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(aClass);
            final Marshaller marshaller = jaxbContext.createMarshaller();

            StringWriter sw = new StringWriter();
            marshaller.marshal(body, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new UnprocessableEntityException("Error while serialization " + aClass + " to XML with JAXB", e);
        }
    }

    private Object deserializeBodyFromJson() throws UnprocessableEntityException {
        try {
            return mapper.readValue(data, aClass);
        } catch (IOException e) {
            throw new UnprocessableEntityException("Error while deserialization " + aClass + " from JSON with Jackson", e);
        }
    }

    private Object deserializeBodyFromXML() throws UnprocessableEntityException {
        Object body;
        String parserType = "SAX";
        try {
            final List<String> alternates = headers.get("Alternates");
            if (alternates != null && !alternates.isEmpty())
                parserType = alternates.get(0);
            final Parser parser = (Parser) context.getBean(parserType + "Parser");
            if (aClass.isAssignableFrom(Employee.class))
                body = parser.parse(data, aClass, employeeSchema);
            body = parser.parse(data, aClass);
        } catch (Exception e) {
            throw new UnprocessableEntityException("Error while deserialization " + aClass + " from XML with " + parserType, e);
        }
        return body;
    }

    private void serializeBody() throws UnprocessableEntityException {
        if (body == null)
            return;

        final MediaType contentType = headers.getContentType();
        if (contentType == null || contentType.equals(MediaType.TEXT_PLAIN)) {
            data = body.toString();
        } else if (contentType.equals(MediaType.APPLICATION_XML)) {
            data = serializeBodyToXML();
        } else if (contentType.equals(MediaType.APPLICATION_JSON)) {
            data = serializeBodyToJSON();
        } else
            throw new UnprocessableEntityException(contentType.getType());
    }

    private void deserializeBody() throws UnprocessableEntityException {
        if (aClass == null)
            return;

        final MediaType contentType = headers.getContentType();
        if (contentType == null || contentType.equals(MediaType.TEXT_PLAIN)) {
            body = data;
        } else if (contentType.equals(MediaType.APPLICATION_XML)) {
            body = deserializeBodyFromXML();
        } else if (contentType.equals(MediaType.APPLICATION_JSON)) {
            body = deserializeBodyFromJson();
        } else
            throw new UnprocessableEntityException(contentType.getType());
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(headers);
        out.writeObject(data);
        out.writeObject(aClass);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        headers = (HttpHeaders) in.readObject();
        data = (String) in.readObject();
        aClass = (Class<?>) in.readObject();
        read = true;
    }

    private void setEmployeeSchema(File file) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.employeeSchema = schemaFactory.newSchema(file);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        try {
            setEmployeeSchema(new File("src/main/resources/xsd/employee.xsd"));
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
