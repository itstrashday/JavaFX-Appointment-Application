package Calendar;

import java.time.LocalDate;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;


public class AnchorPaneNode extends AnchorPane{
    private LocalDate date;

    public AnchorPaneNode (Node... children) {
        super(children);
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }
    
}
