package ViewController;

import Calendar.MonthlyView;
import Calendar.WeeklyView;
import Model.Customer;
import Model.CustomerList;
import static Model.CustomerList.getCustomerList;
import Model.DBManager;
import static Model.DBManager.logInAppointmentNotification;
import static Model.DBManager.updateAppointmentList;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class MainScreenController implements Initializable {

    @FXML
    private GridPane mainGrid;
    @FXML
    private Button addApt;
    @FXML
    private Button aptSum;
    @FXML
    private Button reports;
    @FXML
    private Button currentDate;
    @FXML
    private Button toggleView;
    @FXML
    private TableView<Customer> tableCustomers;
    @FXML
    private TableColumn<Customer, String> customersName;
    @FXML
    private TableColumn<Customer, String> customersAddress;
    @FXML
    private TableColumn<Customer, String> customersAddress2;
    @FXML
    private TableColumn<Customer, String> customersCity;
    @FXML
    private TableColumn<Customer, String> customersCountry;
    @FXML
    private TableColumn<Customer, String> customersPhone;
    @FXML
    private Button addCustomer;
    @FXML
    private Button modifyCustomer;
    @FXML
    private Button removeCustomer;
    @FXML
    private Button exit;
    @FXML
    private Pane staticPane;
    
    // Initializers
    private static int customerIndexToModify;
    private boolean monthlyView = true;
    private MonthlyView monthlyCalendar;
    private WeeklyView weeklyCalendar;
    private VBox monthlyCalendarView;
    private VBox weeklyCalendarView;
    private boolean errorMessage = true;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
        updateAppointmentList();
        updateCustomerTableView();
        logInAppointmentNotification();
        monthlyCalendar = new MonthlyView(YearMonth.now());
        monthlyCalendarView = monthlyCalendar.getView();
        mainGrid.add(monthlyCalendarView, 0, 0);
        
//Lambda Expressions
        customersName.setCellValueFactory(cellData -> cellData.getValue().customerNameProperty());
        customersAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
        customersAddress2.setCellValueFactory(cellData -> cellData.getValue().address2Property());
        customersCity.setCellValueFactory(cellData -> cellData.getValue().cityProperty());
        customersCountry.setCellValueFactory(cellData -> cellData.getValue().countryProperty());
        customersPhone.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
    }    

    @FXML
    private void addAppointment(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("AddAppointment.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void apptSummery(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("AppointmentSummery.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void reports(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("Reports.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void currentDate(ActionEvent event) {       
        if (monthlyView) {
            mainGrid.getChildren().remove(monthlyCalendarView);
            YearMonth currentYearMonth = YearMonth.now();
            monthlyCalendar = new MonthlyView(currentYearMonth);
            monthlyCalendarView = monthlyCalendar.getView();
            mainGrid.add(monthlyCalendarView, 0, 0);
            
        } else {
            mainGrid.getChildren().remove(weeklyCalendarView);
            LocalDate currentLocalDate = LocalDate.now();
            weeklyCalendar = new WeeklyView(currentLocalDate);
            weeklyCalendarView = weeklyCalendar.getView();
            mainGrid.add(weeklyCalendarView, 0, 0);
        }
    }

    @FXML
    private void changeView(ActionEvent event) {        
        if (monthlyView) {
            mainGrid.getChildren().remove(monthlyCalendarView);
            YearMonth currentYearMonth = monthlyCalendar.getCurrentYearMonth();
            LocalDate currentLocalDate = LocalDate.of(currentYearMonth.getYear(), currentYearMonth.getMonth(), 1);
            weeklyCalendar = new WeeklyView(currentLocalDate);
            weeklyCalendarView = weeklyCalendar.getView();
            mainGrid.add(weeklyCalendarView, 0, 0);
            toggleView.setText(ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault()).getString("toggleMonthly"));
            monthlyView = false;
            
        } else {
            mainGrid.getChildren().remove(weeklyCalendarView);
            LocalDate currentLocalDate = weeklyCalendar.getCurrentLocalDate();
            YearMonth currentYearMonth = YearMonth.from(currentLocalDate);
            monthlyCalendar = new MonthlyView(currentYearMonth);
            monthlyCalendarView = monthlyCalendar.getView();
            mainGrid.add(monthlyCalendarView, 0, 0);
            toggleView.setText(ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault()).getString("toggleWeekly"));
            monthlyView = true;
        }
    }

    @FXML
    private void addCustomer(ActionEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("AddCustomer.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modifyCustomer(ActionEvent event) {
        Customer customer = tableCustomers.getSelectionModel().getSelectedItem();

        if (customer == null) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorModifyingCustomer"));
            alert.setContentText(rb.getString("errorModifyingCustomerMessage"));
            alert.showAndWait();
            return;
        }

        customerIndexToModify = getCustomerList().indexOf(customer);

        try {
            Parent parent = FXMLLoader.load(getClass().getResource("ModifyCustomer.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void removeCustomer(ActionEvent event) {
        Customer customer = tableCustomers.getSelectionModel().getSelectedItem();
        if (customer == null) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorRemoving"));
            alert.setContentText(rb.getString("errorRemovingMessage"));
            alert.showAndWait();
            return;
        }
        CustomerList.customerToInactive(customer);
    }

    public static int getCustomerIndexToModify() {
        return customerIndexToModify;
    }
    
    public void updateCustomerTableView() {
        CustomerList.updateCustomerList();
        tableCustomers.setItems(CustomerList.getCustomerList());
    }
    
    @FXML
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault());
        currentDate.setText(rb.getString("currentDate"));
        toggleView.setText(rb.getString("toggleWeekly"));
        addApt.setText(rb.getString("addApt"));
        aptSum.setText(rb.getString("apptSum"));
        reports.setText(rb.getString("reports"));
        addCustomer.setText(rb.getString("addCust"));
        modifyCustomer.setText(rb.getString("modifyCustomer"));
        removeCustomer.setText(rb.getString("removeCustomer"));
        exit.setText(rb.getString("exit"));
        customersName.setText(rb.getString("tableCustomerName"));
        customersAddress.setText(rb.getString("tableAddress"));
        customersAddress2.setText(rb.getString("tableAddress2"));
        customersCity.setText(rb.getString("tableCity"));
        customersCountry.setText(rb.getString("tableCountry"));
        customersPhone.setText(rb.getString("tablePhone"));
    }
    
    @FXML
    private void exitBtn(ActionEvent event) throws SQLException, Exception {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("exit"));
        alert.setHeaderText(rb.getString("exitConfirm"));
        alert.setContentText(rb.getString("exitMessage"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            DBManager.closeConnection();
            System.exit(0);
        }
    }
}
