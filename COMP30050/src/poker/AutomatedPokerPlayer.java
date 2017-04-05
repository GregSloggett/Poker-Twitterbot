package poker;


import java.util.Random;

public class AutomatedPokerPlayer extends PokerPlayer {
	private int playerType;
	private int handGameValue = this.hand.getGameValue()/100000000;
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

	private int getFirstRoundBet() {
		
		int calculationOne = (handGameValue * this.playerType)/2;
		int calculationTwo = calculationOne*playerPot;
		int betValue = calculationTwo * 1/(playerPot/2);
		
		System.out.println("bet value = "+ betValue + "            |||      highBet = " + HandOfPoker.highBet);
		
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
	
	private int getSecondRoundBet() {
		int betValue = 0;
		betValue = this.playerType;
		
		return betValue;		
	}
	
	private int see(int betValue){
		betValue = HandOfPoker.highBet;
		System.out.println("I call " + betValue + " chips.");
		return betValue;
	}

	private int raise(int betValue){
		int raiseValue = betValue - HandOfPoker.highBet;
		System.out.println("I raise the betting by " + raiseValue + " chips.");
		return betValue;
	}

	private int fold(int betValue){
		betValue = 0;
		System.out.println("I fold my hand.");
		
		return betValue;
	}
}
