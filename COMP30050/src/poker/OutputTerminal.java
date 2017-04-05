package poker;

import java.awt.List;
import java.util.ArrayList;
import java.util.Scanner;

public class OutputTerminal {
	
	
	
	public void printout(String Output){
		System.out.println(Output);
		
		
	}
	
	public String readInString(){
		Scanner reader = new Scanner(System.in);
		
		String input = reader.next();
		
		return input;
	}
	
	public int readInSingleInt(){
		Scanner reader = new Scanner(System.in);
		
		int input = reader.nextInt();
		
		return input;
	}

	public int[] readinMultipleInt(){
		Scanner reader = new Scanner(System.in);

		
		String input = reader.nextLine();
		String[] numberStr= input.split(" ");
		
		int[] numbers = new int[numberStr.length];
		
		for(int i=0;i<numberStr.length;i++){
			numbers[i] = Integer.parseInt(numberStr[i]);
		}
		return numbers;
	}
	
}
