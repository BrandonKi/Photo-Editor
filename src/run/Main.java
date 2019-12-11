package src.run;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
 
/**
 * @web http://java-buddy.blogspot.com/
 */
public class Main extends Application {

    private double pressedX, pressedY;

    public void start(final Stage primaryStage) {
 
        final Canvas canvas = new Canvas(600, 600);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        initDraw(gc);
        gc.drawImage(new Image(getClass().getResourceAsStream("test.png")), 200, 200, 120, 120);
        makePannable(gc);
        
        Group root = new Group();
        makeZoomable(root, gc);
        // VBox vBox = new VBox();
        // vBox.getChildren().add(new Button("TEST"));

        root.getChildren().addAll(canvas);
        primaryStage.setTitle("Photo");
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
     
    private void initDraw(GraphicsContext gc){
        
    }
    private void makeZoomable(Group g, GraphicsContext gc){
        g.setOnKeyPressed(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                System.out.println(event.getCode());
                if (event.getCode() == KeyCode.SPACE) {
                    gc.getCanvas().setScaleX(2);
                    gc.getCanvas().setScaleY(2);
                    gc.drawImage(new Image(getClass().getResourceAsStream("test.png")), 200, 200, 120, 120);
                }
            }
        });
    }
     
    private void makePannable(GraphicsContext gc){
        Canvas canvas = gc.getCanvas();
        canvas.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                pressedX = event.getX();
                pressedY = event.getY();
            }
        });

        canvas.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                canvas.setTranslateX(canvas.getTranslateX() + event.getX() - pressedX);
                canvas.setTranslateY(canvas.getTranslateY() + event.getY() - pressedY);

                event.consume();
            }
        });
    }
}