package client.controller;

import client.Client;
import client.http.Session;
import client.http.SocketRunner;
import client.model.Model;
import client.view.EmployeeForm;
import client.view.LoginDialog;
import client.view.UserForm;
import client.view.View;
import javafx.beans.property.ObjectProperty;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import server.spring.data.model.Employee;
import server.spring.data.model.EmployeeEntity;
import server.spring.data.model.Identifiable;
import server.spring.data.model.UserEntity;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Ilya Ivanov
 */
@Component
public class Controller {
    /**  log4j logger */
    private static final Logger log = Logger.getLogger(Controller.class);

    @Autowired private Client client;

    @Autowired private View view;

    @Autowired private Model model;

    @Autowired private ApplicationContext context;

    @Autowired private AbstractAction signInAction;

    @Autowired private HttpActionBase signOutRequestAction;

    @Autowired private HttpActionBase signInRequestAction;

    @Autowired private HttpActionBase signUpRequestAction;

    @Autowired private HttpActionBase requestAction;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void initialize() {
        model.sessionProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasWritePermission = false;
            boolean hasAdminPermission = false;
            String login = "Unauthorized";

            if (newValue != null) {
                final UserEntity userEntity = newValue.getUserEntity();
                login = userEntity.getLogin();
                hasWritePermission = userEntity.hasWritePermission();
                hasAdminPermission = userEntity.hasAdminPermission();
                log.debug("New session");
            }

            final JComboBox<String> parserComboBox = view.getParserComboBox();
            parserComboBox.setSelectedIndex(0);
            parserComboBox.setEnabled(hasAdminPermission);

            view.getMainList().getComponentPopupMenu().setEnabled(hasWritePermission);

            final JList<Object> mainList = view.getMainList();
            mainList.clearSelection();
            mainList.setListData(new Object[0]);

            view.getUserLine().setText(login);
            view.getAddressLineUrl().setText("");

            view.switchTo("blank");

            if (newValue == null)
                signInAction.actionPerformed(null);
        });

        model.lastResponseProperty().addListener((observable, oldValue, newValue) -> {
            JLabel statusLine = view.getStatusLine();
            if (newValue != null) {
                final HttpStatus statusCode = newValue.getStatusCode();
                String text = statusCode.toString()+ " " + statusCode.getReasonPhrase();
                statusLine.setText(text);
                if (!statusCode.is2xxSuccessful()) {
                    JOptionPane.showMessageDialog(
                            view.getMainFrame(), text + " " + (newValue.hasBody() ? newValue.getBody().toString() : ""),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                statusLine.setText("No response . . .");
            }
        });

        // manually called first sign in
        model.resetSession();
    }

    public abstract class SignActionBase extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isSessionPresent())
                signOutRequestAction.actionPerformed(null);

            final LoginDialog loginDialog = view.getLoginDialog();
            if (!loginDialog.isDisplayable())
                loginDialog.pack();
            loginDialog.setVisible(true);
        }
    }

    @Bean
    public SignActionBase signInAction() {
        return new SignActionBase() {
            {
                putValue(NAME, "Sign In");
            }
        };
    }

    @Bean
    public SignActionBase signUpAction() {
        return new SignActionBase() {
            {
                putValue(NAME, "Sign Up");
            }
        };
    }

    private abstract class HttpActionBase extends AbstractAction {
        @Override
        final public void actionPerformed(ActionEvent e) {
            final URI uri = getURI(e);
            final RequestEntity.HeadersBuilder requestHeaders = getRequestHeaders(uri);
            final RequestEntity.HeadersBuilder injectHeaders = injectHeaders(e, requestHeaders);
            final RequestEntity.HeadersBuilder headersBuilder = buildDefaultHeaders(injectHeaders);
            RequestEntity request;
            if (headersBuilder instanceof RequestEntity.BodyBuilder) {
                final Object body = getBody(e);
                request = ((RequestEntity.BodyBuilder) headersBuilder).body(body, body == null ? null : body.getClass());
            }
            else
                request = headersBuilder.build();
            preprocessRequest(request);
            final ResponseEntity response = execute(request);
            if (response != null) {
                final HttpStatus statusCode = response.getStatusCode();
                if (statusCode.is2xxSuccessful())
                    processResponse(response);
                else
                    onFail(statusCode);
            }
        }

        abstract URI getURI(ActionEvent e);

        abstract RequestEntity.HeadersBuilder getRequestHeaders(URI uri);

        abstract Object getBody(ActionEvent e);

        RequestEntity.HeadersBuilder injectHeaders(ActionEvent e, RequestEntity.HeadersBuilder builder) {
            return builder;
        }

        private RequestEntity.HeadersBuilder buildDefaultHeaders(RequestEntity.HeadersBuilder headersBuilder) {
//            final Object value = getValue(NAME);
//            if (value != null)
//                view.getStatusLine().setText(value.toString());

            if (model.isSessionPresent())
                headersBuilder.header("Token", model.getSession().getToken());

            return headersBuilder.header("Accept", MediaType.APPLICATION_JSON_VALUE);
        }

        void preprocessRequest(RequestEntity request) {}

        private ResponseEntity execute(RequestEntity request) {
            final SocketRunner socketRunner = new SocketRunner(request, client, context);
            final Future<?> submit = executor.submit(socketRunner);

            ResponseEntity responseEntity = null;
            try {
                responseEntity = (ResponseEntity) submit.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.warn("Server timed out or execution error occurred", e);
                submit.cancel(true);
            }

            model.setLastResponse(responseEntity);
            return responseEntity;
        }

        void processResponse(ResponseEntity response) {}

        void onFail(HttpStatus status) {}
    }

    private abstract class HttpGetRequestActionBase extends HttpActionBase {
        @Override
        final RequestEntity.HeadersBuilder getRequestHeaders(URI uri) {
            return RequestEntity.get(uri);
        }

        @Override
        final void preprocessRequest(RequestEntity request) {
            model.setLastGetRequest(request);
        }

        @Override
        Object getBody(ActionEvent e) {
            return null;
        }
    }

    @Bean
    public HttpActionBase requestAction() {
        return new HttpGetRequestActionBase() {
            @Override
            void processResponse(ResponseEntity response) {
                String panelName;
                final Object body = response.getBody();
                if (body instanceof Object[]) {
                    final Object[] listData = (Object[]) body;
                    view.getMainList().setListData(listData);
                    panelName = "listPanel";
                } else if (body instanceof Identifiable) {
                    final Identifiable<Long> identifiable = (Identifiable<Long>) response.getBody();
                    if (body instanceof UserEntity) {
                        panelName = "userForm";
                        final UserForm userForm = view.getUserForm();
                        userForm.open((UserEntity) identifiable);
                    } else if (body instanceof Employee) {
                        panelName = "employeeForm";
                        final EmployeeForm employeeForm = view.getEmployeeForm();
                        employeeForm.open((Employee) identifiable);
                    } else
                        throw new RuntimeException("Unknown entity");

                    final JTextField addressLineUrl = view.getAddressLineUrl();
                    final RequestEntity lastGetRequest = model.getLastGetRequest();
                    addressLineUrl.setText(lastGetRequest == null ? addressLineUrl.getText() : lastGetRequest.getUrl().getPath().substring(1));
                } else
                    throw new RuntimeException("Unknown entity");

                view.switchTo(panelName);
            }

            @Override
            URI getURI(ActionEvent e) {
                final JTextField source = view.getAddressLineUrl();
                return URI.create("/" + source.getText());
            }
        };
    }

    @Bean
    public HttpActionBase updateAction() {
        return new HttpGetRequestActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            }

            @Override
            void processResponse(ResponseEntity response) {
                requestAction.processResponse(response);
            }

            @Override
            URI getURI(ActionEvent e) {
                final RequestEntity lastRequest = model.getLastGetRequest();
                return lastRequest == null ? null : lastRequest.getUrl();
            }
        };
    }

    @Bean
    public HttpActionBase openAction() {
        return new HttpGetRequestActionBase() {
            {
                putValue(NAME, "Open");
            }

            @Override
            void processResponse(ResponseEntity response) {
                requestAction.processResponse(response);
            }

            @Override
            URI getURI(ActionEvent e) {
                final Object source = e.getSource();
                if (!(source instanceof Identifiable))
                    return null;

                final Identifiable<Long> identifiable = (Identifiable<Long>) source;
                final Long id = identifiable.getId();

                return URI.create("/" + view.getAddressLineUrl().getText() + "/" + id);
            }
        };
    }

    private abstract class SignRequestActionBase extends HttpGetRequestActionBase {
        @Override
        RequestEntity.HeadersBuilder injectHeaders(ActionEvent e, RequestEntity.HeadersBuilder builder) {
            return builder.header("Authorization", e.getActionCommand());
        }
    }

    @Bean
    public HttpActionBase signInRequestAction() {
        return new SignRequestActionBase() {
            private final URI requestUrl = URI.create("/signin");

            @Override
            URI getURI(ActionEvent e) {
                return requestUrl;
            }

            @Override
            void processResponse(ResponseEntity response) {
                model.setSession(new Session((UserEntity) response.getBody(), response.getHeaders().get("Token").get(0)));
            }
        };
    }

    @Bean
    public HttpActionBase signUpRequestAction() {
        return new SignRequestActionBase() {
            private final URI requestUrl = URI.create("/signup");

            @Override
            URI getURI(ActionEvent e) {
                return requestUrl;
            }

            @Override
            void processResponse(ResponseEntity response) {
                signInRequestAction.processResponse(response);
            }
        };
    }

    @Bean
    public HttpActionBase signOutRequestAction() {
        return new SignRequestActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
                putValue(NAME, "Sign Out");
            }

            private final URI requestUrl = URI.create("/signout");

            @Override
            URI getURI(ActionEvent e) {
                return requestUrl;
            }

            @Override
            void processResponse(ResponseEntity response) {
                model.resetSession();
                log.debug("Signed Out");
            }

            @Override
            RequestEntity.HeadersBuilder injectHeaders(ActionEvent e, RequestEntity.HeadersBuilder builder) {
                return builder;
            }
        };
    }

    private abstract class HttpRequestWithBodyBase extends HttpActionBase {
        @Override
        final RequestEntity.HeadersBuilder injectHeaders(ActionEvent e, RequestEntity.HeadersBuilder builder) {
            builder.header("Content-Type", MediaType.APPLICATION_XML_VALUE);

            if (model.getSession().getUserEntity().hasAdminPermission())
                return builder.header("Alternates", view.getParserComboBox().getSelectedItem().toString());
            else
                return builder;
        }
    }

    private abstract class HttpPostRequestActionBase extends HttpRequestWithBodyBase {
        @Override
        final RequestEntity.HeadersBuilder getRequestHeaders(URI uri) {
            return RequestEntity.post(uri);
        }
    }

    @Bean
    public HttpActionBase createAction() {
        return new HttpPostRequestActionBase() {
            {
                putValue(NAME, "Create");
            }

            @Override
            URI getURI(ActionEvent e) {
                return URI.create(e.getActionCommand());
            }

            @Override
            Object getBody(ActionEvent e) {
                return e.getSource();
            }
        };
    }

    private abstract class HttpPutRequestActionBase extends HttpRequestWithBodyBase {
        @Override
        final RequestEntity.HeadersBuilder getRequestHeaders(URI uri) {
            return RequestEntity.put(uri);
        }
    }

    @Bean
    public HttpActionBase editAction() {
        return new HttpPutRequestActionBase() {
            {
                putValue(NAME, "Edit");
            }

            @Override
            URI getURI(ActionEvent e) {
                return URI.create(e.getActionCommand());
            }

            @Override
            Object getBody(ActionEvent e) {
                return e.getSource();
            }
        };
    }

    private abstract class HttpDeleteRequestActionBase extends HttpActionBase {
        @Override
        final RequestEntity.HeadersBuilder getRequestHeaders(URI uri) {
            return RequestEntity.delete(uri);
        }

        @Override
        Object getBody(ActionEvent e) {
            return null;
        }
    }

    @Bean
    public HttpActionBase deleteAction() {
        return new HttpDeleteRequestActionBase() {
            {
                putValue(NAME, "Delete");
            }

            @Override
            URI getURI(ActionEvent e) {
                return URI.create(e.getActionCommand());
            }
        };
    }

    @Bean
    private JPopupMenu mainListPopup() {
        JPopupMenu mainListPopup = new JPopupMenu();
        mainListPopup.add(new AbstractAction() {
            {
                putValue(NAME, "Create new");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                final RequestEntity lastGetRequest = model.getLastGetRequest();
                if (lastGetRequest != null) {
                    if (!model.getSession().getUserEntity().hasWritePermission()) {
                        JOptionPane.showMessageDialog(view.getMainFrame(),
                                "You should have WRITER permissions to perform this operation",
                                "Denied", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    final String path = lastGetRequest.getUrl().getPath();
                    switch (path) {
                        case "/users":
                            JOptionPane.showMessageDialog(view.getMainFrame(),
                                    "To create new user, use registry! (Ctrl + Q)",
                                    "Unsupported", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        case "/employees":
                            view.switchTo("employeeForm");
                            view.getEmployeeForm().create();
                            break;
                        default:
                            throw new RuntimeException("Unknown path");
                    }
                }
            }
        });
        return mainListPopup;
    }

    public boolean authenticate(Object source, String username, String password) {
        final UserEntity user = new UserEntity(username, password);
        signInRequestAction.actionPerformed(new ActionEvent(source, 1,user.getLogin() + ":" + user.getPassword()));
        return model.isSessionPresent();
    }

    public boolean registry(Object source, String username, String password) {
        final UserEntity user = new UserEntity(username, password);
        final Session session = model.getSession();
        signUpRequestAction.actionPerformed(new ActionEvent(source, 1,user.getLogin() + ":" + user.getPassword()));
        return session != model.getSession();
    }
}
