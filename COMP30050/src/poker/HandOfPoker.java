package poker;

import java.util.ArrayList;

import twitter4j.TwitterFactory;

public class HandOfPoker {
	
	final private static int OPENING_HAND = HandOfCards.ONE_PAIR_DEFAULT;
	public static int highBet = 0;

	ArrayList<PokerPlayer> players;
	int ante;
	public static int pot;
	OutputTerminal UI;
	DeckOfCards deck;
	TwitterInteraction twitter;

	
	
	public HandOfPoker(ArrayList<PokerPlayer> players, int ante, DeckOfCards deck, OutputTerminal UI){
		this.players = players;
		this.ante = ante;
		this.UI = UI;
		this.deck = deck;
		
		try {
			gameLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public HandOfPoker(ArrayList<PokerPlayer> players, int ante, DeckOfCards deck, TwitterInteraction t){
		this.players = players;
		this.ante = ante;
		this.UI = UI;
		this.twitter = t;
		
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
	 */
	private void gameLoop() throws InterruptedException {
		dealHandsUntilOpen();
		pot += collectAntes();
		displayPot();
		pot += takeBets();
		highBet = 0;
		displayPot();
		// TODO: if >1 player not folded
		discardCards();
		displayPot();
		pot += takeBets();
		displayPot();
		// TODO: if >1 player not folded
		showCards();
		awardWinner(calculateWinners());
	}

	/**
	 * Deals hands until an opening hand is achieved from at least one player,
	 * shuffles and resets deck each time
	 * @throws InterruptedException 
	 */
	private void dealHandsUntilOpen() throws InterruptedException {
		do {
			deck.shuffle();
			deck.reset();
			UI.printout("Dealing hands...");
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
				UI.printout("Player "+ players.get(i).playerName + " says I can open!\n");
				break;
			}
		}
		if (!openingHand){
			UI.printout("Nobody can open this round.");
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
			UI.printout(players.get(i).playerName + " paid " + ante + " chips for deal.");
		}
		return antesTotal;
	}
	
	/**
	 * Takes bets from players. Players can fold their hands here.
	 * @return Number of chips to be added to the pot
	 * TODO: Take bets from players and remove random
	 * TODO: Should be a nested loop for going around the table until all bets are seen or folded
	 */
	private int takeBets() {
		int totalBets =0;
		UI.printout("## Place your bets!");
		
		ArrayList<PokerPlayer> playersNotFolded = new ArrayList<PokerPlayer>();
		for (int i=0; i<players.size(); i++){
			// End Result should be: bets += players.get(i).getBet();
			//Random rand = new Random();
			

			int bet = players.get(i).getBet();
			if(bet > highBet){  //should be reset after each round of betting
				highBet = bet;
			}
			
			totalBets += bet;
			if(bet == 0){
				UI.printout(players.get(i).playerName + " folds.\n");
			}
			else{
				UI.printout(players.get(i).playerName + " bets " + bet + "\n");
				playersNotFolded.add(players.get(i));
			}
		}
		players = playersNotFolded;
		return totalBets;
	}
	
	/**
	 * All players discard up to three cards from their hand and re-deal themselves 
	 * and are re-dealt three from the deck
	 * @throws InterruptedException 
	 */
	private void discardCards() throws InterruptedException {
		for (int i=0; i<players.size(); i++){
			int discardedCount = players.get(i).hand.discard();
			UI.printout(players.get(i).playerName + " discards " + discardedCount + "cards");
		}
		UI.printout("Players are redealt their cards.");
	}
	
	/**
	 * Shows all hands remaining in the game
	 */
	private void showCards() {
		PokerPlayer handWinner = getHandWinner();
		
		for (int i=0; i<players.size(); i++){
			UI.printout(players.get(i).playerName + " says ");
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
	 */
	private void awardWinner(ArrayList<PokerPlayer> winners) { 
		
		if (winners.size() == 1){
			UI.printout(winners.get(0).playerName + " wins with a " + winners.get(0).getHandType());
			UI.printout("## " + winners.get(0).playerName + " gets " + pot/winners.size() + " chips. ##\n");
			winners.get(0).awardChips(pot);
			pot = 0;
		}
		
		else {
			
			for (int i=0; i<winners.size(); i++){
				UI.printout(winners.get(0).playerName + " ties with a " + winners.get(0).getHandType());
			}
			
			for (int i=0; i< winners.size(); i++){
				winners.get(i).awardChips(pot/winners.size());
				UI.printout("## " + winners.get(0).playerName + " gets " + pot/winners.size() + " chips. ##\n");
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
		UI.printout("\nThe pot has " + pot + " chips to play for.\n");
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
			PokerPlayer computerPlayer = new AutomatedPokerPlayer(deck, testTwitteri);
			players.add(computerPlayer);			
		}
		
		// First hand of poker
		new HandOfPoker(players, ante, deck, console);
		new HandOfPoker(players, ante, deck, console);
		/*console.printout("\n\n\n" +
				"~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
				"\n\n\n"
				);
		
		// Second hand
		console.printout("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~");*/
	}

}
