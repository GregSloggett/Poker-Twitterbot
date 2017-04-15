package poker;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PictureOfHand {
	
	protected HandOfCards hand;
	
	PictureOfHand(HandOfCards inputhand){
		hand = inputhand;
	}
	
	public void createImage() throws IOException {
		


       
        
        
		 
		Image Scaled1 = null;
        BufferedImage finalHand = new BufferedImage(500, 300, BufferedImage.TYPE_INT_RGB); 
        Graphics2D bgr = finalHand.createGraphics();
    	
        	for(int i =0;i<5;i++){
        		
        		String filename = hand.cardArray[i].toString()+".png" ;
        		File file = new File(filename);
        		BufferedImage img1 = ImageIO.read(PictureOfHand.class.getResource("/PlayingCards/"+filename));
        		Scaled1 = img1.getScaledInstance(150, 200, 1);
        		
        		if(i == 0){
        			bgr.drawImage(Scaled1, 0, 0, null);	
        		}else if(i==1){
        			bgr.drawImage(Scaled1, 75, 0, null);
        		}else if(i==2){
        			bgr.drawImage(Scaled1, 150, 0, null);
        		}else if(i==3){
        			bgr.drawImage(Scaled1, 225, 0, null);
        		}else if(i==4){
        			bgr.drawImage(Scaled1, 300, 0, null);
        		}
        		
    		
        	}
        	
            
            
            File outputfile = new File("PlayingCard.png");
        	ImageIO.write(finalHand, "png", outputfile);
        	




		
	
	}
	
	public static void main(String[] args) throws IOException{
		
		DeckOfCards deck = new DeckOfCards();
		HandOfCards hand = null;
		try {
			hand = new HandOfCards(deck);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PictureOfHand pic = new PictureOfHand(hand);
		
		pic.createImage();
		System.out.println(hand);
	
	}
}