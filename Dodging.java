// Drew	Harris	2/2/2020
// information in each level found in levels.txt
// add new rows	to make	your own levels

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.text.Font;
import javafx.scene.effect.*;
import javafx.stage.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.*;
import javafx.scene.effect.MotionBlur;
import javafx.util.Duration;
import javafx.scene.shape.*;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import java.util.*;
import java.io.*;

public class Dodging extends Application{

	
	public static final	int	numOfVars =	32;
	public static final	int	STARTING_LEVEL = 1;
	public int level;
	public int deaths;

	public static boolean moveEnabled;
	public int godMode;
	KeyBoard kb;
	boolean[] keyStatus;
	
	Rectangle player;
	Rectangle goalRect;
	Obstacle[] obArray;
	DangerSquare dangerSquare;
	
	DropShadow playerGlow;
	
	boolean	freezeLeftRight;

	int[] levelArray;
	
	Text levelDisplay;
	Text deathDisplay;

	public class KeyBoard implements EventHandler<KeyEvent>{
		@Override
		public void	handle(KeyEvent	e){
			if(e.getEventType()	== KeyEvent.KEY_PRESSED){
				if(e.getCode() == KeyCode.RIGHT){
					keyStatus[3] = true;
				}else if(e.getCode() ==	KeyCode.LEFT){
					keyStatus[2] = true;
				}else if(e.getCode() ==	KeyCode.UP){
					keyStatus[0] = true;
				}else if(e.getCode() ==	KeyCode.DOWN){
					keyStatus[1] = true;
				}else if(e.getCode() ==	KeyCode.G){
					godMode	*= -1;
					if(godMode == 1){
						System.out.println("GOD MODE ENABLED");
					}else{
						System.out.println("GOD MODE DISABLED");
					}
				}


			}

			if(e.getEventType()	== KeyEvent.KEY_RELEASED){
				if(e.getCode() == KeyCode.RIGHT){
					keyStatus[3] = false;
				}else if(e.getCode() ==	KeyCode.LEFT){
					keyStatus[2] = false;
				}else if(e.getCode() ==	KeyCode.UP){
					keyStatus[0] = false;
				}else if(e.getCode() ==	KeyCode.DOWN){
					keyStatus[1] = false;
				}
			}
		}
	}

	public class AniTimer extends AnimationTimer{
		public void	handle(long	now){

			if(keyStatus[0]	== true	&& player.getTranslateY()>3	&& moveEnabled){//up
				player.setTranslateY(player.getTranslateY()	- levelArray[5]);			//ARRAY: Player	Speed Y
			}else if(keyStatus[1] == true && player.getTranslateY()+levelArray[2] <	600	&& moveEnabled){//down
				player.setTranslateY(player.getTranslateY()	+ levelArray[5]);			//ARRAY: PLAYER	SPEED Y
			}

			if(keyStatus[2]	== true	&& player.getTranslateX()>3	&& moveEnabled && !freezeLeftRight){//left
				player.setTranslateX(player.getTranslateX()	- levelArray[4]);			//ARRAY: PLAYER	SPEED X
			}else if(keyStatus[3] == true && player.getTranslateX()	+ levelArray[2]	< 1500 && moveEnabled && !freezeLeftRight){
				player.setTranslateX(player.getTranslateX()	+ levelArray[4]);			//ARRAY: PLAYER	SPEED x
			}


			for(int	i =	0; i<obArray.length; i++){
				obArray[i].reflect();
				obArray[i].placeObs();
			}
 
			if(checkForHits() && (godMode == -1)){
				playerToStartBad();
				deaths++;
				deathDisplay.setText("Deaths: "	+ deaths);
			}

			if(checkForGoalTouch()){
				player.setStroke(Color.rgb(100,255,100));
				playerGlow.setColor(Color.GREEN);
			}

			if(checkForGoalInside()){
				level++;
				levelArray = getLevelArray();
				levelStarter();
				
				levelDisplay.setText("Level: " + (level));
				
			}
			


		}
	}

