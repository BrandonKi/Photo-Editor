package src.run;

import javafx.application.Application;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import position.Delta;

import java.awt.Toolkit;

import java.awt.Dimension;

public class Main extends Application {

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private BufferedImage bImg;
    private final double SCREEN_HEIGHT = screenSize.getHeight(), SCREEN_WIDTH = screenSize.getWidth();
    private ImageView cursorNode;
    private double cImgWidth, cImgHeight;
    private Image cImg = new Image("src\\resource\\scalable_circle.png");

    private double pressedX, pressedY;
    private boolean isPressed = false;
    private boolean panMode = false, tempPanMode = false;
    private boolean drawMode = false;

    private double currentScale = 1;
    private double imgWidth = 1920, imgHeight = 1200;
    private double canvasWidth = imgWidth > imgHeight ? imgWidth * 2 : imgHeight * 2, canvasHeight = canvasWidth;
    //private int preScale = 1; never again

    public void start(final Stage stage) {
 
        Group root = new Group();
        final Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        final HBox taskBar = new HBox();
        final VBox toolBar = new VBox();

        initCursor(root);
        initCanvas(gc);
        drawImage("test1.jpg", gc);
        makePannable(root, gc);
        makeZoomable(root, canvas);
        makeDrawable(root, canvas);
        initTaskBar(taskBar, gc);
        initToolBar(toolBar, gc);
        initCanvasDragListener(canvas);
        initCanvasPressedListener(canvas);
        initSaving(root, canvas);
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

    private void initCursor(Group g){
        cursorNode = new ImageView(cImg);
        cursorNode.setMouseTransparent(true);
        cursorNode.toFront();
        cursorNode.setFocusTraversable(false);
        cursorNode.setLayoutX(0);
        cursorNode.setLayoutY(0);
        cImgWidth = cImg.getWidth();
        cImgHeight = cImg.getHeight();
    }
     
    private void initTaskBar(HBox taskBar, GraphicsContext gc){

    }



    private void initToolBar(VBox toolBar, GraphicsContext gc){

        toolBar.setOnMouseEntered(e -> {
            if(drawMode)
                hideDrawCursor();
        });

        toolBar.setOnMouseExited(e -> {
            if(drawMode)
                showDrawCursor();
            else if(allModesFalse()){
                hideDrawCursor();
                gc.getCanvas().setCursor(Cursor.DEFAULT);
            }

        });

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
        styleButton(panModeButton, "src\\resource\\mouse_hand_open.png", 3, 0, 22, 25);
        panModeButton.setOnMousePressed(e -> {
            if(panMode){
                setAllModesFalse();
            }
            else{
                setAllModesFalse();
                panMode = true;
            }
            gc.getCanvas().setCursor(panMode ? Cursor.HAND : Cursor.DEFAULT);
        });

        Button drawModeButton = new Button();
        styleButton(drawModeButton, "src\\resource\\brush.png", 0, 0, 25, 25);
        drawModeButton.setOnMousePressed(e -> {
            if(drawMode){
                setAllModesFalse();
                gc.getCanvas().setCursor(Cursor.DEFAULT);
            }
            else{
                setAllModesFalse();
                drawMode = true;
                gc.getCanvas().setCursor(Cursor.CROSSHAIR);
            }
        });

        toolBar.getChildren().addAll(handle, panModeButton, drawModeButton);
        
    }

    private void setCursor(Image cursor){
        cursorNode = new ImageView(cursor);
    }

    private void hideDrawCursor(){
        cursorNode.setVisible(false);
    }

    private void showDrawCursor(){
        cursorNode.setVisible(true);
    }

    private void resizeDrawCursor(boolean increase){  // true = increase false = decrease
        if (increase){
            cImg = new Image("src\\resource\\scalable_circle.png", ++cImgWidth, ++cImgHeight, false, false);
            // cursorNode.setTranslateX(cursorNode.getLayoutX() + cursorNode.getTranslateX() - cImgWidth/2);
            // cursorNode.setTranslateY(cursorNode.getLayoutY() + cursorNode.getTranslateY() - cImgHeight/2);
        }
        cursorNode.setImage(cImg);
    }

    private void rescaleDrawCursor(double scale){
        cursorNode.setScaleX(scale);
        cursorNode.setScaleY(scale);
    }

    private void styleButton(Button b, String imgPath, int x, int y, int width, int height){
        Image panIconImage = new Image(imgPath, x + width, y + height, false, false);
        PixelReader reader = panIconImage.getPixelReader();
        panIconImage = new WritableImage(reader, x, y, width, height);
        ImageView panIcon = new ImageView(panIconImage);
        b.setGraphic(panIcon);
        b.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), new CornerRadii(10), Insets.EMPTY)));
        b.setStyle("-fx-border-color: #ffffff; -fx-border-width: 1px; -fx-border-radius: 10 10 10 10");
        b.setMaxSize(x + width + 5, y + height + 5);
    }

    private void setAllModesFalse(){
        panMode = false;
        drawMode = false;
    }

    private boolean allModesFalse(){
        if(panMode)
            return false;
        if(drawMode)
            return drawMode;
        return false;
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
        Image img = new Image(getClass().getResourceAsStream(path));
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        bImg = SwingFXUtils.fromFXImage(img, null);
        gc.drawImage(img, gc.getCanvas().getWidth()/2 - imgWidth/2 + 40, gc.getCanvas().getHeight()/2 - imgHeight/2 + 80, imgWidth, imgHeight);
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
                rescaleDrawCursor(currentScale);
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
                else{
                    if(drawMode)
                        hideDrawCursor();
                    panMode = true;
                }
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
                tempPanMode = true;
                if(!isPressed){
                    canvas.setCursor(Cursor.HAND);
                    if(isDrawingMode())
                        hideDrawCursor();
                }
            }
        });

        g.addEventFilter(KeyEvent.KEY_RELEASED, event->{
            if (event.getCode() == KeyCode.SPACE) {
                tempPanMode = false;
                canvas.setCursor(Cursor.DEFAULT);
                if(isDrawingMode()){
                    showDrawCursor();
                    canvas.setCursor(Cursor.CROSSHAIR);
                    
                }
            }
        });


        canvas.setOnMouseReleased(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
                if(isPanningMode()){
                    isPressed = false;
                    canvas.setCursor(Cursor.HAND);
                }
                event.consume();
            }
        });
    }

    private void makeDrawable(Group root, Canvas canvas){

        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                if(isDrawingMode()){
                    showDrawCursor();
                    resizeDrawCursor(true);
                }
            }
        });

        canvas.setOnMouseMoved(e -> {
            if(drawMode && !tempPanMode){
                showDrawCursor();
                cursorNode.setTranslateX(e.getSceneX() - cImgWidth/2);
                cursorNode.setTranslateY(e.getSceneY() - cImgHeight/2);
            }else{
                hideDrawCursor();
            }
        });
    }

    private void saveImage(Canvas canvas){
        WritableImage bounds = new WritableImage((int)imgWidth, (int)imgHeight);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setViewport(new Rectangle2D(0, 0, imgWidth, imgHeight));
        WritableImage snapshot = canvas.snapshot(sp, bounds);
        try{
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(System.getProperty("user.dir") + "\\test.png"));
        }catch(Exception e){ 
            e.printStackTrace();
        }
    }

    private void initCanvasDragListener(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.setOnMouseDragged(e -> {
        if(isPanningMode()){
            canvas.setTranslateX(canvas.getTranslateX() + e.getX() - pressedX);
            canvas.setTranslateY(canvas.getTranslateY() + e.getY() - pressedY);
        }
        else if(drawMode && !tempPanMode){
            showDrawCursor();
            cursorNode.setTranslateX(e.getSceneX() - cImgWidth/2);
            cursorNode.setTranslateY(e.getSceneY() - cImgHeight/2);
            gc.fillOval(e.getX() - cImgWidth/2, e.getY() - cImgHeight/2, cImgWidth, cImgHeight);
        }else{
            hideDrawCursor();
        }
            e.consume();
        });
    }

    private void initCanvasPressedListener(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(e -> {
            if(isPanningMode()){
                isPressed = true;
                canvas.setCursor(Cursor.CLOSED_HAND);
                pressedX = e.getX();
                pressedY = e.getY();
            }
            if(isDrawingMode() && !tempPanMode){
                showDrawCursor();
                cursorNode.setTranslateX(e.getSceneX() - cImgWidth/2);
                cursorNode.setTranslateY(e.getSceneY() - cImgHeight/2);
                gc.fillOval(e.getX() - cImgWidth/2, e.getY() - cImgHeight/2, cImgWidth, cImgHeight);
            }
            e.consume();
        });
    }

    private void initSaving(Group g, Canvas canvas){
        KeyCombination save = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(save.match(event)){
                saveImage(canvas);
            }
        });
    }

    private boolean isPanningMode(){
        return panMode || tempPanMode;
    }

    private boolean isDrawingMode(){
        return drawMode;
    }
}