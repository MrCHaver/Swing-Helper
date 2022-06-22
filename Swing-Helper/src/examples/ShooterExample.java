package examples;

import clara.Canvas;
import java.awt.*;

public class ShooterExample{

   public static void main(String[]args){
      int cHeight = 800, cWidth = 800; //Overall Frame Bounds

      // PLAYER BALL
      int ballX = 50, ballY = (cHeight- 80), ballXInc = 2, ballYInc = 2, ballSize = 20;

      // OBSTACLE BALL
      int  obsSize = 30;
	  int[] obsX =    {150, 300, 450, 600, 700}; // each column represents a ball
	  int[] obsY =    { 10,  10,  10,  10, 10};
	  int[] obsXInc = {  1,   0,   2,  -1, 0};
	  int[] obsYInc = {  1,   2,   0,   1, 5};

	  //BULLETS
	  int[] bX =    new int[20]; // each column represents a ball
	  int[] bY =    new int[bX.length];
	  int[] bInc =  new int[bX.length];
     int score = 0;

     // Rectangle
     int rX = 300, rY = 550, rW = 200, rH = 20;

      Canvas canvas = new Canvas(cHeight,cWidth); // Set up canvas for graphics

	  canvas.getAudioManager().setAudioPath("src/assets/audio");

	  // Load game.wav audio file
	  canvas.getAudioManager().load("bg", "gamemusic.wav");
	  canvas.getAudioManager().load("collision", "collision.wav");
	  canvas.getAudioManager().load("hit", "hit.wav");

      //DISPLAY STARTING DIRECTIONS
      canvas.drawColor(Color.BLUE);
	  canvas.drawText("You Are the Blue Ball",10,40);
	  canvas.drawText("Change Diections with W,A,S,D",10,60);
	  canvas.drawText("Shoot the Red Balls and don't let them hit you",10,80);
	  canvas.drawText("Get your best score in 60 seconds",10,120);
     canvas.repaint();  // refresh display
	  canvas.addDelay(3000); // delay for animation (in ms)

     double fireTimer = 0;
     double time = 0;

	  double  startTime = (double)System.currentTimeMillis();  //Get Starting time for timer

      while(time < 60){ //game loop
		
		canvas.getAudioManager().get("bg").start();
		// DRAW SHAPES
		canvas.drawColor(Color.LIGHT_GRAY); // BACKGROUND
		canvas.drawRect(0,0,cHeight,cWidth, true);

		canvas.drawColor(Color.BLUE); // BALL
		canvas.drawOval(ballX,ballY,ballSize,ballSize,true);

		canvas.drawColor(Color.RED); // OBSTACLE
		for (int i = 0; i < obsX.length; i++)
			canvas.drawOval(obsX[i],obsY[i],obsSize,obsSize,true);

      canvas.drawColor(Color.GREEN); // RECTANGLE
      canvas.drawRect(rX,rY,rW,rH, true);


		canvas.drawColor(Color.BLACK); // BULLET
		for (int i = 0; i < bX.length; i++)
			if (bX[i] >0){
				canvas.drawOval(bX[i],bY[i],8,8,true);

			}

        // WRITE TEXT
		canvas.drawColor(Color.BLACK);
		canvas.drawText("W-> up, Z -> down, A -> left, S -> right",10,20);
		canvas.drawText("SCORE = "+score,10,cHeight-50);
		time = (double)((long)(System.currentTimeMillis()-startTime)*10)/10000;
		canvas.drawText("TIME = "+time+" sec.",200,cHeight-50);

		/***   CHECK COLLISION ****/
		for (int i = 0; i < obsX.length; i++){
			if (collision(ballX,ballY,ballSize,obsX[i],obsY[i],obsSize)){ // obstacle w/ player
				canvas.getAudioManager().get("hit").setMicrosecondPosition(0);
				canvas.getAudioManager().get("hit").start();
				score--;
				respawnObstacle(i,obsX,obsY,obsXInc,obsYInc);

			}

         if (rectOverlap(obsX[i],obsY[i],obsSize,obsSize,rX,rY,rW,rH)){ // Obstacle w/ Rectangle
            obsYInc[i] = -obsYInc[i];
            if (obsYInc[i] == 0)
           		obsXInc[i] = -obsXInc[i];
         }


         for (int j = 0; j < bX.length; j++){
            if (collision(bX[j],bY[j],8,obsX[i],obsY[i],obsSize)){ // obstacle w/ bullet
				canvas.getAudioManager().get("collision").setMicrosecondPosition(0);
				canvas.getAudioManager().get("collision").start();
				score++;
               respawnObstacle(i,obsX,obsY,obsXInc,obsYInc);
            }

         }

		}


		/******   MOVEMENT ********/

		// change motion based on keyboard
		//System.out.println(canvas.returnKey());
		if (canvas.containsKey("w"))
			ballY += -ballYInc;
		if (canvas.containsKey("s"))
			ballY += ballYInc;
		if (canvas.containsKey("a"))
			ballX += -ballXInc;
		if (canvas.containsKey("d"))
			ballX += ballXInc;

      // Handle blaster fire
		if (canvas.containsKey(" ")  &&  (time - fireTimer) >= 0.2){  // Space bar and ready to fire
			int i = 0;
			while ( i < bX.length && bX[i] > 0)  // find place in array for bullet
           	 	i++;

			 if (i< bX.length){  // Create new bullet at top of ball moving up at speed -4
				bY[i] = ballY;
				bX[i] = ballX+ballSize/2;
				bInc[i] = -4;
			 }
			// printBulletLocs(bX,bY,bInc);
         fireTimer = time;
         //System.out.println(fireTimer);
		}


		// Move each obstacle in array
		for (int i = 0; i < obsX.length; i++){
			obsX[i]+=obsXInc[i];
			obsY[i]+=obsYInc[i];
			if (obsX[i] > (cWidth-40)  || obsX[i] < 0)
				obsXInc[i] = -obsXInc[i];
			if (obsY[i] > (cHeight-40) || obsY[i] <0)
				obsYInc[i] = -obsYInc[i];
		}

		// Move each bullet
		for (int i = 0; i < bX.length; i++){
			bY[i]+=bInc[i];
			if (bY[i] < 0){
				bX[i] = 0;  // reset if above screen
				bY[i] = 0;  // allows to re-use spot in arrays
				bInc[i] = 0;
			}
		}

		canvas.addDelay(8);
		canvas.repaint();  // refresh display


      } // end while
      canvas.drawText("GAME OVER!  Final Score = "+score,10,40);
      canvas.repaint();

    }  // end main



