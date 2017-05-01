package poker;

import java.io.IOException;
import java.util.ArrayList;

import twitter4j.TwitterException;

public class GameOfPoker implements Runnable{

	public String playerName = "";
	private DeckOfCards deck;
	public HumanPokerPlayer humanPlayer;
	private TwitterInteraction twitter;
	ArrayList<PokerPlayer> players = new ArrayList<PokerPlayer>(6);
	boolean playerWin = false;
	boolean playerLose = false;
	boolean continueGame = true;
	OutputTerminal a = new OutputTerminal();

	public GameOfPoker(String username, TwitterInteraction t, DeckOfCards d) throws InterruptedException{
		playerName = username;
		deck = d;
		humanPlayer = new HumanPokerPlayer(deck, t);
		twitter = t;
		players.add(humanPlayer);
		for(int i=0;i<5;i++){
			PokerPlayer computerPlayer = new AutomatedPokerPlayer(deck, twitter);
			players.add(computerPlayer);	
		}
	}

	public static final int PLAYER_POT_DEFAULT = 20;
	public static final int ROUND_NUMBER = 0;
	int ante = 1;

	//Runnable code segment
	@Override
	public void run() {
		System.out.println("getting into run");

		try {
			while(!playerWin && !playerLose && continueGame && !(Thread.currentThread().isInterrupted())){
				HandOfPoker handOfPoker = new HandOfPoker(players,ante,deck,twitter);
				
				
				ArrayList<PokerPlayer> nextRoundPlayers = new ArrayList<PokerPlayer>();
				
				for(int i=0;i<players.size();i++){
					if(!(players.get(i).playerPot<=0)){
						nextRoundPlayers.add(players.get(i));
					}
					else{
						a.printout("---Player "+players.get(i).playerName+" is out of chips, and out of the game.---");
						if(players.get(i).isHuman()){
							playerLose = true;
							a.printout("Sorry, you are out of the game. Goodbye and thanks for playing!");
						}
					}
				}
				players = nextRoundPlayers;
				
				if(players.size()==1){
					if(players.get(0).isHuman()){
						a.printout("You have beaten the bots and won the game! Congratulations!");
						a.printout("To play another game, Tweet #FOAKDeal !");
						playerWin = true;
					}
					else{
						a.printout("Hard luck, you have lost the game.");
						a.printout("You can play again by Tweeting #FOAKDeal");
						playerLose = true;
					}
				}
				
				if(TwitterStreamer.gamesOfPoker.containsKey(playerName)){
					if(TwitterStreamer.userHasQuit(playerName) == true){
						break;
					}
				}
			}
			if(playerWin || playerLose){
				TwitterStreamer.usersPlayingGames.remove(playerName);
				TwitterStreamer.gamesOfPoker.remove(playerName);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
