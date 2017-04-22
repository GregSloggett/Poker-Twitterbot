package poker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;



public class AutomatedPokerPlayer extends PokerPlayer {
	private static int playerType;
	private int playerBluffProbability;
	public static final String FILE_OF_NAMES = "src/PlayerNames/AutomatedPokerPlayerNames.txt";
	public static final int FILE_OF_NAMES_LENGTH = 157;
	private static TwitterInteraction twitter;
	
	OutputTerminal output = new OutputTerminal();
	
	public AutomatedPokerPlayer(DeckOfCards inputDeck, TwitterInteraction t) throws InterruptedException {
		super(inputDeck);
		playerName = getPlayerName();
		playerType = randomPokerPlayerType();
		playerBluffProbability = getBluffProbability();
		twitter = t;
		
	}
	
	public static int getPlayerType() {
		// TODO Auto-generated method stub
		return playerType;
	}
	
	/**
	 * Retrieves a random name from the FILE_OF_NAMES for this 
	 * automated poker player.
	 * @return
	 */
	private String getPlayerName() {
		String playerName = null;
		try{
			Random rand = new Random();
			int number_line = rand.nextInt(FILE_OF_NAMES_LENGTH);
			BufferedReader read = new BufferedReader(new FileReader(FILE_OF_NAMES));
			String line;
			for(int i=0; i<=number_line; i++){
				line = read.readLine();
				if(i==number_line){
					playerName = line;
				}
			}
			read.close();
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
			twitter.appendToCompoundTweet("I bet " + betValue + " to start off the betting.");
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
			twitter.appendToCompoundTweet("Hmm, let me see. I see your " + betValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet("I think you're bluffing. I see your " + betValue + " chips.");			
		}
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int raise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		
		if(playerType < 4){
			twitter.appendToCompoundTweet("I can win this one, I raise the betting by " + raiseValue + " chips.");
		}
		else{
			twitter.appendToCompoundTweet("I can't lose, I raise the betting by " + raiseValue + " chips.");
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
			twitter.appendToCompoundTweet("I fold.");
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
		AutomatedPokerPlayer playerOne = new AutomatedPokerPlayer(deck, t);
		OutputTerminal out = new OutputTerminal();
		
		HandOfPoker.highBet = 0;

		/*
		 * Tests betting against high bet values for a number
		 * of random hands for every player type.
		 */
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
	}
}
