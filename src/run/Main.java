package src.run;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import position.Delta;

import java.awt.Toolkit;

import java.awt.Dimension;

public class Main extends Application {

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final double SCREEN_HEIGHT = screenSize.getHeight(), SCREEN_WIDTH = screenSize.getWidth();
    Node cursorNode;

    private boolean customCursor = false;

    private double pressedX, pressedY;
    private boolean isPressed = false;
    private boolean panMode = false;
    private boolean drawMode = false;

    private double currentScale = 1;
    private int imgWidth = 1920, imgHeight = 1200;
    private int canvasWidth = imgWidth > imgHeight ? imgWidth * 2 : imgHeight * 2, canvasHeight = canvasWidth;
    private int preScale = 4;

    public void start(final Stage stage) {
 
        Group root = new Group();
        final Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        final HBox taskBar = new HBox();
        final VBox toolBar = new VBox();

        initCursor(gc);
        initCanvas(gc);
        drawImage("test1.jpg", gc);
        makePannable(root, gc);
        makeZoomable(root, canvas);
        initTaskBar(taskBar, gc);
        initToolBar(toolBar, gc);
        root.getChildren().addAll(canvas, toolBar, taskBar, cursorNode);
        stage.setTitle("Photo");
        //stage.setFullScreen(true);
        //stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        Scene s = new Scene(root, 800, 800);
        s.setFill(Color.rgb(60, 60, 60));
        stage.setScene(s);
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }

    private void initCursor(GraphicsContext gc){
        Image cImg = new Image("src\\resource\\scalable_circle.png");
        cursorNode = new ImageView(cImg);
        gc.getCanvas().setOnMouseMoved(e -> {
            cursorNode.setVisible(true);
            System.out.println(cursorNode.getTranslateX() + " " + cursorNode.getTranslateY());
            cursorNode.setTranslateX(e.getX() );//- cImg.getWidth()/2);
            cursorNode.setTranslateY(e.getY() );//- cImg.getHeight()/2);
        });
    }
     
    private void initTaskBar(HBox taskBar, GraphicsContext gc){

    }



