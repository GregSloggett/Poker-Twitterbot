package poker;

import java.awt.List;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class OutputTerminal {
	
	public void printout(String Output){		
		System.out.println("TERMINAL##>"+Output);
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

	public ArrayList<Integer> readinMultipleInt(){
		Scanner reader = new Scanner(System.in);
				
		String input = reader.nextLine();		
		ArrayList<Integer> numbers = new ArrayList<Integer>();

		for(int i=0;i<input.length();i++){
			if(Character.getNumericValue(input.charAt(i)) >0 && Character.getNumericValue(input.charAt(i)) <=5){
				numbers.add(Character.getNumericValue(input.charAt(i)));
			}
		}
		return numbers;
	}
	
}
