package PatternGame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class OfflineSVMInput {

	String folder = "MatrixData/";
	
	static String datePrefix = "2013-04-04-08-56";  //THIS IS WHERE YOU PUT THE DATE PREFIX!

	static String trainingA = "_TrainingData_A";
	static String trainingB = "_TrainingData_B";
	static String trainingC = "_TrainingData_C";
	
	// These fields need to be the same as in the PatternDriver
	// when samples were taken 
	private static int n = 10; //number of test data rounds
	private static int t = 2;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	
	public static void main(String[] args) {
		
		/*
		 * build the file names;
		 */
		trainingA = datePrefix + trainingA;
		trainingB = datePrefix + trainingB;
		trainingC = datePrefix + trainingC;
		
		ArrayList<String> testDataFiles = new ArrayList<String>();
		for(int i = 1; i <= n; i++) {
			testDataFiles.add("_TestData_A_1stSecond_" + i);
			testDataFiles.add("_TestData_A_2ndSecond_" + i);
			
			testDataFiles.add("_TestData_B_1stSecond_" + i);
			testDataFiles.add("_TestData_B_2ndSecond_" + i);
			
			testDataFiles.add("_TestData_C_1stSecond_" + i);
			testDataFiles.add("_TestData_C_2ndSecond_" + i);
		}
		
		SvmMatrix svm1 = new SvmMatrix(trainingA,T1,T2);
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
		
		for(String s: testDataFiles) {
			System.out.println();
			System.out.println(s);
			System.out.println(PatternDriver.outputresult(model.predict(PatternDriver.prepareTest(readMatrix(s)))));
		}
	}

	static Matrix readMatrix(String fileName) {
		Matrix m = new Matrix(T2);
		
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
