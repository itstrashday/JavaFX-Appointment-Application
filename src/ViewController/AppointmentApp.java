package ViewController;

import Model.DBManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Daniel Richardson
 */

public class AppointmentApp extends Application {
    
    static Stage stage;
    
    @Override
    public void start(Stage stage) throws IOException {


        Parent root = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
        
        Scene scene = new Scene(root);
        this.stage = stage;
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
    
    static Stage getStage() {
        return stage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /******************
        *******************
        * Username = user *
        * Password = pass *
        *******************
        *******************/
      
        try {
            DBManager.makeConnection(); 

            launch(args);
            
        } catch (Exception ex) {
           Logger.getLogger(AppointmentApp.class.getName()).log(Level.SEVERE, null, ex);
           System.out.println("Error with connecting to database: " + ex.getMessage());
        }
       
        
    }
    
}
