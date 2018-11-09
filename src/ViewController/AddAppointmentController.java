package ViewController;

import Model.Appointment;
import Model.Customer;
import Model.CustomerList;
import static Model.DBManager.addNewAppointment;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class AddAppointmentController implements Initializable {

    @FXML
    private Label addAptTitle;
    @FXML
    private Label title;
    @FXML
    private Label desc;
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
    private TextArea textDesc;
    @FXML
    private TextField textTitle;
    @FXML
    private TextField textLocation;
    @FXML
    private TextField textContact;
    @FXML
    private TextField textURL;
    @FXML
    private TextField textStartTimeHour;
    @FXML
    private TextField textStartTimeMinute;
    @FXML
    private TextField textEndTimeHour;
    @FXML
    private TextField textEndTimeMinute;
    @FXML
    private ChoiceBox<String> startAMPM;
    @FXML
    private ChoiceBox<String> endAMPM;
    @FXML
    private String AM;
    @FXML
    private String PM;
    @FXML
    private DatePicker datePicker;
    
    //upper table
    @FXML
    private TableView<Customer> tableViewAdd;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> cityColumn;
    @FXML
    private TableColumn<Customer, String> countryColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    
    //lower table
    @FXML
    private TableView<Customer> tableViewDelete;
    @FXML
    private TableColumn<Customer, String> nameColumnDelete;
    @FXML
    private TableColumn<Customer, String> cityColumnDelete;
    @FXML
    private TableColumn<Customer, String> countryColumnDelete;
    @FXML
    private TableColumn<Customer, String> phoneColumnDelete;
    @FXML
    private Button addBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private Button btnAddAppointmentCancel;
    @FXML
    private Button btnAddAppointmentSave;
    
    //List to hold customers
    private ObservableList<Customer> currentCustomers = FXCollections.observableArrayList();

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
        updateAddAppointmentAddTableView();
        updateAddAppointmentDeleteTableView();
//Lambda Expressions       
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        cityColumn.setCellValueFactory(cellData -> cellData.getValue().cityProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        nameColumnDelete.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        cityColumnDelete.setCellValueFactory(cellData -> cellData.getValue().cityProperty());
        countryColumnDelete.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
        phoneColumnDelete.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
    }    
    
    @FXML
    private void addCustomer(ActionEvent event) {
        Customer customer = tableViewAdd.getSelectionModel().getSelectedItem();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());

        if (customer == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("addingCustomer"));
            alert.setContentText(rb.getString("addingCustomerSelectOne"));
            alert.showAndWait();
            return;
        }

        if (currentCustomers.size() > 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("addingCustomer"));
            alert.setContentText(rb.getString("addingCustomerOnlyOne"));
            alert.showAndWait();
            return;
        }
        currentCustomers.add(customer);
        updateAddAppointmentDeleteTableView();
    }

    @FXML
    private void deleteCustomer(ActionEvent event) {
        Customer customer = tableViewDelete.getSelectionModel().getSelectedItem();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());

        if (customer == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("removingCustomer"));
            alert.setContentText(rb.getString("removingCustomerMsg"));
            alert.showAndWait();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("confirmRemove"));
        alert.setHeaderText(rb.getString("confirmRemoveCustomer"));
        alert.setContentText(rb.getString("confirmRemoveCustomerMsg"));
        
