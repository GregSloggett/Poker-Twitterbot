package poker;

import java.util.ArrayList;

public class HumanPokerPlayer extends PokerPlayer {

	public HumanPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		// TODO Auto-generated constructor stub
	}
	
	public int currentBet =0;
	public boolean hasBetted = false;

	OutputTerminal output = new OutputTerminal();

	public void discard() throws InterruptedException {
		String positiveResponse = "y";
		String negativeResponse = "n";

		output.printout("Do you want to replace some of your cards??\n If so tweet Y for yes or N for no");
		String Answer = output.readInString();

		if (Answer.equalsIgnoreCase(positiveResponse)) {
			output.printout("OK how many cards do you need to change you can discard up to 3 cards");
			int amountToDiscard = output.readInSingleInt();

			if (amountToDiscard == 1) {
				output.printout("which card do you want to discard? 1 is the first card up to 5 the rightmost card");
				int discardedCard = output.readinMultipleInt().get(0);
				if (discardedCard > 0 && discardedCard <= 5) {
					this.hand.replaceCardFromDeck(discardedCard - 1);
				} else {
					output.printout("Sorry this isnt a valid card..");
					this.discard();
				}

			}else if(amountToDiscard == 2 || amountToDiscard == 3 ){
				output.printout("which cards do you want to discard? 1 is the first card up to 5 the rightmost card ");
				ArrayList<Integer> discardedCard = new ArrayList<Integer>();

				discardedCard = output.readinMultipleInt();

				if(discardedCard.size() == amountToDiscard){
					for(int i = 0; i<amountToDiscard; i++){
						this.hand.replaceCardFromDeck(discardedCard.get(i)-1);
					}
					this.hand.sort();
					
				}else{
					output.printout("Sorry one of the card positions you entered is invalid");
					this.discard();
					
				}
				
			}else{
				output.printout("Sorry you can only remove between 1 and 3 cards");
				this.discard();
			}
		}else if(Answer.equalsIgnoreCase(negativeResponse)){
			output.printout("OK lets continue...");
		}
	}
	
	
	public int openingBet(){
		String betResponse = "Bet";
		String checkResponse = "Check";
		
		output.printout("Do you want to open betting? \n tweet 'Bet' to bet or 'Check' to check");
		String Answer = output.readInString();
		int bet =0;
		if (Answer.equalsIgnoreCase(betResponse)){
			output.printout("How much do you wanna bet?");
			bet = output.readinMultipleInt().get(0);
			hasBetted = true;
		}else if(Answer.equalsIgnoreCase(checkResponse)){
			bet =0;
		}else{
			output.printout("Sorry not a valid response");
			this.openingBet();
			
		}
		currentBet = bet;
		return bet;
	}
	
	public int inHandBet(){
		int bet = 0;
		String callResponse = "Call";
		String raiseResponse = "Raise";
		String FoldResponse = "Fold";
		if(HandOfPoker.pot == 0){
			this.openingBet();
		}else{
		output.printout("The pot is at " + HandOfPoker.pot + " Do you want to 'call', 'raise', or 'fold', reply with any of these words to contiue");
		String Answer = output.readInString();
		if(Answer.equalsIgnoreCase(callResponse)){
			output.printout("Ok you have called the pot at "+ HandOfPoker.highBet + "betting");
			bet = (HandOfPoker.highBet-currentBet);
		}else if(Answer.equalsIgnoreCase(raiseResponse)){
			output.printout("The pot is at " + HandOfPoker.pot + " and it will take " + (HandOfPoker.highBet - currentBet) + " to meet the current bet,"
					+ " how much do you want to raise by");
			bet = output.readinMultipleInt().get(0);
			bet = bet + (HandOfPoker.highBet - currentBet);
			currentBet = bet;
		}else if(Answer.equalsIgnoreCase(FoldResponse)){
			this.Fold();
			currentBet = 0;
		}
		else{
			output.printout("Sorry that isnt a valid response");
			this.inHandBet();
		}
		}
		
		return bet;
	}

	public boolean Fold() {
		String positiveResponse = "y";
		String negativeResponse = "n";

		boolean isFold = false;

		output.printout("Do you want to fold??\n If so tweet Y for yes or N for no");
		String Answer = output.readInString();

		if (Answer.equalsIgnoreCase(positiveResponse)) {
			isFold = true;
		} else if (Answer.equalsIgnoreCase(negativeResponse)) {
			isFold = false;
			this.inHandBet();
		} else {
			output.printout("Sorry I didnt regcognise this response");
			this.Fold();
		}
		System.out.println(isFold);

		return isFold;
	}

	public static void main(String[] args) throws InterruptedException {

		DeckOfCards deck = new DeckOfCards();
		HumanPokerPlayer human = new HumanPokerPlayer(deck);

		System.out.println(human.hand);
		human.discard();
		System.out.println(human.hand);

		human.Fold();

	}

}
