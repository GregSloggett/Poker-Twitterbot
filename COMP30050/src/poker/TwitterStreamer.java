package poker;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;

public class TwitterStreamer {
	static Twitter twitter = TwitterFactory.getSingleton();
	//Stores simple boolean when user is in a game.
	public static Map<String, Boolean> usersPlayingGames = new HashMap<String, Boolean>();
	
	/* Stores Future objects of currently running game threads, allowing
	 * them to be cancelled and interrupted when a user wants to quit
	 */
	public static Map<String, Future<?>> gamesOfPoker = new HashMap<String, Future<?>>();
	
	//Sets the limit on the maximum number of concurrent games on our Twitter bot.
	private static final int NUMTHREADS = 30;
	
	//Allows for GameOfPoker objects running concurrently
	static ExecutorService executor = Executors.newFixedThreadPool(NUMTHREADS);
	public static final String outputMethod = "twitter";

	/**
	 * Begins listening out on Twitter for our hashtags. When they are posted, 
	 * it checks whether or not they are currently playing a running game and 
	 * replies accordingly. Creates GameOfPoker objects and launches them for
	 * new users which join games.
	 */
	public static void StartHashtagStream() {
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
		StatusListener statusListener = new StatusListener() {

			@Override
			//This is what happens when a status with our hashtag is detected
			public void onStatus(Status status) {

				try {		
					String userNickname = status.getUser().getScreenName();
					if(containsIgnoreCase(status.getText(),"FOAKDeal")){
						if(!(usersPlayingGames.containsKey(status.getUser().getScreenName()))){
							incrementProfileGameCount();
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You have posted our hashtag to play poker.. Welcome to the game!.");
							replyStatus.setInReplyToStatusId(status.getId());
							Status latestTweet = twitter.updateStatus(replyStatus);
							usersPlayingGames.put(status.getUser().getScreenName(), true);
							DeckOfCards d = new DeckOfCards();
							TwitterInteraction t = new TwitterInteraction(twitter, latestTweet,userNickname);
							GameOfPoker g = new GameOfPoker(status.getUser().getScreenName(),t,d);
							gamesOfPoker.put(userNickname, executor.submit(g));
						}
						//If a user tries to join a game and they are already playing
						else{
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You're already playing a running game.");
							replyStatus.setInReplyToStatusId(status.getId());
							twitter.updateStatus(replyStatus);
						}
					}
					else if(containsIgnoreCase(status.getText(), "#FOAKLeave")){
						if((usersPlayingGames.containsKey(status.getUser().getScreenName()))){
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You have posted the hashtag to leave a poker game.. Thanks for playing!");
							replyStatus.setInReplyToStatusId(status.getId());
							twitter.updateStatus(replyStatus);
							usersPlayingGames.remove(status.getUser().getScreenName());
							gamesOfPoker.get(userNickname).cancel(true);
						}
						//If a user tries to leave a game but they are not currently playing one.
						else{
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You aren't currently playing a poker game. To start a new game post a tweet with the hashtag 'FOAKDeal'");
							replyStatus.setInReplyToStatusId(status.getId());
							twitter.updateStatus(replyStatus);
						}

					}	

				} catch (TwitterException | InterruptedException e) {
					e.printStackTrace();
				}
			}


			
			@Override
			public void onDeletionNotice(StatusDeletionNotice sdn) {
				throw new UnsupportedOperationException("Not implemented."); 
			}

			@Override
			public void onTrackLimitationNotice(int i) {
				throw new UnsupportedOperationException("Not implemented."); 
			}

			@Override
			public void onScrubGeo(long l, long l1) {
				throw new UnsupportedOperationException("Not implemented."); 
			}

			@Override
			public void onStallWarning(StallWarning sw) {
				throw new UnsupportedOperationException("Not implemented.");
			}

			@Override
			public void onException(Exception ex) {
			}
		};

		FilterQuery filter = new FilterQuery();        

		//These are the keywords the Streamer looks for
		String keywords[] = {"#FOAKDeal","#FOAKLeave","#FOAKDEAL","#FOAKLEAVE","#foakdeal", "#foakleave","#FOAKdeal","#FOAKleave"};

		filter.track(keywords);      
		twitterStream.addListener(statusListener);
		twitterStream.filter(filter);          
	}  

	//Basic algorithm to check if one string contains another, regardless of case.
	public static boolean containsIgnoreCase(String string, String stringToFind)     {
		if(string == null || stringToFind == null) return false;

		final int strlen = stringToFind.length();
		if (strlen == 0)
			return true;

		for (int i = string.length() - strlen; i >= 0; i--) {
			if (string.regionMatches(true, i, stringToFind, 0, strlen))
				return true;
		}
		return false;
	}

	/**
	 * Checks in the HashMap of Future objects to see if a user has decided to quit
	 * their game.
	 * @param username
	 * @return
	 */
	public  static boolean userHasQuit(String username){
		if(gamesOfPoker.containsKey(username)){
			return gamesOfPoker.get(username).isCancelled();
		}
		else{
			return false;
		}
	}
	/**
	 * Gets the game counter from our Twitter bio.
	 * @return
	 * @throws TwitterException
	 */
	private static int getNumGamesPlayed() throws TwitterException{
		User user = twitter.showUser("PokerFOAK");
		String description = user.getDescription();
		String numGamesString = "";
		//Checks last 5 digits of bio for game counter.
		for(int i =description.length()-1;i>=description.length()-5;i--){
			if(Character.isDigit(description.charAt(i))){
				numGamesString = description.charAt(i)+numGamesString;
			}
		}
		int numGamesPlayed = Integer.parseInt(numGamesString);
		return numGamesPlayed;
	}
	
	/**
	 * Increments the game counter on our bio on Twitter.
	 * @throws TwitterException
	 */
	private static void incrementProfileGameCount() throws TwitterException{
		int currentGameCount = getNumGamesPlayed();
		currentGameCount++;
		twitter.updateProfile("FOAKPoker", "http://cs.ucd.ie", "Ireland", ("To Play: Tweet with #FOAKDeal&emsp;&emsp;&emsp;&emsp;To Leave: Tweet with #FOAKLeave&emsp;&emsp;Number of games played in this version of game: "+currentGameCount));
	}



	public static void main(String[] args) throws InterruptedException, TwitterException {
		StartHashtagStream();			
	}
}
