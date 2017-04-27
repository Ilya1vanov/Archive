package server.employee;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ilya Ivanov
 */
@XmlRootElement
public class Employee {
    @XmlAttribute(required = true)
    private Long id;

    @XmlElement
    private String firstName;

    @XmlElement
    private String middleName;

    @XmlElement
    private String lastName;

    @XmlElement
    private Long age;

    @XmlElement
    private Sex sex;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getAge() {
        return age;
    }

    public Sex getSex() {
        return sex;
    }
}
