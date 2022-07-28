package com.github.ormfux.esi.ui.connections;

import com.github.ormfux.esi.model.settings.connection.ApiKeyAuthentication;
import com.github.ormfux.esi.model.settings.connection.BasicAuthentication;
import com.github.ormfux.esi.model.settings.connection.ESConnection;
import com.github.ormfux.esi.util.DialogUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.function.Function;

public class ConnectionDialog extends Dialog<ESConnection> {

    private final ESConnection connection;

    private TextField nameField = new TextField();

    private TextField urlField = new TextField();

    private ComboBox<AuthenticationType> authenticationTypeField = new ComboBox<>(FXCollections.observableArrayList(AuthenticationType.values()));

    private final TextField apiKeyField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    private final TextField indexFilterField = new TextField();
    private final TextField aliasFilterField = new TextField();

    private final Text testResultText = new Text();
    private final Button testButton = new Button("Test Connection");

    public ConnectionDialog(final ESConnection connection, final Function<ESConnection, String> testConnectionFunction) {
        final DialogPane dialogPane = getDialogPane();
        DialogUtils.configureForOperatingSystem(this);

        dialogPane.setContent(createContentGrid());
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        final Button okButton = (Button)dialogPane.lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setText("Save");

        testButton.setOnAction(e -> {
            final ESConnection testConnection = new ESConnection();
            fillResult(testConnection);

            final String esVersion = testConnectionFunction.apply(testConnection);

            if (esVersion == null) {
                testResultText.setText("Failed to connect!");
                testResultText.setFill(Color.RED);
            } else {
                testResultText.setText("Elasticsearch version: " + esVersion);
                testResultText.setFill(Color.GREEN);
            }

        });

        testButton.disableProperty().bind(okButton.disableProperty());

        defineInputInteractions(okButton);

        if (connection == null) {
            this.connection = new ESConnection();
            initForCreate();
        } else {
            this.connection = connection;
            initForEdit();
        }

        Platform.runLater(() -> nameField.requestFocus());

        setResultConverter((dialogButton) -> {
            final ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();

            if (data == ButtonData.OK_DONE) {
                fillResult(this.connection);
                return this.connection;
            } else {
                return null;
            }
        });
    }

    private void initForEdit() {
        setTitle("Edit Connection");
        nameField.setText(connection.getName());
        urlField.setText(connection.getUrl());
        indexFilterField.setText(connection.getDefaultIndexFilter());
        aliasFilterField.setText(connection.getDefaultAliasFilter());

        if (connection.getAuthentication() instanceof BasicAuthentication) {
            authenticationTypeField.setValue(AuthenticationType.BASIC);
            usernameField.setText(((BasicAuthentication) connection.getAuthentication()).getUsername());
            passwordField.setText(((BasicAuthentication) connection.getAuthentication()).getPassword());
        } else if (connection.getAuthentication() instanceof ApiKeyAuthentication) {
            authenticationTypeField.setValue(AuthenticationType.APIKEY);
            apiKeyField.setText(((ApiKeyAuthentication) connection.getAuthentication()).getApiKey());
        } else {
            authenticationTypeField.setValue(AuthenticationType.NONE);
        }
    }

    private void initForCreate() {
        setTitle("Create Connection");
        authenticationTypeField.setValue(AuthenticationType.NONE);
    }

    private Node createContentGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setPrefWidth(300);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Name");
        nameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        nameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setFillWidth(nameField, true);
        grid.addRow(0, nameLabel, nameField);

        Label urlLabel = new Label("URL");
        urlLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        urlField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(urlField, Priority.ALWAYS);
        GridPane.setFillWidth(urlField, true);
        grid.addRow(1, urlLabel, urlField);

        Label indexFilterLabel = new Label("Default Index Filter");
        indexFilterLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        indexFilterField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(indexFilterField, Priority.ALWAYS);
        GridPane.setFillWidth(indexFilterField, true);
        grid.addRow(2, indexFilterLabel, indexFilterField);

        Label aliasFilterLabel = new Label("Default Alias Filter");
        aliasFilterLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        aliasFilterField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(aliasFilterField, Priority.ALWAYS);
        GridPane.setFillWidth(aliasFilterField, true);
        grid.addRow(3, aliasFilterLabel, aliasFilterField);

        Label authenticationTypeLabel = new Label("Authentication Type");
        authenticationTypeLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        authenticationTypeField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(authenticationTypeField, Priority.ALWAYS);
        GridPane.setFillWidth(authenticationTypeField, true);
        grid.addRow(4, authenticationTypeLabel, authenticationTypeField);

        Label apiKeyLabel = new Label("API Key");
        apiKeyLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        apiKeyField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(apiKeyField, Priority.ALWAYS);
        GridPane.setFillWidth(apiKeyField, true);
        grid.addRow(5, apiKeyLabel, apiKeyField);

        Label usernameLabel = new Label("Username");
        usernameLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        usernameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(usernameField, Priority.ALWAYS);
        GridPane.setFillWidth(usernameField, true);
        grid.addRow(6, usernameLabel, usernameField);

        Label passwordLabel = new Label("Password");
        passwordLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        passwordField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);
        GridPane.setFillWidth(passwordField, true);
        grid.addRow(7, passwordLabel, passwordField);

        grid.addRow(8, testButton, testResultText);

        return grid;
    }

    private void defineInputInteractions(final Button okButton) {
        okButton.disableProperty().bind(nameField.textProperty().isEmpty()
                                            .or(urlField.textProperty().isEmpty())
                                            .or(authenticationTypeField.valueProperty().isEqualTo(AuthenticationType.APIKEY)
                                                    .and(apiKeyField.textProperty().isEmpty()))
                                            .or(authenticationTypeField.valueProperty().isEqualTo(AuthenticationType.BASIC)
                                                    .and(usernameField.textProperty().isEmpty()
                                                            .or(passwordField.textProperty().isEmpty()))));

        authenticationTypeField.valueProperty().addListener((e, old, newValue) -> {
            apiKeyField.setText(null);
            usernameField.setText(null);
            passwordField.setText(null);
            switch (newValue) {
                case APIKEY:
                    apiKeyField.setDisable(false);
                    usernameField.setDisable(true);
                    passwordField.setDisable(true);
                    break;
                case BASIC:
                    apiKeyField.setDisable(true);
                    usernameField.setDisable(false);
                    passwordField.setDisable(false);
                    break;
                case NONE:
                    apiKeyField.setDisable(true);
                    usernameField.setDisable(true);
                    passwordField.setDisable(true);
                    break;
                default:
                    break;
            }
        });
    }

    private void fillResult(final ESConnection filledConnection) {
        filledConnection.setName(nameField.getText());
        filledConnection.setUrl(urlField.getText());
        filledConnection.setDefaultAliasFilter(aliasFilterField.getText());
        filledConnection.setDefaultIndexFilter(indexFilterField.getText());

        switch (authenticationTypeField.getValue()) {
            case APIKEY:
                filledConnection.setAuthentication(new ApiKeyAuthentication(apiKeyField.getText()));
                break;
            case BASIC:
                filledConnection.setAuthentication(new BasicAuthentication(usernameField.getText(), passwordField.getText()));
                break;
            case NONE:
                filledConnection.setAuthentication(null);
                break;
            default:
                break;

        }
    }

    private enum AuthenticationType {
        NONE,
        BASIC,
        APIKEY
    }

}