    private void initToolBar(VBox toolBar, GraphicsContext gc){
        toolBar.setTranslateY(toolBar.getLayoutY() + 70);
        toolBar.setStyle("-fx-border-color: #000000; -fx-border-radius: 10 10 10 10;");
        toolBar.setSpacing(3);
        toolBar.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), new CornerRadii(10), Insets.EMPTY)));
        Pane handle = new Pane();
        Rectangle base = new Rectangle(0, 0, 40, 25);
        base.setFill(Color.rgb(40, 40, 40));
        base.setArcWidth(10);
        base.setArcHeight(10);
        Color lineColor = Color.rgb(255, 255, 255);
        Line l1 = new Line(7, 5, 33, 5);
        l1.setStroke(lineColor);
        Line l2 = new Line(7, 12, 33, 12);
        l2.setStroke(lineColor);
        Line l3 = new Line(7, 19, 33, 19);
        l3.setStroke(lineColor);
        handle.getChildren().addAll(base, l1, l2, l3);
        makeDraggableByChild(handle, toolBar);

        Button panModeButton = new Button();
        styleButton(panModeButton, "src\\resource\\mouse_hand_open.png");
        panModeButton.setOnMousePressed(e -> {
            if(panMode)
                setAllModesFalse();
            else
                panMode = !panMode;
            if(panMode)
                gc.getCanvas().setCursor(Cursor.HAND);
            else if(!panMode)   
                gc.getCanvas().setCursor(Cursor.DEFAULT);
        });

        Button drawModeButton = new Button();
        styleButton(drawModeButton, "src\\resource\\mouse_hand_open.png");
        drawModeButton.setOnMouseClicked(e -> {
            if(drawMode)
                setAllModesFalse();
            else
                drawMode = !drawMode;
        });

        toolBar.getChildren().addAll(handle, panModeButton, drawModeButton);
        
    }

    private void setCursor(Image cursor){
        cursorNode = new ImageView(cursor);
    }

    private void styleButton(Button b, String imgPath){
        Image panIconImage = new Image(imgPath, 25, 25, false, false);
        PixelReader reader = panIconImage.getPixelReader();
        panIconImage = new WritableImage(reader, 3, 0, 22, 25);
        ImageView panIcon = new ImageView(panIconImage);
        b.setGraphic(panIcon);
        b.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), new CornerRadii(10), Insets.EMPTY)));
        b.setStyle("-fx-border-color: #ffffff; -fx-border-width: 1px; -fx-border-radius: 10 10 10 10");
        b.setMaxSize(30, 30);
    }

    private void setAllModesFalse(){
        panMode = false;
        drawMode = false;
    }

    private void makeDraggable(Node p) {
        Node node = (Node)p;
        node.setOnMouseEntered(e -> {
            node.getScene().setCursor(Cursor.HAND);
        });

        node.setOnMouseExited(e -> {
            node.getScene().setCursor(Cursor.DEFAULT);
        });

        node.setOnMousePressed(e -> {
            node.getScene().setCursor(Cursor.CLOSED_HAND);
        });

        node.setOnMouseReleased(e -> {
            node.getScene().setCursor(Cursor.HAND);
        });

        node.setOnMouseDragged(e -> {
            node.setTranslateX(node.getLayoutX() + e.getX());
            node.setTranslateY(node.getLayoutY() + e.getY());
        });
    }

    private void makeDraggableByChild(Node child, Node parent) {
        Delta pos = new Delta();
        child.setOnMouseEntered(e -> {
            parent.getScene().setCursor(Cursor.HAND);
        });

        child.setOnMouseExited(e -> {
            parent.getScene().setCursor(Cursor.DEFAULT);
        });

        child.setOnMousePressed(e -> {
            parent.getScene().setCursor(Cursor.CLOSED_HAND);
            pos.x = e.getSceneX() - (parent.getTranslateX());
            pos.y = e.getSceneY() - (parent.getTranslateY());
        });

        child.setOnMouseReleased(e -> {
            parent.getScene().setCursor(Cursor.HAND);
        });

        child.setOnMouseDragged(e -> {
            parent.setTranslateX(e.getSceneX() - pos.x);
            parent.setTranslateY(e.getSceneY() - pos.y);
        });
    }

    private void initCanvas(GraphicsContext gc){
        gc.getCanvas().setLayoutX(-canvasWidth/2 + SCREEN_WIDTH/2);
        gc.getCanvas().setLayoutY(-canvasHeight/2 + SCREEN_HEIGHT/2);
        gc.setLineWidth(4);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    private void drawImage(String path, GraphicsContext gc){
        gc.drawImage(new Image(getClass().getResourceAsStream(path)), gc.getCanvas().getWidth()/2 - imgWidth/preScale, gc.getCanvas().getHeight()/2 - imgHeight/preScale, imgWidth/2, imgHeight/2);
    }

    private void makeZoomable(Group g, Canvas canvas){
        KeyCombination zoomIn = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (zoomIn.match(event)) {
                canvas.setScaleX(canvas.getScaleX() + 0.2);
                canvas.setScaleY(canvas.getScaleY() + 0.2);
                currentScale = canvas.getScaleX();
            }
        });

        KeyCombination zoomOut = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (zoomOut.match(event)) {
                canvas.setScaleX(canvas.getScaleX() - (currentScale - 0.2 > 0.6 ? 0.2 : 0));
                canvas.setScaleY(canvas.getScaleY() - (currentScale - 0.2 > 0.6 ? 0.2 : 0));
                currentScale = canvas.getScaleX();
            }
        });

        g.setOnScroll((ScrollEvent event) -> {
            if((currentScale < 10 && event.getDeltaY() > 0) || (currentScale > 0.6 && event.getDeltaY() < 0)){
                canvas.setScaleX(canvas.getScaleX() + (event.getDeltaY() < 0 ? -0.05 : 0.05));
                canvas.setScaleY(canvas.getScaleX());
                currentScale = canvas.getScaleX();
                // canvas.setTranslateX(canvas.getTranslateX() + (event.getDeltaY() < 0 ? -event.getX()/currentScale : event.getX()/currentScale));
                // canvas.setTranslateY(canvas.getTranslateY() + (event.getDeltaY() < 0 ? -event.getY()/currentScale : event.getY()/currentScale));
            }
        });
    }
     
    private void makePannable(Group g, GraphicsContext gc){

        Canvas canvas = gc.getCanvas();

        KeyCombination pan = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if(pan.match(event)){
                if(panMode)
                    setAllModesFalse();
                else
                    panMode = !panMode;
                canvas.setCursor(panMode ? Cursor.HAND : Cursor.DEFAULT);
            }
        });

        KeyCombination fitScreen = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if(fitScreen.match(event)){
                canvas.setTranslateX(0);
                canvas.setTranslateY(0);
                canvas.setScaleX(1);
                canvas.setScaleY(1);
            }
        });

        g.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                panMode = true;
                if(!isPressed)
                    canvas.setCursor(Cursor.HAND);
            }
        });

        g.addEventFilter(KeyEvent.KEY_RELEASED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                panMode = false;
                canvas.setCursor(Cursor.DEFAULT);

            }
        });

        canvas.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    isPressed = true;
                    canvas.setCursor(Cursor.CLOSED_HAND);
                    pressedX = event.getX();
                    pressedY = event.getY();
                }
                event.consume();
            }
        });

        canvas.setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    canvas.setCursor(Cursor.HAND);
                }
                event.consume();
            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    canvas.setTranslateX(canvas.getTranslateX() + event.getX() - pressedX);
                    canvas.setTranslateY(canvas.getTranslateY() + event.getY() - pressedY);
                }
                    event.consume();
            }
        });
    }

    private boolean isPanningMode(){
        return panMode;
    }
}