	public int[] getLevelArray(){
		int[] levelArray = new int[numOfVars];


		try{
			Scanner	levelInput = new Scanner(new File("levels.txt"));
			levelInput.reset();
			for(int	i =	0; i < level; i++){
				if(levelInput.hasNext()){
					levelInput.nextLine();
				}
				
			}

			for(int	q =	0; q<numOfVars;	q++){
				if(levelInput.hasNext()){
					levelArray[q] =	levelInput.nextInt();
				}
				
			}
			 
		}catch (FileNotFoundException ex)  {
			System.out.println("YOU DO NOT HAVE LEVELS.txt IN THE SAME FOLDER AS THIS FILE");
		}

		return levelArray;
	}

	public void	levelStarter(){
		System.out.println(Arrays.toString(getLevelArray()));
		playerStart();
		goalStart();
		if(levelArray[7] ==	1){		//ARRAY: OBSTACLES ON
			resetObsPos();
			setObsSpeed();
		}else{
			disableObstacles();
		}
		
		if(levelArray[25] == 1){	//ARRAY: DANGER SQUARE ON
			dangerSquare.initDangerSquare(levelArray[26], levelArray[27], levelArray[28], levelArray[29]);
			dangerSquare.doSizeChange(levelArray[30]);
		}else{
			dangerSquare.disableDangerSquare();
		}
		
		specialCase();
	}
	
	public void	disableObstacles(){
		for(int	i =	0; i<obArray.length; i++){
			obArray[i].setVisible(false);
		}
	}
	
	public void	specialCase(){
		if(levelArray[18] == 1){	//ARRAY: SPECIAL CASE
			TranslateTransition	slideRace =	new	TranslateTransition(Duration.millis(9000), player);
			slideRace.setByX(1310);
			slideRace.play();
			freezeLeftRight	= true;
		}else if(levelArray[18]	== 2){	//ARRAY: SPECIAL CASE
			freezeLeftRight	= false;
		}
	}

	public void	goalStart(){
		goalRect.setTranslateX(levelArray[14]);	//ARRAY: GOAL POS X
		goalRect.setTranslateY(levelArray[15]);	//ARRAY: GOAL POS Y
		goalRect.setHeight(levelArray[17]);		//ARRAY: GOAL SCALE	Y
		goalRect.setWidth(levelArray[16]);		//ARRAY: GOAL SCALE	X
	}

	public void	playerStart(){
		///
		player.setTranslateX(levelArray[0]);	//ARRAY: PLAYER	START X
		player.setTranslateY(levelArray[1]);	//ARRAY: PLAYER	START Y
		player.setWidth(levelArray[2]);			//ARRAY: PLAYER	WIDTH
		player.setHeight(levelArray[3]);		//ARRAY: PLAYER	HEIGHT
		///
		player.setFill(null);
		player.setStroke(Color.rgb(187,255,255));
		player.setStrokeWidth(5);
		playerGlow = new DropShadow(15,	0, 0, Color.rgb(0,0,255));
		player.setEffect(playerGlow);
		
		
		PauseTransition	waitT =	new	PauseTransition(Duration.millis(300));
		moveEnabled	= false;
		waitT.setOnFinished((new EventHandler<ActionEvent>() {
			public void	handle(ActionEvent e){
				moveEnabled	= true;

			}
		}));
		///
		
		///
		waitT.play();

	}
	
	public void	setObsSpeed(){
		for(int	i =	0; i<obArray.length; i++){
			///
			obArray[i].setSpeedX(levelArray[13], levelArray[12], levelArray[24]);	//ARRAY: OBSTACLE MIN SPEED	X	ARRAY: MAX SPEED X		ARRAY: DO FLIP
			obArray[i].setSpeedY(levelArray[11], levelArray[10], levelArray[24]);	//ARRAY: MIN SPEED Y			ARRAY: MAX SPEED Y		ARRAY: DO FLIP
		}
	}
	
	public void	resetObsPos(){
		for(int	i =	0; i<obArray.length; i++){
			if(levelArray[19] +	i*levelArray[20] < levelArray[21]){	//ARRAY: OBSTACLE START	 ARRAY:	OBSTACLE SPACING	 //ARRAY: OBSTACLE END X
				obArray[i].setVisible(true);
				obArray[i].setPosX(levelArray[19] +	i*levelArray[20]);
			}else{
				obArray[i].setPosX(1);	//ARRAY: OBSTACLE START	X	  //ARRAY: OBSTACLE	SPACING
				obArray[i].setVisible(false);
			}
			obArray[i].setPosY((int)(Math.random() * (levelArray[23] - levelArray[22]))	+ levelArray[22]);		//ARRAY: OBS END Y		ARRAY: OBS START Y

			obArray[i].setScaleX(levelArray[8]);		//ARRAY: OBSTACLE SCALE	X
			obArray[i].setScaleY(levelArray[9]);		//ARRAY: OBSTACLE SCALE	Y
		}
	}

