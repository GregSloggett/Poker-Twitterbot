package poker;


import java.util.Random;

public class AutomatedPokerPlayer extends PokerPlayer {
	private int playerType;
	private int playerBluffProbability;
	private boolean hasRaised = false;
	private int betCalculationValue = 15;
	
	OutputTerminal output = new OutputTerminal();
	
	public AutomatedPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		playerType = randomPokerPlayerType();
		playerBluffProbability = getBluffProbability();
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
		boolean roundRaised = false;
		if(HandOfPoker.highBet == 0){
			return betValue;
		}
		if(betValue < HandOfPoker.highBet-playerType){
			return fold(betValue);
		}
		else if(betValue >= HandOfPoker.highBet + 2 && hasRaised  != true){
			hasRaised = true;
			if(roundRaised == true){
				return reRaise(betValue);
			}
			else{
				roundRaised = true;
				return raise(betValue);
			}
		}
		else if(playerBluffProbability > 75){
			hasRaised = true;
			if(roundRaised == true){
				return reRaise(HandOfPoker.highBet+betValue);
			}
			else{
				roundRaised = true;
				return raise(HandOfPoker.highBet+betValue);
			}
		}
		else{
			return see(betValue);
		}
	}
	
	/**
	 * Uses the HandGameValue(HGV), the PlayerType(PT) & the Pot Size(PS) to calculate
	 * a betting value for the hand: (PT * HGV) / (15 - PT)
	 */
	private int getBetValueCalculation(){
		int handGameValue = this.hand.getGameValue()/100000000;
		
		int betValue = (int) ((playerPot*handGameValue)/(betCalculationValue-playerType));
		
		
		 /* 
		  * uncomment to show stats behind bet value
		  */
	/*
		if(playerBluffProbability > 75){
			output.printout("I bluffed");
		}
		output.printout("Hand: " + hand.toString() + hand.getGameValue() + "\nPT = " + playerType + "       HGV = " + handGameValue + "        PS = " + playerPot);
		output.printout("bet value = "+ betValue + "            |||      highBet = " + HandOfPoker.highBet);
	*/
		
		return betValue;
	}
	
	/**
	 * Calls the high bet if the betValue for the hand is within a small range of the high bet
	 */
	private int see(int betValue){
		betValue = HandOfPoker.highBet;
		if(playerType < 4){
			output.printout("Hmm, let me see. I see your " + betValue + " chips.");
		}
		else{
			output.printout("I think you're bluffing. I see your " + betValue + " chips.");			
		}
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int raise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		
		if(playerType < 4){
			output.printout("I think I can win this one, "
					+ "I raise the betting by " + raiseValue + " chips.");
		}
		else{
			output.printout("I can't lose, I raise the betting by " + raiseValue + " chips.");
		}
		return betValue;
	}

	/**
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int reRaise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		
		output.printout("I re-raise the betting by " + raiseValue + " chips.");
		return betValue;
	}

	/**
	 * Folds if the value of the hand is significantly lower than the value of the high bet.
	 */
	private int fold(int betValue){
		betValue = 0;
		
		if(playerType < 4){
			output.printout("My hand is weak, I fold.");
		}
		else{
			output.printout("I fold.");
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
			output.printout("I bluffed and I won! Here is my hand: " + this.hand + "\n");
			return true;
		}
		else if(wonRound == true){
			output.printout("here is my winning hand: " + this.hand + "\n");
			return true;
		}
		else if(wonRound == false){
			output.printout("I choose not to show my hand.\n");
			return false;
		}
		else{
			output.printout("here is my hand: \n" + this.hand + "\n");
			return true;
		}
	}
}
