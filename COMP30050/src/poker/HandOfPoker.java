package poker;

import java.io.IOException;
import java.util.ArrayList;

import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class HandOfPoker {
	
	final private static int OPENING_HAND = HandOfCards.ONE_PAIR_DEFAULT;
	public int highBet = 0;

	private ArrayList<PokerPlayer> players;
	int ante;
	//public static ThreadLocal<Integer> pot = new ThreadLocal<Integer>();
	public int pot;

	
	OutputTerminal UI;
	DeckOfCards deck;
	TwitterInteraction twitter;
	HumanPokerPlayer human;

	/*
	public HandOfPoker(ArrayList<PokerPlayer> players, int ante, DeckOfCards deck, OutputTerminal UI){
		this.players = new ArrayList<PokerPlayer>();
		this.players.addAll(players);
		this.ante = ante;
		this.UI = UI;
		this.deck = deck;
		
		try {
			gameLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	*/
	
	public HandOfPoker(ArrayList<PokerPlayer> players, int ante, DeckOfCards deck, TwitterInteraction t) throws TwitterException, IOException{
		this.players = new ArrayList<PokerPlayer>();
		this.players.addAll(players);
		for (int i=0; i< this.players.size(); i++){
			this.players.get(i).passHandOfPokerRef(this);
		}
		this.ante = ante;
		this.twitter = t;
		this.deck = deck;
		this.human = (HumanPokerPlayer) players.get(0);
		//pot.set(0);
		
		try {
			gameLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Runs the events of the game from a very high level
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws TwitterException 
	 */
	void gameLoop() throws InterruptedException, TwitterException, IOException {
		System.out.println("getting into gameLoop");
		dealHandsUntilOpen();
		System.out.println("game loop 1");
		//System.out.println("\n\n\n\n\n\n***********************\n" +players +"\n\n\n\n\n\n***********************\n");
		//twitter.appendToCompoundTweet(human.hand.toString());
		//twitter.postCompoundTweet();
		//System.out.println("################################################################");
		//twitter.updateStatusWithTextAndImage("Here are your cards!", human.pic.createImage(human.hand)  );
		twitter.postCompoundTweet();
		human.tweetInitialCards();
		//System.out.println("\n\n\n\n\n\n***********************\n2\n" +players +"\n\n\n\n\n\n***********************\n");
		twitter.postCompoundTweet();
		pot += collectAntes();
		//pot.set(pot.get() + collectAntes());
		displayPot();
		twitter.postCompoundTweet();
		System.out.println("game loop 2");
		pot += takeBets();
		//pot.set(pot.get() + takeBets());
		twitter.postCompoundTweet();
		discardCards();
		highBet = 0;
		System.out.println("game loop 3");
		displayPot();
		twitter.postCompoundTweet();
		System.out.println("number of players "+players.size());
		if (players.size() > 1){
			//discardCards();
			displayPot();
			pot += takeBets();
			//pot.set(pot.get()+takeBets());
			displayPot();
			if (players.size() >1){
				showCards();
			}
		}
		System.out.println("game loop 4");
		
		awardWinner(calculateWinners());
		human.replyForNextRound();
	}

	/**
	 * Deals hands until an opening hand is achieved from at least one player,
	 * shuffles and resets deck each time
	 * @throws InterruptedException 
	 */
	private void dealHandsUntilOpen() throws InterruptedException {
		do {
			System.out.println("got into dealhandsuntilopen");
			System.out.println("checking if null");
			System.out.println(deck==null);
			deck.shuffle();
			System.out.println("deck was shuffled");
			deck.reset();
			System.out.println("deck was reset");
			twitter.appendToCompoundTweet("Dealing hands...");
			for (int i=0; i<players.size(); i++){
				players.get(i).dealNewHand();
			}
		} while (checkOpen() == false);
	}
	
	/**
	 * Returns true if any of the players has an opening hand.
	 * Prints prompts when a player opens and when no player can open.
	 */
	private boolean checkOpen() {
		boolean openingHand = false;
		for (int i=0; i<players.size(); i++){
			if (players.get(i).hand.getGameValue() >= OPENING_HAND){
				openingHand = true;
				twitter.appendToCompoundTweet("Player "+ players.get(i).playerName + " says I can open!\n");
				System.out.println("Player "+ players.get(i).playerName + " says I can open!\n");
				break;
			}
		}
		if (!openingHand){
			twitter.appendToCompoundTweet("Nobody can open this round.");
		}
		return openingHand;
	}
	
	/**
	 * TODO: Implement antes properly when betting is implemented
	 * @return number of chips to be added to the pot
	 */
	public int collectAntes() {
		int antesTotal =0;
		for (int i=0; i<players.size(); i++){
			antesTotal += ante; // player.takeAnte(ante);
			twitter.appendToCompoundTweet(players.get(i).playerName + " paid " + ante + " chips for deal.");
		}
		return antesTotal;
	}
	
	/**
	 * Takes bets from players. Players can fold their hands here.
	 * @return Number of chips to be added to the pot
	 * TODO: Should be a nested loop for going around the table until all bets are seen or folded
	 * @throws TwitterException 
	 */
	private int takeBets() throws TwitterException {
		int totalBets =0;
		twitter.appendToCompoundTweet("## Place your bets!\n");
		//twitter.postCompoundTweet();
		System.out.println("appended to tweet");
		
		showBanks();
		
		
		boolean raisedBet = false;
		int lastRaise = 0;
		ArrayList<Integer> betRecord = new ArrayList<Integer>(); // list for keeping track of bets,bet record[i] will represent player[i]'s bet
		
		ArrayList<PokerPlayer> playersNotFolded = new ArrayList<PokerPlayer>();
		System.out.println("going into for loop");
		for (int i=0; i<players.size() ; i++){
			
			int bet = players.get(i).getBet();
			System.out.println("got bets");
			if(bet > highBet){  //should be reset after each round of betting
				if (i >0){
					raisedBet = true;
				}
				highBet = bet;
				lastRaise = i;
			}
			
			totalBets += bet;
			pot += bet;
			//pot.set(pot.get() + bet);
			
			if (i == players.size() -1 && playersNotFolded.isEmpty()){
				twitter.appendToCompoundTweet("Everyone else has folded!");
				playersNotFolded.add(players.get(i));
			}
			else if (bet == 0){
				twitter.appendToCompoundTweet(players.get(i).playerName + " folds.\n");
			}
			else{
				twitter.appendToCompoundTweet(players.get(i).playerName + " bets " + bet + "\n");
				playersNotFolded.add(players.get(i));
				betRecord.add(bet);
			}
		}
		
		twitter.postCompoundTweet();
		
		System.out.println("got through for loop in takeBets");

		twitter.appendToCompoundTweet("## Betting comes back around and \n");
		players.clear();
		players.addAll(playersNotFolded);
		twitter.appendToCompoundTweet("~~~");
		
		// If there was a raise after the first player, they can raise again
		boolean foldedFirstPlayer = false;
		if (raisedBet && playersNotFolded.size() >1){
			
			int bet = players.get(0).getBet();
			if(bet > highBet){
				highBet = bet;
				lastRaise = 0;
				twitter.appendToCompoundTweet(players.get(0).playerName + " finally bets " + bet + "\n");
				betRecord.set(0, bet);
			}
			else if (bet == 0){
				twitter.appendToCompoundTweet(players.get(0).playerName + " folds.\n");
				foldedFirstPlayer = true;
				players.remove(players.get(0));
				betRecord.remove(0);
			}
			else {
				int betDifference = highBet - betRecord.get(0);
				totalBets += betDifference;
				twitter.appendToCompoundTweet(players.get(0).playerName + " sees the bet of " + highBet + " and throws in the additional " + betDifference + " chips.\n");
			}
		}
		
		/*
		System.out.println(players);
		System.out.println("highBet = " + highBet);
		System.out.println("lastRaise = " + lastRaise);
		System.out.println("raisedBet = " + raisedBet);
		System.out.println(betRecord);
		*/
		
		// Ask all the rest of the players will they call the bet
		if (raisedBet && players.size() > 1){
			
			playersNotFolded.clear();
			int i = foldedFirstPlayer ? 0: 1;
			for (; i<players.size(); i++) {
				// TODO Change this to a bet that can only be seen 
				//System.out.println("AAAAA");
				twitter.appendToCompoundTweet(" Checking if " + players.get(i).playerName + " will see.");
				int bet = players.get(i).getBet();
				
				if ((bet >= highBet && i<lastRaise) || (lastRaise ==0 && i != lastRaise)  || (i == players.size()-1 && playersNotFolded.size() ==0)){
					int betDifference = highBet - betRecord.get(i);
					totalBets += betDifference;
					twitter.appendToCompoundTweet(players.get(i).playerName + " sees the bet of " + highBet + " and throws in the additional " + betDifference + " chips.");
					playersNotFolded.add(players.get(i));
				}
			}
			

			players.clear();
			players.addAll(playersNotFolded);
		}
		

		twitter.appendToCompoundTweet("~~~");
		
		return totalBets;
	}
	
	private void showBanks() throws TwitterException {
		for (int i=0; i< players.size(); i++){
			twitter.appendToCompoundTweet(players.get(i).playerName + " has " + players.get(i).playerPot + " chips");
		}
		twitter.postCompoundTweet();
	}
	
	/**
	 * All players discard up to three cards from their hand and re-deal themselves 
	 * and are re-dealt three from the deck
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws TwitterException 
	 */
	private void discardCards() throws InterruptedException, TwitterException, IOException {
		human.discard();
		players.set(0, human);
		for (int i=1; i<players.size(); i++){
			int discardedCount = players.get(i).hand.discard();
			twitter.appendToCompoundTweet(players.get(i).playerName + " discards " + discardedCount + "cards");
		}
		twitter.appendToCompoundTweet("\n\n## Players are redealt their cards.");
		twitter.postCompoundTweet();
	}
	
	/**
	 * Shows all hands remaining in the game
	 */
	private void showCards() {
		PokerPlayer handWinner = getHandWinner();
		
		for (int i=0; i<players.size(); i++){
			twitter.appendToCompoundTweet(players.get(i).playerName + " says ");
			players.get(i).showCards(handWinner);
		}
	}
	
	/**
	 * Determines the winner of this hand of poker.
	 * @return
	 */
	private PokerPlayer getHandWinner(){
		PokerPlayer winningPlayer = players.get(0);
		
		for(int i=1; i<players.size(); i++){
			if(players.get(i).hand.getGameValue()>winningPlayer.hand.getGameValue()){
				winningPlayer = players.get(i);
			}
		}
		return winningPlayer;
	}
	
	/**
	 * Calculates who has the highest scoring hand in the group
	 * @return an arrayList containing the winner or tied winners in a very rare case
	 */
	private ArrayList<PokerPlayer> calculateWinners() {
		ArrayList<PokerPlayer> winnersCircle = new ArrayList<PokerPlayer>();
		PokerPlayer winner = players.get(0);
		
		// Look for highest scoring hand
		for (int i=1; i<players.size(); i++){
			if (players.get(i).hand.getGameValue() > winner.hand.getGameValue()){
				winner = players.get(i);
			}
		}
		
		// Store winner
		winnersCircle.add(winner);
		players.remove(winner);
		
		// Check for very rare occurrence of a draw for a split pot
		for (int i=0; i<players.size(); i++){
			if (players.get(i).hand.getGameValue() == winner.hand.getGameValue()){
				winnersCircle.add(players.get(i));
			}
		}
		
		return winnersCircle;
	}
	
	/**
	 * Awards winner the pot and declares the amount.
	 * TODO Implement when split pot betting occurs
	 * @param winners
	 * @throws TwitterException 
	 */
	private void awardWinner(ArrayList<PokerPlayer> winners) throws TwitterException { 
		
		if (winners.size() == 1){
			twitter.postCompoundTweet(); //Make sure compound tweet is clear
			twitter.appendToCompoundTweet("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			twitter.appendToCompoundTweet(winners.get(0).playerName + " wins with a " + winners.get(0).getHandType());
			twitter.appendToCompoundTweet("## " + winners.get(0).playerName + " gets " + pot/winners.size() + " chips. ##\n");
			twitter.appendToCompoundTweet("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			twitter.postCompoundTweet();
			winners.get(0).awardChips(pot);
			pot = 0;
		}
		
		else {
			
			for (int i=0; i<winners.size(); i++){
				twitter.appendToCompoundTweet(winners.get(0).playerName + " ties with a " + winners.get(0).getHandType());
			}
			
			for (int i=0; i< winners.size(); i++){
				winners.get(i).awardChips(pot/winners.size());
				twitter.appendToCompoundTweet("## " + winners.get(0).playerName + " gets " + pot/winners.size() + " chips. ##\n");
			}
		}
	}
	
	private TwitterInteraction getTwitter(){
		return twitter;
	}
	
	/**
	 * Shows the pot to the interface
	 */
	private void displayPot(){
		twitter.appendToCompoundTweet("\nThe pot has " + pot + " chips to play for.\n");
	}
	
	/*
	 * Initialises and plays two separate instances of a hand of poker 
	 */
	public static void main(String[] args) throws InterruptedException {
		DeckOfCards deck = new DeckOfCards();
		OutputTerminal console = new OutputTerminal();
		int ante = 1;
		//TwitterFactory twitterO = new TwitterFactory();
		//TwitterStreamer twitterS = new TwitterStreamer();
		TwitterInteraction testTwitteri = new TwitterInteraction(TwitterStreamer.twitter);
		
		ArrayList<PokerPlayer> players = new ArrayList<PokerPlayer>(5);
		
		for(int i=0;i<5;i++){
			AutomatedPokerPlayer computerPlayer = new AutomatedPokerPlayer(deck, testTwitteri);
			players.add(computerPlayer);
			
			System.out.println(computerPlayer.getHandType());
		}
		
		
		// First hand of poker
		/*
		new HandOfPoker(players, ante, deck, console);
		new HandOfPoker(players, ante, deck, console);
		new HandOfPoker(players, ante, deck, console);
		new HandOfPoker(players, ante, deck, console);
		*/
		/*console.printout("\n\n\n" +
				"~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
				"\n\n\n"
				);
		
		// Second hand
		console.printout("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~");*/
	}

	public int getPot(){
		return pot;
	}
	public void setPot(int pots){
		pot = pots;
	}
}
