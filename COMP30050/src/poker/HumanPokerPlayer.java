package poker;

import java.util.ArrayList;
import java.util.Scanner;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class HumanPokerPlayer extends PokerPlayer implements Runnable {
	TwitterInteraction twitter;
	public HumanPokerPlayer(DeckOfCards inputDeck, TwitterInteraction t) throws InterruptedException {
		super(inputDeck);
		twitter = t;
		// TODO Auto-generated constructor stub
	}

	public HumanPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		// TODO Auto-generated constructor stub
	}

	public int currentBet =0;
	public boolean askToDiscard = false;
	public boolean splitPot = false;
	public boolean isSplitPot() {
		return splitPot;
	}

	public void setSplitPot(boolean splitPot) {
		this.splitPot = splitPot;
	}

	



	OutputTerminal output = new OutputTerminal();

	public void discard() throws InterruptedException, TwitterException {
		String positiveResponse = "y";
		String negativeResponse = "n";
		askToDiscard = true;
		//output.printout("Do you want to replace some of your cards??\n If so tweet Y for yes or N for no");
		//String Answer = output.readInString();
		System.out.println("getting here 1");
		twitter.updateStatus("Do you want to replace some of your cards??\n If so tweet Y for yes or N for no");
		System.out.println("getting here 2");
		String Answer = twitter.waitForTweet();

		if (Answer.equalsIgnoreCase(positiveResponse)) {
			//output.printout("OK how many cards do you need to change you can discard up to 3 cards");
			twitter.updateStatus("OK how many cards do you need to change you can discard up to 3 cards");
			//int amountToDiscard = output.readInSingleInt();
			int amountToDiscard = -1;
			boolean gotNumber = false;
			while(gotNumber == false){
				try{
					amountToDiscard = Integer.parseInt(twitter.waitForTweet());
					gotNumber =true;
				}catch(NumberFormatException ex){ // handle your exception
					twitter.updateStatus("Invalid input, try again.");
				}
				if (amountToDiscard == 1) {
					//output.printout("which card do you want to discard? 1 is the first card up to 5 the rightmost card");
					twitter.updateStatus("which card do you want to discard? 1 is the first card up to 5 the rightmost card");
					//int discardedCard = output.readinMultipleInt().get(0);
					String discardedCardString = twitter.waitForTweet();
					int discardedCard = readinMultipleInt(discardedCardString).get(0);
					if (discardedCard > 0 && discardedCard <= 5) {
						this.hand.replaceCardFromDeck(discardedCard - 1);
					} else {
						//output.printout("Sorry this isnt a valid card..");
						twitter.updateStatus("Sorry this isnt a valid card..");
						this.discard();
					}

				}else if(amountToDiscard == 2 || amountToDiscard == 3 ){
					//output.printout("which cards do you want to discard? 1 is the first card up to 5 the rightmost card ");
					twitter.updateStatus("which cards do you want to discard? 1 is the first card up to 5 the rightmost card ");
					ArrayList<Integer> discardedCard = new ArrayList<Integer>();

					String discardedCardString = twitter.waitForTweet();
					//discardedCard = output.readinMultipleInt();
					discardedCard = readinMultipleInt(discardedCardString);
					if(discardedCard.size() == amountToDiscard){
						for(int i = 0; i<amountToDiscard; i++){
							this.hand.replaceCardFromDeck(discardedCard.get(i)-1);
						}
						this.hand.sort();

					}else{
						//output.printout("Sorry one of the card positions you entered is invalid");
						twitter.updateStatus("Sorry one of the card positions you entered is invalid");
						this.discard();
					}

				}else{
					//output.printout("Sorry you can only remove between 1 and 3 cards");
					twitter.updateStatus("Sorry you can only remove between 1 and 3 cards");
					this.discard();
				}
			}}else if(Answer.equalsIgnoreCase(negativeResponse)){
				//output.printout("OK lets continue...");
				twitter.updateStatus("OK lets continue...");
			}
	}

	public ArrayList<Integer> readinMultipleInt(String input){	
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for(int i=0;i<input.length();i++){
			if(Character.getNumericValue(input.charAt(i)) >0 && Character.getNumericValue(input.charAt(i)) <=5){
				numbers.add(Character.getNumericValue(input.charAt(i)));
			}
		}
		if(numbers.size() == 0){
			numbers.add(-1);
		}
		return numbers;
	}
	
	public boolean validBet(int bet){
		
		boolean validBet = false;
		
		if(bet<playerPot){
			validBet = true;
		}
		
		return validBet;
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
			if(!validBet(bet)){
				output.printout("sorry you dont have this amount to bet");
				this.openingBet();
			}
		}else if(Answer.equalsIgnoreCase(checkResponse)){
			bet =0;
		}else{
			output.printout("Sorry not a valid response");
			this.openingBet();

		}
		currentBet = bet;
		return bet;
	}

	public boolean isAskToDiscard() {
		return askToDiscard;
	}




	public void setAskToDiscard(boolean askToDiscard) {
		this.askToDiscard = askToDiscard;
	}


	public int inHandBet(){
		int bet = 0;
		String callResponse = "Call";
		String raiseResponse = "Raise";
		String FoldResponse = "Fold";
		
		if(playerPot< HandOfPoker.highBet){
			output.printout("sorry you cannot take part as the bet is larger than your pot the pot will be split here and you can win up to this amount in the hand");
			splitPot = true;
		}
		if(HandOfPoker.pot == 0){
			this.openingBet();
		}else{
			output.printout("The pot is at " + HandOfPoker.pot + " Do you want to 'call', 'raise', or 'fold', reply with any of these words to continue");
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
				if(!validBet(currentBet)){
					output.printout("Sorry you dont have the money to make this bet");
					this.inHandBet();
				}
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

	public void runApp() throws InterruptedException{

		DeckOfCards deck = new DeckOfCards();
		HumanPokerPlayer human = new HumanPokerPlayer(deck);

		System.out.println(human.hand);
		try {
			System.out.println("discarding");
			human.discard();
			System.out.println("discarded");
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(human.hand);

		human.Fold();
	}

	public static void main(String[] args) throws InterruptedException {

		DeckOfCards deck = new DeckOfCards();
		HumanPokerPlayer human = new HumanPokerPlayer(deck);

		System.out.println(human.hand);
		try {
			human.discard();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(human.hand);

		human.Fold();

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		DeckOfCards deck = new DeckOfCards();
		HumanPokerPlayer human;
		try {
			human = new HumanPokerPlayer(deck);
			System.out.println(human.hand);
			human.discard();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