	public static void main(String[] args){
		launch(args);
	}

	public void	start(Stage	myStage){
		Group root = new Group();

		myStage.setScene(new Scene(root, 1500, 600,	Color.BLACK));
		myStage.show();

		Rectangle rKey = new Rectangle(0,0,5,5);
		rKey.setFill(Color.RED);
		kb = new KeyBoard();
		root.getChildren().add(rKey);
		rKey.addEventHandler(KeyEvent.ANY, kb);
		rKey.requestFocus();

		keyStatus =	new	boolean[4];

		AniTimer at	= new AniTimer();
		
		level =	STARTING_LEVEL;
		levelArray = getLevelArray();
		
		player = new Rectangle();
		initPlayerStart();
		root.getChildren().add(player);

		obArray	= new Obstacle[levelArray[6]];		//ARRAY: NUM OF	OBSTACLES (unused)

		createObstacles();

		for(int	i =	0; i<obArray.length; i++){
			root.getChildren().add(obArray[i]);
		}


		goalRect = new Rectangle(0,	0, 100,	400);

		goalRect.setFill(null);
		goalRect.setStroke(Color.rgb(100,255,100));
		goalRect.setStrokeWidth(5);
		goalRect.setArcHeight(30);
		goalRect.setArcWidth(30);
		goalRect.setEffect(new DropShadow(15, 0, 0,	Color.GREEN));
		goalRect.toFront();

		root.getChildren().add(goalRect);

		
		levelDisplay = new Text(10,	30,	"Level: " +	level);
		Font pixelFont = new Font("Arial", 35);// Font.font("Candara", 25);
		levelDisplay.setFont(pixelFont);
		levelDisplay.setFill(Color.WHITE);
		levelDisplay.setEffect(new DropShadow(15,0,0, Color.WHITE));
		root.getChildren().add(levelDisplay);

		deaths = 0;
		deathDisplay = new Text(1280, 30, "Deaths: " + deaths);
		deathDisplay.setFont(pixelFont);
		deathDisplay.setFill(Color.WHITE);
		deathDisplay.setEffect(new DropShadow(15,0,0, Color.WHITE));
		root.getChildren().add(deathDisplay);
		
		dangerSquare = new DangerSquare(root);

		
		moveEnabled	= true;
		godMode	= -1;
		freezeLeftRight	= false;


		System.out.println(Arrays.toString((Font.getFamilies().toArray())));
		at.start();
		levelStarter();

	}

	public void	createObstacles(){
		for(int	i =	0; i<obArray.length; i++){
			///
			obArray[i] = new Obstacle(150 +	i*120, 30, 20, 20);
			///
			obArray[i].placeObs();
			obArray[i].setFill(null);
			obArray[i].setStroke(Color.rgb(255,100,100));
			obArray[i].setStrokeWidth(3);
			DropShadow obsGlow = new DropShadow(15,	0, 0, Color.RED);
			obArray[i].setEffect(obsGlow);
			obArray[i].setArcHeight(10);
			obArray[i].setArcWidth(10);
		}
	}



	public void	initPlayerStart(){
		player.setFill(null);
		player.setStroke(Color.rgb(187,255,255));
		player.setStrokeWidth(5);
		playerGlow = new DropShadow(15,	0, 0, Color.rgb(0,0,255));
		player.setEffect(playerGlow);
		player.setArcHeight(20);
		player.setArcWidth(20);
	}

	public BoundingBox getPlayerBounds(){
		BoundingBox	playerBounds = new BoundingBox(player.getTranslateX(), player.getTranslateY(), levelArray[2], levelArray[3]);		//ARRAY: PLAYER	WIDTH		//ARRAY: PLAYER	HEIGHT
		return playerBounds;
	}
	
