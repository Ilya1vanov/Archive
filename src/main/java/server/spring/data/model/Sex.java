package server.spring.data.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ilya Ivanov
 */
@XmlType(name = "sexType")
@XmlEnum
public enum Sex {
    male, female;
}
