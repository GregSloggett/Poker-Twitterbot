package poker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.xml.internal.ws.util.StringUtils;

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

public class TwitterStreamer {
	static Twitter twitter = TwitterFactory.getSingleton();
	static Map<String, Boolean> usersPlayingGames = new HashMap<String, Boolean>();
	static Map<String, GameOfPoker> gamesOfPoker = new HashMap<String, GameOfPoker>();
	private static final int NUMTHREADS = 30;
	static ExecutorService executor = Executors.newFixedThreadPool(NUMTHREADS);
	Thread thread;

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
							System.out.println("1");
							
							System.out.println(status.getUser().getScreenName());
							System.out.println("Status id: " +status.getId());
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You have posted our hashtag to play poker. Welcome to the game!");
							replyStatus.setInReplyToStatusId(status.getId());
							System.out.println("replyStatus is replying to tweet: "+replyStatus.getInReplyToStatusId());
							Status latestTweet = twitter.updateStatus(replyStatus);
							
							System.out.println("2");
							usersPlayingGames.put(status.getUser().getScreenName(), true);
							System.out.println("3");
							System.out.println(usersPlayingGames.containsKey(status.getUser().getScreenName()));
							gamesOfPoker.put((status.getUser().getScreenName()), new GameOfPoker(status.getUser().getScreenName()));
							//waitForTweet(status);
							DeckOfCards d = new DeckOfCards();
							String nick = status.getUser().getScreenName();
							TwitterInteraction t = new TwitterInteraction(twitter, latestTweet,nick);
							HumanPokerPlayer p = new HumanPokerPlayer(d,t);
							//p.run();
							//Thread thread = new Thread(p);
							//thread.start();
							executor.execute(p);
							//gamesOfPoker.get(status.getUser().getScreenName()).humanPlayer.setAskToDiscard(true);
						}
						else{
							System.out.println("4");
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You're already playing a running game.");
							System.out.println("5");
							replyStatus.setInReplyToStatusId(status.getId());
							System.out.println("6");
							twitter.updateStatus(replyStatus);
							System.out.println("7");
						}
					}
					else if(containsIgnoreCase(status.getText(), "#FOAKLeave")){
						if((usersPlayingGames.containsKey(status.getUser().getScreenName()))){
							System.out.println("1");
							System.out.println(status.getUser().getScreenName());

							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You have posted the hashtag to leave a poker game. Thanks for playing!");
							replyStatus.setInReplyToStatusId(status.getId());
							twitter.updateStatus(replyStatus);
							System.out.println("2");
							usersPlayingGames.remove(status.getUser().getScreenName());
							System.out.println("3");
						}
						else{
							System.out.println("4");
							StatusUpdate replyStatus = new StatusUpdate("@"+userNickname+" You aren't currently playing a poker game. To start a new game post a tweet with the hashtag 'FOAKDeal'");
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
		String keywords[] = {"#FOAKDeal","#FOAKLeave","#FOAKDEAL","#FOAKLEAVE","#foakdeal", "#foakleave","#FOAKdeal","#FOAKleave"};

		filter.track(keywords);      
		

		twitterStream.addListener(statusListener);
		twitterStream.filter(filter);          
	}  
	
	public static boolean containsIgnoreCase(String str, String searchStr)     {
	    if(str == null || searchStr == null) return false;

	    final int length = searchStr.length();
	    if (length == 0)
	        return true;

	    for (int i = str.length() - length; i >= 0; i--) {
	        if (str.regionMatches(true, i, searchStr, 0, length))
	            return true;
	    }
	    return false;
	}
	

	
	public static void main(String[] args) {
		StartHashtagStream();
	}
	
	
	
	
	
	
}
