package poker;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import twitter4j.TwitterException;


public class AutomatedPokerPlayer extends PokerPlayer {
	private int playerType;
	private int playerBluffProbability;
	private static final String FILE_OF_NAMES = "src/PlayerNames/AutomatedPokerPlayerNames.txt";
	private static final String COCKY_PLAYER_RAISE_QUOTES = "src/PlayerQuotes/CockyPlayerRaiseQuotes.txt";
	private static final String COCKY_PLAYER_SEE_QUOTES = "src/PlayerQuotes/CockyPlayerSeeQuotes.txt";
	private static final String COCKY_PLAYER_FOLD_QUOTES = "src/PlayerQuotes/CockyPlayerFoldQuotes.txt";;
	private static final String CONSERVATIVE_PLAYER_RAISE_QUOTES = "src/PlayerQuotes/ConservativePlayerRaiseQuotes.txt";
	private static final String CONSERVATIVE_PLAYER_SEE_QUOTES = "src/PlayerQuotes/ConservativePlayerSeeQuotes.txt";
	private static final String CONSERVATIVE_PLAYER_FOLD_QUOTES = "src/PlayerQuotes/ConservativePlayerFoldQuotes.txt";
	private static final int COCKY_RAISE = 0;
	private static final int COCKY_SEE = 1;
	private static final int COCKY_FOLD = 2;
	private static final int CONSERVATIVE_RAISE = 3;
	private static final int CONSERVATIVE_SEE = 4;
	private static final int CONSERVATIVE_FOLD = 5;

	private static TwitterInteraction twitter;

	OutputTerminal output = new OutputTerminal();

	public AutomatedPokerPlayer(DeckOfCards inputDeck, TwitterInteraction t) throws InterruptedException {
		super(inputDeck);
		playerName = getPlayerName(FILE_OF_NAMES);
		playerType = randomPokerPlayerType();
		playerBluffProbability = getBluffProbability();
		twitter = t;
	}
	
