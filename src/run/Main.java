package src.run;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.control.ColorPicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Stage;
import position.Delta;

public class Main extends Application {

    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private BufferedImage bImg;
    private final double SCREEN_HEIGHT = screenSize.getHeight(), SCREEN_WIDTH = screenSize.getWidth();
    private ImageView cursorNode;
    private double cImgWidth, cImgHeight;
    private Image cImg = new Image("src\\resource\\scalable_circle.png");

    private double pressedX, pressedY;
    private boolean isPressed = false;
    private boolean stampSelection;
    private WritableImage currentStampSelection;
    private double stampSourceX, stampSourceY;
    private boolean panMode = false, tempPanMode = false;
    private boolean drawMode = false;
    private boolean eyedropperMode = false;
    private boolean stampMode = false;

    private ColorPicker colorPicker = new ColorPicker();
    private Button drawModeButton, panModeButton, eyedropperModeButton, stampModeButton;
    private PixelReader tempPR;

    private double currentScale = 1;
    private double imgWidth, imgHeight;
    private double canvasWidth, canvasHeight;
    //private int preScale = 4; never again

    public void start(final Stage stage) {

        String imgName = "test2.jpg";
 
        Group root = new Group();
        
        final HBox taskBar = new HBox();
        final VBox toolBar = new VBox();

        initImage(imgName);
        initCursor(root);

        final Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        initCanvas(gc);
        drawImage(imgName, gc);
        makePannable(root, gc);
        makeZoomable(root, canvas);
        makeDrawable(root, canvas);
        initStampMode(root);
        initTaskBar(taskBar, gc);
        initToolBar(toolBar, gc);
        initCanvasDragListener(canvas);
        initCanvasPressedListener(canvas);
        initCanvasReleasedListener(canvas);
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

    private void initImage(String path){
        Image img = new Image(getClass().getResourceAsStream(path));
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        canvasWidth = imgWidth > imgHeight ? imgWidth * 2 : imgHeight * 2; 
        canvasHeight = canvasWidth;
    }

    private void initCursor(Group g){
        cursorNode = new ImageView(cImg);
        cursorNode.setMouseTransparent(true);
        cursorNode.toFront();
        cursorNode.setFocusTraversable(false);
        cursorNode.setVisible(false);
        cursorNode.setLayoutX(0);
        cursorNode.setLayoutY(0);
        cImgWidth = cImg.getWidth();
        cImgHeight = cImg.getHeight();
    }
     
    private void initTaskBar(HBox taskBar, GraphicsContext gc){
        taskBar.setTranslateX(taskBar.getLayoutX() + 70);
        taskBar.setStyle("-fx-border-color: #000000; -fx-border-radius: 10 10 10 10;");
        taskBar.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), new CornerRadii(10), Insets.EMPTY)));
        colorPicker.getStyleClass().add("button");
        colorPicker.setStyle("-fx-background-color: #669999; -fx-background-radius: 0 15 15 0; ");
        styleColorPicker(colorPicker, 0, 0, 25, 25);
        taskBar.getChildren().addAll(colorPicker);
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

        panModeButton = new Button();
        styleButton(panModeButton, "src\\resource\\mouse_hand_open.png", 3, 0, 22, 25);
        panModeButton.setOnMousePressed(e -> {
            if(panMode){
                setAllModesFalse();
            }
            else{
                setAllModesFalse();
                panMode = true;
            }
            eventTriggered();
            gc.getCanvas().setCursor(panMode ? Cursor.HAND : Cursor.DEFAULT);
        });

        drawModeButton = new Button();
        styleButton(drawModeButton, "src\\resource\\brush.png", 0, 0, 22, 25);
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
            eventTriggered();
        });

        eyedropperModeButton = new Button();
        styleButton(eyedropperModeButton, "src\\resource\\eyedropper.png", 0, 0, 22, 25);
        eyedropperModeButton.setOnMousePressed(e -> {
            if(eyedropperMode){
                setAllModesFalse();
                gc.getCanvas().setCursor(Cursor.DEFAULT);
            }
            else{
                setAllModesFalse();
                eyedropperMode = true;
                gc.getCanvas().setCursor(Cursor.CROSSHAIR);
            }
            eventTriggered();
        });

        stampModeButton = new Button();
        styleButton(stampModeButton, "src\\resource\\brush.png", 0, 0, 22, 25);
        stampModeButton.setOnMousePressed(e -> {
            if(stampMode){
                setAllModesFalse();
                gc.getCanvas().setCursor(Cursor.DEFAULT);
            }
            else{
                setAllModesFalse();
                stampMode = true;
                gc.getCanvas().setCursor(Cursor.CROSSHAIR);
            }
            eventTriggered();
        });

        toolBar.getChildren().addAll(handle, panModeButton, drawModeButton, eyedropperModeButton, stampModeButton);
        
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
        if(!increase && cImgWidth <= 3){
            cImgWidth = 1;
            cImgHeight = 1;
        }
        if (increase && cImgWidth != 1)
            cImg = new Image("src\\resource\\scalable_circle.png", cImgWidth+=3, cImgHeight+=3, false, false);
        else if(cImgWidth != 1)    
            cImg = new Image("src\\resource\\scalable_circle.png", cImgWidth-=3, cImgHeight-=3, false, false);
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

    private void styleColorPicker(ColorPicker b, int x, int y, int width, int height){
        b.setMinSize(25, 25);
        b.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        b.setStyle("-fx-border-color: #ffffff; -fx-border-width: 1px;");
        b.setMaxSize(x + width + 5, y + height + 5);
    }

    private void setAllModesFalse(){
        panMode = false;
        drawMode = false;
        eyedropperMode = false;
        stampMode = false;
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
        gc.setStroke(colorPicker.getValue());
        gc.strokeRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    private void drawImage(String path, GraphicsContext gc){
        Image img = new Image(getClass().getResourceAsStream(path));
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
        bImg = SwingFXUtils.fromFXImage(img, null);
        gc.drawImage(img, gc.getCanvas().getWidth()/2 - imgWidth/2, gc.getCanvas().getHeight()/2 - imgHeight/2);
    }

    private void makeZoomable(Group g, Canvas canvas){
        KeyCombination zoomIn = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (zoomIn.match(event)) {
                canvas.setScaleX(canvas.getScaleX() + 0.4);
                canvas.setScaleY(canvas.getScaleY() + 0.4);
                currentScale = canvas.getScaleX();
                rescaleDrawCursor(currentScale);
            }
        });

        KeyCombination zoomOut = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        g.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (zoomOut.match(event)) {
                canvas.setScaleX(canvas.getScaleX() - (currentScale - 0.4 > 0.6 ? 0.4 : 0));
                canvas.setScaleY(canvas.getScaleY() - (currentScale - 0.4 > 0.6 ? 0.4 : 0));
                currentScale = canvas.getScaleX();
                rescaleDrawCursor(currentScale);
            }
        });

        g.setOnScroll((ScrollEvent event) -> {
            if((currentScale < 10 && event.getDeltaY() > 0) || (currentScale > 0.6 && event.getDeltaY() < 0)){
                canvas.setScaleX(canvas.getScaleX() + (event.getDeltaY() < 0 ? -0.1 : 0.1));
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
                    setAllModesFalse();
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
                eventTriggered();
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
                eventTriggered();
                canvas.setCursor(Cursor.DEFAULT);
                if(isDrawingMode()){
                    showDrawCursor();
                    canvas.setCursor(Cursor.CROSSHAIR);
                }
                else if (isEyedropperMode())
                    canvas.setCursor(Cursor.CROSSHAIR);
            }
        });


        
    }

    private void makeDrawable(Group root, Canvas canvas){

        colorPicker.setValue(Color.BLACK);
        colorPicker.setOnAction(e -> {
            canvas.getGraphicsContext2D().setFill(colorPicker.getValue());               
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.CLOSE_BRACKET) {
                if(isDrawingMode()){
                    showDrawCursor();
                    resizeDrawCursor(true);
                    cursorNode.setTranslateX(cursorNode.getTranslateX() - 1.5);
                    cursorNode.setTranslateY(cursorNode.getTranslateY() - 1.5);
                }
            }
        });

        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.OPEN_BRACKET) {
                if(isDrawingMode()){
                    showDrawCursor();
                    resizeDrawCursor(false);
                    cursorNode.setTranslateX(cursorNode.getTranslateX() + 1.5);
                    cursorNode.setTranslateY(cursorNode.getTranslateY() + 1.5);
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
        sp.setViewport(new Rectangle2D(canvas.getLayoutX() + canvas.getTranslateX() + imgWidth/2 + (imgWidth < imgHeight ? imgHeight - imgWidth : 0), canvas.getLayoutY() + canvas.getTranslateY()+ imgHeight/2 + (imgWidth > imgHeight ? imgWidth - imgHeight : 0), imgWidth/2, imgHeight));
        WritableImage snapshot = canvas.snapshot(sp, bounds);
        try{
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", new File(System.getProperty("user.dir") + "\\test.png"));
        }catch(Exception e){  
            e.printStackTrace();
        }
    }

    private void initStampMode(Group root){
        root.addEventFilter(KeyEvent.KEY_PRESSED, event->{
            if (event.getCode() == KeyCode.ALT) {
                if(isStampMode()){
                    stampSelection = true;
                }
            }
        });
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
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            gc.closePath();
            gc.beginPath();
            //gc.fillOval(e.getX() - cImgWidth/2, e.getY() - cImgHeight/2, cImgWidth, cImgHeight);
        }else if (eyedropperMode){
            int tempInt = tempPR.getArgb((int)(e.getX() * currentScale), (int)(e.getY() * currentScale));
            colorPicker.setValue(Color.rgb(((tempInt >> 16) & 0xff), ((tempInt >> 8) & 0xff), (tempInt & 0xff)));
        }else if (stampMode){
            BufferedImage cropped = new BufferedImage((int) 20, (int) 20, bImg.getType());
            Graphics g = cropped.getGraphics();
            g.drawImage(bImg, (int)(-(stampSourceX + (e.getX() - stampSourceX))), (int)(-(stampSourceY + (e.getX() - stampSourceY))), null);
            g.dispose();
            gc.drawImage(SwingFXUtils.toFXImage(cropped, null), e.getX() - 10, e.getY() - 10);
        }
        hideDrawCursor();
        e.consume();
        });
    }

    private void initCanvasPressedListener(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMousePressed(e -> {
            eventTriggered();
            if(isPanningMode()){
                isPressed = true;
                canvas.setCursor(Cursor.CLOSED_HAND);
                pressedX = e.getX();
                pressedY = e.getY();
            }
            else if(isDrawingMode()){
                showDrawCursor();
                cursorNode.setTranslateX(e.getSceneX() - cImgWidth/2);
                cursorNode.setTranslateY(e.getSceneY() - cImgHeight/2);
                gc.setStroke(colorPicker.getValue());
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.setLineJoin(StrokeLineJoin.ROUND);
                gc.setLineWidth(cImgHeight);
                gc.beginPath();
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                //gc.fillOval(e.getX() - cImgWidth/2, e.getY() - cImgHeight/2, cImgWidth, cImgHeight);
            }
            else if(isEyedropperMode()){
                WritableImage img = new WritableImage((int)canvasWidth, (int)canvasHeight);
                canvas.snapshot(new SnapshotParameters(), img);
                tempPR = img.getPixelReader();
                int tempInt = tempPR.getArgb((int)(e.getX() * currentScale), (int)(e.getY() * currentScale));
                colorPicker.setValue(Color.rgb(((tempInt >> 16) & 0xff), ((tempInt >> 8) & 0xff), (tempInt & 0xff)));
            }
            else if(isStampMode()){
                if(stampSelection){
                    stampSelection = false;
                    // SnapshotParameters sp = new SnapshotParameters();
                    // sp.setViewport(new Rectangle2D(e.getSceneX() - 10, e.getSceneY() - 10, e.getSceneX() + 10, e.getSceneY() + 10));  //hard-coded
                    // WritableImage bounds = new WritableImage(20, 20);                                                                   //hard-coded
                    // currentStampSelection = canvas.snapshot(sp, bounds); 

                    //WritableImage bounds = new WritableImage((int)imgWidth, (int)imgHeight);
                    WritableImage bounds = new WritableImage((int)imgWidth, (int)imgHeight);

                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setViewport(new Rectangle2D(canvas.getLayoutX() + canvas.getTranslateX() + imgWidth/2 + (imgWidth < imgHeight ? imgHeight - imgWidth : 0), canvas.getLayoutY() + canvas.getTranslateY()+ imgHeight/2 + (imgWidth > imgHeight ? imgWidth - imgHeight : 0), imgWidth/2, imgHeight));
                    WritableImage snapshot = canvas.snapshot(sp, bounds);
                    bImg = SwingFXUtils.fromFXImage(snapshot, null);
                    stampSourceX = e.getX();
                    stampSourceY = e.getY();
                    stampSourceX -= imgWidth/2;
                    stampSourceY -= imgWidth;
                }
                else if (!stampSelection){
                    BufferedImage cropped = new BufferedImage((int) 20, (int) 20, bImg.getType());
                    Graphics g = cropped.getGraphics();
                    g.drawImage(bImg, (int)(-(stampSourceX + (e.getX() - stampSourceX))), (int)(-(stampSourceY + (e.getX() - stampSourceY))), null);
                    g.dispose();
                    gc.drawImage(SwingFXUtils.toFXImage(cropped, null), e.getX() - 10, e.getY() - 10);
                }
            }
            e.consume();
        });
    }

    private void initCanvasReleasedListener(Canvas canvas){
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setOnMouseReleased(e -> {
            eventTriggered();
            if(isPanningMode()){
                isPressed = false;
                canvas.setCursor(Cursor.HAND);
            }
            else if(isDrawingMode()){
                cursorNode.setTranslateX(e.getSceneX() - cImgWidth/2);
                cursorNode.setTranslateY(e.getSceneY() - cImgHeight/2);
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                gc.closePath();
                //gc.fillOval(e.getX() - cImgWidth/2, e.getY() - cImgHeight/2, cImgWidth, cImgHeight);
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

    private boolean isEyedropperMode(){
        return eyedropperMode;
    }

    private boolean isStampMode(){
        return stampMode;
    }

    private void eventTriggered(){
        drawModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        panModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        eyedropperModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));
        stampModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(40, 40, 40), CornerRadii.EMPTY, Insets.EMPTY)));

        if (isPanningMode())
            panModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), new CornerRadii(10), Insets.EMPTY)));  
        if  (isDrawingMode())
            drawModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), new CornerRadii(10), Insets.EMPTY)));
        else if (isEyedropperMode())
            eyedropperModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), new CornerRadii(10), Insets.EMPTY)));
        else if (isStampMode())
            stampModeButton.setBackground(new Background(new BackgroundFill(Color.rgb(100, 100, 100), new CornerRadii(10), Insets.EMPTY)));
    }
}
