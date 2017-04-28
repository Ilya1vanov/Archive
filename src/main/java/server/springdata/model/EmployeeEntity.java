package server.springdata.model;

import javax.persistence.*;

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
    @Column(length = 0x30_0000)
    private byte[] data;

    protected EmployeeEntity() {
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

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "EmployeeEntity{" +
                "id=" + id +
                '}';
    }
}
