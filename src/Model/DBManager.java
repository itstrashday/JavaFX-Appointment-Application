package Model;

import static Model.CustomerList.updateCustomerList;
import ViewController.AppointmentSummeryController;
import ViewController.LoginScreenController;
import com.mysql.jdbc.Connection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;

/**
 *
 * @author Daniel Richardson
 */
public class DBManager {
    
    private static final String databaseName = "U04UZK";
    private static final String DB_URL = "jdbc:mysql://52.206.157.109/" + databaseName;
    private static final String username = "U04UZK";
    private static final String password = "53688348446";
    private static final String driver = "com.mysql.jdbc.Driver";
    public static Connection conn;
    private static String currentUser;
    private static int sessionCount = 0;
    
    //Connect to database
    public static void makeConnection() throws ClassNotFoundException, SQLException, Exception {
        Class.forName(driver);
        conn = (Connection) DriverManager.getConnection(DB_URL, username, password);
        System.out.println("Connection Successful!");
    }
    
    //Disconnect from database
    public static void closeConnection() throws ClassNotFoundException, SQLException, Exception {
        conn.close();
        System.out.println("Connection Closed!");
    }
    
    public static boolean checkLogInCredentials(String username, String password) {
        int userId = getUserId(username);
        boolean correctPass = checkPass(userId, password);
        
        if (correctPass) {
//Lambda expression implementing functional interface           
            SetCurrentUser user = (String c, String u) -> currentUser = username;
            
            //Create logins.txt file to log timestamps of successfull logins
            try {
                Path path = Paths.get("UserLogins.txt");
                Files.write(path, Arrays.asList("User " + currentUser + " logged in at " + Date.from(Instant.now()).toString() + "."),
                        StandardCharsets.UTF_8, Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
            
        } else {
            return false;
        }
    }

    private static int getUserId(String userName) {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        int userId = -1;
        
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT userId FROM user WHERE userName = '" + userName + "'");

            if (rs.next()) {
                userId = rs.getInt("userId");
            }
            rs.close();
        } catch (SQLException e) {
            LoginScreenController.incrementDatabaseError();
        }
        return userId;
    }

    private static boolean checkPass(int userId, String password) {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT password FROM user WHERE userId = " + userId);

            String dbPass = null;
            if (rs.next()) {
                dbPass = rs.getString("password");
            } else {
                return false;
            }
            
            rs.close();
            if (dbPass.equals(password)) {
                return true;
                
            } else {
                return false;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    //Interface used for lambda expression
    @FunctionalInterface
    interface SetCurrentUser {
       void user(String currentUser, String userName);
    }
    
    public static void logInAppointmentNotification() {
        // Checks session
        if (sessionCount == 0) {
            // Create ObservableList of appointments
            ObservableList<Appointment> userAppointments = FXCollections.observableArrayList();
            for (Appointment appointment : AppointmentList.getAppointmentList()) {
                if (appointment.getCreatedBy().equals(currentUser)) {
                    userAppointments.add(appointment);
                }
            }
            // Check each appointment in userAppointments to see if any start in the next 15 minutes
            // and create a date object for 15 min in the future
            for (Appointment appointment : userAppointments) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Date.from(Instant.now()));
                calendar.add(Calendar.MINUTE, 15);
                Date notificationCutoff = calendar.getTime();
                
                //Check if appointment is 15 minutes from now, if true, alert
                if (appointment.getStartDate().before(notificationCutoff)) {
                    ResourceBundle rb = ResourceBundle.getBundle("Resources.MainScreen", Locale.getDefault());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(rb.getString("upcomingApt"));
                    alert.setHeaderText(rb.getString("upcomingApt"));
                    alert.setContentText(rb.getString("upcomingAptMsg") + "\n" 
                            + rb.getString("aptTitle") + ": " + appointment.getTitle() + "\n" 
                            + rb.getString("aptDesc") + ": " + appointment.getDescription() + "\n" 
                            + rb.getString("aptLocation") + ": " + appointment.getLocation() + "\n" 
                            + rb.getString("aptContact") + ": " + appointment.getContact() + "\n" 
                            + rb.getString("aptURL") + ": " + appointment.getUrl() + "\n" 
                            + rb.getString("aptDate") + ": " + appointment.getDateString() + "\n" 
                            + rb.getString("aptStart") + ": " + appointment.getStartString() + "\n" 
                            + rb.getString("aptEnd") + ": " + appointment.getEndString());
                    alert.showAndWait();
                }
            }
            // Increment, so session increases by 1 to avoid multiple alerts
            sessionCount++;
        }
    }
    
    public static void addNewCustomer(String customerName, String address, String address2,
                                   String city, String country, String postalCode, String phone) {
        try {
            int countryId = calculateCountryId(country);
            int cityId = calculateCityId(city, countryId);
            int addressId = calculateAddressId(address, address2, postalCode, phone, cityId);
            
            // Check if customer new/exists
            if (checkIfCustomerExists(customerName, addressId)) {
                try (Statement stmt = conn.createStatement();) {
                    ResultSet rs = stmt.executeQuery("SELECT active FROM customer WHERE " 
                            + "customerName = '" + customerName + "' AND addressId = " + addressId);
                    rs.next();
                    
                    int active = rs.getInt(1);
                    // Check if existing customer is active/inactive
                    if (active == 1) {
                        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle(rb.getString("error"));
                        alert.setHeaderText(rb.getString("errorAddingCustomer"));
                        alert.setContentText(rb.getString("errorCustomerAlreadyExists"));
                        alert.showAndWait();
                        
                    // Set to active if inactive
                    } else if (active == 0) {
                        setCustomerToActive(customerName, addressId);
                    }
                }
            // Add new if not exists
            } else {
                addCustomer(customerName, addressId);
            }
        } catch (SQLException e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingCustomer"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
        }
    }
    
    public static int calculateCountryId(String country) {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT countryId FROM country WHERE country = '" + country + "'");
            
            // Check if entry already exists, return rs if it does
            if (rs.next()) {
                int countryId = rs.getInt(1);
                rs.close();
                return countryId;
                
            } else {
                rs.close();
                int countryId;
                ResultSet tableCountryId = stmt.executeQuery("SELECT countryId FROM country ORDER BY countryId");
                
                // Check last countryId value add 1 for new
                if (tableCountryId.last()) {
                    countryId = tableCountryId.getInt(1) + 1;
                    tableCountryId.close();
                    
                // If null, set countryId to 1
                } else {
                    tableCountryId.close();
                    countryId = 1;
                }
                // Create new entry with new countryId value
                stmt.executeUpdate("INSERT INTO country VALUES (" 
                        + countryId + ", '" + country + "', CURRENT_DATE, " 
                        + "'" + currentUser + "', CURRENT_TIMESTAMP, '" + currentUser + "')");
                return countryId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int calculateCityId(String city, int countryId) {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT cityId FROM city WHERE city = '" 
                        + city + "' AND countryid = " + countryId);
            
            // Check if already exists
            if (rs.next()) {
                int cityId = rs.getInt(1);
                rs.close();
                return cityId;
            } else {
                rs.close();
                int cityId;
                ResultSet tableCityId = stmt.executeQuery("SELECT cityId FROM city ORDER BY cityId");
                
                // Check last cityId value add 1 for new
                if (tableCityId.last()) {
                    cityId = tableCityId.getInt(1) + 1;
                    tableCityId.close();
                    
                // If no values set to 1
                } else {
                    tableCityId.close();
                    cityId = 1;
                }
                // Create new entry
                stmt.executeUpdate("INSERT INTO city VALUES (" 
                        + cityId + ", '" + city + "', " + countryId + ", CURRENT_DATE, " 
                        + "'" + currentUser + "', CURRENT_TIMESTAMP, '" + currentUser + "')");
                return cityId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int calculateAddressId(String address, String address2, 
                                         String postalCode, String phone, int cityId) {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT addressId FROM address WHERE address = '" 
                        + address + "' AND " + "address2 = '" + address2 
                        + "' AND postalCode = '" + postalCode + "' AND phone = '" 
                        + phone + "' AND cityId = " + cityId);
            // Check if exists
            if (rs.next()) {
                int addressId = rs.getInt(1);
                rs.close();
                return addressId;
                
            } else {
                rs.close();
                int addressId;
                ResultSet allAddressId = stmt.executeQuery("SELECT addressId FROM address ORDER BY addressId");
                
                // Check last value and add 1 if new
                if (allAddressId.last()) {
                    addressId = allAddressId.getInt(1) + 1;
                    allAddressId.close();
                    
                // If no values set to 1
                } else {
                    allAddressId.close();
                    addressId = 1;
                }
                // Create new entry with new addressId value
                stmt.executeUpdate("INSERT INTO address VALUES (" + addressId + ", '" 
                        + address + "', '" +address2 + "', " + cityId + ", " + "'" + postalCode + "', '" + phone 
                        + "', CURRENT_DATE, '" + currentUser + "', CURRENT_TIMESTAMP, '" + currentUser + "')");
                return addressId;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean checkIfCustomerExists(String customerName, int addressId) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT customerId FROM customer WHERE customerName = '" 
                        + customerName + "' " + "AND addressId = " + addressId);
            
            // Check if exists
            if (rs.next()) {
                rs.close();
                return true;
                
            } else {
                rs.close();
                return false;
            }
        }
    }

    public static void setCustomerToActive(String customerName, int addressId) {
        try (Statement stmt = conn.createStatement();) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initModality(Modality.NONE);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorModifyingCustomer"));
            alert.setContentText(rb.getString("errorSetActive"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                stmt.executeUpdate("UPDATE customer SET active = 1, lastUpdate = CURRENT_TIMESTAMP, " 
                        + "lastUpdateBy = '" + currentUser + "' WHERE customerName = '" + customerName 
                        + "' AND addressId = " + addressId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addCustomer(String customerName, int addressId) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT customerId FROM customer ORDER BY customerId");
            int customerId;
            
            // Check value and add one if new
            if (rs.last()) {
                customerId = rs.getInt(1) + 1;
                rs.close();
                
            // If no values present set to 1
            } else {
                rs.close();
                customerId = 1;
            }
            // Create new entry
            stmt.executeUpdate("INSERT INTO customer VALUES (" + customerId + ", '" + customerName + "', " 
                    + addressId + ", 1, " + "CURRENT_DATE, '" + currentUser + "', CURRENT_TIMESTAMP, '" + currentUser + "')");
        }
    }
    
    public static int modifyCustomer(int customerId, String customerName, String address, String address2,
                                      String city, String country, String postalCode, String phone) {
        try {
            // Find customer's country, city and addressId
            int countryId = calculateCountryId(country);
            int cityId = calculateCityId(city, countryId);
            int addressId = calculateAddressId(address, address2, postalCode, phone, cityId);
            
            // Check if customer exists
            if (checkIfCustomerExists(customerName, addressId)) {
                // If exists, get customerId and use customerId to get their active status
                int existingCustomerId = getCustomerId(customerName, addressId);
                int activeStatus = getActiveStatus(existingCustomerId);
                return activeStatus;
                
            } else {
                // If customer does not exist, update database and clean the database of unused entries
                updateCustomer(customerId, customerName, addressId);
                cleanDatabase();
                return -1;
            }
        } catch (SQLException e) {
            ResourceBundle rb = ResourceBundle.getBundle("DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorModifyingCustomer"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
            return -1;
        }
    }
    
    private static int getCustomerId(String customerName, int addressId) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT customerId FROM customer WHERE customerName = '" + customerName + "' AND addressId = " + addressId);
            rs.next();
            int customerId = rs.getInt(1);
            return customerId;
        }
    }

    private static int getActiveStatus(int customerId) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT active FROM customer WHERE customerId = " + customerId);
            rs.next();
            int active = rs.getInt(1);
            return active;
        }
    }

    private static void updateCustomer(int customerId, String customerName, int addressId) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            stmt.executeUpdate("UPDATE customer SET customerName = '" + customerName + "', addressId = " + addressId + ", " +
                    "lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = '" + currentUser + "' WHERE customerId = " + customerId);
        }
    }
    
    public static boolean addNewAppointment(Customer customer, String title, String description, String location,
                                         String contact, String url, ZonedDateTime startUTC, ZonedDateTime endUTC) {
        // Convert ZonedDateTimes to Timestamps
        String startUTCString = startUTC.toString();
        startUTCString = startUTCString.substring(0,10) + " " + startUTCString.substring(11,16) + ":00";
        Timestamp startTimestamp = Timestamp.valueOf(startUTCString);
        String endUTCString = endUTC.toString();
        endUTCString = endUTCString.substring(0,10) + " " + endUTCString.substring(11,16) + ":00";
        Timestamp endTimestamp = Timestamp.valueOf(endUTCString);
        
        // Check for overlaps. Alert if it does.
        if (doesAppointmentOverlap(startTimestamp, endTimestamp)) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingApt"));
            alert.setContentText(rb.getString("errorAptsOverlap"));
            alert.showAndWait();
            return false;
            
        // Add new if doesn't overlap
        } else {
            int customerId = customer.getCustomerId();
            addAppointment(customerId, title, description, location, contact, url, startTimestamp, endTimestamp);
            return true;
        }
    }

    private static boolean doesAppointmentOverlap(Timestamp startTimestamp, Timestamp endTimestamp) {
        updateAppointmentList();
        ObservableList<Appointment> appointmentList = AppointmentList.getAppointmentList();
        for (Appointment appointment: appointmentList) {
            Timestamp existingStartTimestamp = appointment.getStartTimestamp();
            Timestamp existingEndTimestamp = appointment.getEndTimestamp();

            if (startTimestamp.after(existingStartTimestamp) && startTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (endTimestamp.after(existingStartTimestamp) && endTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.after(existingStartTimestamp) && endTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.before(existingStartTimestamp) && endTimestamp.after(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.equals(existingStartTimestamp)) {
                return true;
            }
            if (endTimestamp.equals(existingEndTimestamp)) {
                return true;
            }
        }
        return false;
    }

    private static void addAppointment(int customerId, String title, String description, String location,
                                       String contact, String url, Timestamp startTimestamp, Timestamp endTimestamp) {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT appointmentId FROM appointment ORDER BY appointmentId");
            int appointmentId;
            
            // Check last appointmentId value and add one to it for new
            if (rs.last()) {
                appointmentId = rs.getInt(1) + 1;
                rs.close();
                
            } else {
                rs.close();
                appointmentId = 1;
            }
            
            // Create new entry 
            stmt.executeUpdate("INSERT INTO appointment VALUES (" + appointmentId +", " + customerId + ", '" + title + "', '" +
                    description + "', '" + location + "', '" + contact + "', '" + url + "', '" + startTimestamp + "', '" + endTimestamp + "', " +
                    "CURRENT_DATE, '" + currentUser + "', CURRENT_TIMESTAMP, '" + currentUser + "')");
        } catch (SQLException e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingApt"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
        }
    }

    public static boolean modifyAppointment(int appointmentId, Customer customer, String title, String description, String location,
                                         String contact, String url, ZonedDateTime startUTC, ZonedDateTime endUTC) {
        try {
            // Convert ZonedDateTimes to Timestamps
            String startUTCString = startUTC.toString();
            startUTCString = startUTCString.substring(0, 10) + " " + startUTCString.substring(11, 16) + ":00";
            Timestamp startTimestamp = Timestamp.valueOf(startUTCString);
            String endUTCString = endUTC.toString();
            endUTCString = endUTCString.substring(0, 10) + " " + endUTCString.substring(11, 16) + ":00";
            Timestamp endTimestamp = Timestamp.valueOf(endUTCString);
            
            // Check for overlaps.
            if (doesAppointmentOverlapOthers(startTimestamp, endTimestamp)) {
                ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(rb.getString("error"));
                alert.setHeaderText(rb.getString("errorModifyingApt"));
                alert.setContentText(rb.getString("errorAptsOverlap"));
                alert.showAndWait();
                return false;
            } else {
                int customerId = customer.getCustomerId();
                updateAppointment(appointmentId, customerId, title, description, location, contact, url, startTimestamp, endTimestamp);
                return true;
            }
        } catch (Exception e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingApt"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
            return false;
        }
    }

    private static void updateAppointment(int appointmentId, int customerId, String title, String description, String location,
                                          String contact, String url, Timestamp startTimestamp, Timestamp endTimestamp) throws SQLException {
        try (Statement stmt = conn.createStatement();) {
            stmt.executeUpdate("UPDATE appointment SET customerId = " + customerId + ", title = '" + title 
                    + "', description = '" + description + "', " + "location = '" + location + "', contact = '" 
                    + contact + "', url = '" + url + "', start = '" + startTimestamp + "', end = '" + endTimestamp 
                    + "', " +  "lastUpdate = CURRENT_TIMESTAMP, lastUpdateBy = '" + currentUser 
                    + "' WHERE appointmentId = " + appointmentId);
        }
    }

    private static boolean doesAppointmentOverlapOthers(Timestamp startTimestamp, Timestamp endTimestamp) throws SQLException, ParseException {
        int appointmentIndexToRemove = AppointmentSummeryController.getAppointmentIndexToModify();
        ObservableList<Appointment> appointmentList = AppointmentList.getAppointmentList();
        appointmentList.remove(appointmentIndexToRemove);
        for (Appointment appointment: appointmentList) {
            Timestamp existingStartTimestamp = appointment.getStartTimestamp();
            Timestamp existingEndTimestamp = appointment.getEndTimestamp();

            if (startTimestamp.after(existingStartTimestamp) && startTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (endTimestamp.after(existingStartTimestamp) && endTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.after(existingStartTimestamp) && endTimestamp.before(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.before(existingStartTimestamp) && endTimestamp.after(existingEndTimestamp)) {
                return true;
            }
            if (startTimestamp.equals(existingStartTimestamp)) {
                return true;
            }
            if (endTimestamp.equals(existingEndTimestamp)) {
                return true;
            }
        }
        return false;
    }

    public static void deleteAppointment(Appointment appointmentToDelete) {
        int appointmentId = appointmentToDelete.getAppointmentId();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initModality(Modality.NONE);
        alert.setTitle(rb.getString("delete"));
        alert.setHeaderText(rb.getString("deleteApt"));
        alert.setContentText(rb.getString("deleteAptMsg"));
        Optional<ButtonType> result = alert.showAndWait();

        if (result.get() == ButtonType.OK) {
            try (Statement stmt = conn.createStatement();) {
                stmt.executeUpdate("DELETE FROM appointment WHERE appointmentId =" + appointmentId);
                
            } catch (Exception e) {
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                alert2.setTitle(rb.getString("error"));
                alert2.setHeaderText(rb.getString("errorModifyingApt"));
                alert2.setContentText(rb.getString("errorRequiresDatabase"));
                alert2.showAndWait();
            }
            updateAppointmentList();
        }
    }
    
    public static void generateAppointmentTypeByMonthReport() {
        updateAppointmentList();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
        String report = rb.getString("aptTypeByMonth");
        ArrayList<String> monthsWithAppointments = new ArrayList<>();

        for (Appointment appointment : AppointmentList.getAppointmentList()) {
            Date startDate = appointment.getStartDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            String yearMonth = year + "-" + month;
            
            if (month < 10) {
                yearMonth = year + "-0" + month;
            }
            
            if (!monthsWithAppointments.contains(yearMonth)) {
                monthsWithAppointments.add(yearMonth);
            }
        }
        
        // Sort year/months
        Collections.sort(monthsWithAppointments);
        for (String yearMonth : monthsWithAppointments) {
            int year = Integer.parseInt(yearMonth.substring(0,4));
            int month = Integer.parseInt(yearMonth.substring(5,7));

            int typeCount = 0;
            ArrayList<String> descriptions = new ArrayList<>();
            for (Appointment appointment : AppointmentList.getAppointmentList()) {
                // Get appointment start date
                Date startDate = appointment.getStartDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                int appointmentYear = calendar.get(Calendar.YEAR);
                int appointmentMonth = calendar.get(Calendar.MONTH) + 1;
                
                // If year and month match, get description
                if (year == appointmentYear && month == appointmentMonth) {
                    String description = appointment.getDescription();
                    // If appointment description is not in ArrayList, add it and increment typeCount
                    if (!descriptions.contains(description)) {
                        descriptions.add(description);
                        typeCount++;
                    }
                }
            }
            
            // Add year/month to report
            report = report + yearMonth + ": " + typeCount + "\n";
            report = report + rb.getString("types");
            
            // Add each type description to report
            for (String description : descriptions) {
                report = report + " " + description + ",";
            }
            
            // Remove comma from type descriptions
            report = report.substring(0, report.length()-1);
            
            // Add paragraph break between
            report = report + "\n \n";
        }
        // Print report. Overwrite file if it already exists.
        try {
            Path path = Paths.get("AppointmentTypeByMonth.txt");
            Files.write(path, Arrays.asList(report), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void generateScheduleForConsultants() {
        updateAppointmentList();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
        // Initialize report string
        String report = rb.getString("consultantSched");
        ArrayList<String> consultantsWithAppointments = new ArrayList<>();
        
        //Check createdBy and new createdBy
        for (Appointment appointment : AppointmentList.getAppointmentList()) {
            String consultant = appointment.getCreatedBy();
            if (!consultantsWithAppointments.contains(consultant)) {
                consultantsWithAppointments.add(consultant);
            }
        }
        
        // Sort consultants
        Collections.sort(consultantsWithAppointments);
        
        for (String consultant : consultantsWithAppointments) {
            // Add consultant name to report
            report = report + consultant + ": \n";
            for (Appointment appointment : AppointmentList.getAppointmentList()) {
                String appointmentConsultant = appointment.getCreatedBy();
                
                // Check if createdBy matches consultant
                if (consultant.equals(appointmentConsultant)) {
                    String date = appointment.getDateString();
                    String title = appointment.getTitle();
                    Date startDate = appointment.getStartDate();
                    
                    // Convert times to AM/PM format
                    String startTime = startDate.toString().substring(11,16);
                    if (Integer.parseInt(startTime.substring(0,2)) > 12) {
                        startTime = Integer.parseInt(startTime.substring(0,2)) - 12 + startTime.substring(2,5) + "PM";
                    
                    } else if (Integer.parseInt(startTime.substring(0,2)) == 12) {
                        startTime = startTime + "PM";
                    
                    } else {
                        startTime = startTime + "AM";
                    }
                    
                    Date endDate = appointment.getEndDate();
                    String endTime = endDate.toString().substring(11,16);
                    
                    if (Integer.parseInt(endTime.substring(0,2)) > 12) {
                        endTime = Integer.parseInt(endTime.substring(0,2)) - 12 + endTime.substring(2,5) + "PM";
                   
                    } else if (Integer.parseInt(endTime.substring(0,2)) == 12) {
                        endTime = endTime + "PM";
                    
                    } else {
                        endTime = endTime + "AM";
                    }
                    
                    // Get timezone
                    String timeZone = startDate.toString().substring(20,23);
                    
                    // Add appointment info to report
                    report = report + date + ": " + title + rb.getString("from") + startTime + rb.getString("to") +
                            endTime + " " + timeZone + ". \n";
                }
            }
            // Add break between 
            report = report + "\n \n";
        }
        
        // Print report. Overwrite file if it already exists.
        try {
            Path path = Paths.get("ScheduleByConsultant.txt");
            Files.write(path, Arrays.asList(report), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void generateUpcomingMeetingsByCustomer() {
        updateAppointmentList();
        ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
        String report = rb.getString("customerSched");
        ArrayList<Integer> customerIdsWithAppointments = new ArrayList<>();
        
        // Check customerId, add new 
        for (Appointment appointment : AppointmentList.getAppointmentList()) {
            int customerId = appointment.getCustomerId();
            if (!customerIdsWithAppointments.contains(customerId)) {
                customerIdsWithAppointments.add(customerId);
            }
        }
        
        // Sort customerId's
        Collections.sort(customerIdsWithAppointments);
        updateCustomerList();
        
        for (int customerId : customerIdsWithAppointments) {
            for (Customer customer : CustomerList.getCustomerList()) {
                // Traverse customerRoster finding a match for customerId
                int customerIdToCheck = customer.getCustomerId();
                if (customerId == customerIdToCheck) {
                    // Add customer name to report
                    report = report + customer.getCustomerName() + ": \n";
                }
            }
            
            for (Appointment appointment : AppointmentList.getAppointmentList()) {
                int appointmentCustomerId = appointment.getCustomerId();
                // Check if customerId matches customer
                if (customerId == appointmentCustomerId) {
                    // Get appointment date and description
                    String date = appointment.getDateString();
                    String description = appointment.getDescription();
                    Date startDate = appointment.getStartDate();
                    // Convert times to AM/PM format
                    String startTime = startDate.toString().substring(11,16);
                    if (Integer.parseInt(startTime.substring(0,2)) > 12) {
                        startTime = Integer.parseInt(startTime.substring(0,2)) - 12 + startTime.substring(2,5) + "PM";
                    
                    } else if (Integer.parseInt(startTime.substring(0,2)) == 12) {
                        startTime = startTime + "PM";
                   
                    } else {
                        startTime = startTime + "AM";
                    }
                    
                    Date endDate = appointment.getEndDate();
                    String endTime = endDate.toString().substring(11,16);
                    
                    if (Integer.parseInt(endTime.substring(0,2)) > 12) {
                        endTime = Integer.parseInt(endTime.substring(0,2)) - 12 + endTime.substring(2,5) + "PM";
                   
                    } else if (Integer.parseInt(endTime.substring(0,2)) == 12) {
                        endTime = endTime + "PM";
                   
                    } else {
                        endTime = endTime + "AM";
                    }
                    // Get timezone
                    String timeZone = startDate.toString().substring(20,23);
                    // Add appointment info to report
                    report = report + date + ": " + description + rb.getString("from") + startTime + rb.getString("to") +
                            endTime + " " + timeZone + ". \n";
                }
            }
            // Add break between
            report = report + "\n \n";
        }
        // Print report. Overwrite file if exists.
        try {
            Path path = Paths.get("ScheduleByCustomer.txt");
            Files.write(path, Arrays.asList(report), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void updateAppointmentList() {
        try (Statement stmt = conn.createStatement();) {
            ObservableList<Appointment> appointmentList = AppointmentList.getAppointmentList();
            appointmentList.clear();

            ResultSet appointmentResultSet = stmt.executeQuery("SELECT appointmentId FROM appointment "
                    + "WHERE start >= CURRENT_TIMESTAMP");
            ArrayList<Integer> appointmentIdList = new ArrayList<>();
            while(appointmentResultSet.next()) {
                appointmentIdList.add(appointmentResultSet.getInt(1));
            }
            // Create Appointment object for each appointmentId in list and add Appointment to appointmentList
            for (int appointmentId : appointmentIdList) {
                appointmentResultSet = stmt.executeQuery("SELECT customerId, title, description, location, contact, url, start, end, createdBy "
                        + "FROM appointment WHERE appointmentId = " + appointmentId);
                appointmentResultSet.next();
                int customerId = appointmentResultSet.getInt(1);
                String title = appointmentResultSet.getString(2);
                String description = appointmentResultSet.getString(3);
                String location = appointmentResultSet.getString(4);
                String contact = appointmentResultSet.getString(5);
                String url = appointmentResultSet.getString(6);
                Timestamp startTimestamp = appointmentResultSet.getTimestamp(7);
                Timestamp endTimestamp = appointmentResultSet.getTimestamp(8);
                String createdBy = appointmentResultSet.getString(9);
                
                // Convert startTimestamp and endTimestamp to ZonedDateTimes
                DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                java.util.Date startDate = utcFormat.parse(startTimestamp.toString());
                java.util.Date endDate = utcFormat.parse(endTimestamp.toString());
                
                Appointment appointment = new Appointment(appointmentId, customerId, title, description, location, contact, url, startTimestamp, endTimestamp, startDate, endDate, createdBy);

                appointmentList.add(appointment);
            }
        } catch (Exception e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingApt"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
        }
    }   
    private static void cleanDatabase() {
        try (Statement stmt = conn.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT addressId FROM customer "
                    + "ORDER BY addressId");
            ArrayList<Integer> addressIdListFromCustomer = new ArrayList<>();
            
            while (rs.next()) {
                addressIdListFromCustomer.add(rs.getInt(1));
            }
            
            rs = stmt.executeQuery("SELECT DISTINCT addressId FROM address ORDER BY addressId");
            ArrayList<Integer> addressIdListFromAddress = new ArrayList<>();
            while (rs.next()) {
                addressIdListFromAddress.add(rs.getInt(1));
            }
            
            // Create list of addressId's that exist in Address table but are not used in Customer table
            for (int i = 0; i < addressIdListFromCustomer.size(); i++) {
                for (int j = 0; j < addressIdListFromAddress.size(); j++) {
                    if (addressIdListFromCustomer.get(i) == addressIdListFromAddress.get(j)) {
                        addressIdListFromAddress.remove(j);
                        j--;
                    }
                }
            }
            
            // Delete Address table entries by remaining addressId's
            if (addressIdListFromAddress.isEmpty()) {}
            else {
                for (int addressId : addressIdListFromAddress) {
                    stmt.executeUpdate("DELETE FROM address WHERE addressId = " + addressId);
                }
            }

            // Create list of cityId's used in Address table
            ResultSet cityIdRS = stmt.executeQuery("SELECT DISTINCT cityId FROM address ORDER BY cityId");
            ArrayList<Integer> cityIdListFromAddress = new ArrayList<>();
            while (cityIdRS.next()) {
                cityIdListFromAddress.add(cityIdRS.getInt(1));
            }
            
            // Create list of cityId's used in City table
            cityIdRS = stmt.executeQuery("SELECT DISTINCT cityId FROM city ORDER BY cityId");
            ArrayList<Integer> cityIdListFromCity = new ArrayList<>();
            while (cityIdRS.next()) {
                cityIdListFromCity.add(cityIdRS.getInt(1));
            }
            
            // Create list of cityId's that exist in City table but are not used in Address table
            for (int i = 0; i < cityIdListFromAddress.size(); i++) {
                for (int j = 0; j < cityIdListFromCity.size(); j++) {
                    if (cityIdListFromAddress.get(i) == cityIdListFromCity.get(j)) {
                        cityIdListFromCity.remove(j);
                        j--;
                    }
                }
            }
            
            // Delete City table entries by remaining cityId's
            if (cityIdListFromCity.isEmpty()) {}
            else {
                for (int cityId : cityIdListFromCity) {
                    stmt.executeUpdate("DELETE FROM city WHERE cityId = " + cityId);
                }
            }

            // Create list of countryId's used in City table
            ResultSet countryIdRS = stmt.executeQuery("SELECT DISTINCT countryId FROM city "
                    + "ORDER BY countryId");
            ArrayList<Integer> countryIdListFromCity = new ArrayList<>();
            while (countryIdRS.next()) {
                countryIdListFromCity.add(countryIdRS.getInt(1));
            }
            
            // Create list of countryId's used in Country table
            countryIdRS = stmt.executeQuery("SELECT DISTINCT countryId FROM country ORDER BY countryId");
            ArrayList<Integer> countryIdListFromCountry = new ArrayList<>();
            while (countryIdRS.next()) {
                countryIdListFromCountry.add(countryIdRS.getInt(1));
            }
            
            // Create list of countryId's that exist in Country table but are not used in City table
            for (int i = 0; i < countryIdListFromCity.size(); i++) {
                for (int j = 0; j < countryIdListFromCountry.size(); j++) {
                    if (countryIdListFromCity.get(i) == countryIdListFromCountry.get(j)) {
                        countryIdListFromCountry.remove(j);
                        j--;
                    }
                }
            }
            
            // Delete Country table entries by remaining countryId's
            if (countryIdListFromCountry.isEmpty()) {}
            else {
                for (int countryId : countryIdListFromCountry) {
                    stmt.executeUpdate("DELETE FROM country WHERE countryId = " + countryId);
                }
            }
       
        }  catch (SQLException e) {
            ResourceBundle rb = ResourceBundle.getBundle("Resources.DBManager", Locale.getDefault());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(rb.getString("error"));
            alert.setHeaderText(rb.getString("errorAddingApt"));
            alert.setContentText(rb.getString("errorRequiresDatabase"));
            alert.showAndWait();
        }
    }
}