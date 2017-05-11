package client.view;

import client.Client;
import javafx.scene.control.ComboBox;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import server.spring.data.model.Role;
import server.spring.data.model.UserEntity;

import java.awt.EventQueue;
import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;

/**
 * @author Ilya Ivanov
 */
@org.springframework.stereotype.Component
public class View {
    @Autowired private Client client;

    @Autowired private JFrame mainFrame;

    @Autowired private JComboBox<String> parserComboBox;

    @Autowired private LoginDialog loginDialog;

    @Autowired private JLabel userLine;

    @Autowired private JTextField addressLineUrl;

    @Autowired private JList<Object> mainList;

    @Autowired private JLabel statusLine;

    public JFrame getMainFrame() {
        return mainFrame;
    }

    public JComboBox<String> getParserComboBox() {
        return parserComboBox;
    }

    public JLabel getUserLine() {
        return userLine;
    }

    public LoginDialog getLoginDialog() {
        return loginDialog;
    }

    public JTextField getAddressLineUrl() {
        return addressLineUrl;
    }

    public JList<Object> getMainList() {
        return mainList;
    }

    public JLabel getStatusLine() {
        return statusLine;
    }

    @Bean
    public JFrame mainFrame(
            JList<Object> mainList, JComboBox<String> parserComboBox,
            JMenuBar menuBar, JPanel addressPanel,
            JLabel statusLineBase, JLabel statusLine, JLabel userLine
    ) {
        JFrame mainFrame = new JFrame();
        mainFrame.setResizable(false);
        mainFrame.setMinimumSize(new Dimension(950, 650));
        mainFrame.setBounds(new Rectangle(0, 0, 600, 800));
        mainFrame.setTitle("Archive");
        mainFrame.setBounds(100, 100, 811, 507);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().setLayout(null);

        mainFrame.getContentPane().add(mainList);
        mainFrame.getContentPane().add(menuBar);
        mainFrame.getContentPane().add(parserComboBox);
        mainFrame.getContentPane().add(statusLineBase);
        mainFrame.getContentPane().add(statusLine);
        mainFrame.getContentPane().add(userLine);
        mainFrame.getContentPane().add(addressPanel);

        return mainFrame;
    }

    @Bean
    public JList<Object> mainList(AbstractAction openAction) {
        JList<Object> list = new JList<>();
        list.setBounds(0, 67, 944, 529);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    // Double-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    final Object selectedValue = list.getSelectedValue();
                    openAction.actionPerformed(new ActionEvent(selectedValue, 1, ""));
                }
            }
        });
        return list;
    }

    @Bean
    public JMenuBar menuBar(JMenu editMenu, JMenu userMenu) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        menuBar.setMargin(new Insets(0, 15, 0, 0));
        menuBar.setBounds(0, 0, 944, 21);
        menuBar.add(editMenu);
        menuBar.add(userMenu);
        return  menuBar;
    }
    
    @Bean
    public JMenu editMenu(AbstractAction openAction, AbstractAction editAction, AbstractAction deleteAction) {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMargin(new Insets(0, 20, 0, 20));

        JMenuItem mntmOpen = new JMenuItem(openAction);
        editMenu.add(mntmOpen);

        JMenuItem mntmEdit = new JMenuItem(editAction);
        editMenu.add(mntmEdit);

        JMenuItem mntmDelete = new JMenuItem(deleteAction);
        editMenu.add(mntmDelete);

        return editMenu;
    }
    
    @Bean
    public JMenu userMenu(AbstractAction signInAction, AbstractAction signOutAction, AbstractAction signUpAction) {
        JMenu userMenu = new JMenu("User");
        userMenu.setMargin(new Insets(0, 20, 0, 20));

        JMenuItem mntmSignIn = new JMenuItem(signInAction);
        userMenu.add(mntmSignIn);

        JMenuItem mntmSignOut = new JMenuItem(signOutAction);
        userMenu.add(mntmSignOut);

        JMenuItem mntmSignUp = new JMenuItem(signUpAction);
        userMenu.add(mntmSignUp);

        return userMenu;
    }

    @Bean
    public JPanel addressPanel(JLabel addressLineBase, JTextField addressLineUrl, JButton updateButton) {
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBounds(10, 26, 579, 30);
        panel.setLayout(null);

        panel.add(addressLineBase);
        panel.add(addressLineUrl);
        panel.add(updateButton);

        return panel;
    }

    @Bean
    public JButton updateButton(AbstractAction updateAction) {
        JButton updateButton = new JButton("");
        updateButton.setIcon(new ImageIcon(View.class.getResource("/com/sun/javafx/scene/web/skin/Undo_16x16_JFX.png")));
        updateButton.setBounds(548, 0, 31, 30);
        updateButton.setForeground(Color.BLACK);
        updateButton.setBackground(new Color(105, 105, 105));
        updateButton.setBorder(null);
        updateButton.setAction(updateAction);
        return updateButton;
    }
    
    @Bean
    public JLabel addressLineBase() {
        JLabel addressLineBase = new JLabel("http://" + client.getHost() + ":" + client.getPort() + "/");
        addressLineBase.setBounds(10, 0, 173, 30);
        addressLineBase.setFont(new Font("Sitka Text", Font.PLAIN, 16));
        return addressLineBase;
    }
    
    @Bean
    public JTextField addressLineUrl(AbstractAction requestAction) {
        JTextField addressLineUrl = new JTextField();
        addressLineUrl.setBorder(null);
        addressLineUrl.setBounds(182, 1, 209, 28);
        addressLineUrl.setSelectionStart(21);
        addressLineUrl.setMargin(new Insets(3, 3, 3, 3));
        addressLineUrl.setBackground(Color.LIGHT_GRAY);
        addressLineUrl.setFont(new Font("Sitka Text", Font.PLAIN, 16));
        addressLineUrl.setAction(requestAction);
        addressLineUrl.setText("");
        return addressLineUrl;
    }

    @Bean
    public JComboBox<String> parserComboBox() {
        JComboBox<String> parserComboBox = new JComboBox<>();
        parserComboBox.setBounds(607, 26, 142, 30);
        parserComboBox.addItem("SAX");
        parserComboBox.addItem("StAX");
        parserComboBox.addItem("DOM");
        parserComboBox.addItem("JDOM");
        parserComboBox.setSelectedIndex(0);
        parserComboBox.setEnabled(false);
        return parserComboBox;
    }

    @Bean
    public JLabel statusLineBase() {
        JLabel statusLineBase = new JLabel("Status:");
        statusLineBase.setFont(new Font("Sitka Text", Font.BOLD, 13));
        statusLineBase.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLineBase.setBounds(10, 596, 59, 25);
        return statusLineBase;
    }

    @Bean
    public JLabel statusLine() {
        JLabel statusLine = new JLabel("Connecting...");
        statusLine.setFont(new Font("Sitka Text", Font.PLAIN, 13));
        statusLine.setBounds(63, 596, 881, 25);
        return statusLine;
    }

    @Bean
    public JLabel userLine() {
        JLabel userLine = new JLabel("Unauthorized");
        userLine.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        userLine.setFont(new Font("Sitka Text", Font.ITALIC, 18));
        userLine.setHorizontalAlignment(SwingConstants.CENTER);
        userLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLine.setBounds(812, 28, 122, 28);
        return userLine;
    }

    @PostConstruct
    private void start() {
        EventQueue.invokeLater(() -> {
            try {
                this.mainFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
