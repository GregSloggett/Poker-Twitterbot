package poker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import twitter4j.*;

public class TwitterInteraction {
	static Twitter twitter;
	static Status latestTweet;
	static String username;
	public TwitterInteraction(Twitter t, Status tweet, String nick){
		twitter = t;
		latestTweet = tweet;
		username = nick;
	}

	//static Twitter twitter = TwitterFactory.getSingleton();
	static Map<String, Boolean> usersPlayingGames = new HashMap<String, Boolean>();
	static Map<String, GameOfPoker> gamesOfPoker = new HashMap<String, GameOfPoker>();

	/**
	 * This method is just for displaying a String as a status.
	 * @param status
	 * @throws TwitterException
	 */
	public static void updateStatus(String status) throws TwitterException{
		System.out.println("getting 2");
		StatusUpdate replyStatus = new StatusUpdate("@"+username+" "+status);
		replyStatus.setInReplyToStatusId(latestTweet.getId());
		latestTweet = twitter.updateStatus(replyStatus);
		System.out.println("getting 3");
	}

	/** Pass this method a string and an image file,
	 * and it will post a status of the string with
	 * the picture underneath.
	 * @param string
	 * @param image
	 * @throws TwitterException
	 */
	public static void updateStatusWithTextAndImage(String string, File image) throws TwitterException{
		StatusUpdate status = new StatusUpdate(string);
		status.setMedia(image);
		latestTweet = twitter.updateStatus(status);
	}




	/**
	 * When you run this it starts listening
	 * out for the hashtag #FOAKDeal. When
	 * it detects the hashtag has been posted it
	 * replies to that user with a message.
	 */

	public void StartHashtagStream() {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		StatusListener statusListener = new StatusListener() {

			@Override
			//This is what happens when a status with our hashtag is detected
			public void onStatus(Status status) {

				try {			
					if(status.getText().contains("#FOAKDeal")){
						if(!(usersPlayingGames.containsKey(status.getUser().getScreenName()))){
							System.out.println("1");
							System.out.println(status.getUser().getScreenName());

							StatusUpdate replyStatus = new StatusUpdate("You have posted our hashtag to play poker.\nWould you like to discard some cards? (y/n)");
							replyStatus.setInReplyToStatusId(status.getId());
							latestTweet = twitter.updateStatus(replyStatus);

							System.out.println("2");
							usersPlayingGames.put(status.getUser().getScreenName(), true);
							System.out.println("3");
							System.out.println(usersPlayingGames.containsKey(status.getUser().getScreenName()));
							gamesOfPoker.put((status.getUser().getScreenName()), new GameOfPoker(status.getUser().getScreenName()));
							//waitForTweet(status);
							DeckOfCards d = new DeckOfCards();
							//HumanPokerPlayer p = new HumanPokerPlayer(d,t);
							//gamesOfPoker.get(status.getUser().getScreenName()).humanPlayer.setAskToDiscard(true);
						}
						else{
							System.out.println("4");
							StatusUpdate replyStatus = new StatusUpdate("You're already playing a running game.");
							System.out.println("5");
							replyStatus.setInReplyToStatusId(status.getId());
							System.out.println("6");
							twitter.updateStatus(replyStatus);
							System.out.println("7");
						}
					}
					else if(status.getText().contains("#FOAKLeave")){
						if((usersPlayingGames.containsKey(status.getUser().getScreenName()))){
							System.out.println("1");
							System.out.println(status.getUser().getScreenName());

							StatusUpdate replyStatus = new StatusUpdate("You have posted the hashtag to leave a poker game. Thanks for playing!");
							replyStatus.setInReplyToStatusId(status.getId());
							twitter.updateStatus(replyStatus);
							System.out.println("2");
							usersPlayingGames.remove(status.getUser().getScreenName());
							System.out.println("3");
						}
						else{
							System.out.println("4");
							StatusUpdate replyStatus = new StatusUpdate("You're aren't currently playing a poker game. To start a new game post a tweet with the hashtag 'FOAKDeal'");
							System.out.println("5");
							replyStatus.setInReplyToStatusId(status.getId());
							System.out.println("6");
							twitter.updateStatus(replyStatus);
							System.out.println("7");
						}

					}	

				} catch (TwitterException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


			//The below methods are just auto-generated ones that we don't need right now.
			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
				throw new UnsupportedOperationException("Not supported yet."); 
			}

			@Override
			public void onTrackLimitationNotice(int i) {
				throw new UnsupportedOperationException("Not supported yet."); 
			}

			@Override
			public void onScrubGeo(long l, long l1) {
				throw new UnsupportedOperationException("Not supported yet."); 
			}

			@Override
			public void onStallWarning(StallWarning sw) {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public void onException(Exception ex) {
			}
		};

		FilterQuery filter = new FilterQuery();        

		//These are the keywords it listens out for.
		String keywords[] = {"#FOAKDeal","#FOAKLeave"};

		filter.track(keywords);        

		twitterStream.addListener(statusListener);
		twitterStream.filter(filter);          
	}  



	/**
	 * Testing updating status with text with an image below it.
	 * I just loaded from URL so that I didn't have to load any 
	 * images into the project, it'd be the same process for an image
	 * loaded from file.
	 */
	private static void testStatusWithTextAndImage() throws IOException, TwitterException{
		URL url = new URL("https://www.drupal.org/files/issues/sample_7.png");
		BufferedImage image = ImageIO.read(url);
		File file = new File("image.png");
		ImageIO.write(image,"png",file);

		updateStatusWithTextAndImage("Testing png",file);
	}

	public static String waitForTweet() throws TwitterException, InterruptedException{
		boolean waiting = true;
		while(waiting){
			Thread.sleep(10000);
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
				return latestReplyString;
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

	private static ArrayList<Status> getDiscussion() throws TwitterException{
		//Query query = new Query("from:PokerFOAK");
		//QueryResult result = twitter.search(query);
		//System.out.println("count : "+result.getTweets().size());
		List<Status> statuses = twitter.getUserTimeline(username);
		ArrayList<Status> replies = new ArrayList<Status>();
		System.out.println("latest tweet:"+latestTweet.getText());
		for(Status s : statuses){
			System.out.println("lll");
			System.out.println(s.getInReplyToStatusId() + " || ");// + latestTweet.getId());
			System.out.println("mmm");
			if( s.getInReplyToStatusId() == latestTweet.getId()){
				System.out.println("the reply is: " + s.getText());
				replies.add(s);
			}
		}
		System.out.println("num replies:" +statuses.size());
		return replies;
	}







	public static void main(String[] args) throws TwitterException, IOException {


	}

}