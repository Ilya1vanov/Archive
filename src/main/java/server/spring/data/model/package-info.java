@XmlSchema(
        namespace = "https://www.ilya.ivanov.com/archive/employee",
        elementFormDefault = XmlNsForm.QUALIFIED,
        location = "https://www.ilya.ivanov.com/archive/employee xsd/client.xsd",
        xmlns = {
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "", namespaceURI = "https://www.ilya.ivanov.com/archive/employee")
        })
package server.spring.data.model;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;