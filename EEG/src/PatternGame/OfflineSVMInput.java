package PatternGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * A simple class for reading in matrix data without
 * the helmet. Used primarily in the testing of
 * the SVM configurations and accuracy of predictions
 * 
 * @author Mark Galloway
 *
 */
public class OfflineSVMInput {
	
	static String trainingA = "_TrainingData_A.txt";
	static String trainingB = "_TrainingData_B.txt";
	static String trainingC = "_TrainingData_C.txt";
	
	
	/**  MODIFY HERE  **/
	
	static String folder = "MatrixData/"; //folder location may vary
	
	static String datePrefix = "2013-04-07-11-14";  //THIS IS WHERE YOU PUT THE DATE PREFIX!
	static boolean testData = true;                // SET TRUE IF YOU WANT TO PREDICT TEST DATA (we should just test training until model is better)
	
	/**  MODIFY HERE **/
	
	// These fields need to be the same as in the PatternDriver
	// when samples were taken 
	private static int n = 2; //number of test data rounds
	private static int t = 10;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	
	public static void main(String[] args) {
		
		/*
		 * build the file names;
		 */
		trainingA = folder + datePrefix + trainingA;
		trainingB = folder + datePrefix + trainingB;
		trainingC = folder + datePrefix + trainingC;
		
		SvmMatrix svm1 = new SvmMatrix(trainingA, T1, T2);
		svm1.generateSVM();
		
		SvmMatrix svm2 = new SvmMatrix(trainingB,T1,T2);
		svm2.generateSVM();
		
		SvmMatrix svm3 = new SvmMatrix(trainingC,T1,T2);
		svm3.generateSVM();
		
		CombineSvmMatrix svm = new CombineSvmMatrix(svm1,svm2,svm3);
		svm1 = null; svm2 = null; svm3 = null; // don't need these anymore
		
		//START TRAINING
		svmModel model = new svmModel();
		model.train(svm);
		
		// don't need svm anymore
		svm = null;
		
		//print the accuracies
		System.out.println();
		System.out.println("Pattern A training Data:");
		System.out.println(PatternDriver.outputresult(model.predict(PatternDriver.prepareTest(readMatrix(trainingA, T1)))));
		
		System.out.println();
		System.out.println("Pattern b training Data:");
		System.out.println(PatternDriver.outputresult(model.predict(PatternDriver.prepareTest(readMatrix(trainingB, T1)))));
		
		System.out.println();
		System.out.println("Pattern C training Data:");
		System.out.println(PatternDriver.outputresult(model.predict(PatternDriver.prepareTest(readMatrix(trainingC, T1)))));
		

		/*
		 * predict test data
		 */
		if(testData) {
			ArrayList<String> testDataFiles = new ArrayList<String>();
			for(int i = 1; i <= n; i++) {
				testDataFiles.add(folder + datePrefix + "_TestData_A_1stSecond_" + i  + ".txt");
				testDataFiles.add(folder + datePrefix + "_TestData_A_2ndSecond_" + i  + ".txt");
				
				testDataFiles.add(folder + datePrefix + "_TestData_B_1stSecond_" + i  + ".txt");
				testDataFiles.add(folder + datePrefix + "_TestData_B_2ndSecond_" + i  + ".txt");
				
				testDataFiles.add(folder + datePrefix + "_TestData_C_1stSecond_" + i  + ".txt");
				testDataFiles.add(folder + datePrefix + "_TestData_C_2ndSecond_" + i  + ".txt");
			}
			predictTestData(model, testDataFiles);
		}
		
	}

	/*
	 * loops through the file names and elicits the 
	 * prediction for each one
	 */
	static void predictTestData(svmModel model, ArrayList<String> testDataFiles) {
		for(String s: testDataFiles) {
			System.out.println();
			System.out.println(s);
			System.out.println(PatternDriver.outputresult(model.predict(PatternDriver.prepareTest(readMatrix(s, T2)))));
		}
	}
	
	/*
	 * Parses the filenames and builds matrix objects
	 */
	static Matrix readMatrix(String fileName, int T) {
		Matrix m = new Matrix(T);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			
			StringBuilder sb = new StringBuilder();
			String input;
			
			while ((input = reader.readLine()) != null) {
				sb.append(input).append(" ");
			}
			
			String[] tokens = sb.toString().trim().split("\\s+");
			
			for (int sample = 0; sample < m.matrixSize; sample++) {
				for (int sensor = 0; sensor < m.numSensors; sensor++) {
					m.matrix[sample][sensor] = Double.parseDouble(tokens[sample * m.numSensors + sensor]);
				}
			}
			
			reader.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
		
		return m;
	}
	
}
