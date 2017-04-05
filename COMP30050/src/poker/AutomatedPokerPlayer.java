package poker;


import java.util.Random;

public class AutomatedPokerPlayer extends PokerPlayer {
	private int playerType;
	private boolean hasPotBeenRaised = false;
	
	public AutomatedPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		playerType = randomPokerPlayerType();
	}
	
	/*      
	 * Selects a random strategy for an automated poker player ranging from risky to conservative.
	 * This produces a random value between 0 & 4, where 0 is conservative and 4 is risky, 1 is 
	 * slightly conservative, 3 is slightly risky, and 2 is balanced. 
	 */
	private int randomPokerPlayerType(){		
		Random rand = new Random();
		int playerType = rand.nextInt(5) + 1;
		
		return playerType;
	}

	/*
	 * Retrieves the bet value the player wishes to bet.
	 */
	public int getBet(){
		int betValue = 0;
		
		if(GameOfPoker.ROUND_NUMBER < 1){
			betValue = getFirstRoundBet();
		}
		else{
			betValue = getSecondRoundBet();
		}
		
		return betValue;
	}

	/*
	 * Retrieves the bet value for the first round of betting.
	 */
	private int getFirstRoundBet() {
		int betValue = getBetValueCalculation();
		
		if(HandOfPoker.highBet == 0){
			return betValue;
		}
		if(betValue < HandOfPoker.highBet-2){
			return fold(betValue);
		}
		else if(betValue >= HandOfPoker.highBet + 2 && hasPotBeenRaised  != true){
			return raise(betValue);
		}
		else{
			return see(betValue);
		}
		
	/*	System.out.print("Player Type = " + this.playerType);
		System.out.println(" |||| Hand Game Value = " + handGameValue);
		System.out.println(betValue + " betValue ");
		return betValue;	*/	
	}
	
	/*
	 * Retrieves the bet value for the second round of betting
	 * Yet to be implemented.
	 */
	private int getSecondRoundBet() {
		int betValue = 0;
		betValue = this.playerType;
		
		return betValue;		
	}
	
	/*
	 * Uses the HandGameValue(HGV), the PlayerType(PT) & the Pot Size(PS) to calculate
	 * a betting value for the hand: ((HGV * PT/2) * PS) * 1/(PS/2).
	 */
	private int getBetValueCalculation(){
		int handGameValue = this.hand.getGameValue()/100000000;
		
		int calculationOne = (handGameValue * this.playerType)/2;
		int calculationTwo = calculationOne * playerPot;
		int betValue = calculationTwo * 1/(playerPot/2);
		
		System.out.println("Hand: " + hand.toString() + hand.getGameValue() + "\nPT = " + playerType + "       HGV = " + handGameValue + "        PS = " + playerPot);
		System.out.println("bet value = "+ betValue + "            |||      highBet = " + HandOfPoker.highBet);
		
		return betValue;
	}
	
	/*
	 * Calls the high bet if the betValue for the hand is within a small range of the high bet
	 */
	private int see(int betValue){
		betValue = HandOfPoker.highBet;
		System.out.println("I call " + betValue + " chips.");
		return betValue;
	}

	/*
	 * Raises the betting if the value of the hand is greater than the high bet by 2 or more.
	 */
	private int raise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		System.out.println("I raise the betting by " + raiseValue + " chips.");
		return betValue;
	}

	/*
	 * Folds if the value of the hand is lower significantly than the value of the high bet.
	 */
	private int fold(int betValue){
		betValue = 0;
		System.out.println("I fold my hand.");
		
		return betValue;
	}
}
