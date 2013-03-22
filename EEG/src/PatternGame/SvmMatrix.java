package PatternGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SvmMatrix {
	
	double svm[][] = null;
	
	// treat ArrayList as queue 
	ArrayList<Double> window;
	String filename;
	int T1, T2;
	int rows, cols;
	
	/**
	 * @param args
	 */
	public SvmMatrix(String filename, int t1, int t2) {
		window = new ArrayList<Double>();
		this.filename = filename;
		this.T1 = t1;
		this.T2 = t2;
		
		rows = (T1*128)-((128*T2)-1);
		cols = T2*128*14;
		
		this.svm = new double[rows][cols];
	}

	public void generateSVM() {
		BufferedReader reader = null;
		String line;
		int currentRow = 0;
		
		try {
			reader = new BufferedReader(new FileReader("MatrixData/" + filename));
			
			//for (int i=0; i<(T1*128)-127; i++) {
			for (int i=0; i<rows; i++) {
				// first run
				if (i == 0) {
					for (int j=0; j<(128*T2); j++) {
						line = reader.readLine();
						String[] tokens = line.split(" ");
						
						for (int token=0; token<tokens.length;token++) {
							window.add(Double.parseDouble(tokens[token]));
						}
					}
					
					// put xSec sample into svm
					for (int currentCol=0; currentCol<window.size(); currentCol++) {
						svm[currentRow][currentCol] = window.get(currentCol);
					}
					
					// remove first sample
					for (int j=0; j<14; j++) {
						window.remove(0);
					}
				}
				else {
					line = reader.readLine();
					String[] tokens = line.split(" ");
					
					// add the single sample to the end of the ArrayList
					for (int token=0; token<tokens.length;token++) {
						window.add(Double.parseDouble(tokens[token]));
					}
					
					//
					for (int currentCol=0; currentCol<window.size(); currentCol++) {
						svm[currentRow][currentCol] = window.get(currentCol);
					}
					
					// remove first sample
					for (int j=0; j<14; j++) {
						window.remove(0);
					}
				}

				currentRow++;
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
	}
	
	public int getArrayRow() {
		return rows;
	}
	
	public int getArrayCol() {
		return cols;
	}
	
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