	public BoundingBox getGoalBounds(){
		BoundingBox	goalBounds = new BoundingBox(goalRect.getTranslateX(), goalRect.getTranslateY(), levelArray[16], levelArray[17]);	//ARRAY: GOAL WIDTH			//ARRAY: GOAL HEIGHT
		return goalBounds;
	}

	public boolean checkForGoalTouch(){
		if(getGoalBounds().intersects(getPlayerBounds()) &&	!(getGoalBounds().contains(getPlayerBounds()))){
			return true;
		}else{
			return false;
		}
	}

	public boolean checkForGoalInside(){
		if(getGoalBounds().contains(getPlayerBounds())){
			return true;
		}else{
			return false;
		}
	}

	public boolean checkForHits(){
		boolean	hit	= false;
		for(int	i =	0; (i<obArray.length) && !hit; i++){
			if(obArray[i].checkObjHit()	&& player.getTranslateX()!=levelArray[0]){		//ARRAY: PLAYER	START X
				hit	= true;
			}
		}
		
		if(dangerSquare.checkCollision()){
			hit	= true;
		}
		

		return hit;
	}

	public void	playerToStartBad(){
		PauseTransition	waitT =	new	PauseTransition(Duration.millis(300));
		moveEnabled	= false;
		playerGlow.setColor(Color.RED);
		waitT.setOnFinished((new EventHandler<ActionEvent>() {
			public void	handle(ActionEvent e){
				playerGlow.setColor(Color.BLUE);
				player.setStroke(Color.rgb(187,255,255));
				moveEnabled	= true;

			}
		}));
		///
		player.setTranslateX(levelArray[0]);		//ARRAY: PLAYER	POS	X
		player.setTranslateY(levelArray[1]);		//ARRAY: PLAYER	POS	Y
		specialCase();
		///
		waitT.play();
	}
	
	// public class TextMap{
// 		Text textDisplay;
// 		Text textBoundary;
// 		String[] mapChoice = {"000", "III", "H  __"};
// 		
// 		public TextMap(Group root){
// 			textDisplay = new Text(0, 0, "");
// 			textDisplay.setFill(null);
// 			textDisplay.setStroke(Color.rgb(255,100,100));
// 			textDisplay.setStrokeWidth(3);
// 			//textDisplay.setEffect(new DropShadow(10, 0, 0, Color.RED));
// 			
// 			textBoundary = new Text(0, 0, "");
// 			textBoundary.setVisible(false);
// 			textBoundary.setBoundsType(TextBoundsType.VISUAL);
// 			
// 			root.getChildren().add(textDisplay);
// 			root.getChildren().add(textBoundary);
// 			
// 		}
// 		
// 		public boolean checkForTouch(){
// 			if(getMapBounds().intersects(getPlayerBounds()) && levelArray[32]  == 1){		//ARRAY: MAP ON
// 				return true;
// 			}else{
// 				return false;
// 			}
// 		}
// 		
// 		public Bounds getMapBounds(){
// 			return textBoundary.getBoundsInParent();
// 		}
// 		
// 		public void initTextMap(int choice, int x, int y, int fSize, int sclX, int sclY){
// 			textDisplay.setTranslateX(x);
// 			textDisplay.setTranslateY(y);
// 			textDisplay.setText(mapChoice[2]); 	//CHange
// 			textDisplay.setFont(new Font(fSize*4));
// 			
// 			textBoundary.setTranslateX(x);
// 			textBoundary.setTranslateY(y);
// 			textBoundary.setText(mapChoice[2]); 	//CHange
// 			textBoundary.setFont(new Font(fSize*4));
// 		}
// 	}
	
	
	
	
	public class DangerSquare{
		Rectangle visualSquare;
		Rectangle boundarySquare;
		
		boolean activated;
		
		public DangerSquare(Group root){
			visualSquare = new Rectangle(0,	0, 0, 0);
			
			boundarySquare = new Rectangle(0, 0, 0,	0);
			
			boundarySquare.setVisible(false);
			
			visualSquare.setFill(null);
			visualSquare.setStroke(Color.rgb(255,100,100));
			visualSquare.setStrokeWidth(3);
			//visualSquare.setEffect(new DropShadow(15, 0, 0,	Color.RED));
			
			root.getChildren().add(visualSquare);
			root.getChildren().add(boundarySquare);
		}
		
		public void disableDangerSquare(){
			visualSquare.setVisible(false);
			
		}
		
