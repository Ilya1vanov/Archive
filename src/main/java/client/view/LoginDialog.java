package client.view;

import client.controller.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.*;

@org.springframework.stereotype.Component
public class LoginDialog extends JDialog {
    private JTextField tfUsername;

    private JPasswordField pfPassword;

    @Autowired private Controller controller;

    @Autowired
    public LoginDialog(JFrame mainFrame) {
        super(mainFrame, "Login", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));

        abstract class ButtonActionListener implements ActionListener {
            private final String secondPartSuccess;

            private final String action;

            private final String errorMessage;

            private ButtonActionListener(String secondPartSuccess, String action, String errorMessage) {
                this.secondPartSuccess = secondPartSuccess;
                this.action = action;
                this.errorMessage = errorMessage;
            }

            abstract boolean check(Object source, String username, String password);

            @Override
            public void actionPerformed(ActionEvent e) {
                if (check(this, getUsername(), getPassword())) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Welcome, " + getUsername() + secondPartSuccess,
                            action, JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            errorMessage, action, JOptionPane.ERROR_MESSAGE);
                }
                // reset username and password
                tfUsername.setText("");
                pfPassword.setText("");
            }
        }

        JButton btnLogin = new JButton("Sign In");
        final ActionListener signInActionListener =
                new ButtonActionListener("! You are successfully signed in.", "Sign In", "Invalid username or password") {
                    @Override
                    boolean check(Object source, String username, String password) {
                        return controller.authenticate(source, username, password);
                    }
                };
        btnLogin.addActionListener(signInActionListener);

        JButton registry = new JButton("Sign Up");
        final ActionListener signUpActionListener =
                new ButtonActionListener("! You have been successfully registered.", "Sign Up", "Cannot create new user") {
                    @Override
                    boolean check(Object source, String username, String password) {
                        return controller.registry(source, username, password);
                    }
                };
        registry.addActionListener(signUpActionListener);

        JButton btnExit = new JButton("Exit");
        btnExit.addActionListener(e -> {
            Runtime.getRuntime().halt(0);
        });
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        bp.add(registry);
        bp.add(btnExit);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(mainFrame);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private String getUsername() {
        return tfUsername.getText().trim();
    }

    private String getPassword() {
        return new String(pfPassword.getPassword());
    }
}