package PatternGame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is a wrapper for raw EEG data, which is
 * stored in the form of a matrix.
 * 
 * double [sample] [sensor]
 * {[a,b,c,...],[a',b',c',...],[a",b",c",..]}
 * 
 * @author Thomas Fung
 * @author Mark Galloway
 */
public class Matrix {

	public final int sampleRate = 128;
	public final int numSensors = 14;
	public double[][] matrix = null;
	public int matrixSize;
	
	/*
	 * Constructs a new Matrix object that holds
	 * 1 second worth of data.
	 */
	public Matrix() {
	    super();
	    this.matrixSize = sampleRate;
	    matrix = new double[matrixSize][numSensors];
	}
	
	/*
	 * Constructs a new Matrix object that holds
	 * numSeconds worth of data.
	 */
	public Matrix(int numSeconds) {
	    super();
	    this.matrixSize = sampleRate * numSeconds;
	    matrix = new double[matrixSize][numSensors];
	}

	/*
	 * Write the matrix data to a text file.
	 */
	public String toFile(String fileName, String fileSuffix) {
	    
	    BufferedWriter matrixout = null;
	    
	    try {
	        matrixout = new BufferedWriter(new FileWriter("MatrixData/" + fileName + fileSuffix + ".txt"));
	        
		    for (int i = 0; i < matrixSize; i++) {
		    	for (int j = 0; j < numSensors; j++){
		            matrixout.write(Double.toString(matrix[i][j]) + " ");
		        }
		        matrixout.newLine();
		    }
	    
		    matrixout.flush();
		    matrixout.close();
	        
	        } catch (IOException e) {
	            System.err.println(e.getMessage());
	            System.exit(-1);
	        }
	    return fileName + fileSuffix + ".txt";
    }
}
