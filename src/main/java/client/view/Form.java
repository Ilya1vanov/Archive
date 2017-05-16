package client.view;

import client.model.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import server.spring.data.model.Identifiable;
import server.spring.data.model.UserEntity;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.stream.Stream;

/**
 * @author Ilya Ivanov
 */
abstract class Form<T extends Identifiable<Long>> {
    /** object to store in form */
    T object;

    /** common id text field */
    final JTextField id = new JTextField(10);

    /** main panel */
    private final JPanel panel;

    /** constraints violation edit */
    private final JLabel invalidMessage;

    /** edit button */
    private final JButton edit;

    /** create button */
    private final JButton create;

    /** delete button*/
    private final JButton delete;

    /** url request base path */
    private final String url;

    /** */
    @Autowired Model model;

    /** spring applicattion context */
    @Autowired ApplicationContext context;

    /** application view */
    private View view;

    Form(AbstractAction editAction, AbstractAction createAction, AbstractAction deleteAction, AbstractAction requestAction, String url) {
        this.url = url;
        edit = new JButton();

        edit.setAction(new AbstractAction() {
            {
                putValue(NAME, "Edit");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isValid()) {
                    finallyEdit();
                    editAction.actionPerformed(new ActionEvent(object, 1, url + "/" + object.getId()));
                }
                else
                    invalidMessage.setVisible(true);
            }
        });

        create = new JButton();

        create.setAction(new AbstractAction() {
            {
                putValue(NAME, "Create");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isValid()) {
                    finallyCreate();
                    createAction.actionPerformed(new ActionEvent(object, 1, url));
                } else
                    invalidMessage.setVisible(true);
            }
        });

        delete = new JButton();

        delete.setAction(new AbstractAction() {
            {
                putValue(NAME, "Delete");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAction.actionPerformed(new ActionEvent(object, 1, url + "/" + object.getId()));
                if (model.getLastResponse() != null && model.getLastResponse().getStatusCode().is2xxSuccessful()) {
                    view.getAddressLineUrl().setText(url.substring(1));
                    requestAction.actionPerformed(new ActionEvent(this, 1, "any"));
                }
            }
        });

        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout(""));
        builder.appendColumn("right:pref");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 200px)");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 200px)");
        builder.appendColumn("3dlu");
        builder.appendColumn("fill:max(pref; 200px)");

        builder.append("ID: ", id);
        id.setEnabled(false);
        builder.nextLine();

        build(builder);
        setEnable(false);

        invalidMessage = new JLabel("Invalid form");
        invalidMessage.setVisible(false);
        builder.append(invalidMessage);

        panel = new JPanel();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(edit);
        buttonPanel.add(delete);
        buttonPanel.add(create);

        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        panel.add(builder.getPanel());
        panel.add(buttonPanel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    }

    /**
     * Place UI elements on form
     * @param builder form builder
     */
    abstract void build(DefaultFormBuilder builder);

    JPanel getPanel() {
        return panel;
    }

    public String getUrl() {
        return url;
    }

    @PostConstruct
    public void init() {
        view = context.getBean(View.class);
    }

    public void open(T object) {
        this.object = object;
        id.setText(object.getId().toString());
        invalidMessage.setVisible(false);
        {
            final UserEntity userEntity = model.getSession().getUserEntity();
            final boolean hasEditPermission = userEntity.hasEditPermission();
            final boolean hasWritePermission = userEntity.hasWritePermission();
            setEnable(hasEditPermission);
            create.setVisible(false);
            edit.setVisible(hasEditPermission);
            delete.setVisible(hasWritePermission);
        }
        openInner();
    }

    public void create() {
        invalidMessage.setVisible(false);
        id.setText("0");
        Stream.of(edit, create, delete).forEach(b -> b.setVisible(false));
        create.setVisible(true);
        createInner();
        setEnable(true);
    }

    /**
     * @return if form is valid
     */
    private boolean isValid() {
        final String text = id.getText();
        return isValidInner() && (!text.isEmpty()) && NumberUtils.isNumber(text);
    }

    abstract boolean isValidInner();

    /**
     * Instantiate edit object from filled fields
     */
    abstract void finallyEdit();

    /**
     * Instantiate new object from filled fields
     */
    abstract void finallyCreate();

    /**
     * @return true if form is valid
     */
    abstract void setEnable(boolean enable);

    /**
     * innner
     */
    abstract void openInner();

    /**
     * inner creation routine
     */
    abstract void createInner();
}
