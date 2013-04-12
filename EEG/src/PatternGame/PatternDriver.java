package PatternGame;

import javax.swing.JOptionPane;
import javax.swing.JFrame;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * The pattern game
 * 
 * 
 * Turn on the helmet, start the program.
 * Wait 10 sec for patterns to settle down.
 * Check the Connection Quality (abort the program if not all connections are perfect with an error message, don't waste time on any UI here)
 *
 * Repeat for <p> = {A, B, C}
 *   ask user to think pattern <p>, record the data into a Matrix object for 10s. Name it file_<p>.txt
 *
 * #train SVM
 *
 * Repeat for <i> = 1...N  (N = 10)
 *   repeat pattern <p> = {A, R1, B, R2, C, R3}
 *       if <p> == R1 or R2 or R3
 *           ask the user to take a break for a couple seconds, record a randomly selected 1 seconds long interval of the break into file_<i>_<R?>.txt
 *       else
 *           ask user to think <p> for 1 sec. record data into file_<i>_<p>.txt
 *       end if
 *       #SVM_predict
 *       # record SVM predict
 *
 *
 *  @author Mark Galloway
 *  @author tfung - helped out with parts
 *  @author bmhayduk - helped out with parts
 *  @author bing - modified for svm
 *  
 */
public class PatternDriver extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static String fileName = null;
	private static BufferedWriter fileHandle = null;
	private static Sample sample = null;
	private static DataCollector dc = null;
	private static int n = 10; //number of test data rounds
	private static int t = 10;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	
	private static String firstTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    					  "left side of your head.\nFocus on the ball and follow its movement.\n" +
		    					  "You will continue this thought for "+ PatternDriver.T1 +" seconds.\n" +
		    					  "Click OK when you are ready to begin.";
	
	private static String secondTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    								  "right side of your head.\nFocus on the ball and follow its movement.\n" +
		    								  "You will continue this thought for "+ PatternDriver.T1 +" seconds.\n" +
		    								  "Start thinking about the thought before you click." +
		    								  "Click OK when you are ready to begin.";
	
	private static String thirdTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
		    								 "top of your head.\nFocus on the ball and follow its movement.\n" +
		    								 "You will continue this thought for "+ PatternDriver.T1 +" seconds.\n" +
		    								 "Click OK when you are ready to begin.";
	
	private static String firstTestPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    								 "left side of your head.\nFocus on the ball and follow its movement.\n" +
		    								 "You will continue this thought for "+ PatternDriver.T2 +" second.\n" +
		    								 "Start thinking about the thought before you click." +
		    								 "Click OK when you are ready to begin.";
	
	private static String secondTestPattern = 
					"Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
				    "right side of your head.\nFocus on the ball and follow its movement.\n" +
					"You will continue this thought for "+ PatternDriver.T2 +" second.\n" +
					"Start thinking about the thought before you click." +
				    "Click OK when you are ready to begin.";
	
	private static String thirdTestPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
		    "top of your head.\nFocus on the ball and follow its movement.\n" +
			"You will continue this thought for "+ PatternDriver.T2 +" seconds.\n" +
		    "Click OK when you are ready to begin.";
		
	/*
	 * May not be instanced
	 */
	private PatternDriver() {
		
	}
	
	/*
	 * The main flow for the Pattern Game
	 */
	public static void main(String[] args) {
	
		Matrix M = null; //handle for the matrices
		Matrix M1 =  null; // need another matrix for the second sample during testing
		sample = new Sample(); //handle for the sample object
		
		// file name of matrices
		String matrixFilename;
		
		//the filename is the date
		fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());
		
		//start the data collecting thread
		dc = new DataCollector("thread1", sample);

		
		/*
		 * Elicit pattern A
		 */
		M = elicitPattern(firstTrainingPattern, "The first pattern", "_TrainingData_A", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_A");
		// svm generation test
		SvmMatrix svm1 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm1.generateSVM();
		
		
		/*
		 * Elicit pattern B
		 */
		M = elicitPattern(secondTrainingPattern, "The second pattern", "_TrainingData_B", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_B");
		SvmMatrix svm2 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm2.generateSVM();
	   
		
		
		/*
		 * elicit pattern C
		 */
	  	M = elicitPattern(thirdTrainingPattern, "The third pattern", "_TrainingData_C", T1,false);
	  	matrixFilename = M.toFile(fileName, "_TrainingData_C");
		SvmMatrix svm3 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm3.generateSVM();
		
		CombineSvmMatrix svm = new CombineSvmMatrix(svm1,svm2,svm3);
		svm1 = null; svm2 = null; svm3 = null; // don't need these anymore
		
		//START TRAINING
		svmModel model = new svmModel();
		model.train(svm);
		
		// don't need svm anymore
		svm = null;
		
		/*Elicit patterns n times*/
		for(int i = 1; i <= n; i++) {
			
			/*
			 * Elicit pattern A
			 */
			M = elicitPattern(firstTestPattern, "The first pattern", "_TestData_A_1stSecond_"+ i, T2, true);	
			M1 =elicitPattern(null, null,"_TestData_A_2ndSecond_"+ i, T2, false);
			M.toFile(fileName, "_TestData_A_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_A_2ndSecond_"+ i);
			
			//obtain and display results
			System.out.println("_TestData_A_1stSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println("_TestData_A_2ndSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M1))));
			
			
			/*
			 * Elicit pattern B
			 */
			M = elicitPattern(secondTestPattern, "The second pattern", "_TestData_B_1stSecond_"+ i, T2, true);	
			M1 =elicitPattern(null, null, "_TestData_B_2ndSecond_"+ i, T2, false);
			M.toFile(fileName, "_TestData_B_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_B_2ndSecond_"+ i);
			
			//obtain and display results
			System.out.println("_TestData_B_1stSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println("_TestData_B_2ndSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M1))));
			
			
			/*
			 * Elicit pattern C
			 */
			M = elicitPattern(thirdTestPattern, "The third pattern", "_TestData_C_1stSecond_"+ i, T2, true);	
			M1 =elicitPattern(null, null, "_TestData_C_2ndSecond_"+ i, T2, false);
			M.toFile(fileName, "_TestData_C_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_C_2ndSecond_"+ i);
			
			//obtain and display results
			System.out.println("_TestData_C_1stSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println("_TestData_C_2ndSecond_"+ i);
			System.out.println(outputresult(model.predict(prepareTest(M1))));

			
            if (i == n) {
            	JOptionPane.showMessageDialog(null, 
						"Thats it! You're done.\n" +
						"Sorry, you didn't win the prize.\nPlease play again",
						"You're done!", 
					    JOptionPane.PLAIN_MESSAGE);
            }
            
		} //END for()
		
		
		//stop the thread and wait for it to exit
		dc.collecting = false;
		try {
			dc.join();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println("Output files are prefixed with the date " + fileName);
		System.out.println("Exiting");
	}
	
	
	/*
	 * Elicits a pattern from the data collector. Returns the matrix data
	 */
	static Matrix elicitPattern(String message, String title, String fileSuffix, int length, boolean wait) {
		
		if(title != null) {
			//show the JOptionPane Dialog
			JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
		}
		
		if(wait) {
			// wait 1 second before recording the actual data
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}
		
		Matrix m = null;
		/*Initialize the text file we are printing to for the visualization data*/
		try {
			fileHandle = new BufferedWriter(new FileWriter("VisualizationData/" + fileName + fileSuffix + ".txt"));
		
			sample.requestSample(fileHandle, length, dc);
			//This will block until the sample is ready
			m = sample.getSample();
			fileHandle.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		
		return m;
	}
	
	
	/* 
	 * convert the matrix into a sample for the svm
	 */
	static double[] prepareTest(Matrix input){
		double [] test = new double [128 * 14];
		for ( int i = 0; i < 128;  i++)
		{
			for ( int s = 0; s < 14; s++)
			{
				test[i * 14 + s] = input.matrix[i][s];
			}
		}
		return test;
		
	}
	
	/* 
	 * Builds an output string of the results of 
	 * SVM's estimation with is passed in as the argument
	 * results
	 */
	static String outputresult(double [] results)
	{
		return results[0] + " chance it was pattern A\n" + 
			   results[1] + " chance it was pattern B\n" + 
			   results[2] + " chance it was pattern C\n";
		
	}
	
}
