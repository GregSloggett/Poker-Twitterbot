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
		Status ret = latestTweet;
		System.out.println("Thread is interrupted? "+ Thread.currentThread().isInterrupted());
		System.out.println("User has quit? "+TwitterStreamer.userHasQuit(username));
			ret =  twitter.updateStatus(a);
		
		return ret;
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
		while(!(TwitterStreamer.userHasQuit(username))){
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
				//latestTweet = latestReply;
				String latestReplyString = latestReply.getText();
				latestReplyString = stripAmpersandInfo(latestReplyString);
				latestTweet = latestReply;
				if(!(TwitterStreamer.userHasQuit(username))){
					return latestReplyString;
				}
			}
			try{
				Thread.sleep(10000);
			}
			catch(InterruptedException e){
				Thread.currentThread().interrupt();
			}

		}
		/*
		 * If null is returned then we know the thread has been interrupted.
		 * This means user has Tweeted #FOAKLeave and we need to try
		 * break out of all running code, because the game is over.
		 */
		return null;
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
		System.out.println("Pot in this thread is at " + "#Had to edit this to remove static access#");
		//System.out.println("Pot in this thread is at: "+HandOfPoker.pot);
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
		ArrayList<StatusUpdate> statusesToPost = new ArrayList<StatusUpdate>();

		System.out.println("compound tweet: \n"+compoundTweet);
		while(compoundTweet.length() > 120 && !(TwitterStreamer.userHasQuit(username))){
			boolean foundLine = false;
			int foundIndex=0;
			String substr = "";
			for(int i=0;i<compoundTweet.length();i++){
				if(compoundTweet.charAt(i)=='\n' && i<=120){
					substr = compoundTweet.substring(0, i+1);
					foundLine = true;
					foundIndex = i;
				}
				if(compoundTweet.charAt(i) == '\n' && i>120 && foundLine){
					System.out.println("Substring which is becoming tweet: "+substr);
					StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+substr);
					statusesToPost.add(replyStatus);
					compoundTweet = compoundTweet.substring(foundIndex+1,compoundTweet.length());
					break;
				}
			}
		}


		StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+compoundTweet);
		replyStatus.setInReplyToStatusId(latestTweet.getId());
		statusesToPost.add(replyStatus);
		System.out.println(compoundTweet+"\n\n");

		for(StatusUpdate status:statusesToPost){
			//	System.out.println("\n\n------Status: "+status.toString()+"-------\n\n");
			status.setInReplyToStatusId(latestTweet.getId());
			latestTweet = update(status);
		}
		compoundTweet = "";
	}

	public static void main(String[] args) throws TwitterException, IOException {


	}





}