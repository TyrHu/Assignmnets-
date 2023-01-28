import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class LocationNode extends Circle {
    private final String name;
    private final double x;
    private final double y;

    public LocationNode(String name, double x, double y) {
        super(x,y, 10);
        this.name = name;
        this.x = x;
        this.y = y;
        unselected();
        this.setCursor(Cursor.HAND);
    }

    public String getName() {
        return name;
    }

    public double getX () {
        return x;
    }

    public double getY() {
        return y;
    }

    public void selected () {
        setFill(Color.RED);
    }

    public void unselected () {
        setFill(Color.BLUE);
    }

    @Override
    public String toString() {
        return String.format("Location: %s (%.1f %.1f)", name, x, y);
    }

}
