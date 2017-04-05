package poker;

import java.util.ArrayList;
import java.util.Random;

public class HandOfPoker {
	
	final private static int OPENING_HAND = HandOfCards.ONE_PAIR_DEFAULT;
	public static int highBet = 0;

	ArrayList<PokerPlayer> players;
	int ante;
	public static int pot;
	OutputTerminal UI;
	DeckOfCards deck;
	
	
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
	 * TODO: names
	 */
	private boolean checkOpen() {
		boolean openingHand = false;
		for (int i=0; i<players.size(); i++){
			if (players.get(i).hand.getGameValue() >= OPENING_HAND){
				openingHand = true;
				UI.printout("Player "+ i + " says I can open!\n");
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
	 * TODO: Names
	 * @return number of chips to be added to the pot
	 */
	public int collectAntes() {
		int antesTotal =0;
		for (int i=0; i<players.size(); i++){
			antesTotal += ante; // player.takeAnte(ante);
			UI.printout("Player " + i + " paid " + ante + " chips for deal.");
		}
		return antesTotal;
	}
	
	/**
	 * Takes bets from players. Players can fold their hands here.
	 * @return Number of chips to be added to the pot
	 * TODO: Take bets from players and remove random
	 * TODO: Players should print their own betting prompts
	 * TODO: Should be a nested loop for going around the table until all bets are seen or folded
	 */
	private int takeBets() {
		int totalBets =0;
		UI.printout("## Place your bets!");
		for (int i=0; i<players.size(); i++){
			// End Result should be: bets += players.get(i).getBet();
			//Random rand = new Random();

			int bet = players.get(i).getBet();
			if(bet > highBet){  //should be reset after each round of betting
				highBet = bet;
			}
			
			totalBets += bet;
			UI.printout("Player " + i + " bets " + bet);
			
		}
		return totalBets;
	}
	
	/**
	 * All players discard up to three cards from their hand and re-deal themselves 
	 * and are re-dealt three from the deck
	 * TODO: Names
	 * @throws InterruptedException 
	 */
	private void discardCards() throws InterruptedException {
		for (int i=0; i<players.size(); i++){
			int discardedCount = players.get(i).hand.discard();
			UI.printout("Player " + i + " discards " + discardedCount + "cards");
		}
		UI.printout("Players are redealt their cards.");
	}
	
	/**
	 * Shows all hands remaining in the game
	 */
	private void showCards() {
		for (int i=0; i<players.size(); i++){
			UI.printout("Player " + i + " says read em' and weep!");
			UI.printout("~~ " + players.get(i).hand.toString());
		}
		
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
		
		ArrayList<PokerPlayer> players = new ArrayList<PokerPlayer>(5);
		
		for(int i=0;i<5;i++){
			PokerPlayer computerPlayer = new AutomatedPokerPlayer(deck);
			players.add(computerPlayer);			
		}
		
		// First hand of poker
		new HandOfPoker(players, ante, deck, console);
		console.printout("\n\n\n" +
				"~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~" +
				"\n\n\n"
				);
		
		// Second hand
		new HandOfPoker(players, ante, deck, console);
		console.printout("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~-----------~~~~~~~~~~~~~~~~~~~~~~~~~~~");
	}

}
