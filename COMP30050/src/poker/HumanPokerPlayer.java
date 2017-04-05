package poker;

import java.util.ArrayList;

public class HumanPokerPlayer extends PokerPlayer {

	public HumanPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		// TODO Auto-generated constructor stub
	}

	
	
	OutputTerminal output = new OutputTerminal();
	
	

	public void discard() throws InterruptedException {
		String positiveResponse = "y";
		String negativeResponse = "n";
		

		output.printout("Do you want to replace some of your cards??\n If so type Y for yes or N for no");
		String Answer = output.readInString();

		if (Answer.equalsIgnoreCase(positiveResponse)) {
			output.printout("OK how many cards do you need to change you can discard up to 3 cards");
			int amountToDiscard = output.readInSingleInt();

			if (amountToDiscard == 1) {
				output.printout("which card do you want to discard? 1 is the first card up to 5 the rightmost card");
				int discardedCard = output.readInSingleInt();
				if (discardedCard > 0 && discardedCard <= 5) {
					this.hand.replaceCardFromDeck(discardedCard - 1);
				} else {
					output.printout("Sorry this isnt a valid card..");
					this.discard();
				}

			}else if(amountToDiscard == 2 || amountToDiscard == 3 ){
				output.printout("which cards do you want to discard? 1 is the first card up to 5 the rightmost card ");
				ArrayList<Integer> discardedCard = new ArrayList<Integer>();
				int cardcheck = 0;
				int[] discarded = new int[amountToDiscard];
				for(int i =0; i<amountToDiscard;i++){
					discarded[i] = output.readInSingleInt();
					if(discarded[i] >0 && discarded[i] <=5){
						cardcheck+=1;
					}
				}
				if(cardcheck == amountToDiscard){
					for(int i = 0; i<amountToDiscard; i++){
						this.hand.replaceCardFromDeck(discarded[i]-1);
					}
					
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

	public boolean Fold() {
		String positiveResponse = "y";
		String negativeResponse = "n";

		boolean isFold = false;

		output.printout("Do you want to fold??\n If so type Y for yes or N for no");
		String Answer = output.readInString();

		if (Answer.equalsIgnoreCase(positiveResponse)) {
			isFold = true;
		} else if (Answer.equalsIgnoreCase(negativeResponse)) {
			isFold = false;
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
