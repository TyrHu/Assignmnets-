import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


public class PathFinder extends Application {

    private ListGraph listGraph = new ListGraph();
    private Stage stage;
    private BorderPane root;
    private Pane mapPane;
    private ImageView mapView;
    private VBox top;
    private MenuBar menuBar;
    private Menu fileMenu;
    private MenuItem newMapItem;
    private MenuItem openItem;
    private MenuItem saveItem;
    private MenuItem saveImageItem;
    private MenuItem exitItem;
    private Button findPath;
    private Button showConnection;
    private Button newPlace;
    private Button newConnection;
    private Button changeConnection;
    private boolean changed;
    private LocationNode nodeOne;
    private LocationNode nodeTwo;


    @Override
    public void start(Stage stage) throws Exception {

        this.stage = stage;
        root = new BorderPane();
        top = new VBox();
        root.setTop(top);

        mapPane = new Pane();
        root.setCenter(mapPane);

        createMenu();
        createButtonPane();

        menuBar.setId("menu");
        fileMenu.setId("menuFile");
        newMapItem.setId("menuNewMap");
        openItem.setId("menuOpenFile");
        saveItem.setId("menuSaveFile");
        saveImageItem.setId("menuSaveImage");
        exitItem.setId("menuExit");
        findPath.setId("btnFindPath");
        showConnection.setId("btnShowConnection");
        newPlace.setId("btnNewPlace");
        changeConnection.setId("btnChangeConnection");
        newConnection.setId("btnNewConnection");
        mapPane.setId("outputArea");
        openItem.setId("menuOpenFile");

        Scene scene = new Scene(root, 620, 70);
        stage.setScene(scene);
        stage.setOnCloseRequest(new ExitHandler());
        stage.setTitle("PathFinder");
        stage.show();
    }

    private void createMenu() {

        menuBar = new MenuBar();

        fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        newMapItem = new MenuItem("New Map");
        fileMenu.getItems().add(newMapItem);
        newMapItem.setOnAction(new NewMapHandler());

        openItem = new MenuItem("Open");
        fileMenu.getItems().add(openItem);
        openItem.setOnAction(new OpenHandler());

        saveItem = new MenuItem("Save");
        fileMenu.getItems().add(saveItem);
        saveItem.setOnAction(new SaveHandler());

        saveImageItem = new MenuItem("Save Image");
        fileMenu.getItems().add(saveImageItem);
        saveImageItem.setOnAction(new SaveImageHandler());

        exitItem = new MenuItem("Exit");
        fileMenu.getItems().add(exitItem);
        exitItem.setOnAction(new ExitItemHandler());

        top.getChildren().add(menuBar);
    }

    private void createButtonPane() {

        FlowPane buttonPane = new FlowPane();
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(5);
        buttonPane.setAlignment(Pos.CENTER);

        findPath = new Button("Find Path");
        findPath.setOnAction(new FindPathHandler());
        findPath.setDisable(true);

        showConnection = new Button("Show Connection");
        showConnection.setOnAction(new ShowConnectionHandler());
        showConnection.setDisable(true);

        newPlace = new Button("New Place");
        newPlace.setOnAction(new NewPlaceHandler());
        newPlace.setDisable(true);

        newConnection = new Button("New Connection");
        newConnection.setOnAction(new NewConnectionHandler());
        newConnection.setDisable(true);

        changeConnection = new Button("Change Connection");
        changeConnection.setOnAction(new ChangeConnectionHandler());
        changeConnection.setDisable(true);

        buttonPane.getChildren().addAll(findPath, showConnection, newPlace, newConnection, changeConnection);
        top.getChildren().add(buttonPane);
    }

