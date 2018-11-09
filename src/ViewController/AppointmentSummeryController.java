package ViewController;

import Model.Appointment;
import Model.AppointmentList;
import static Model.AppointmentList.getAppointmentList;
import Model.DBManager;
import static Model.DBManager.updateAppointmentList;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class AppointmentSummeryController implements Initializable {

    @FXML
    private TableView<Appointment> tableSummery;
    @FXML
    private TableColumn<Appointment, String> summeryTitle;
    @FXML
    private TableColumn<Appointment, String> summeryDate;
    @FXML
    private TableColumn<Appointment, String> summeryContact;
    @FXML
    private Button getInfo;
    @FXML
    private Button modify;
    @FXML
    private Button delete;
    @FXML
    private Button back;
    @FXML
    private Label title;
    @FXML
    private Label description;
    @FXML
    private Label location;
    @FXML
    private Label contact;
    @FXML
    private Label url;
    @FXML
    private Label date;
    @FXML
    private Label startTime;
    @FXML
    private Label endTime;
    @FXML
    private Label createdBy;
    
    private static int appointmentIndexToModify;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
        
        // Assign data to table
//Lambda Expression
        summeryTitle.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        summeryDate.setCellValueFactory(cellData -> cellData.getValue().dateStringProperty());
        summeryContact.setCellValueFactory(cellData -> cellData.getValue().contactProperty());
        updateAddAppointmentTableView();
    }    

    @FXML
    private void getInfoBtn(ActionEvent event) {
        Appointment apt = tableSummery.getSelectionModel().getSelectedItem();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());

        if (apt == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("gettingInfo"));
            alert.setContentText(rb.getString("gettingInfoMsg"));
            alert.showAndWait();
            
        } else {
            title.setText(rb.getString("title") + ": " + apt.getTitle());
            description.setText(rb.getString("description") + ": " + apt.getDescription());
            location.setText(rb.getString("location") + ": " + apt.getLocation());
            contact.setText(rb.getString("contact") + ": " + apt.getContact());
            url.setText(rb.getString("url") + ": " + apt.getUrl());
            date.setText(rb.getString("date") + ": " + apt.getDateString());
            startTime.setText(rb.getString("startTime") + ": " + apt.getStartString());
            endTime.setText(rb.getString("endTime") + ": " + apt.getEndString());
            createdBy.setText(rb.getString("createdBy") + ": " + apt.getCreatedBy());
        }
    }

    @FXML
    private void modifyBtn(ActionEvent event) {
        Appointment appointmentToModify = tableSummery.getSelectionModel().getSelectedItem();
        if (appointmentToModify == null) {

            ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("modifyingApt"));
            alert.setContentText(rb.getString("modifyingAptPleaseSelect"));
            alert.showAndWait();
            return;
        }

        appointmentIndexToModify = getAppointmentList().indexOf(appointmentToModify);

        try {
            Parent parent = FXMLLoader.load(getClass().getResource("ModifyAppointment.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void deleteBtn(ActionEvent event) {
        Appointment apt = tableSummery.getSelectionModel().getSelectedItem();

        if (apt == null) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("deletingApt"));
            alert.setContentText(rb.getString("deletingAptMsg"));
            alert.showAndWait();
            return;
        }
        DBManager.deleteAppointment(apt);
    }

    @FXML
    private void backBtn(ActionEvent event) {
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
    
    public static int getAppointmentIndexToModify() {
        return appointmentIndexToModify;
    }

    @FXML
    public void updateAddAppointmentTableView() {
        updateAppointmentList();
        tableSummery.setItems(AppointmentList.getAppointmentList());
    }
    
    @FXML
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());
        summeryTitle.setText(rb.getString("title"));
        summeryDate.setText(rb.getString("date"));
        summeryContact.setText(rb.getString("contact"));
        getInfo.setText(rb.getString("getInfo"));
        modify.setText(rb.getString("modify"));
        delete.setText(rb.getString("delete"));
        back.setText(rb.getString("back"));
        title.setText(rb.getString("title") + ":");
        description.setText(rb.getString("description") + ":");
        location.setText(rb.getString("location") + ":");
        contact.setText(rb.getString("contact") + ":");
        url.setText(rb.getString("url") + ":");
        date.setText(rb.getString("date") + ":");
        startTime.setText(rb.getString("startTime") + ":");
        endTime.setText(rb.getString("endTime") + ":");
        createdBy.setText(rb.getString("createdBy"));
    }
}
