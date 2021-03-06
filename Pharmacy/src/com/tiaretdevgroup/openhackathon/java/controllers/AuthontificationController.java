package com.tiaretdevgroup.openhackathon.java.controllers;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.tiaretdevgroup.openhackathon.java.dao.UserDao;
import com.tiaretdevgroup.openhackathon.java.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

public class AuthontificationController implements Initializable {

    /* Start Login Part */
    @FXML
    private AnchorPane authontificationPane;

    @FXML
    private Tab signUpTab;

    @FXML
    private JFXTextField usernameSignInField;
    @FXML
    private JFXPasswordField passwordSignInField;

    private JFXSnackbar toastErrorMsg;

    /* End Login Part */

    /* Start SignUp Part */

    @FXML
    private TextField usernameSignUpField, nRegistreComSignUpField;
    @FXML
    private PasswordField passwordSignUpField;

    /* End SignUp Part */

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!new File(Constants.FILE_TOKEN).exists()) {
            signUpTab.setDisable(false);
        }
        toastErrorMsg = new JFXSnackbar(authontificationPane);
    }

    @FXML
    private void onSignIn() {
        if (usernameSignInField.getText().trim().isEmpty()) {
            toastErrorMsg.show("Username is Empty !", 1500);
            return;
        }
        if (!usernameSignInField.getText().trim().matches("[A-Za-z0-9_]{4,}")) {
            toastErrorMsg.show("Username not valid !", 1500);
            return;
        }


        if (passwordSignInField.getText().trim().isEmpty()) {
            toastErrorMsg.show("Password is Empty !", 1500);
            return;
        }
        if (passwordSignInField.getText().trim().length() < 4) {
            toastErrorMsg.show("Password not valid !", 1500);
            return;
        }

        int status = new UserDao().checkUsernameAndPassword(usernameSignInField.getText().trim().toLowerCase(), passwordSignInField.getText().toLowerCase());
        switch (status) {
            case -1:
                toastErrorMsg.show("Connection Failed !", 1500);
                break;
            case 1:
                toastErrorMsg.show("Username and/or password incorrect !", 1500);
                break;
            default:

                goToMain();
                break;
        }

    }

    private void goToMain() {
        Parent rootSystem = null;
        Stage stage;

        //get reference - stage
        stage = (Stage) usernameSignInField.getScene().getWindow();

        try {
            //load up other FXML document
            rootSystem = FXMLLoader.load(getClass().getResource("/com/tiaretdevgroup/openhackathon/resources/views/System.fxml"));

        } catch (IOException ex) {
            System.out.println("Error msg: " + ex.getMessage());
        }

        //create a new scene with root and set the stage
        Scene scene = new Scene(rootSystem);
        stage.setScene(scene);
        stage.show();
        stage.setX(100);
        stage.setY(20);
    }

    @FXML
    private void onRegister() {
        if (usernameSignUpField.getText().trim().isEmpty()) {
            toastErrorMsg.show("Username is Empty !", 1500);
            return;
        }
        if (!usernameSignUpField.getText().trim().matches("[A-Za-z0-9_]{4,}")) {
            toastErrorMsg.show("Username not valid !", 1500);
            return;
        }

        if (nRegistreComSignUpField.getText().trim().isEmpty()) {
            toastErrorMsg.show("N° Register Commercial is Empty !", 1500);
            return;
        }
        if (!nRegistreComSignUpField.getText().trim().matches("[0-9]{4,}")) {
            toastErrorMsg.show("N° Register Commercial not valid !", 1500);
            return;
        }

        if (passwordSignUpField.getText().trim().isEmpty()) {
            toastErrorMsg.show("Password is Empty !", 1500);
            return;
        }
        if (passwordSignUpField.getText().trim().length() < 4) {
            toastErrorMsg.show("Password not valid !", 1500);
            return;
        }

        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            HttpResponse<JsonNode> response = Unirest.post(Constants.PHARMACY_SIGNUP)
                    .field("code", nRegistreComSignUpField.getText().trim())
                    .field("ip", inetAddress.getHostAddress())
                    .asJson();

            JSONObject object = new JSONObject(response.getBody().toString());

            File file = new File(Constants.FILE_TOKEN);
            FileOutputStream out = new FileOutputStream(file);
            out.write(object.toString().getBytes());

            //add user to data base
            int status = new UserDao().addUser(usernameSignUpField.getText().trim().toLowerCase(), passwordSignUpField.getText());

            switch (status) {
                case -1 : toastErrorMsg.show("Connection Error !", 2000);
                    break;
                case 1 : toastErrorMsg.show("Error Insert !", 2000);
                    break;
                default: goToMain();
                    break;
            }

        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }
    }

}