		public void	doSizeChange(int period){
			ScaleTransition	st = new ScaleTransition(Duration.millis(period*1000), visualSquare);
			ScaleTransition	stBounds = new ScaleTransition(Duration.millis(period*1000), boundarySquare);
			
			if(period != 0){
				st.setByX(1.2);
				st.setByY(1.2);
				st.setCycleCount(Animation.INDEFINITE);
				st.setAutoReverse(true);
 
	 			
				stBounds.setByX(1.2);											///LOOK HERE
				stBounds.setByY(1.2);
				stBounds.setCycleCount(Animation.INDEFINITE);
 				
				stBounds.play();
	 			st.play();
			}else{
				stBounds.stop();
				st.stop();
			}
		}
		
		public void	setXPos(int	xVal){
			visualSquare.setTranslateX(xVal);
			boundarySquare.setTranslateX(xVal);
		}
		
		public void	setYPos(int	yVal){
			visualSquare.setTranslateY(yVal);
			boundarySquare.setTranslateY(yVal);
		}
		
		public void	setSize(int	width, int height){
			visualSquare.setWidth(width);
			visualSquare.setHeight(height);
			
			boundarySquare.setWidth(width);
			boundarySquare.setHeight(height);
		}
		
		public boolean checkCollision(){
			if(getBoundaryDS().intersects(getPlayerBounds()) && levelArray[25]  == 1){
				return true;
			}else{
				return false;
			}
		}
		
		
		public Bounds getBoundaryDS(){
			return boundarySquare.getBoundsInParent();
		}
		
		public void	initDangerSquare(int x,	int	y, int width, int height){
			setXPos(x);
			setYPos(y);
			setSize(width, height);
			visualSquare.setVisible(true);
		}
		
		
	}	
	
	

	public class Obstacle extends Rectangle{

		int	xVel;
		int	yVel;

		public BoundingBox getObsBounds(){
			BoundingBox	geomBoundsObs =	new	BoundingBox(getTranslateX(), getTranslateY(), levelArray[8], levelArray[9]);		//ARRAY: OBSTACLE WIDTH		//ARRAY: OBSTACLE HEIGHT
			return geomBoundsObs;
		}

		public boolean checkObjHit(){
			if(getObsBounds().intersects(getPlayerBounds())	&& isVisible()){
				return true;

			}else{
				return false;
			}
		}

		public void	placeObs(){
			setTranslateX(getTranslateX() +	xVel);
			setTranslateY(getTranslateY() +	yVel);
		}
		
		public void	setPosX(int	pos){
			setTranslateX(pos);
		}
		
		public void	setPosY(int	pos){
			setTranslateY(pos);
		}


		public void	reflect(){
			if((getTranslateY()	+ levelArray[9]) > 600){	//ARRAY: OBS SCALE Y
				yVel *=	-1;
			}else if(getTranslateY() < 0){
				yVel *=	-1;
			}
			if(getTranslateX()<0){
				xVel *=	-1;
			}else if((getTranslateX() +	levelArray[8]) > 1500){	//ARRAY: OBS SCALE X
				xVel *=	-1;
			}
		}

		public Obstacle(int	x, int y, int xh, int yh){
			super(0, 0,	xh,	yh);
			//xVel = (int)(Math.random() * 3) +	0;
			xVel = 0;
			if(Math.random() > .5){
				xVel *=	-1;
			}

			yVel = (int)(Math.random() * 7)	+ 4;
			if(Math.random() > .5){
				yVel *=	-1;
			}
			setTranslateX(x);
			setTranslateY((int)(Math.random() *	570) + 0);
		}

		public void	setSpeedX(int minX,	int	maxX, int doFlip){
			xVel = (int) (Math.random()	* (maxX-minX)) + minX;
			if(Math.random() > .5 && doFlip==1){
				xVel *=	-1;
			}
		}
		
		public void	setSpeedY(int minY,	int	maxY, int doFlip){
			yVel = (int) (Math.random()	* (maxY-minY)) + minY;
			if(Math.random() > .5 && doFlip==1){
				yVel *=	-1;
			}
		}
		public void	setScaleX(int size){
			setWidth(size);
		}
		
		public void	setScaleY(int size){
			setHeight(size);
		}
		
		
	}
}
