package com.vehicleservice.controller;

import com.vehicleservice.model.User;
import com.vehicleservice.service.AuthService;
import com.vehicleservice.util.ErrorHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controller and view builder for staff authentication.
 */
public class LoginController {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    public LoginController(Consumer<User> onLoginSuccess) {
        this.authService = new AuthService();
        this.onLoginSuccess = onLoginSuccess;
    }

    public Parent getView() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #0f172a;");

        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(380);
        card.getStyleClass().add("card");
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px; -fx-padding: 30px;");

        Label title = new Label("Vehicle Service Hub");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Staff Management Portal");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        userField.setText("admin");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setText("admin123");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        errorLabel.setVisible(false);

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("button-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-padding: 10px; -fx-font-size: 14px;");

        Label hintLabel = new Label("Default credentials: admin / admin123");
        hintLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");

        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("Please enter username and password.");
                errorLabel.setVisible(true);
                return;
            }

            try {
                Optional<User> userOpt = authService.login(u, p);
                if (userOpt.isPresent()) {
                    errorLabel.setVisible(false);
                    onLoginSuccess.accept(userOpt.get());
                } else {
                    errorLabel.setText("Invalid username or password.");
                    errorLabel.setVisible(true);
                }
            } catch (Exception ex) {
                ErrorHandler.showError("Login Error", ErrorHandler.getFriendlyDatabaseErrorMessage(ex));
            }
        });

        // Allow pressing Enter in password field to submit
        passField.setOnAction(e -> loginBtn.fire());

        card.getChildren().addAll(title, subtitle, userLabel, userField, passLabel, passField, errorLabel, loginBtn, hintLabel);
        root.getChildren().add(card);
        return root;
    }
}
