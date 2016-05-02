package multiplayer;

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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import singleplayer.AI_;
import singleplayer.PingPong;

public class ClientPlayer2 extends Canvas implements Runnable, KeyListener {
	
	private static final long serialVersionUID = 1L;
	public  Ball b1 ;
	double speed;
	public final boolean diff = PingPong.diff_level;
	//DatagramPacket sendPacket;
	public static int connected;
	AI_ ai1, ai2;
    DatagramSocket clientSocket;
    
    Rectangle serverRect;
	Rectangle client1Rect , client2Rect, client3Rect;
	int player1,player2,player3,player4;
	
	//Connection info
	String serverIP;
	int serverPort;
	Socket socket;
	int a,b,c,d,e;
	//Frame
	JFrame frame;
	int width = 500;
	int height = 500;
	public final Dimension gameDim = new Dimension(width,height);
	String received;
	//Screen
	BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	
	//Game info
	int pWidth1,pWidth2,pWidth3,pWidth4;
	int pHeight1,pHeight2,pHeight3,pHeight4;
	int xPos;
	int yPos;
	int sXPos;
	static int sYPos;
	int port1,port2,port3,position1,position2,position3;
	
	int c2XPos , c3XPos;
	int c2YPos , c3YPos;
	
	int loop;
	
	
	// Booleans for movement
		boolean moveRight = false;
		boolean moveLeft = false;
		public static boolean c2;
	   InetAddress IPAddress, IPAddress1, IPAddress2, IPAddress3;
		

	// Scores
			int serverScore = 0;
			int client1Score = 0;
			int client2Score = 0;
			int client3Score = 0;

	// Ball info
		public int bX;
		public int bY;
		int bSize = 16;
		
		// For run
		private int ticks = 0;
		private int frames = 0;
		private int FPS = 0;
		private int UPS = 0;
		public double delta;
		
		// Used in the "run" method to limit the frame rate to the UPS
		boolean limitFrameRate = false;
		boolean shouldRender;

		private String username;
		//private int userid = 1;
		
		public void run() {
			long lastTime = System.nanoTime();
			double nsPerTick = 1000000000D / 60D;

			long lastTimer = System.currentTimeMillis();
			delta = 0D;
			
			try {
				clientSocket = new DatagramSocket();
			} catch (Exception e1) {
			
				e1.printStackTrace();
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
					} catch (IOException e) {
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

		private void requestInformation() {
			serverIP = JOptionPane.showInputDialog("What is the IP of the server you are connecting to?");
			serverPort = Integer.parseInt(JOptionPane.showInputDialog("What is the port you are connecting through?"));
			username = JOptionPane.showInputDialog("Enter username:");
		}

		private void createFrame() {
			// Frame stuff
			setMinimumSize(gameDim);
			setMaximumSize(gameDim);
			setPreferredSize(gameDim);
			frame = new JFrame("Pong Multiplayer : "+ username );

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());

			frame.add(this, BorderLayout.CENTER);
			frame.pack();

			frame.setResizable(false);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);

			if(diff == true){
	        	pWidth1 = pWidth2 = pWidth3 = pWidth4 = 15;
	        	pHeight1 = pHeight2 = pHeight3 = pHeight4 = 40;
	        	speed=3;
	        }
	        else{
	        	pWidth1 = pWidth2 = pWidth3 = pWidth4 = 15;
	        	pHeight1 = pHeight2 = pHeight3 = pHeight4 = 60;
	        	speed=4;
	        }
			// Players initializing
			xPos =  frame.getWidth()-20;
			yPos = 60;
			
			sXPos = 1;
			sYPos = 60;
			
			c2XPos = 200;
			c2YPos = 0;
			
			c3XPos = 200;
			c3YPos = 492;
			
			serverRect = new Rectangle(sXPos, sYPos, pWidth1, pHeight1);
			client1Rect = new Rectangle(xPos, yPos, pWidth2, pHeight2);			
			client2Rect = new Rectangle(c2XPos, c2YPos,pHeight3,pWidth3);
			client3Rect = new Rectangle(c3XPos,c3YPos,pHeight4,pWidth4);
			
			b1 = new Ball(this.getWidth()/2 - 16, this.getHeight()/2 - 16);
			
			/*if(PingPong.players==3 && CreateServer.ainumber==1){
				ai1 = new AI_(height-26,1);
			}
			else if(PingPong.players==4 && CreateServer.ainumber==2){
				ai1 = new AI_(height-26,1);
				ai2 = new AI_(height-26,494);
			}*/

			addKeyListener(this);

			requestFocus();

			Thread thread = new Thread(this);
			thread.start();
		}


		public ClientPlayer2() throws Exception {
			requestInformation();
			
			//handShake();
			createFrame();
			c2=true;
			
		}

		private void movement() {
			
			if (moveLeft && c2XPos > 0) {
				c2XPos -= speed;
			}

			if (moveRight &&  ((c2XPos + pHeight3 < getWidth())) ) {
				c2XPos += speed;
			}
			
		}

