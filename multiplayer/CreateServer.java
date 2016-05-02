package multiplayer;
import singleplayer.PingPong;
import singleplayer.AI_;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CreateServer extends Canvas implements Runnable, KeyListener {
		
	
	private static final long serialVersionUID = 1L;

	// Ball
	public  Ball b;
	AI_ ai1, ai2;


	// Socket information
	public static DatagramSocket serverSocket;
	public static InetAddress IPAddress1,IPAddress2,IPAddress3;
	int serverPort;
	String received1,received2,received3;
	boolean receive1,receive2,receive3;
	int player1,player2,player3,player4;

	public static int ainumber;
	
	// Frame
		JFrame frame;
		int width = 500;
		int height = 500;
		static int position1,position2,position3;
		public final Dimension gameDim = new Dimension(width, height);
		public static double speed;
		public static final boolean diff = PingPong.diff_level;
		
		// Screen
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Game Info
		public static int pWidth1, pWidth2 ,pWidth3 , pWidth4;
		public static int pHeight1, pHeight2, pHeight3, pHeight4;
		
		
		// server's coordinates
		int xPos;
		int yPos;
		// client's coordinates
		int c1XPos;
		static int c1YPos;
		
		static int c2XPos , c3XPos;
		int c2YPos , c3YPos;
		int port1,port2,port3;
		

		// Booleans for movement
		boolean moveUp = false;
		boolean moveDown = false;

		// Rectangles for ball collision
		Rectangle serverRect;
		Rectangle client1Rect , client2Rect, client3Rect;

		// Scores
		int serverScore = 0;
		int client1Score = 0;
		int client2Score = 0;
		int client3Score = 0;

		// For run
		private int ticks = 0;
		private int frames = 0;
		private int FPS = 0;
		private int UPS = 0;
		public double delta;
		String s1,s2,s3,s4,s5;

		// Used in the "run" method to limit the frame rate to the UPS
		boolean limitFrameRate = false;
		boolean shouldRender;
		
		private void requestInformation() {
			try {
				serverPort = Integer.parseInt(JOptionPane.showInputDialog("What is the port that you wish to utilize? (Make sure to PortForward, if needed)"));
				//ainumber = Integer.parseInt(JOptionPane.showInputDialog("How many AI do you want? (Maximum of 2 AIs can be used"));
				
				//JOptionPane.showMessageDialog(null, "Creating server...");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			long lastTime = System.nanoTime();
			double nsPerTick = 1000000000D / 60D;

			long lastTimer = System.currentTimeMillis();
			delta = 0D;
			
			try {
				 
				 System.out.println(c1YPos+","+c2XPos+","+c3XPos);
				serverSocket = new DatagramSocket(serverPort);
				
				
			} catch (SocketException e) {
				e.printStackTrace();
			}

			while (true) {
				long now = System.nanoTime();
				delta += (now - lastTime) / nsPerTick;
				lastTime = now;
				
				
				// If you want to limit frame rate, shouldRender = false
				shouldRender = false;

				// If the time between ticks = 1, then various things (shouldRender = true, keeps FPS locked at UPS)
				while (delta >= 1) {
					ticks++;
					try {
				  
						tick();
						render();
				    
					} catch (Exception e) {
						e.printStackTrace();
					}
					delta -= 1;
					shouldRender = true;
				}
				
				if (!limitFrameRate && ticks > 0)
					shouldRender = true;

				// If you should render, render!
				if (shouldRender) {
					frames++;
					//render();
				}

				// Reset stuff every second for the new "FPS" and "UPS"
				if (System.currentTimeMillis() - lastTimer >= 1000) {
					lastTimer += 1000;
					FPS = frames;
					UPS = ticks;
					frames = 0;
					ticks = 0;
				}
			}
		} // End run
		private void createFrame() {
			// Frame stuff
			setMinimumSize(gameDim);
			setMaximumSize(gameDim);
			setPreferredSize(gameDim);
			
			if(diff == true){
	        	pWidth1 = pWidth2= pWidth3 = pWidth4 = 15;
	        	pHeight1 = pHeight2 =pHeight3 = pHeight4 = 40;
	        	speed=3;
	        }
	        else{
	        	pWidth1 = pWidth2 = pWidth3 = pWidth4 = 15;
	        	pHeight1 = pHeight2 = pHeight3 = pHeight4 = 60;
	        	speed=4;
	        }
			
			frame = new JFrame("Pong Multiplayer -Host Player-");

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			frame.add(this, BorderLayout.CENTER);
			frame.pack();

			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			// Players initializing
			xPos = 1;
			yPos = 60;
			c1XPos = frame.getWidth()-20;
			c1YPos = 60;
			
			c2XPos = 200;
			c2YPos = 0;
			
			c3XPos = 200;
			c3YPos = 492;

			serverRect = new Rectangle(xPos, yPos, pWidth1, pHeight1);
			client1Rect = new Rectangle(c1XPos, c1YPos, pWidth2, pHeight2);
			
			client2Rect = new Rectangle(c2XPos, c2YPos,pHeight3,pWidth3);
			client3Rect = new Rectangle(c3XPos,c3YPos,pHeight4,pWidth4);

			b = new Ball(this.getWidth()/2 - 16, this.getHeight()/2 - 16);
			
			if((PingPong.players==3 || PingPong.players==4) && ainumber==1){
				ai1 = new AI_(height-26,1);
			}
			else if(PingPong.players==4 && ainumber==2){
				ai1 = new AI_(height-26,1);
				ai2 = new AI_(height-26,494);
			}
			

			addKeyListener(this);

			requestFocus();
			
			Thread thread = new Thread(this);
			//Thread thread2 = new Thread(this);
			thread.start();
			//thread2.start();
	
			
		}

		public CreateServer() {
			requestInformation();
			//handShake();
			createFrame();
		}

		private void movement() {
			
			if (moveUp && yPos > 0) {
				yPos -= speed;
			}

			if (moveDown && ((yPos + pHeight1 < getHeight()) || (yPos + pHeight2 < getHeight())) ) {
				yPos += speed;
			}

		}

		private void tick() throws Exception {
			byte[] sendData1 = new byte[1024];
			byte[] receiveData1  =new byte[1024];
			byte[] sendData2 = new byte[1024];
			byte[] sendData3 = new byte[1024];

			c1YPos = position1;
			c2XPos = position2;
			c3XPos = position3;
			
			movement(); // Check for any movement

			// Re-bind the collision rectangles to new positions after movement
			serverRect.setBounds(xPos, yPos, pWidth1, pHeight1);
			client1Rect.setBounds(c1XPos, c1YPos, pWidth2, pHeight2);
			client2Rect.setBounds(c2XPos,c2YPos,pWidth3,pHeight3);
			client3Rect.setBounds(c3XPos,c3YPos,pWidth4,pHeight4);

			// Move the ball
			b.tick(this, delta);
			
			
			 System.out.println(c1YPos);
			 DatagramPacket receivePacket1 = new DatagramPacket(receiveData1, receiveData1.length);
			 serverSocket.receive(receivePacket1);
			  String delims = "[,]+";
			  
		      received1 = new String(receivePacket1.getData());
		      
		      String[] tokens = received1.split(delims);
		      
		      if (Integer.parseInt(tokens[0])==1){
		    	   receive1 = true;
		           position1 = Integer.parseInt(tokens[1]);
		           port1  =receivePacket1.getPort();
			       IPAddress1 = receivePacket1.getAddress();
		      }
			  
		      else if (Integer.parseInt(tokens[0])==2) {
		    	 
		    	     receive2 = true;
		             position2 = Integer.parseInt(tokens[1]);
		             port2  =receivePacket1.getPort();
		        	 IPAddress2 = receivePacket1.getAddress();
		        	  
		      }
			  
		      else if (Integer.parseInt(tokens[0])==3)  {
		    	  
		    	  receive3 = true;
		    	  System.out.println("from client3");
		    	  position3 = Integer.parseInt(tokens[1]);
		    	  port3  =receivePacket1.getPort();
		    	  IPAddress3 = receivePacket1.getAddress();
		      }
			  
			  System.out.println(position1);
			
			
			/*if((PingPong.players==3||PingPong.players==4) && ainumber==1){
				ai1.tick_(this,delta);
			}
		    else if(PingPong.players==4 && ainumber==2){
				ai1.tick_(this,delta);
				ai2.tick_(this,delta);
			}*/

			// Send coordinates to Client
			 if ( ( PingPong.players == 2 && receive1) || ( PingPong.players == 3 && receive1 && receive2 )
			       ||  ( PingPong.players == 4 && receive1 && receive2 && receive3 )) {
			try {
				s3 = Integer.toString(yPos);
				String sentence =  "0," + yPos + "," + IPAddress1 + "," + port1  + "," + IPAddress2 + "," + port2 + "," + IPAddress3 + "," + port3 + ", 0";
				
				sendData1 = sentence.getBytes();
				DatagramPacket sendPacket1 = new DatagramPacket(sendData1, sendData1.length, IPAddress1, port1);
				serverSocket.send(sendPacket1);
				
				if((PingPong.players==3||PingPong.players==4)){
					sendData2 = sentence.getBytes();
					DatagramPacket sendPacket2 = new DatagramPacket(sendData2, sendData2.length, IPAddress2, port2);
					serverSocket.send(sendPacket2);
				}
				
				if(PingPong.players==4){
					sendData3 = sentence.getBytes();
					DatagramPacket sendPacket3 = new DatagramPacket(sendData3, sendData3.length, IPAddress3, port3);
					serverSocket.send(sendPacket3);
				}
				
				
			} catch (Exception e) {
			}
			       }
		}
		private void render() {
			BufferStrategy bs = getBufferStrategy();

			if (bs == null) {
				createBufferStrategy(3);
				return;
			}

			Graphics g = bs.getDrawGraphics();

			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

			g.setColor(Color.RED);
			g.fillRect(xPos, yPos, pWidth1, pHeight1);
			g.fillRect(c1XPos, c1YPos, pWidth2, pHeight2);
			
			if(PingPong.players==3||PingPong.players==4){
			g.fillRect(c2XPos,c2YPos,pHeight3,pWidth3);
			}
			
			if(PingPong.players==4){
			g.fillRect(c3XPos,c3YPos,pHeight4,pWidth4);
			}
			
			b.render(g);

			// Draw scores
			g.setColor(Color.WHITE);
			if(serverScore<3){
				 g.drawString("Player1 : "+serverScore,5,10);
				 }
				 else{
					 g.drawString("Player1 : Lost",5,10);
					 pWidth1 = 0;
					 pHeight1 = 0;
					 player1=1;
				 }
				 if(client1Score<3){
				 g.drawString("Player2 : "+client1Score,width-69,10);
				 }
				 else{
					 g.drawString("Player2 :Lost",width-78,10);
					 pWidth2 = 0;
					 pHeight2 = 0;
					 player2=1;
				 }
				 if(PingPong.players==3){
					 g.setColor(Color.WHITE);
					 if(client2Score<3){
					 g.drawString("Player3 : "+client2Score,(width-69)/2,10);
					 }
					 else{
						 g.drawString("Player3 : Lost",(width-69)/2,10); 
						 pWidth3=0;
						 pHeight3=0;
						 player3=1;
					 }
					
					}
				 else if(PingPong.players==4){
					 g.setColor(Color.WHITE);
					 if(client2Score<3){
					 g.drawString("Player3 : "+client2Score,5,492);
					 }
					 else{
						 g.drawString("Player3 : Lost",5,492); 
						 pWidth3=0;
						 pHeight3=0;
						 player3=1;
					 }
					 if(client3Score<3){
					 g.drawString("Player4 : "+client3Score,width-69,492);
					 }
					 else{
						 g.drawString("Player4 :Lost",width-78,492);
						 pWidth4=0;
						 pHeight4=0;
						 player4=1;
					 }
					 
					}
				 if(PingPong.players==2){
				    	if (player1==1 && player2==0){
							g.drawString("Player2 wins",200,200);
							b.remove();
						}
				    	else if (player2==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b.remove();
						}
					}
			
					else if(PingPong.players==3){
						if (player1==1 && player3==1 && player2==0){
							g.drawString("Player2 wins",200,200);
							b.remove();
						}
						if (player2==1 && player3==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b.remove();
						}
						if(player1==1 && player2==1 && player3==0)
						{
							g.drawString("Player3 wins",200,200);
							b.remove();
						}
					}
					else if(PingPong.players==4){
						if (player1==1 && player3==1 && player4==1 && player2==0){
							g.drawString("Player2 wins",200,200);
							b.remove();
						}
						if (player2==1 && player3==1 && player4==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b.remove();
						}
						if(player1==1 && player2==1 && player4==1 && player3==0)
						{
							g.drawString("Player3 wins",200,200);
							b.remove();
						}
						if(player1==1 && player2==1 && player3==1 && player4==0)
						{
							g.drawString("Player4 wins",200,200);
							b.remove();
						}
					}
				
				
			
			/*if((PingPong.players==3 || PingPong.players==4) && ainumber==1){
				ai1.render(g);
			}
		    else if(PingPong.players==4 && ainumber==2){
				ai1.render(g);
				ai2.render(g);
			}*/
			
			//g.drawString("P3 score: " + client2Score, getWidth()-125, 20);

			g.dispose();
			bs.show();
			
			
		}

		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				moveUp = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				moveDown = true;
			}
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_UP) {
				moveUp = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_DOWN) {
				moveDown = false;
			}
		}

		public void keyTyped(KeyEvent e) {

		}
		
		
		

		

		

}
