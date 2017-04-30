package server.spring.rest.parsers;

import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

/**
 * @author Ilya Ivanov
 */
public class XMLValidator {
    private Schema schema;

    @Autowired
    public XMLValidator(File schema) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        this.schema = schemaFactory.newSchema(schema);
    }


    public void validate(Source source) throws IOException, SAXException {
        Validator validator = schema.newValidator();
        validator.validate(source);
    }
}
