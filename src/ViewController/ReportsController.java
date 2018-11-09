package ViewController;

import Model.DBManager;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class ReportsController implements Initializable {

    @FXML
    private Label reports;
    @FXML
    private Label typesByMonth;
    @FXML
    private Label scheduleConsultant;
    @FXML
    private Label meetingsCustomer;
    @FXML
    private Button typesByMonthBtn;
    @FXML
    private Button scheduleConsultantBtn;
    @FXML
    private Button meetingsCustomerBtn;
    @FXML
    private Button exitBtn;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
    }    

    @FXML
    private void typesByMonthGenerate(ActionEvent event) {
        DBManager.generateAppointmentTypeByMonthReport();
    }

    @FXML
    private void scheduleConsultantGenerate(ActionEvent event) {
        DBManager.generateScheduleForConsultants();
    }

    @FXML
    private void meetingsCustomerGenerate(ActionEvent event) {
        DBManager.generateUpcomingMeetingsByCustomer();
    }

    @FXML
    private void exitHandler(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.Reports", Locale.getDefault());
        reports.setText(rb.getString("reports"));
        typesByMonth.setText(rb.getString("typesByMonth"));
        typesByMonthBtn.setText(rb.getString("generate"));
        scheduleConsultant.setText(rb.getString("scheduleConsultant"));
        scheduleConsultantBtn.setText(rb.getString("generate"));
        meetingsCustomer.setText(rb.getString("meetingsCustomer"));
        meetingsCustomerBtn.setText(rb.getString("generate"));
        exitBtn.setText(rb.getString("exit"));
    }
}
