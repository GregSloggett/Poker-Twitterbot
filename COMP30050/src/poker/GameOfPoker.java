package poker;

import java.util.ArrayList;

public class GameOfPoker {

	public static void main(String[] args) throws InterruptedException {
		DeckOfCards deck = new DeckOfCards();
		OutputTerminal console = new OutputTerminal();
		int ante = 1;
		
		ArrayList<PokerPlayer> players = new ArrayList<PokerPlayer>(6);
		PokerPlayer humanPlayer = new HumanPokerPlayer(deck);
		players.add(humanPlayer);
		
		for(int i=0;i<5;i++){
			PokerPlayer computerPlayer = new AutomatedPokerPlayer(deck);
			players.add(computerPlayer);			
		}
		
		//HandOfPoker(players, ante, console)
		
		boolean playerWin = false;
		boolean playerLose = false;
		boolean continueGame = true;
		
		while(!playerWin && !playerLose && continueGame){
			
		}
		
	}

}
