package Model;

import static Model.DBManager.conn;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;


public class CustomerList {

    private static ObservableList<Customer> customerList = FXCollections.observableArrayList();

    public static ObservableList<Customer> getCustomerList() {
        return customerList;
    }
    
    public static void updateCustomerList() {
        try (Statement stmt = conn.createStatement();) {
            ObservableList<Customer> customers = getCustomerList();
            customers.clear();
            
            ResultSet rs = stmt.executeQuery("SELECT customerId "
                    + "FROM customer WHERE active = 1");
            ArrayList<Integer> customerIdList = new ArrayList<>();
            
            while (rs.next()) {
                customerIdList.add(rs.getInt(1));
            }
            // Create Customer object for each customerId, then add that Customer to customerList
            for (int customerId : customerIdList) {
                Customer customer = new Customer();
                
                // Retrieve customer info
                ResultSet customerResultSet = stmt.executeQuery("SELECT customerName, active, addressId "
                        + "FROM customer WHERE customerId = " + customerId);
                customerResultSet.next();
                String customerName = customerResultSet.getString(1);
                int active = customerResultSet.getInt(2);
                int addressId = customerResultSet.getInt(3);
                customer.setCustomerId(customerId);
                customer.setCustomerName(customerName);
                customer.setActive(active);
                customer.setAddressId(addressId);
                
                // Retrieve address info
                ResultSet addressResultSet = stmt.executeQuery("SELECT address, address2, postalCode, phone, cityId "
                        + "FROM address WHERE addressId = " + addressId);
                addressResultSet.next();
                String address = addressResultSet.getString(1);
                String address2 = addressResultSet.getString(2);
                String postalCode = addressResultSet.getString(3);
                String phone = addressResultSet.getString(4);
                int cityId = addressResultSet.getInt(5);
                customer.setAddress(address);
                customer.setAddress2(address2);
                customer.setPostalCode(postalCode);
                customer.setPhone(phone);
                customer.setCityId(cityId);
                
                // Retrieve city info
                ResultSet cityResultSet = stmt.executeQuery("SELECT city, countryId "
                        + "FROM city WHERE cityId = " + cityId);
                cityResultSet.next();
                String city = cityResultSet.getString(1);
                int countryId = cityResultSet.getInt(2);
                customer.setCity(city);
                customer.setCountryId(countryId);
                
                // Retrieve country info
                ResultSet countryResultSet = stmt.executeQuery("SELECT country "
                        + "FROM country WHERE countryId = " + countryId);
                countryResultSet.next();
                String country = countryResultSet.getString(1);
                customer.setCountry(country);
                
                // Add the new Customer object to customerList
                customers.add(customer);
            }
        } catch (SQLException e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorConnectingToDatabase"));
            alert.setContentText(rb.getString("errorConnectingToDatabaseMsg"));
            alert.show();
        }
    }
    
    public static void customerToInactive(Customer customer) {
        int customerId = customer.getCustomerId();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("remove"));
        alert.setHeaderText(rb.getString("removingCustomer"));
        alert.setContentText(rb.getString("removingCustomerMsg"));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            try (Statement stmt = conn.createStatement();) {
                stmt.executeUpdate("UPDATE customer SET active = 0 WHERE customerId = " + customerId);
                
            } catch (SQLException e) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                alert2.setTitle(rb.getString("error"));
                alert2.setHeaderText(rb.getString("errorModifyingCustomer"));
                alert2.setContentText(rb.getString("errorRequiresDatabase"));
                alert2.showAndWait();
            }
            updateCustomerList();
        }
    }
}