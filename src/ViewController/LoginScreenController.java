package ViewController;

import Model.DBManager;
import static Model.DBManager.checkLogInCredentials;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class LoginScreenController implements Initializable {

    @FXML
    private Label title;
    @FXML
    private Label intro;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private Label errorMsg;
    @FXML
    private Label hint;
    @FXML
    private TextField usernameText;
    @FXML
    private PasswordField passwordText;
    @FXML
    private Button loginBtn;
    @FXML
    private Button exitBtn;
    
    public static int databaseError = 0;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
    }    
    
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.LoginScreen", Locale.getDefault());
        title.setText(rb.getString("title"));
        intro.setText(rb.getString("intro"));
        usernameLabel.setText(rb.getString("usernameLabel"));
        passwordLabel.setText(rb.getString("passwordLabel"));
        loginBtn.setText(rb.getString("loginBtn"));
        exitBtn.setText(rb.getString("exitBtn"));
    }

    @FXML
    private void loginHandler(ActionEvent event) {
        /******************
        *******************
        * Username = user *
        * Password = pass *
        *******************
        *******************/
        String username = usernameText.getText();
        String password = passwordText.getText();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.LoginScreen", Locale.getDefault());

        if (username.equals("") || password.equals("")) {
            hint.setText(rb.getString("noUserPass"));
            return;
        }

        boolean correctCredentials = checkLogInCredentials(username, password);

        if (correctCredentials) {
            try {
                Parent parent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
                Scene scene = new Scene(parent);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } else if (databaseError > 0) {
            errorMsg.setText(rb.getString("connError"));
       
        } else {
            hint.setText(rb.getString("hint"));
        }
    }

    @FXML
    public static void incrementDatabaseError() {
        databaseError++;
    }
    
    @FXML
    private void exitHandler(ActionEvent event) throws SQLException, Exception {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.LoginScreen", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("confirmExit"));
        alert.setHeaderText(rb.getString("confirmExit"));
        alert.setContentText(rb.getString("confirmExitMsg"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            DBManager.closeConnection();
            System.exit(0);
        }
    }
    
}
