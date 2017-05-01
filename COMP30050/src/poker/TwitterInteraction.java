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

	public TwitterInteraction(Twitter t, Status tweet, String nick){
		twitter = t;
		firstTweet = tweet;
		latestTweet = tweet;
		username = nick;
	}
	//Constructor which is only used for testing.
	public TwitterInteraction(Twitter t){
		twitter = t;
	}

	/**
	 * This method is just for displaying a String as a status.
	 * @param status
	 * @throws TwitterException
	 */
	public void updateStatus(String status) throws TwitterException{
		StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+status);
		replyStatus.setInReplyToStatusId(latestTweet.getId());
		latestTweet = update(replyStatus);
	}

	public Status update(StatusUpdate a) throws TwitterException{
		Status ret = latestTweet;
		if(TwitterStreamer.outputMethod.compareTo("twitter")==0){
			ret =  twitter.updateStatus(a);
		}

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
		updateStatusWithTextAndImage("Testing png",image);
	}

	public  String waitForTweet() throws TwitterException, InterruptedException{
		while(!(TwitterStreamer.userHasQuit(username)) && TwitterStreamer.outputMethod.compareTo("twitter")==0){
			ArrayList<Status> replies = getDiscussion();

			if(replies.size() > 0){
				Status latestReply = replies.get(replies.size()-1);
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

	/**
	 * Returns the a Tweet with the "@username" prefix removed
	 * @param latestReplyString
	 * @return
	 */
	private static String stripAmpersandInfo(String latestReplyString) {
		int ampersandIndex = -1;
		String returnString = latestReplyString;
		for(int i =0;i<latestReplyString.length();i++){
			if(latestReplyString.charAt(i)=='@'){
				ampersandIndex = i;
				break;
			}
		}
		String toRemove = "";
		if(ampersandIndex >=0){
			for(int i=ampersandIndex;i<latestReplyString.length()-1;i++){
				if(latestReplyString.charAt(i)!=' '){
					toRemove+=latestReplyString.charAt(i);
				}
				else{
					break;
				}
			}

		}
		if(toRemove.length()>0){
			returnString = returnString.replace(toRemove+" ", "");
		}
		return returnString;
	}

	/**
	 * Gets latest Tweets for a user's timeline. Used for getting replies to Tweets.
	 * @return
	 * @throws TwitterException
	 */
	private ArrayList<Status> getDiscussion() throws TwitterException{
		List<Status> statuses = twitter.getUserTimeline(username);
		ArrayList<Status> replies = new ArrayList<Status>();
		for(Status s : statuses){
			if( s.getInReplyToStatusId() == latestTweet.getId()){
				replies.add(s);
			}
		}
		return replies;
	}

	/**
	 * Adds strings to a compound string which is to be Tweeted at a later stage
	 * @param string
	 */
	public void appendToCompoundTweet(String string){
		compoundTweet += (string+"\n");
	}

	
	/**
	 * Posts the Tweet that has been compounded in the string. If Strings are too
	 * long it divides the text up into smaller length strings within Twitter's
	 * character limit and posts them consecutively.
	 * @throws TwitterException
	 */
	public void postCompoundTweet() throws TwitterException{
		if(compoundTweet.length()>0){
			ArrayList<StatusUpdate> statusesToPost = new ArrayList<StatusUpdate>();

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

			for(StatusUpdate status:statusesToPost){
				status.setInReplyToStatusId(latestTweet.getId());
				latestTweet = update(status);
			}
			compoundTweet = "";
		}
	}

	public static void main(String[] args) throws TwitterException, IOException {


	}





}