		private void tick() throws IOException {
			
			IPAddress = InetAddress.getByName(serverIP);
			
		    byte[] receiveData = new byte[1024];
		    byte[] sendData1 = new byte[1024];
		    byte[] sendData2 = new byte[1024];
		    byte[] sendData3 = new byte[1024];
		     
    		String sentence = "2," + Integer.toString(c2XPos)+","+"0";
			
			 sendData1 = sentence.getBytes();
			 System.out.println(sentence);
			 DatagramPacket sendPacket = new DatagramPacket(sendData1, sendData1.length,IPAddress, serverPort);
		     clientSocket.send(sendPacket);
		     
		         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			     System.out.println("recieve packet..");
			      clientSocket.receive(receivePacket);
			      //receiveData = receivePacket.getData();
			      String delims = "[,]+";
			      received = new String(receivePacket.getData());
			      
			      String[] tokens = received.split(delims);
			      
			      System.out.println(tokens[0] + "," );
			      
			      int c = Integer.parseInt(tokens[0]);
			      
			      System.out.println(c + "," + tokens[2]);
			      
			      if (c==0){
			    	  
			    	   loop++;
			           position1 = Integer.parseInt(tokens[1]);
			         if (loop==1){
			           port1  =Integer.parseInt(tokens[3]);
			           tokens[2] = tokens[2].startsWith("/") ? tokens[2].substring(1) : tokens[2];
				       IPAddress1 = InetAddress.getByName(tokens[2]);
				       System.out.println(IPAddress1 + "," + position1 + "," + port1);
					      
				       
				       if((PingPong.players==3||PingPong.players==4)){
				    	   port2  =Integer.parseInt(tokens[5]);
				    	   tokens[4] = tokens[4].startsWith("/") ? tokens[4].substring(1) : tokens[4];
				    	   IPAddress2 = InetAddress.getByName(tokens[4]);
				       }
				       if((PingPong.players==4)){
				    	   port3  =Integer.parseInt(tokens[7]);
				    	   tokens[6] = tokens[6].startsWith("/") ? tokens[6].substring(1) : tokens[6];
				    	   IPAddress3 = InetAddress.getByName(tokens[6]);
				       }
			         }
			      }
				  
			      else if (Integer.parseInt(tokens[0])==1) {
			    	 
			             position2 = Integer.parseInt(tokens[1]);
			      }
				  
			      else if (Integer.parseInt(tokens[0])==3)  {
			    	  
			    	  position3 = Integer.parseInt(tokens[1]);
			      }
			      
			  
		     
		     if((PingPong.players==3||PingPong.players==4)){
					sendData2 = sentence.getBytes();
					DatagramPacket sendPacket2 = new DatagramPacket(sendData2, sendData2.length, IPAddress1, port1);
					clientSocket.send(sendPacket2);
				}
				
				if(PingPong.players==4){
					sendData3 = sentence.getBytes();
					DatagramPacket sendPacket3 = new DatagramPacket(sendData3, sendData3.length, IPAddress3, port3);
					clientSocket.send(sendPacket3);
				}
				
				
				      
		       
				
			  serverRect.setBounds(sXPos, sYPos, pWidth1, pHeight1);
			  client1Rect.setBounds(xPos, yPos, pWidth2, pHeight2);
			  client2Rect.setBounds(c2XPos,c2YPos,pHeight3,pWidth3);
			  client3Rect.setBounds(c3XPos,c3YPos,pHeight4,pWidth4);
			  
			  sYPos = position1;
			  c3XPos = position3;
			  yPos = position2;
				
			  System.out.println(a+" "+b);
		      
			  movement();
			  
			  
				
			  b1.tick_1(this, delta);
			
			 /* if(PingPong.players==3 && CreateServer.ainumber==1){
					ai1.tick_1(this,delta);
				}
			    else if(PingPong.players==4 && CreateServer.ainumber==2){
					ai1.tick_1(this,delta);
					ai2.tick_1(this,delta);
				}*/
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
			g.fillRect(sXPos, sYPos, pWidth1, pHeight1);
			g.fillRect(xPos, yPos, pWidth2, pHeight2);
			if(PingPong.players==3||PingPong.players==4){
				g.fillRect(c2XPos,c2YPos,pHeight3,pWidth3);
				}
				
				if(PingPong.players==4){
				g.fillRect(c3XPos,c3YPos,pHeight4,pWidth4);
				}
			//g.fillRect(c2XPos,c2YPos,pHeight,pWidth);
			b1.render(g);

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
							b1.remove();
						}
				    	else if (player2==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b1.remove();
						}
					}
			
					else if(PingPong.players==3){
						if (player1==1 && player3==1 && player2==0){
							g.drawString("Player2 wins",200,200);
							b1.remove();
						}
						if (player2==1 && player3==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b1.remove();
						}
						if(player1==1 && player2==1 && player3==0)
						{
							g.drawString("Player3 wins",200,200);
							b1.remove();
						}
					}
					else if(PingPong.players==4){
						if (player1==1 && player3==1 && player4==1 && player2==0){
							g.drawString("Player2 wins",200,200);
							b1.remove();
						}
						if (player2==1 && player3==1 && player4==1 && player1==0){
							g.drawString("Player1 wins",200,200);
							b1.remove();
						}
						if(player1==1 && player2==1 && player4==1 && player3==0)
						{
							g.drawString("Player3 wins",200,200);
							b1.remove();
						}
						if(player1==1 && player2==1 && player3==1 && player4==0)
						{
							g.drawString("Player4 wins",200,200);
							b1.remove();
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
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				moveRight = true;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				moveLeft = true;
			}
			
		}

		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				moveRight = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				moveLeft = false;
			}
			
		}

		public void keyTyped(KeyEvent e) {
               //nothing to be done here
		}
	}
