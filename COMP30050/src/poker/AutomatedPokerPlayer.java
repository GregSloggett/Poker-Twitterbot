package poker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;


public class AutomatedPokerPlayer extends PokerPlayer {
	private static int playerType;
	private int playerBluffProbability;
	public static final String FILE_OF_NAMES = "src/PlayerNames/AutomatedPokerPlayerNames.txt";
	public static final String COCKY_PLAYER_QUOTES = "src/PlayerQuotes/CockyPlayerRaiseQuotes.txt";
	private static TwitterInteraction twitter;
	
	OutputTerminal output = new OutputTerminal();
	
	public AutomatedPokerPlayer(DeckOfCards inputDeck, TwitterInteraction t) throws InterruptedException {
		super(inputDeck);
		playerName = getPlayerName(FILE_OF_NAMES);
		playerType = randomPokerPlayerType();
		playerBluffProbability = getBluffProbability();
		twitter = t;
		
	}
	
	public static int countLines(String f) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(f));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        return (count == 0 && !empty) ? 1 : count;
	    } finally {
	        is.close();
	    }
	}
	
	public static int getPlayerType() {
		// TODO Auto-generated method stub
		return playerType;
	}
	
	public String getCockyPlayerRaiseQuote(){
		String quote = null;
		try{
			quote = getRandomLineFromFile(COCKY_PLAYER_QUOTES);
		}
		catch (Exception e){
			quote = "I can't lose, ";
		}
		return quote;		
	}
	
	public String getRandomLineFromFile(String filename){
		String out = "";
		try{
			Random rand = new Random();
			int number_line = rand.nextInt(countLines(filename));
			@SuppressWarnings("resource")
			BufferedReader read = new BufferedReader(new FileReader(filename));
			String line;
			for(int i=0; i<=number_line; i++){
				line = read.readLine();
				if(i==number_line){
					out = line;
				}
			}
			return out;
		}
		catch(Exception e){
			return out;
		}
	}
	
	/**
	 * Retrieves a random name from the FILE_OF_NAMES for this 
	 * automated poker player.
	 * @return
	 */
	private String getPlayerName(String fileOfNames) {
		String playerName = null;
		try{
			playerName = getRandomLineFromFile(fileOfNames);
		}
		catch (Exception e){
			playerName = "Bot Player";
		}
		return playerName;
	}

	/**      
	 * Selects a random strategy for an automated poker player ranging from risky to conservative.
	 * This produces a random value between 1 & 5, where 1 is conservative and 5 is risky, 2 is 
	 * slightly conservative, 4 is slightly risky, and 3 is balanced. 
	 */
	private int randomPokerPlayerType(){		
		playerType = getRandomValue(5);
		return playerType;
	}

	/**
	 * Selects a bluff probability that affects whether a player will fold, raise, see etc.
	 */
	private int getBluffProbability(){
		playerBluffProbability = getRandomValue(100);
		return playerBluffProbability;
	}
	
	/**
	 * Produces a random integer value in the range passed in as a parameter,
	 * from one up to and including the range value.
	 */
	private int getRandomValue(int range){	
		Random rand = new Random();
		return rand.nextInt(range) + 1;
	}
	
	/**
	 * Retrieves the bet value the player wishes to bet.
	 */
	public int getBet(){
		int betValue = getBetValueCalculation();
		int callValue = getCallValueCalculation(betValue);
		boolean hasRaised = false;
		boolean bettingHasBeenRaised = false;
		
		//if nobody has bet
		if(HandOfPoker.highBet == 0){
			twitter.appendToCompoundTweet("I bet " + betValue + " to start.");
			return betValue;
		}
		//if a players betValue/callValue are both less than the highbet then fold. 
		if(betValue <= HandOfPoker.highBet && callValue < HandOfPoker.highBet){
			return fold(betValue);
		}
		//if the betValue is higher than the high bet, and this player has not previously raised, then raise.
		else if(betValue > HandOfPoker.highBet && hasRaised != true){
			hasRaised = true;
			if(bettingHasBeenRaised == true){
				return reRaise(betValue);
			}
			else{
				bettingHasBeenRaised = true;
				return raise(betValue);
			}
		}
		//this may occur if a player chooses to bluff
		else if(playerBluffProbability > 75 && hasRaised != true){
			hasRaised = true;
			if(bettingHasBeenRaised == true){
				return reRaise(HandOfPoker.highBet+betValue);
			}
			else{
				bettingHasBeenRaised = true;
				return raise(HandOfPoker.highBet+betValue);
			}
		}
		//if decides not to fold/raise/bluff then see(call) the highBet.
		else{
			return see(betValue);
		}
	}
	
	/**
	 * Multiplies the betValue of a players and by a certain value based on their playerType to
	 * find a value that they would risk calling to. For example, if the highBet is at 60 and the
	 * betValue of the players hand is at 40, a player with type 5 (risky) would call up to a value
	 * of 40 * 1.55 = 62 however a less risky player with type 2 (slightly conservative would only
	 * call up to a value of 40 * 1.25 = 50.
	 */
	private int getCallValueCalculation(int betValue) {
		float playerTypeCalculation = (float) (0.75 + (1 - ((float)1/playerType)));
		int callValue = (int) (betValue*playerTypeCalculation);
			
		return callValue;
	}


	/**
	 * Uses the HandGameValue(HGV), the PlayerType(PT) & the Pot Size(PS) to calculate
	 * a betting value for the hand: (PS * HGV) / (15 - PT)
	 */
	private int getBetValueCalculation(){
		int betCalculationValue = 15;
		int handGameValue = this.hand.getGameValue()/100000000;	
		int betValue = (int) ((playerPot*handGameValue)/(betCalculationValue-playerType));
		return betValue;
	}
	
	/**
	 * Calls the high bet if the betValue for the hand is within a small range of the high bet
	 */
	private int see(int betValue){
		betValue = HandOfPoker.highBet;
		if(playerType < 4){
			twitter.appendToCompoundTweet("I'm not confident but I'll see your " + betValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet("You're bluffing. I see your " + betValue + " chips.");			
		}
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int raise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		
		if(playerType < 4){
			twitter.appendToCompoundTweet("I raise " + raiseValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet(getCockyPlayerRaiseQuote() + " I raise the betting by " + raiseValue + " chips.");
		}
		
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int reRaise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		
		twitter.appendToCompoundTweet("I re-raise the betting by " + raiseValue + " chips.");
		return betValue;
	}

	/**
	 * Folds if the value of the hand is significantly lower than the value of the high bet.
	 */
	private int fold(int betValue){
		betValue = 0;
		
		if(playerType < 4){
			twitter.appendToCompoundTweet("My hand is weak, I fold.");
		}
		else{
			twitter.appendToCompoundTweet("No luck, I fold.");
		}
		return betValue;
	}
	
	/**
	 * Determines if the poker player won this hand of poker
	 * @param handWinner
	 * @return
	 */
	private boolean didWinThisHand(PokerPlayer handWinner){
		if (this.hand.getGameValue() == handWinner.hand.getGameValue()){
			return true;
		}
		else{
			return false;
		}
	}
	
	public boolean showCards(PokerPlayer handWinner){
		boolean wonRound = didWinThisHand(handWinner);
		
		if(playerBluffProbability > 75 && wonRound == true){
			twitter.appendToCompoundTweet("I bluffed and I won! Here is my hand: " + this.hand + "\n");
			return true;
		}
		else if(wonRound == true){
			twitter.appendToCompoundTweet("here is my winning hand: " + this.hand + "\n");
			return true;
		}
		else if(wonRound == false){
			twitter.appendToCompoundTweet("I choose not to show my hand.\n");
			return false;
		}
		else{
			twitter.appendToCompoundTweet("here is my hand: \n" + this.hand + "\n");
			return true;
		}
	}
	
	public void testAppendString(){
		twitter.appendToCompoundTweet("This is coming from AutomatedPokerPlayer Class");
	}

	public static void main(String[] args) throws InterruptedException, IOException{
				
		DeckOfCards deck = new DeckOfCards();
		TwitterInteraction t = new TwitterInteraction(TwitterStreamer.twitter);
		OutputTerminal out = new OutputTerminal();
		
		ArrayList<AutomatedPokerPlayer> players = new ArrayList<AutomatedPokerPlayer>();
		AutomatedPokerPlayer playerOne = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerTwo = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerThree = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerFour = new AutomatedPokerPlayer(deck, t);
		
		players.add(playerOne);
		players.add(playerTwo);
		players.add(playerThree);
		players.add(playerFour);
	
		for(AutomatedPokerPlayer p : players){
			out.printout(p.playerName + " " + p.playerType);
			int temp = p.getBet();
			if(temp > HandOfPoker.highBet){
				HandOfPoker.highBet = temp;
			}
		}
		
		
		
		HandOfPoker.highBet = 0;

		/*
		 * Tests betting against high bet values for a number
		 * of random hands for every player type.
		 
		for(int j=0; j<10; j++){
			
			playerOne.dealNewHand();
			deck.reset();
			deck.shuffle();
			out.printout("\n\nTESTING AGAINST A HIGH BET OF: " + HandOfPoker.highBet + "\n" + playerOne.hand + " - HAND VALUE = " + playerOne.hand.getGameValue());	
			
			for(int i=1; i<6; i++){
				playerOne.playerType = i;
				out.printout("Player Type = " + playerOne.playerType + ", therefore bet value = " + playerOne.getBet() + "\n");
			}
			
			HandOfPoker.highBet+=2;
		}
		*/
	}
}