//Lambda Expression
        alert.showAndWait().ifPresent((response -> {
            if (response == ButtonType.OK) {
                currentCustomers.remove(customer);
                updateAddAppointmentDeleteTableView();
            }
        }));
    }

    @FXML
    private void saveAdd(ActionEvent event) {
        Customer customer = null;
        
        // Check currentCustomers list, get if contains
        if (currentCustomers.size() == 1) {
            customer = currentCustomers.get(0);
        }
        // Get other appointment info
        String title = textTitle.getText();
        String description = textDesc.getText();
        String location = textLocation.getText();
        String contact = textContact.getText();
        
        // If contact == null, add w/ customers name and phone
        if (contact.length() == 0 && customer != null) {
            contact = customer.getCustomerName() + ", " + customer.getPhone();
        }
        
        String url = textURL.getText();
        LocalDate appointmentDate = datePicker.getValue();
        String startHour = textStartTimeHour.getText();
        String startMinute = textStartTimeMinute.getText();
        String startAmPm = startAMPM.getSelectionModel().getSelectedItem();
        String endHour = textEndTimeHour.getText();
        String endMinute = textEndTimeMinute.getText();
        String endAmPm = endAMPM.getSelectionModel().getSelectedItem();
        
        // Validation
        String errorMsg = Appointment.isAppointmentValid(customer, title, description, location,
                appointmentDate, startHour, startMinute, startAmPm, endHour, endMinute, endAmPm);
        
        // Check if errorMsg contains anything
        if (errorMsg.length() > 0) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("addingAppointment"));
            alert.setContentText(errorMsg);
            alert.showAndWait();
            return;
        }
        SimpleDateFormat localDateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        localDateFormat.setTimeZone(TimeZone.getDefault());
        Date startTimeLocal = null;
        Date endTimeLocal = null;
        
        // Format date and time strings into Date objects
        try {
            startTimeLocal = localDateFormat.parse(appointmentDate.toString() + " " + startHour + ":" + startMinute + " " + startAmPm);
            endTimeLocal = localDateFormat.parse(appointmentDate.toString() + " " + endHour + ":" + endMinute + " " + endAmPm);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        // Create ZonedDateTime out of Date objects
        ZonedDateTime startUTC = ZonedDateTime.ofInstant(startTimeLocal.toInstant(), ZoneId.of("UTC"));
        ZonedDateTime endUTC = ZonedDateTime.ofInstant(endTimeLocal.toInstant(), ZoneId.of("UTC"));
        
        // Submit to database
        if (addNewAppointment(customer, title, description, location, contact, url, startUTC, endUTC)) {
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
    }
    
    @FXML
    private void cancelAdd(ActionEvent event) {
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
    
    // Update upper table
    public void updateAddAppointmentAddTableView() {
        tableViewAdd.setItems(CustomerList.getCustomerList());
    }

    // Update lower table 
    public void updateAddAppointmentDeleteTableView() {
        tableViewDelete.setItems(currentCustomers);
    }
    
    @FXML
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AppointmentSummery", Locale.getDefault());
        addAptTitle.setText(rb.getString("addApt"));
        title.setText(rb.getString("title"));
        textTitle.setPromptText(rb.getString("title"));
        desc.setText(rb.getString("description"));
        textDesc.setPromptText(rb.getString("description"));
        location.setText(rb.getString("location"));
        textLocation.setPromptText(rb.getString("location"));
        contact.setText(rb.getString("contact"));
        textContact.setPromptText(rb.getString("contact"));
        url.setText(rb.getString("url"));
        textURL.setPromptText(rb.getString("url"));
        date.setText(rb.getString("date"));
        startTime.setText(rb.getString("startTime"));
        endTime.setText(rb.getString("endTime"));
        nameColumn.setText(rb.getString("nameColumn"));
        cityColumn.setText(rb.getString("cityColumn"));
        countryColumn.setText(rb.getString("countryColumn"));
        phoneColumn.setText(rb.getString("phoneColumn"));
        nameColumnDelete.setText(rb.getString("nameColumn"));
        cityColumnDelete.setText(rb.getString("cityColumn"));
        countryColumnDelete.setText(rb.getString("countryColumn"));
        phoneColumnDelete.setText(rb.getString("phoneColumn"));
        addBtn.setText(rb.getString("add"));
        deleteBtn.setText(rb.getString("delete"));
        btnAddAppointmentSave.setText(rb.getString("save"));
        btnAddAppointmentCancel.setText(rb.getString("cancel"));
    }
    
}
