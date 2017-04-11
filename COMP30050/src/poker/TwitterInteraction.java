package poker;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import twitter4j.*;

public class TwitterInteraction {
	static Twitter twitter = TwitterFactory.getSingleton();
	static Map<String, Boolean> usersPlayingGames = new HashMap<String, Boolean>();
	/**
	 * This method is just for displaying a String as a status.
	 * @param status
	 * @throws TwitterException
	 */
	public void updateStatus(String status) throws TwitterException{
		twitter.updateStatus(status);
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
		twitter.updateStatus(status);
	}




	/**
	 * When you run this it starts listening
	 * out for the hashtag #FOAKDeal. When
	 * it detects the hashtag has been posted it
	 * replies to that user with a message.
	 */

	private static void StartHashtagStream() {
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

								StatusUpdate replyStatus = new StatusUpdate("You have posted our hashtag to play poker.");
								replyStatus.setInReplyToStatusId(status.getId());
								twitter.updateStatus(replyStatus);
								System.out.println("2");
								usersPlayingGames.put(status.getUser().getScreenName(), true);
								System.out.println("3");
								System.out.println(usersPlayingGames.containsKey(status.getUser().getScreenName()));
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

			} catch (TwitterException e) {
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


public static void main(String[] args) throws TwitterException, IOException {
	StartHashtagStream();
	//testStatusWithTextAndImage();
}

}