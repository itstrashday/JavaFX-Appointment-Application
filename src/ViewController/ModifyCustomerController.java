package ViewController;

import Model.Customer;
import static Model.CustomerList.getCustomerList;
import static Model.DBManager.calculateAddressId;
import static Model.DBManager.calculateCityId;
import static Model.DBManager.calculateCountryId;
import static Model.DBManager.modifyCustomer;
import static Model.DBManager.setCustomerToActive;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Daniel Richardson
 */
public class ModifyCustomerController implements Initializable {

    @FXML
    private Label title;
    @FXML
    private Label name;
    @FXML
    private Label address;
    @FXML
    private Label address2;
    @FXML
    private Label city;
    @FXML
    private Label country;
    @FXML
    private Label postalCode;
    @FXML
    private Label phone;
    @FXML
    private TextField textName;
    @FXML
    private TextField textAddress;
    @FXML
    private TextField textAddress2;
    @FXML
    private TextField textCity;
    @FXML
    private TextField textCountry;
    @FXML
    private TextField textPostalCode;
    @FXML
    private TextField textPhone;
    @FXML
    private Button saveBtn;
    @FXML
    private Button cancelBtn;
    
    private Customer customer;

    int customerIndexToModify = MainScreenController.getCustomerIndexToModify();

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setLanguage();
        
        customer = getCustomerList().get(customerIndexToModify);
        String customerName = customer.getCustomerName();
        String address = customer.getAddress();
        String address2 = customer.getAddress2();
        String city = customer.getCity();
        String country = customer.getCountry();
        String postalCode = customer.getPostalCode();
        String phone = customer.getPhone();
        
        textName.setText(customerName);
        textAddress.setText(address);
        textAddress2.setText(address2);
        textCity.setText(city);
        textCountry.setText(country);
        textPostalCode.setText(postalCode);
        textPhone.setText(phone);
    }    

    @FXML
    private void saveHandler(ActionEvent event) {
        int customerId = customer.getCustomerId();
        String customerName = textName.getText();
        String address = textAddress.getText();
        String address2 = textAddress2.getText();
        String city = textCity.getText();
        String country = textCountry.getText();
        String postalCode = textPostalCode.getText();
        String phone = textPhone.getText();

        String errorMsg = Customer.isCustomerValid(customerName, address, city, country, postalCode, phone);

        if (errorMsg.length() > 0) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.AddAndModify", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorModifyingCustomer"));
            alert.setContentText(errorMsg);
            alert.showAndWait();
            return;
        }
        
        // Submit info to database
        int modifyCustomerCheck = modifyCustomer(customerId, customerName, address, address2, city, country, postalCode, phone);
       
        // Check active status
        if (modifyCustomerCheck == 1) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.AddAndModify", Locale.getDefault());
            // Create alert saying that customer already exists
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorModifyingCustomer"));
            alert.setContentText(rb.getString("errorCustomerAlreadyExists"));
            alert.showAndWait();

        } else if (modifyCustomerCheck == 0) {
            // Calculate country, city and addressId's
            int countryId = calculateCountryId(country);
            int cityId = calculateCityId(city, countryId);
            int addressId = calculateAddressId(address, address2, postalCode, phone, cityId);
            setCustomerToActive(customerName, addressId);
        }
        
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

    @FXML
    private void cancelHandler(ActionEvent event) {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AddAndModify", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("confirmCancel"));
        alert.setHeaderText(rb.getString("confirmCancel"));
        alert.setContentText(rb.getString("confirmCancelAddMsg"));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
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
    private void setLanguage() {
        ResourceBundle rb = ResourceBundle.getBundle("Resources.AddAndModify", Locale.getDefault());
        title.setText(rb.getString("modifyCustomer"));
        name.setText(rb.getString("customerName"));
        textName.setPromptText(rb.getString("customerName"));
        address.setText(rb.getString("address"));
        textAddress.setPromptText(rb.getString("address"));
        address2.setText(rb.getString("address2"));
        textAddress2.setPromptText(rb.getString("address2"));
        city.setText(rb.getString("city"));
        textCity.setPromptText(rb.getString("city"));
        country.setText(rb.getString("country"));
        textCountry.setPromptText(rb.getString("country"));
        postalCode.setText(rb.getString("postalCode"));
        textPostalCode.setPromptText(rb.getString("postalCode"));
        phone.setText(rb.getString("phone"));
        textPhone.setPromptText(rb.getString("phone"));
        saveBtn.setText(rb.getString("save"));
        cancelBtn.setText(rb.getString("cancel"));
    }
}