    public static void respawnObstacle(int i, int[] obsX, int[] obsY, int[] obsXInc, int[] obsYInc){
		 obsY[i] = 10;   // respawn in top left
		 obsX[i] = 10;
	     obsXInc[i] = (int)(Math.random()*5);
	   	 obsYInc[i] = (int)(Math.random()*5);
	}

	// This has nothing to do with game play.   just used for diagnostic purposes
	 public static void printBulletLocs(int[] bX, int[] bY, int[] bInc){
			 for (int i = 0; i < bX.length; i++)
			 	System.out.println("("+bX[i]+","+bY[i]+") ["+bInc[i]+"]");
			 System.out.println();
	}




	/*******  SUPPORT METHODS FOR COLLISION DETECTION   *****/
    public static int distance(int x1, int y1, int x2, int y2){
		 int dx = x1 - x2;
		 int dy = y1 - y2;
		 return (int)Math.sqrt( dx*dx + dy*dy  );
 	}

	public static boolean collision(int x1, int y1, int s1, int x2, int y2, int s2){
		if (distance(x1+s1/2,y1+s1/2,x2+s2/2,y2+s2/2) < s1/2+ s2/2)
			return true;
		return false;
	}

   public static boolean rectOverlap(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
		return (x1 +w1 >= x2 && x1 <= x2 +w2 && y1+h1 >=y2 && y1 <= y2+h2);
	}


} // end class