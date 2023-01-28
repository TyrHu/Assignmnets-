import javafx.scene.control.Alert;

public class ErrorAlert extends Alert {
    public ErrorAlert(String msg) {
        super(AlertType.ERROR, msg);
        setTitle("Error!");
        setHeaderText(null);
    }

}
