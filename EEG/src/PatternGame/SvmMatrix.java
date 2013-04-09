
package PatternGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


// Records every piece of data

//public class SvmMatrix {
//	
//	double svm[][] = null;
//	
//	// treat ArrayList as queue 
//	ArrayList<Double> window;
//	String filename;
//	int T1, T2;
//	int rows, cols;
//	
//
//	public SvmMatrix(String filename, int t1, int t2) {
//		window = new ArrayList<Double>();
//		this.filename = filename;
//		this.T1 = t1;
//		this.T2 = t2;
//		
//		rows = (T1*128)-((128*T2)-1);
//		cols = T2*128*14;
//		
//		this.svm = new double[rows][cols];
//	}
//
//	public void generateSVM() {
//		BufferedReader reader = null;
//		String line;
//		int currentRow = 0;
//		
//		try {
//			reader = new BufferedReader(new FileReader(filename));
//			
//			//for (int i=0; i<(T1*128)-127; i++) {
//			for (int i=0; i<rows; i++) {
//				// first run
//				if (i == 0) {
//					for (int j=0; j<(128*T2); j++) {
//						line = reader.readLine();
//						String[] tokens = line.split(" ");
//						
//						for (int token=0; token<tokens.length;token++) {
//							window.add(Double.parseDouble(tokens[token]));
//						}
//					}
//					
//					// put xSec sample into svm
//					for (int currentCol=0; currentCol<window.size(); currentCol++) {
//						svm[currentRow][currentCol] = window.get(currentCol);
//					}
//					
//					// remove first sample
//					for (int j=0; j<14; j++) {
//						window.remove(0);
//					}
//				}
//				else {
//					line = reader.readLine();
//					String[] tokens = line.split(" ");
//					
//					// add the single sample to the end of the ArrayList
//					for (int token=0; token<tokens.length;token++) {
//						window.add(Double.parseDouble(tokens[token]));
//					}
//					
//					//
//					for (int currentCol=0; currentCol<window.size(); currentCol++) {
//						svm[currentRow][currentCol] = window.get(currentCol);
//					}
//					
//					// remove first sample
//					for (int j=0; j<14; j++) {
//						window.remove(0);
//					}
//				}
//
//				currentRow++;
//			}
//			
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//            System.exit(-1);
//		}
//	}
//	
//	public int getArrayRow() {
//		return rows;
//	}
//	
//	public int getArrayCol() {
//		return cols;
//	}
//	
//	public double[][] getMatrix() {
//		return svm;
//	}
//	
//	// prints out svm matrix for current pattern
//	public void svmout() {
//		
//		BufferedWriter writer = null;
//	
//		try {
//			writer = new BufferedWriter(new FileWriter("Svm/SVM_" + filename + ".txt"));
//			
//			for (int i=0; i<rows; i++) {
//				writer.write("row "+i+": ");
//				for (int j=0; j<cols; j++) {
//					writer.write(String.format("%.2f",svm[i][j]) + " "); 
//				}
//				writer.newLine();
//			}
//						
//			
//			writer.flush();
//			writer.close();
//		} catch (IOException e) {
//			System.err.println(e.getMessage());
//            System.exit(-1);
//		}
//	}
//}




// Records data at every 8 shifts
/*
 * Constructs a 2 dimensional svm matrix
 */
public class SvmMatrix {
	
	double svm[][] = null;
	
	// treat ArrayList as queue 
	ArrayList<Double> window;
	String filename;
	int T1, T2;
	int rows, cols;
	static int shift = 8;
	
	/**
	 * SvmMatrix Constructor
	 * Constructs a SvmMatrix by taking its output filename
	 * t1 time window, and t2 time window
	 * 
	 * @param filename 
	 * @param t1
	 * @param t2
	 */
	public SvmMatrix(String filename, int t1, int t2) {
		window = new ArrayList<Double>();
		this.filename = filename;
		this.T1 = t1;
		this.T2 = t2;
		
		rows = (int) Math.floor((T1*128 - T2*128) / shift); // how many times we can shift to the right
		cols = T2*128*14;
		
		this.svm = new double[rows][cols];
	}
	
	/**
	 * Prints an svm matrix to file
	 */
	public void generateSVM() {
		BufferedReader reader = null;
		String line;
		//int currentRow = 0;
		
		try {

			reader = new BufferedReader(new FileReader(filename));

			// put all the data in the queue
			// file contains 1280 lines?
			for ( int i = 0;  i < (T1 * 128); i++ )
			{
				line = reader.readLine();
				String[] tokens = line.split(" ");
				
				for (int token=0; token<tokens.length;token++) {
					window.add(Double.parseDouble(tokens[token]));
				}
				
			}
			// fill a row of the svmMatrix with T2 seconds of data
			for ( int currentrow = 0; currentrow < rows; currentrow++)
			{
				for ( int currentcol = 0; currentcol < cols; currentcol++)
				{
					svm[currentrow][currentcol] = window.get(currentcol);
				}
				
				// remove the values that are shifted out
				for ( int removal = 0; removal < shift; removal++ )
				{
					window.remove(0);
				}
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
	}
	
	/*
	 * returns the number of array rows 
	 */
	public int getArrayRow() {
		return rows;
	}
	
	/*
	 * returns the number of array columns
	 */
	public int getArrayCol() {
		return cols;
	}
	
	/*
	 * returns the 2 dimensional svm matrix
	 */
	public double[][] getMatrix() {
		return svm;
	}
	
	// prints out svm matrix for current pattern
	public void svmout() {
		
		BufferedWriter writer = null;
	
		try {
			writer = new BufferedWriter(new FileWriter("Svm/SVM_" + filename + ".txt"));
			
			for (int i=0; i<rows; i++) {
				writer.write("row "+i+": ");
				for (int j=0; j<cols; j++) {
					writer.write(String.format("%.2f",svm[i][j]) + " "); 
				}
				writer.newLine();
			}
						
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
	}
}