	/**
	 * Counts the number of lines in a given file
	 * @param location of File 
	 * @return
	 * @throws IOException
	 */
	public static int countFileLines(String f) throws IOException {
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

	/**
	 * Returns the player type of the player in question
	 * @return
	 */
	public int getPlayerType() {
		System.out.println("PT =     " + this.playerType);
		return this.playerType;
	}

	/**
	 * Retrieves a quote for folding/raising/calling for each player type
	 * @param quoteNumber
	 * @return
	 */
	public String getPlayerQuote(int quoteNumber){
		String quote = "";
		
		if(quoteNumber == COCKY_RAISE){
			quote = getRandomLineFromFile(COCKY_PLAYER_RAISE_QUOTES);
		}
		else if(quoteNumber == COCKY_SEE){
			quote = getRandomLineFromFile(COCKY_PLAYER_SEE_QUOTES);
		}
		else if(quoteNumber == COCKY_FOLD){
			quote = getRandomLineFromFile(COCKY_PLAYER_FOLD_QUOTES);
		}
		else if(quoteNumber == CONSERVATIVE_RAISE){
			quote = getRandomLineFromFile(CONSERVATIVE_PLAYER_RAISE_QUOTES);
		}
		else if(quoteNumber == CONSERVATIVE_SEE){
			quote = getRandomLineFromFile(CONSERVATIVE_PLAYER_SEE_QUOTES);
		}
		else if(quoteNumber == CONSERVATIVE_FOLD){
			quote = getRandomLineFromFile(CONSERVATIVE_PLAYER_FOLD_QUOTES);
		}
		
		
		return quote;
	}

	public String getRandomLineFromFile(String filename){
		String out = "";
		try{
			Random rand = new Random();
			int number_line = rand.nextInt(countFileLines(filename));
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

	public int getCall(){
		int betValue = getBetValueCalculation();

		return see(betValue);
	}
	
	/**
	 * Retrieves the bet value the player wishes to bet.
	 */
	public int getBet(){
		//output.printout("high bet in ai player class = " + currentRound.highBet);
		int betValue = getBetValueCalculation();	//the value at which a player would bet up to 
		int callValue = getCallValueCalculation(betValue);   //the value at which a player would call up to, based on the bet value and player type
		boolean hasRaised = false;
		boolean bettingHasBeenRaised = false;

		int returnValue = 0;
		
		//if nobody has bet
		if(currentRound.highBet == 0){
			if(this.hand.getGameValue() < 100500000){
				twitter.appendToCompoundTweet("I check");
			}
			else{
				twitter.appendToCompoundTweet("I bet " + betValue + " to start.");
			}
			returnValue = betValue;
		}
		//if a players betValue/callValue are both less than the highbet then fold. 
		if(betValue <= currentRound.highBet && callValue < currentRound.highBet){
			returnValue = fold(betValue);
		}
		//if the betValue is higher than the high bet, and this player has not previously raised, then raise.
		else if(betValue > currentRound.highBet && hasRaised == false){
			hasRaised = true;
			
			if(bettingHasBeenRaised == true){
				returnValue = reRaise(betValue);
			}
			else{
				bettingHasBeenRaised = true;
				returnValue = raise(betValue);
			}
		}
		//this may occur if a player chooses to bluff
		else if(playerBluffProbability > 75 && hasRaised != true){
			hasRaised = true;
			if(bettingHasBeenRaised == true){
				returnValue = reRaise(currentRound.highBet+betValue);
			}
			else{
				bettingHasBeenRaised = true;
				returnValue = raise(currentRound.highBet+betValue);
			}
		}
		//if decides not to fold/raise/bluff then see(call) the highBet.
		else{
			returnValue = see(betValue);
		}
		
		this.roundOverallBet+=returnValue;
		this.subtractChips(returnValue);
		return returnValue;
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
		betValue = currentRound.highBet;
		if(playerType < 4){
			twitter.appendToCompoundTweet(getPlayerQuote(CONSERVATIVE_SEE) + "I'll see your " + betValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet(getPlayerQuote(COCKY_SEE) + "I see your " + betValue + " chips.");			
		}
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int raise(int betValue){
		int raiseValue = betValue - currentRound.highBet;

		//different types of betting to be implemented
		
		/*
		 * if a player has a strong hand there's a number of betting strategies they can use:
		 * - Slow Play = where player checks/bets low in order to tempt other players into betting
		 * - Value Bet = player believes they have the strongest hand, & wants to bet without scaring other players into folding
		 * - Over Bet = pressurize opponents into 
		 * - All in Bet = pressurize opponents into 
		 */
		
		if(playerType < 4){
			twitter.appendToCompoundTweet(getPlayerQuote(CONSERVATIVE_RAISE) + "I raise " + raiseValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet(getPlayerQuote(COCKY_RAISE) + "I raise " + raiseValue + " chips.");
		}

		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int reRaise(int betValue){
		int raiseValue = betValue - currentRound.highBet;

		twitter.appendToCompoundTweet("I re-raise the betting by " + raiseValue + " chips.");
		return betValue;
	}

	/**
	 * Folds if the value of the hand is significantly lower than the value of the high bet.
	 */
	private int fold(int betValue){
		betValue = 0;

		if(playerType < 4){
			twitter.appendToCompoundTweet(getPlayerQuote(CONSERVATIVE_FOLD));
		}
		else{
			twitter.appendToCompoundTweet(getPlayerQuote(COCKY_FOLD));
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

	public static void main(String[] args) throws InterruptedException{

		DeckOfCards deck = new DeckOfCards();
		TwitterInteraction t = new TwitterInteraction(TwitterStreamer.twitter);

		//ArrayList<AutomatedPokerPlayer> players = new ArrayList<AutomatedPokerPlayer>();
		AutomatedPokerPlayer playerOne = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerTwo = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerThree = new AutomatedPokerPlayer(deck, t);
		AutomatedPokerPlayer playerFour = new AutomatedPokerPlayer(deck, t);

		System.out.println("name: " + playerOne.playerName + " type: " + playerOne.playerType);
		System.out.println("name: " + playerTwo.playerName + " type: " + playerOne.playerType);
		System.out.println("name: " + playerThree.playerName + " type: " + playerOne.playerType);
		System.out.println("name: " + playerFour.playerName + " type: " + playerOne.playerType);
		

		
		//players.add(playerOne);
		//players.add(playerTwo);
		//players.add(playerThree);
		//players.add(playerFour);
		
				/*//Had to comment this out to change static variables to dynamic in hand
		for(int i=0; i<3; i++){
			for(AutomatedPokerPlayer p : players){
				out.printout(p.playerName);
				int temp = p.getBet();
				if(temp > HandOfPoker.highBet){
					HandOfPoker.highBet = temp;
				}
			}
		}



		HandOfPoker.highBet = 0;*/

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

	@Override
	public int discard() throws InterruptedException, TwitterException, IOException {
		int cards = hand.discard();
		return cards;
	}
}
