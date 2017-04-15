package poker;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import twitter4j.*;

public class TwitterInteraction {
	Twitter twitter;
	Status latestTweet;
	String username;
	Status firstTweet;
	String compoundTweet = "";
	
	static int hashCode = 0;
	public TwitterInteraction(Twitter t, Status tweet, String nick){
		twitter = t;
		firstTweet = tweet;
		latestTweet = tweet;
		username = nick;
		hashCode = this.hashCode();
	}

	public TwitterInteraction(Twitter t){
		twitter = t;
		hashCode = this.hashCode();
	}

	//static Twitter twitter = TwitterFactory.getSingleton();
	static Map<String, Boolean> usersPlayingGames = new HashMap<String, Boolean>();
	static Map<String, GameOfPoker> gamesOfPoker = new HashMap<String, GameOfPoker>();

	/**
	 * This method is just for displaying a String as a status.
	 * @param status
	 * @throws TwitterException
	 */
	public void updateStatus(String status) throws TwitterException{
		System.out.println("getting 2");
		StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+status);
		replyStatus.setInReplyToStatusId(latestTweet.getId());
		latestTweet = update(replyStatus);
		System.out.println("getting 3");
	}

	public Status update(StatusUpdate a) throws TwitterException{
		return this.twitter.updateStatus(a);
	}


	/** Pass this method a string and an image file,
	 * and it will post a status of the string with
	 * the picture underneath.
	 * @param string
	 * @param image
	 * @throws TwitterException
	 * @throws IOException 
	 */
	public  void updateStatusWithTextAndImage(String string, BufferedImage image) throws TwitterException, IOException{
		StatusUpdate status = new StatusUpdate("@"+username+" "+string);
		status.setInReplyToStatusId(latestTweet.getId());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		status.setMedia("filename",is);
		latestTweet = update(status);
	}


	/**
	 * Testing updating status with text with an image below it.
	 * I just loaded from URL so that I didn't have to load any 
	 * images into the project, it'd be the same process for an image
	 * loaded from file.
	 */
	private void testStatusWithTextAndImage() throws IOException, TwitterException{
		URL url = new URL("https://www.drupal.org/files/issues/sample_7.png");
		BufferedImage image = ImageIO.read(url);
		//File file = new File("image.png");
		//ImageIO.write(image,"png",file);

		updateStatusWithTextAndImage("Testing png",image);
	}

	public  String waitForTweet() throws TwitterException, InterruptedException{
		boolean waiting = true;
		while(waiting){
			Thread.yield();
			Thread.sleep(30000);
			System.out.println(Thread.currentThread().getId());
			ArrayList<Status> replies = getDiscussion();
			System.out.println(replies.size());

			if(replies.size() > 0){
				System.out.println("getting here");
				for(int i=0;i<replies.size();i++){
					System.out.println("reply "+i+": "+replies.get(i).getText());
				}
				Status latestReply = replies.get(replies.size()-1);
				System.out.println("latest reply below");
				System.out.println(latestReply.getText());
				if(!(latestReply.getText().contains("#FOAKDeal")|| latestReply.getText().contains("#FOAKLeave"))){
					//latestTweet = latestReply;
					String latestReplyString = latestReply.getText();
					latestReplyString = stripAmpersandInfo(latestReplyString);
					latestTweet = latestReply;
					return latestReplyString;
				}
				/*
				if(latestReply.getText().compareTo("y")==0)	{
					StatusUpdate replyStatus = new StatusUpdate("You have decided to discard some cards.");
					replyStatus.setInReplyToStatusId(latestReply.getId());
					latestTweet = twitter.updateStatus(replyStatus);
					return "y";
				}
				else if(latestReply.getText().compareTo("n")==0)	{
					StatusUpdate replyStatus = new StatusUpdate("You have decided against discarding cards.");
					replyStatus.setInReplyToStatusId(latestReply.getId());
					latestTweet = twitter.updateStatus(replyStatus);
					return "n";
				}
				else{
					StatusUpdate replyStatus = new StatusUpdate("Command was not recognized, try again.");
					replyStatus.setInReplyToStatusId(latestReply.getId());
					latestTweet = twitter.updateStatus(replyStatus);
				}
				 */
			}
		}
		return "";
	}

	private static String stripAmpersandInfo(String latestReplyString) {
		int ampersandIndex = -1;
		String returnString = latestReplyString;
		for(int i =0;i<latestReplyString.length();i++){
			if(latestReplyString.charAt(i)=='@'){
				ampersandIndex = i;
				break;
			}
		}
		System.out.println("Ampersand index: "+ampersandIndex);
		String toRemove = "";
		if(ampersandIndex >=0){
			for(int i=ampersandIndex;i<latestReplyString.length()-1;i++){
				if(latestReplyString.charAt(i)!=' '){
					toRemove+=latestReplyString.charAt(i);
					System.out.println("toRemove = "+toRemove);
				}
				else{
					break;
				}
			}

		}
		if(toRemove.length()>0){
			returnString = returnString.replace(toRemove+" ", "");
		}
		System.out.println("return string is '"+returnString+"'");
		return returnString;
	}

	private ArrayList<Status> getDiscussion() throws TwitterException{
		//Query query = new Query("from:PokerFOAK");
		//QueryResult result = twitter.search(query);
		//System.out.println("count : "+result.getTweets().size());
		System.out.println("-------------------------------------------");
		System.out.println("This is thread "+Thread.currentThread().getId());
		System.out.println("Conversation with user: "+username);
		System.out.println("This started with the tweet: "+firstTweet.getText());
		System.out.println("Hashcode: "+hashCode);
		System.out.println("-------------------------------------------");
		List<Status> statuses = twitter.getUserTimeline(username);
		ArrayList<Status> replies = new ArrayList<Status>();
		System.out.println("latest tweet:"+latestTweet.getText());
		for(Status s : statuses){
			System.out.println("Status from user's timeline: "+s.getText());
			System.out.println(s.getInReplyToStatusId() + " || " + latestTweet.getId());
			if( s.getInReplyToStatusId() == latestTweet.getId()){
				System.out.println("the reply is: " + s.getText());
				replies.add(s);
			}
		}
		System.out.println("num replies:" +statuses.size());
		return replies;
	}

	public void appendToCompoundTweet(String string){
		compoundTweet += (string+"\n");
	}
	
	public void postCompoundTweet() throws TwitterException{
		StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+compoundTweet);
		replyStatus.setInReplyToStatusId(latestTweet.getId());
		System.out.println(compoundTweet+"\n\n");
		latestTweet = update(replyStatus);
		compoundTweet = "";
	}

	public static void main(String[] args) throws TwitterException, IOException {


	}



}