    private void showMap(String gif) {

        nodeOne = null;
        nodeTwo = null;

        listGraph = new ListGraph();

        mapPane.getChildren().clear();
        Image map = new Image(gif);
        mapView = new ImageView(map);
        mapPane.getChildren().add(mapView);
        stage.setHeight(839);
    }

    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            showMap("File:europa.gif");
            changed = true;
            enableButtons();
        }
    }

    class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (changed){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, continue anyway?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && !result.get().equals(ButtonType.OK))
                    return;
            }
            open();
            enableButtons();
        }
    }

    private void open(){

        HashMap<String, LocationNode> nodes = new HashMap<>();

        try (BufferedReader in = new BufferedReader(new FileReader("europa.graph"))) {
            String gif = in.readLine();
            showMap(gif);

            String line = in.readLine();
            String[] tokens = line.split(";");
            for (int i = 0; i < tokens.length; i += 3) {
                String name = tokens[i];
                double x = Double.parseDouble(tokens[i + 1]);
                double y = Double.parseDouble(tokens[i + 2]);
                LocationNode location = new LocationNode(name, x, y);
                nodes.put(name, location);
                makeNode(location);
            }

            while ((line = in.readLine()) != null) {
                tokens = line.split(";");
                var from = tokens[0];
                var to = tokens[1];
                String name = tokens[2];
                int time = Integer.parseInt(tokens[3]);
                if (listGraph.getEdgeBetween(nodes.get(from), nodes.get(to)) == null) {
                    listGraph.connect(nodes.get(from), nodes.get(to), name, time);
                    makeLine(nodes.get(from), nodes.get(to));
                }
            }

            changed = false;

        } catch (FileNotFoundException e) {
            ErrorAlert error = new ErrorAlert("Can't open file!");
            error.showAndWait();
        } catch (IOException e) {
            ErrorAlert error = new ErrorAlert("IO-error " + e.getMessage());
            error.showAndWait();
        }
    }

    private void enableButtons () {

        findPath.setDisable(false);
        showConnection.setDisable(false);
        newPlace.setDisable(false);
        newConnection.setDisable(false);
        changeConnection.setDisable(false);
    }

    public void makeLine (LocationNode from, LocationNode to) {

        Canvas canvas = new Canvas(620, 730);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(4);
        gc.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
        mapPane.getChildren().add(canvas);
        canvas.setDisable(true);
    }

    class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            save();
            changed = false;
        }
    }

    private void save(){

        try {
            FileWriter file = new FileWriter("europa.graph");
            PrintWriter out = new PrintWriter(file);

            out.println("file:europa.gif");

            StringBuilder str = new StringBuilder();
            for (var node : listGraph.getNodes()) {
                LocationNode locationNode = (LocationNode) node;
                str.append(locationNode.getName()).append(";").append(locationNode.getX()).append(";").append(locationNode.getY()).append(";");
            }

            if (str.length() > 0) {
                str.deleteCharAt(str.length() - 1);
                out.println(str);
            }

            for (var node : listGraph.getNodes()) {
                LocationNode locationNode = (LocationNode) node;
                for (var edge : listGraph.getEdgesFrom(node)) {
                    Edge e = (Edge) edge;
                    LocationNode destination = (LocationNode) e.getDestination();
                    str = new StringBuilder();
                    out.println(str.append(locationNode.getName()).append(";").append(destination.getName()).append(";").append(e.getName()).append(";").append(e.getWeight()));
                }
            }

            out.close();
            file.close();

        } catch(FileNotFoundException exception){
            ErrorAlert error = new ErrorAlert("Can't find file!");
            error.showAndWait();
        }catch(IOException exception){
            ErrorAlert error = new ErrorAlert("IO-error " + exception.getMessage());
            error.showAndWait();
        }
    }

    class ExitItemHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event){
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }

    class ExitHandler implements EventHandler<WindowEvent> {
        @Override public void handle(WindowEvent event){
            if (changed){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText(null);
                alert.setContentText("Unsaved changes, exit anyway?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() != ButtonType.OK)
                    event.consume();
            }
        }
    }

    class SaveImageHandler implements EventHandler<ActionEvent>{
        @Override
        public void handle(ActionEvent event){
            try {
                WritableImage image = root.snapshot(null, null);
                BufferedImage bimage = SwingFXUtils.fromFXImage(image,null);
                ImageIO.write(bimage, "png",new File("capture.png"));
            }catch (IOException e){
                ErrorAlert error = new ErrorAlert("Error saving image!");
                error.showAndWait();
            }
        }
    }

    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!checkNodesSelected()) return;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("The path from " + nodeOne.getName() + " to " + nodeTwo.getName());
            TextArea display = new TextArea();
            display.setEditable(false);
            alert.getDialogPane().setContent(display);

            if (!listGraph.pathExists(nodeOne, nodeTwo)) {
                display.setText("There is no path");
                alert.showAndWait();
                return;
            }

            StringBuilder str = new StringBuilder();
            int totalWeight = 0;

            for (var edge : listGraph.getPath(nodeOne, nodeTwo)) {
                Edge e = (Edge) edge;
                LocationNode destination = (LocationNode) e.getDestination();
                str.append("to ").append(destination.getName()).append(" by ").append(e.getName()).append(" takes ").append(e.getWeight()).append("\n");
                totalWeight += e.getWeight();
            }
            str.append("Total ").append(totalWeight);

            display.setText(str.toString());

            alert.showAndWait();
        }
    }

    class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!checkNodesSelected()) return;
            if (listGraph.getEdgeBetween(nodeOne, nodeTwo) == null) {
                ErrorAlert e = new ErrorAlert("These locations aren't connected!");
                e.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Connection from " + nodeOne.getName() + " to " + nodeTwo.getName());
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPrefWidth(250);
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField(listGraph.getEdgeBetween(nodeOne, nodeTwo).getName());
            TextField timeField = new TextField(Integer.toString(listGraph.getEdgeBetween(nodeOne, nodeTwo).getWeight()));
            nameField.setEditable(false);
            timeField.setEditable(false);

            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);

            alert.getDialogPane().setContent(grid);

            alert.showAndWait();
        }
    }

    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event){
            mapPane.setOnMouseClicked(new NodeClickHandler());
            mapPane.setCursor(Cursor.CROSSHAIR);
            newPlace.setDisable(true);
        }
    }

    class NodeClickHandler implements EventHandler<MouseEvent>{
        @Override
        public void handle(MouseEvent event){
            double x = event.getX();
            double y = event.getY();

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            FlowPane nameInput = new FlowPane();
            nameInput.setPrefWidth(280);
            nameInput.setHgap(10);
            Label nameLabel = new Label("Name of place: ");
            TextField nameField = new TextField();
            nameInput.getChildren().addAll(nameLabel, nameField);
            alert.getDialogPane().setContent(nameInput);

            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.isPresent() && answer.get() != ButtonType.OK) {
                newPlace.setDisable(false);
                mapPane.setOnMouseClicked(null);
                mapPane.setCursor(Cursor.DEFAULT);
                return;
            }

            String name = nameField.getText();
            LocationNode location = new LocationNode(name, x, y);
            makeNode(location);

            changed = true;
            mapPane.setOnMouseClicked(null);
            mapPane.setCursor(Cursor.DEFAULT);
            newPlace.setDisable(false);
        }
    }

    private void makeNode (LocationNode location) {

        location.setId(location.getName());
        Label name = new Label(location.getName());
        name.setLayoutX(location.getX());
        name.setLayoutY(location.getY()+5);
        name.setStyle("-fx-font-size:15; -fx-font-weight: bold");
        mapPane.getChildren().add(name);
        mapPane.getChildren().add(location);

        location.setOnMouseClicked(new NodeSelectionHandler());
        listGraph.add(location);
    }

    class NodeSelectionHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            LocationNode n = (LocationNode) event.getSource();
            if (nodeOne == null) {
                if (nodeTwo == n) {
                    n.unselected();
                    nodeTwo = null;
                } else {
                    nodeOne = n;
                    n.selected();
                }
            }

            else if (nodeTwo == null) {
                if (nodeOne == n) {
                    n.unselected();
                    nodeOne = null;
                } else {
                    nodeTwo = n;
                    n.selected();
                }
            }

            else {
                if (nodeOne == n) {
                    n.unselected();
                    nodeOne = nodeTwo;
                    nodeTwo = null;
                } else if (nodeTwo == n) {
                    n.unselected();
                    nodeTwo = null;
                }
            }
        }
    }

    class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!checkNodesSelected()) return;
            if (listGraph.getEdgeBetween(nodeOne, nodeTwo) != null) {
                ErrorAlert e = new ErrorAlert("These locations are already connected!");
                e.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Connection from " + nodeOne.getName() + " to " + nodeTwo.getName());
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPrefWidth(250);
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField();
            TextField timeField = new TextField();

            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);

            alert.getDialogPane().setContent(grid);

            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.isPresent() && answer.get() != ButtonType.OK) {
                return;
            }

            String name = nameField.getText();
            if (name.isEmpty()) {
                ErrorAlert e = new ErrorAlert("Name can't be empty!");
                e.showAndWait();
                return;
            }

            int time;
            try {
                time = Integer.parseInt(timeField.getText());
            } catch (NumberFormatException e) {
                ErrorAlert error = new ErrorAlert("Time must be a number!");
                error.showAndWait();
                return;
            }

            listGraph.connect(nodeOne, nodeTwo, name, time);
            makeLine(nodeOne, nodeTwo);
            changed = true;
        }
    }

    class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (!checkNodesSelected()) return;
            if (listGraph.getEdgeBetween(nodeOne, nodeTwo) == null) {
                ErrorAlert e = new ErrorAlert("These locations aren't connected!");
                e.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Connection from " + nodeOne.getName() + " to " + nodeTwo.getName());
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPrefWidth(250);
            grid.setHgap(10);
            grid.setVgap(10);

            TextField nameField = new TextField(listGraph.getEdgeBetween(nodeOne, nodeTwo).getName());
            TextField timeField = new TextField();
            nameField.setEditable(false);

            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);

            alert.getDialogPane().setContent(grid);
            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.isPresent() && answer.get() != ButtonType.OK) {
                return;
            }

            int time;
            try {
                time = Integer.parseInt(timeField.getText());
            } catch (NumberFormatException e) {
                ErrorAlert error = new ErrorAlert("Time must be a number!");
                error.showAndWait();
                return;
            }

            listGraph.setConnectionWeight(nodeOne, nodeTwo, time);
        }
    }

    public boolean checkNodesSelected () {

        if (nodeTwo == null) {
            ErrorAlert e = new ErrorAlert("Two locations must be selected!");
            e.showAndWait();
            return false;
        }
        return true;
    }
}
