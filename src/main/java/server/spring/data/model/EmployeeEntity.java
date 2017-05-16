package server.spring.data.model;

import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.zip.*;

/**
 * @author Ilya Ivanov
 */
@Entity
@Table(name = "employees")
public class EmployeeEntity {
    /** log4j logger */
    private static final Logger log = Logger.getLogger(EmployeeEntity.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    private EmployeeMeta employeeMeta;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data")
    private byte[] data;

    protected EmployeeEntity() {
    }

    public EmployeeEntity(EmployeeMeta employeeMeta, String raw) throws IOException {
        this.employeeMeta = employeeMeta;
        setData(raw);
    }

    public EmployeeEntity(Employee employee) throws IOException, JAXBException {
        this.employeeMeta = employee.getEmployeeMeta();
        setData(toXML(employee));
    }

    public Long getId() {
        return id;
    }

    public EmployeeMeta getEmployeeMeta() {
        return employeeMeta;
    }

    public void setEmployeeMeta(EmployeeMeta employeeMeta) {
        this.employeeMeta = employeeMeta;
    }

    /**
     * Zip data into Bloc.
     * @param raw raw string data
     * @throws IOException if an IO errors occurs
     */
    private void setData(String raw) throws IOException {
        final byte[] input = raw.getBytes();
        Deflater deflater = new Deflater();

        deflater.setInput(input);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        this.data = outputStream.toByteArray();
        log.debug("Original: " + input.length + " B");
        log.debug("Compressed: " + data.length + " B");
    }

    public void setData(Employee employee) throws JAXBException, IOException {
        this.setData(toXML(employee));
    }

    private String toXML(Employee employee) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Employee.class);
        final Marshaller marshaller = jaxbContext.createMarshaller();

        StringWriter sw = new StringWriter();
        marshaller.marshal(employee, sw);
        return sw.toString();
    }

    /**
     * Unzip and makes a defensive copy of blob.
     * @return unziped data
     * @throws IOException if an IO errors occurs
     */
    public String getData() throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        log.debug("Original: " + data.length + " B");
        log.debug("Compressed: " + output.length + " B");
        return new String(output);
    }

    @PostLoad
    private void initMeta() {
        employeeMeta.setId(this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployeeEntity that = (EmployeeEntity) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}