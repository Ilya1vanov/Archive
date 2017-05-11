package server.spring.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.springframework.util.StreamUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
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

    public EmployeeEntity(Employee employee, String raw) throws IOException {
        this.employeeMeta = employee.getEmployeeMeta();
        setData(raw);
    }

    public Long getId() {
        return id;
    }

    public EmployeeMeta getEmployeeMeta() {
        return employeeMeta;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EmployeeEntity that = (EmployeeEntity) o;

        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        return getEmployeeMeta() != null ? getEmployeeMeta().equals(that.getEmployeeMeta()) : that.getEmployeeMeta() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getEmployeeMeta() != null ? getEmployeeMeta().hashCode() : 0);
        return result;
    }
}