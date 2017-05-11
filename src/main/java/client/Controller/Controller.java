package client.Controller;

import client.Client;
import client.http.Session;
import client.http.SocketRunner;
import client.model.Model;
import client.view.LoginDialog;
import client.view.View;
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

    @Autowired private AbstractAction signInRequestAction;

    @Autowired private AbstractAction signOutAction;

    @Autowired private AbstractAction signUpAction;

    @Autowired private AbstractAction signUpRequestAction;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

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

    @PostConstruct
    private void initialize() {
        model.sessionProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasAdminPermission = false;
            String login = "Unauthorized";

            if (newValue != null) {
                final UserEntity userEntity = newValue.getUserEntity();
                login = userEntity.getLogin();
                hasAdminPermission = userEntity.hasAdminPermission();
                log.debug("New session");
            }

            final JComboBox<String> parserComboBox = view.getParserComboBox();
            parserComboBox.setSelectedIndex(0);
            parserComboBox.setEnabled(hasAdminPermission);

            final JList<Object> mainList = view.getMainList();
            mainList.clearSelection();
            mainList.setListData(new Object[0]);

            view.getUserLine().setText(login);
            view.getAddressLineUrl().setText("");

            if (newValue == null)
                signInAction.actionPerformed(null);
        });

        model.lastResponseProperty().addListener((observable, oldValue, newValue) -> {
            JLabel statusLine = view.getStatusLine();;
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

    @Bean
    public AbstractAction openAction() {
        return new HttpGetRequestActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
                putValue(NAME, "Open");
            }

            @Override
            void processResponse(Object body) {
                Map<String, Object> stringObjectMap = null;
                try {
                    stringObjectMap = Stream.of(Introspector.getBeanInfo(body.getClass(), Object.class)
                            .getPropertyDescriptors())
                            .filter(pd -> pd.getReadMethod() != null)
                            .toJavaMap(pd -> {
                                try {
                                    return new Tuple2<>(
                                            pd.getName(),
                                            pd.getReadMethod().invoke(body));
                                } catch (Exception e) {
                                    throw new IllegalStateException();
                                }
                            });
                } catch (IntrospectionException e) {
                    e.printStackTrace();
                }
                System.out.println(stringObjectMap);
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

    @Bean
    public AbstractAction editAction() {
        return new HttpActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
                putValue(NAME, "Edit");
            }

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
    }

    @Bean
    public AbstractAction deleteAction() {
        return new HttpActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
                putValue(NAME, "Delete");
            }

            @Override
            public void actionPerformed(ActionEvent e) {

            }
        };
    }

    @Bean
    public AbstractAction requestAction() {
        return new HttpGetRequestActionBase() {
            @Override
            void processResponse(Object body) {
                final Object[] listData = (Object[]) body;
                view.getMainList().setListData(listData);
            }

            @Override
            URI getURI(ActionEvent e) {
                final JTextField source = view.getAddressLineUrl();
                return URI.create("/" + source.getText());
            }
        };
    }

    @Bean
    public AbstractAction updateAction() {
        return new HttpGetRequestActionBase() {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
            }

            @Override
            void processResponse(Object body) {
                final Object[] listData = (Object[]) body;
                view.getMainList().setListData(listData);
            }

            @Override
            URI getURI(ActionEvent e) {
                final RequestEntity lastRequest = model.getLastGetRequest();
                return lastRequest == null ? null : lastRequest.getUrl();
            }
        };
    }

    @Bean
    public AbstractAction signOutAction() {
        return new SignRequestActionBase() {
            private final URI requestUrl = URI.create("/signout");
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
                putValue(NAME, "Sign Out");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                final RequestEntity request = defaultLoginRequest(requestUrl).build();
                final ResponseEntity responseEntity = execute(request);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    model.resetSession();
                    log.debug("Signed Out");
                }
            }
        };
    }

    @Bean
    public AbstractAction signInAction() {
        return new SignActionBase() {
            {
                putValue(NAME, "Sign In");
            }
        };
    }

    @Bean
    public AbstractAction signInRequestAction() {
        return new SignRequestActionBase() {
            private final URI requestUrl = URI.create("/signin");

            @Override
            public void actionPerformed(ActionEvent e) {
                defaultExecution(requestUrl, e.getActionCommand());
            }
        };
    }

    @Bean
    public AbstractAction signUpAction() {
        return new SignActionBase() {
            {
                putValue(NAME, "Sign Up");
            }
        };
    }

    @Bean
    public AbstractAction signUpRequestAction() {
        return new SignRequestActionBase() {
            private final URI requestUrl = URI.create("/signup");

            @Override
            public void actionPerformed(ActionEvent e) {
                defaultExecution(requestUrl, e.getActionCommand());
            }
        };
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

    private abstract class HttpActionBase extends AbstractAction {
        final RequestEntity.HeadersBuilder buildDefaultHeaders(RequestEntity.HeadersBuilder headersBuilder) {
            final Object value = getValue(NAME);
            if (value != null)
                view.getStatusLine().setText(value.toString());

            if (model.isSessionPresent())
                headersBuilder.header("Token", model.getSession().getToken());

            return headersBuilder.header("Accept", MediaType.APPLICATION_JSON_VALUE);
        }
    }

    private abstract class SignRequestActionBase extends HttpActionBase {
        final RequestEntity.HeadersBuilder defaultLoginRequest(final URI requestUrl) {
            final RequestEntity.HeadersBuilder headersBuilder = RequestEntity.get(requestUrl);
            return buildDefaultHeaders(headersBuilder);
        }

        final void defaultExecution(final URI requestUrl, String authorization) {
            final RequestEntity.HeadersBuilder headersBuilder = RequestEntity.get(requestUrl);
            final RequestEntity.HeadersBuilder defaultHeaders = buildDefaultHeaders(headersBuilder);

            RequestEntity request = defaultHeaders.header("Authorization", authorization).build();

            ResponseEntity response = execute(request);
            if (response != null && response.getStatusCode().is2xxSuccessful())
                model.setSession(new Session((UserEntity) response.getBody(), response.getHeaders().get("Token").get(0)));
        }
    }

    private abstract class SignActionBase extends HttpActionBase {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (model.isSessionPresent())
                signOutAction.actionPerformed(null);

            final LoginDialog loginDialog = view.getLoginDialog();
            if (!loginDialog.isDisplayable())
                loginDialog.pack();
            loginDialog.setVisible(true);
        }
    }

    private abstract class HttpGetRequestActionBase extends HttpActionBase {
        @Override
        public void actionPerformed(ActionEvent e) {
            final URI uri = getURI(e);
            if (uri == null)
                return;

            final RequestEntity.HeadersBuilder headersBuilder = RequestEntity.get(uri);
            final RequestEntity requestEntity = buildDefaultHeaders(headersBuilder).build();
            model.setLastGetRequest(requestEntity);

            final ResponseEntity responseEntity = execute(requestEntity);
            if (responseEntity != null && responseEntity.hasBody()) {
                processResponse(responseEntity.getBody());
            }
        }

        abstract void processResponse(Object body);

        abstract URI getURI(ActionEvent e);
    }
}
