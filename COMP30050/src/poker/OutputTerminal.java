package poker;

import java.util.Scanner;

public class OutputTerminal {
	
	public void printout(String Output){
		System.out.println(Output);
		
	}
	
	public String readIn(){
		Scanner reader = new Scanner(System.in);
		
		String input = reader.next();
		
		return input;
	}

}
