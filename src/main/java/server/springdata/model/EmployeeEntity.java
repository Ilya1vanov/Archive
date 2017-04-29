package server.springdata.model;

import org.hibernate.Hibernate;
import org.hibernate.LobHelper;
import org.hibernate.annotations.Type;
import org.hibernate.engine.jdbc.NonContextualLobCreator;
import org.springframework.util.StreamUtils;

import javax.persistence.*;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.zip.ZipInputStream;

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

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "middleName")
    private String middleName;

    @Column(name = "lastName")
    private String lastName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data")
    private Blob data;

    protected EmployeeEntity() {
    }

    public EmployeeEntity(String firstName, String middleName, String lastName, InputStream in) throws IOException, SQLException {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.data = new SerialBlob(StreamUtils.copyToByteArray(new ZipInputStream(in)));
    }

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

    /**
     * Unzip and makes a defensive copy of blob.
     * @return unziped data
     * @throws SQLException if a SQL errors occurs
     * @throws IOException if an IO errors occurs
     */
    public Blob getData() throws SQLException, IOException {
        return new SerialBlob(StreamUtils.copyToByteArray(new ZipInputStream(data.getBinaryStream())));
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "id=" + id +
                '}';
    }
}
