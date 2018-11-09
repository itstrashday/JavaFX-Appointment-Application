package Calendar;

import Model.Appointment;
import Model.AppointmentList;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;


public class MonthlyView {
    private Text monthTitle;
    private YearMonth currentYearMonth;
    private ArrayList<AnchorPaneNode> calendarDayPanes = new ArrayList<>(35);
    private VBox monthlyCalendarView;

    public MonthlyView(YearMonth yearMonth) {
        currentYearMonth = yearMonth;
        GridPane calendar = new GridPane();
        calendar.setPrefSize(600,400);
        calendar.setGridLinesVisible(true);

        // Use AnchorPaneNode to create individual panes for each day
        // Create 5 rows
        for (int i=0; i<5; i++) {
            // Create 7 columns
            for (int j=0; j<7; j++) {
                AnchorPaneNode ap = new AnchorPaneNode();
                ap.setPrefSize(200,200);
                calendar.add(ap, j, i);
                calendarDayPanes.add(ap);
            }
        }

        // Create Array for days of the week
        Text[] daysOfWeek;
        ResourceBundle rb = ResourceBundle.getBundle("Resources.Calendar", Locale.getDefault());
        daysOfWeek = new Text[]{
                new Text(rb.getString("monday")), 
                new Text(rb.getString("tuesday")), 
                new Text(rb.getString("wednesday")),
                new Text(rb.getString("thursday")), 
                new Text(rb.getString("friday")), 
                new Text(rb.getString("saturday")),
                new Text(rb.getString("sunday"))};
        
        // Create label panes for days of the week
        GridPane dayLabels = new GridPane();
        dayLabels.setPrefWidth(600);
        int col = 0;
        for (Text day : daysOfWeek) {
            AnchorPane ap = new AnchorPane();
            ap.setPrefSize(200,10);
            ap.setBottomAnchor(day, 5.0);
            day.setWrappingWidth(100);
            day.setTextAlignment(TextAlignment.CENTER);
            ap.getChildren().add(day);
            dayLabels.add(ap, col++, 0);
        }

//Lambda Expressions - Used to assign an action(traverse months via calendar view) to a button in place of
//an ActionEvent method
        monthTitle = new Text();
        Button backOneMonth = new Button("<");
        backOneMonth.setOnAction(event -> backOneMonth());
        Button forwardOneMonth = new Button(">");
        forwardOneMonth.setOnAction(event -> forwardOneMonth());
        
        // Create HBox to hold title and buttons
        HBox title = new HBox(backOneMonth, monthTitle, forwardOneMonth);
        title.setAlignment(Pos.BASELINE_CENTER);

        // Populate calendar with day numbers
        populateCalendar(yearMonth);

        monthlyCalendarView = new VBox(title, dayLabels, calendar);
    }

    public void populateCalendar(YearMonth yearMonth) {
        LocalDate calendarDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
        // Move backwards one day at a time until we find a Monday
        while (!calendarDate.getDayOfWeek().toString().equals("MONDAY")) {
            calendarDate = calendarDate.minusDays(1);
        }

        // Set the title based on current month
        String localizedMonth = new DateFormatSymbols().getMonths()[yearMonth.getMonthValue()-1];
        String properMonth = localizedMonth.substring(0,1).toUpperCase() + localizedMonth.substring(1);
        monthTitle.setText("  " + properMonth + " " + String.valueOf(yearMonth.getYear()) + "  ");

        // Fill in day numbers and number of appointments
        for (AnchorPaneNode ap : calendarDayPanes) {
            if (ap.getChildren().size() != 0) {
                ap.getChildren().remove(0,ap.getChildren().size());
            }
            
            // Add day of the month to the corner
            Text date = new Text(String.valueOf(calendarDate.getDayOfMonth()));
            ap.setDate(calendarDate);
            ap.setTopAnchor(date, 5.0);
            ap.setLeftAnchor(date, 5.0);
            ap.getChildren().add(date);
            
            // Calculate number of appointments on that day
            ObservableList<Appointment> appointmentList = AppointmentList.getAppointmentList();
            int calendarDateYear = calendarDate.getYear();
            int calendarDateMonth = calendarDate.getMonthValue();
            int calendarDateDay = calendarDate.getDayOfMonth();
            int appointmentCount = 0;
            for (Appointment appointment : appointmentList) {
                Date appointmentDate = appointment.getStartDate();
                Calendar calendar  = Calendar.getInstance(TimeZone.getDefault());
                calendar.setTime(appointmentDate);
                int appointmentYear = calendar.get(Calendar.YEAR);
                int appointmentMonth = calendar.get(Calendar.MONTH) + 1;
                int appointmentDay = calendar.get(Calendar.DAY_OF_MONTH);
                if (calendarDateYear == appointmentYear && calendarDateMonth == appointmentMonth && calendarDateDay == appointmentDay) {
                    appointmentCount++;
                }
            }
            
            // Add appointments to count
            if (appointmentCount != 0) {
                Text appointmentsForDay = new Text(String.valueOf(appointmentCount));
                appointmentsForDay.setFont(Font.font(30));
                appointmentsForDay.setFill(Color.BLUE);

                ap.getChildren().add(appointmentsForDay);
                ap.setTopAnchor(appointmentsForDay, 20.0);
                ap.setLeftAnchor(appointmentsForDay, 40.0);
            }
            // Increment date
            calendarDate = calendarDate.plusDays(1);
        }
    }

    // Back one month
    private void backOneMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        populateCalendar(currentYearMonth);
    }

    // Forward one month
    private void forwardOneMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        populateCalendar(currentYearMonth);
    }

    public VBox getView() {
        return monthlyCalendarView;
    }

    public YearMonth getCurrentYearMonth() {
        return currentYearMonth;
    }
    
}
