package poker;

public class HumanPokerPlayer extends PokerPlayer {

	public HumanPokerPlayer(DeckOfCards inputDeck) throws InterruptedException {
		super(inputDeck);
		// TODO Auto-generated constructor stub
	}
	
	OutputTerminal output = new OutputTerminal();
	
	public boolean Fold(){
		String positiveResponse = "y";
		String negativeResponse = "n";
		
		boolean isFold = false;
		
		output.printout("Do you want to fold??\n If so type Y for yes or N for no");
		String Answer = output.readIn();
		
		if(Answer.equalsIgnoreCase(positiveResponse)){
			isFold = true;
		}
		else if(Answer.equalsIgnoreCase(negativeResponse)){
			isFold = false;
		}
		else{
			output.printout("Sorry I didnt regcognise this response");
			this.Fold();
		}
		System.out.println(isFold);
		
		return isFold;
	}
	
	public static void main(String[] args) throws InterruptedException{
		
		DeckOfCards deck = new DeckOfCards();
		HumanPokerPlayer human = new HumanPokerPlayer(deck);
		
		human.Fold();
		
	}

}
