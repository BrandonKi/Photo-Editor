package src.run;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
 
public class Main extends Application {

    private double pressedX, pressedY;
    private boolean panMode = false;
    private double currentScale = 1;

    private int imgWidth = 1920, imgHeight = 1200;
    private int canvasWidth = imgWidth > imgHeight ? imgWidth * 2 : imgHeight * 2, canvasHeight = canvasWidth;
    private int preScale = 4;

    public void start(final Stage stage) {
 
        final Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        canvas.setLayoutX(-canvasWidth/2 + imgWidth/preScale);
        canvas.setLayoutY(-canvasHeight/2 + imgHeight/preScale);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        initDraw(gc);
        gc.drawImage(new Image(getClass().getResourceAsStream("test1.jpg")), canvas.getWidth()/2 - imgWidth/preScale, canvas.getHeight()/2 - imgHeight/preScale, imgWidth/2, imgHeight/2);    
        Group root = new Group();
        VBox vBox = new VBox();
        makePannable(root, gc);
        vBox.getChildren().add(new Button("TEST"));
        makeZoomable(root, canvas);
        root.getChildren().addAll(canvas, vBox);
        stage.setTitle("Photo");
        //stage.setFullScreen(true);
        //stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setScene(new Scene(root, 800, 800));
        stage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
     
    private void initDraw(GraphicsContext gc){
        gc.setLineWidth(4);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
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
                canvas.setScaleX(canvas.getScaleX() - (canvas.getScaleX() - 0.2 > 0 ? 0.2 : 0));
                canvas.setScaleY(canvas.getScaleY() - (canvas.getScaleY() - 0.2 > 0 ? 0.2 : 0));
                currentScale = canvas.getScaleX();
            }
        });

        g.setOnScroll((ScrollEvent event) -> {
            System.out.println(currentScale);
            if((currentScale < 10 && event.getDeltaY() > 0) || (currentScale > 0.6 && event.getDeltaY() < 0)){
                canvas.setScaleX(canvas.getScaleX() + (event.getDeltaY() < 0 ? -0.05 : 0.05));
                canvas.setScaleY(canvas.getScaleX());
                currentScale = canvas.getScaleX();
            }
        });
    }
     
    private void makePannable(Group g, GraphicsContext gc){

        Canvas canvas = gc.getCanvas();

        g.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                panMode = true;
                g.setCursor(Cursor.HAND);
            }
        });

        g.addEventFilter(KeyEvent.KEY_RELEASED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                panMode = false;
                g.setCursor(Cursor.DEFAULT);

            }
        });

        canvas.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    g.setCursor(Cursor.CLOSED_HAND);
                    pressedX = event.getX();
                    pressedY = event.getY();
                }
                event.consume();
            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    g.setCursor(Cursor.CLOSED_HAND);
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