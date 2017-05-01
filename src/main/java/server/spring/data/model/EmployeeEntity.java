package server.spring.data.model;

import org.springframework.util.StreamUtils;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Ilya Ivanov
 */
@Entity
@Table(name = "employees")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    private EmployeeMeta employeeMeta;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false, length = 0x30_0000)
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
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
        zipOutputStream.write(raw.getBytes());
        this.data = byteArrayOutputStream.toByteArray();
    }

    /**
     * Unzip and makes a defensive copy of blob.
     * @return unziped data
     * @throws IOException if an IO errors occurs
     */
    public String getData() throws IOException {
        return StreamUtils.copyToString(new ZipInputStream(new ByteArrayInputStream(data)), Charset.forName("UTF-8"));
    }
}