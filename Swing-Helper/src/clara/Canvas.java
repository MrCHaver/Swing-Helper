//For my children, Clara and Gabe, who inspire me to never give up.
//Canvas Library - Assistive Representation Architecture (Clara)
package clara;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Stack;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Canvas extends JPanel implements KeyListener {

    Queue<DrawObject> queue;
    Stack<RotationObject> stack;

    private AudioManager audioManager;
    private HashMap<String,BufferedImage> imageMap;
    private ArrayList<String> keys;

    public Canvas(int xSize,int ySize){
        super(true);
        System.out.println("Hi");
        keys = new ArrayList<String>();
        queue=new LinkedList<>();
        audioManager = new AudioManager();
        stack=new Stack<>();
        imageMap= new HashMap<String,BufferedImage> ();
        setFocusable(true);
        JFrame frame = new JFrame("Canvas");
        frame.setSize(xSize, ySize);
        addKeyListener(this);
        frame.add(this);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addDelay(long time){
        try{
            Thread.sleep(time);
        }catch(Exception e){

        }
    }

    public AudioManager getAudioManager() {
        return this.audioManager;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


        while(!queue.isEmpty()){

            //remove the first object from the queue and determine what type it is
            DrawObject nextDrawableObject = queue.remove();

            //Draw a rectangle not filled
            if (nextDrawableObject instanceof DrawRect  && !((DrawRect)nextDrawableObject).isFilled()) {
                graphics2D.drawRect(nextDrawableObject.getX(), nextDrawableObject.getY(), ((DrawRect) nextDrawableObject).getWidth(), ((DrawRect) nextDrawableObject).getHeight());
            }
            //Draw a rectangle filled
            else if (nextDrawableObject instanceof DrawRect  && ((DrawRect)nextDrawableObject).isFilled()){
                graphics2D.fillRect(nextDrawableObject.getX(), nextDrawableObject.getY(), ((DrawRect) nextDrawableObject).getWidth(), ((DrawRect) nextDrawableObject).getHeight());
            }

            //Draw an oval not filled
            if (nextDrawableObject instanceof DrawOval  && !((DrawOval)nextDrawableObject).isFilled()) {
                graphics2D.drawOval(nextDrawableObject.getX(), nextDrawableObject.getY(), ((DrawOval) nextDrawableObject).getWidth(), ((DrawOval) nextDrawableObject).getHeight());
            }
            //Draw an oval filled
                else if (nextDrawableObject instanceof DrawOval  && ((DrawOval)nextDrawableObject).isFilled()){
                graphics2D.fillOval(nextDrawableObject.getX(), nextDrawableObject.getY(), ((DrawOval) nextDrawableObject).getWidth(), ((DrawOval) nextDrawableObject).getHeight());
            }

            //Draw an image
            if (nextDrawableObject instanceof DrawImage){
                    graphics2D.drawImage(((DrawImage) nextDrawableObject).getImageName(),nextDrawableObject.getX(),nextDrawableObject.getY(),null);
            }

            //Change the color
            if (nextDrawableObject instanceof DrawColor){
                graphics2D.setColor(((DrawColor) nextDrawableObject).getColor());
            }
            //Change the text
            if (nextDrawableObject instanceof DrawText){
                graphics2D.setFont( ((DrawText)nextDrawableObject).returnFont() );
                graphics2D.drawString( ((DrawText)nextDrawableObject).returnText(),nextDrawableObject.getX(),nextDrawableObject.getY() );
            }

            //Change the rotation
            if (nextDrawableObject instanceof Rotation){
                Rotation r =(Rotation)nextDrawableObject;
                graphics2D.rotate( Math.toRadians(r.getRotation()),r.getX(),r.getY() );
                stack.push(new RotationObject(r.getRotation(),r.getX(),r.getY()));
            }

            //Looks at all of previous rotations and resets repeats them in the opposite order with the opposite rotation
            if (nextDrawableObject instanceof Reset){
                while (!stack.isEmpty()){
                    RotationObject currentRotation = stack.pop();
                    graphics2D.rotate( Math.toRadians(-1*currentRotation.returnRotation()),currentRotation.returnX(), currentRotation.returnY() );
                    System.out.println(-1*currentRotation.returnRotation());
                }
            }
        }
        //After everything is done make sure to empty our both the queue of actions and the stack of rotation requests
        queue.clear();
        stack.clear();

    }

    //Adds DrawObjects to the Queue
    public void drawOval(int x,int y, int width, int height,boolean fill){
        queue.add(new DrawOval(x,y,width,height,fill));
    }

    public void drawRect(int x,int y, int width, int height,boolean fill){
        queue.add(new DrawRect(x,y,width,height,fill));
    }


    public void drawText(String text,int x,int y){
        queue.add(new DrawText(x,y,text,12));
    }

    public void drawText(String text,int x,int y, int fontSize){
	    queue.add(new DrawText(x,y,text,fontSize));
    }

    public void drawImage(int x,int y, String imageName){
        BufferedImage image = imageMap.get(imageName);

        if (image == null){
			try {
				image = ImageIO.read(new File(imageName));
				imageMap.put(imageName,image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        queue.add(new DrawImage(x,y,image));
    }

    public void drawColor(Color color){
        queue.add(new DrawColor(color));
    }

    public void rotate(double rotation,int x,int y){
        queue.add(new Rotation(rotation,x,y));
    }

    public String[] returnKeys(){
        return (String[])keys.toArray();
    }

    public boolean containsKey(String key)
    {
        return keys.contains(key);
    }

    public void reset(){
        queue.add(new Reset());
    }

    //Methods for KeyListener
    @Override
    public void keyTyped(KeyEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        String value= String.valueOf(e.getKeyChar());
        if (!keys.contains(value))
            keys.add(value);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(String.valueOf(e.getKeyChar()));
    }

    public class AudioManager {
        private String audioPath = "/";
        protected final HashMap<String, Clip> audio;

        public AudioManager() {
            this.audio = new HashMap<String, Clip>();
        }

        /**
         * 
         * @param key of the audio loaded
         * @return {@link javax.sound.sampled.Clip} audio object or null if not found.
         */
        public Clip get(String key) {
            return this.audio.get(key);
        }

        /**
         * @param audioPath path to the audio directory
         */
        public void setAudioPath(String audioPath) {
            this.audioPath = audioPath; 
        }

        /**
         * 
         * @param key of the audio for future reference
         * @param path to the audio in the audio directory. Defaults to {@link AudioManager#audioPath}. Use 
         * @return boolean value indicating whether operation was a success. 
         */
        public boolean load(String key, String path) {
            if (this.audio.get(key) != null) return true; 
            Clip clip = this.getMediaPlayer(path);
            if (clip != null) this.audio.put(key, clip);
            return clip != null;
        }

        /**
         * @param path path of audio file located in {@link AudioManager#audioPath}
         * @return {@link javax.sound.sampled.Clip} audio Object
         */
        private Clip getMediaPlayer(String path) {
            try {
                String absolutePath = Paths.get(System.getProperty("user.dir"), audioPath, path).toString();
                InputStream audioStream = new FileInputStream(absolutePath);
                InputStream bufferedIn = new BufferedInputStream(audioStream);
    
                Clip clip = AudioSystem.getClip();
                clip.open(AudioSystem.getAudioInputStream(bufferedIn));
    
                return clip; 
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    //Classes for DrawObjects
    public class DrawObject{
        private int x = 0;
        private int y = 0;

        public DrawObject(int x,int y){
            this.x = x;
            this.y = y;
        }
        public int getX(){
            return  x;
        }
        public int getY(){
            return  y;
        }
    }

    public class GabeObject extends DrawObject {
        public GabeObject(int x, int y) {
            super(x, y);
        }
    }

    public class DrawColor extends DrawObject{
        Color color;

        public DrawColor(int x, int y) {
            super(x, y);
        }

        public DrawColor(Color color){
            super(0,0);
            this.color=color;
        }

        public Color getColor(){
            return color;
        }
    }


    public class DrawImage extends DrawObject{

        private BufferedImage image;

        public DrawImage(int x, int y, BufferedImage image) {
            super(x, y);
            this.image=image;
        }

        public BufferedImage getImageName(){
            return image;
        }


    }

    public class DrawText extends  DrawObject{

	        private String text;
	        private Font font;

	        public DrawText(int x, int y,String text, int fontSize) {
	            super(x, y);
	            this.text=text;
	            font = new Font("TIMES NEW ROMAN",Font.BOLD,fontSize);
	        }

	        public String returnText(){
	            return text;
	        }

	        public Font returnFont(){
				return font;
	        }
    }

    public class DrawOval extends GabeObject{

        private int width;
        private int height;
        boolean fill;

        public DrawOval(int x, int y,int width,int height,boolean fill) {
            super(x, y);
            this.width=width;
            this.height=height;
            this.fill=fill;
        }
        public int getWidth(){
            return  width;
        }

        public int getHeight(){
            return  height;
        }

        public boolean isFilled(){return fill;}
    }

    public class DrawRect extends GabeObject {

        private int width;
        private int height;
        boolean fill;

        public DrawRect(int x, int y,int width,int height,boolean fill) {
            super(x, y);
            this.width=width;
            this.height=height;
            this.fill=fill;
        }

        public int getWidth(){
            return  width;
        }

        public int getHeight(){
            return  height;
        }

        public boolean isFilled(){return fill;}
    }

    public class Rotation extends DrawObject{

        private double rotationAmount;
        private int x;
        private int y;

        public Rotation(double rotationAmount,int x, int y) {
            super(0, 0);
            this.x=x;
            this.y=y;
            this.rotationAmount=rotationAmount;
        }

        public double getRotation(){
            return rotationAmount;
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }


    }
    public class Reset extends DrawObject{
        public Reset(){
            super(0, 0);
        }
    }

    public class RotationObject{
        private double rotationAmount;
        private int x;
        private int y;

        public RotationObject(double rotationAmount,int x,int y){
            this.rotationAmount=rotationAmount;
            this.x=x;
            this.y=y;
        }

        public double returnRotation(){
            return rotationAmount;
        }

        public int returnX(){
            return x;
        }

        public int returnY(){
            return y;
        }
    }


}
