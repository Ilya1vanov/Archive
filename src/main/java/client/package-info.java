@XmlSchema(
        namespace = "https://www.ilya.ivanov.com/archive/client",
        elementFormDefault = XmlNsForm.QUALIFIED,
        location = "https://www.ilya.ivanov.com/archive/client xsd/client.xsd",
        xmlns = {
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "", namespaceURI = "https://www.ilya.ivanov.com/archive/client")
        })
package client;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;