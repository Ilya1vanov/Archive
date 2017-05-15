package client.view;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.math.NumberUtils;
import server.spring.data.model.Employee;
import server.spring.data.model.EmployeeMeta;
import server.spring.data.model.Sex;

import javax.swing.*;

/**
 * @author Ilya Ivanov
 */
public class EmployeeForm extends Form<Employee> {
    private JTextField firstName;

    private JTextField middleName;

    private JTextField lastName;

    private JTextField age;

    private JComboBox<Sex> sex;

    private JTextField workspace;

    private JTextField experience;

    EmployeeForm(AbstractAction editAction, AbstractAction createAction, AbstractAction deleteAction, AbstractAction requestAction) {
        super(editAction, createAction, deleteAction, requestAction,"/employees");
    }

    @Override
    void build(DefaultFormBuilder builder) {
        firstName = new JTextField(10);
        middleName = new JTextField(10);
        lastName = new JTextField(10);
        age = new JTextField(10);
        sex = new JComboBox<>();
        workspace = new JTextField(10);
        experience = new JTextField(10);
        final Sex[] values = Sex.values();
        for (Sex value : values) {
            sex.addItem(value);
        }

        builder.append("First name: ", firstName);
        builder.nextLine();

        builder.append("Middle name: ", middleName);
        builder.nextLine();

        builder.append("Last name: ", lastName);
        builder.nextLine();

        builder.append("Age: ", age);
        builder.nextLine();

        builder.append("Sex: ", sex);
        builder.nextLine();

        builder.append("Workplace: ", workspace);
        builder.nextLine();

        builder.append("Experience: ", experience);
        builder.nextLine();
    }

    @Override
    boolean isValidInner() {
        try {
            final Long age = Long.valueOf(this.age.getText());
            final Integer experience = Integer.valueOf(this.experience.getText());
            return (!firstName.getText().isEmpty()) && (!middleName.getText().isEmpty()) &&
                    (!lastName.getText().isEmpty()) && age > 0 &&
                    (!workspace.getText().isEmpty()) && experience > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    void finallyEdit() {
        final EmployeeMeta employeeMeta = object.getEmployeeMeta();
        employeeMeta.setFirstName(firstName.getText());
        employeeMeta.setMiddleName(middleName.getText());
        employeeMeta.setLastName(lastName.getText());
        object.setAge(Long.valueOf(age.getText()));
        object.setSex(Sex.values()[sex.getSelectedIndex()]);
        object.setWorkplace(workspace.getText());
        object.setExperience(Integer.valueOf(experience.getText()));
    }

    @Override
    void finallyCreate() {
        final EmployeeMeta employeeMeta = new EmployeeMeta(firstName.getText(), middleName.getText(), lastName.getText());
        object = new Employee(
                employeeMeta,
                Long.valueOf(age.getText()),
                Sex.values()[sex.getSelectedIndex()],
                workspace.getText(),
                Integer.valueOf(experience.getText()));
    }

    @Override
    void setEnable(boolean enable) {
        firstName.setEnabled(enable);
        middleName.setEnabled(enable);
        lastName.setEnabled(enable);
        age.setEnabled(enable);
        sex.setEnabled(enable);
        workspace.setEnabled(enable);
        experience.setEnabled(enable);
    }

    @Override
    void openInner() {
        final EmployeeMeta employeeMeta = object.getEmployeeMeta();
        firstName.setText(employeeMeta.getFirstName());
        middleName.setText(employeeMeta.getMiddleName());
        lastName.setText(employeeMeta.getLastName());
        age.setText(object.getAge().toString());
        sex.setSelectedItem(object.getSex());
        workspace.setText(object.getWorkplace());
        experience.setText(object.getExperience().toString());
    }

    @Override
    void createInner() {
        firstName.setText("");
        middleName.setText("");
        lastName.setText("");
        age.setText("");
        sex.setSelectedItem(Sex.male);
        workspace.setText("");
        experience.setText("");
    }
}
