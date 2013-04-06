package PatternGame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * BUGS:
 *  This program does not record randomly selected 1 second long intervals of the break
 */

/*
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
	
	private static JWindow window;
	
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
	
	private static String breakText = "Good Job. Take a short break before the next pattern.\n" +
									  "Click Ok when you are ready to continue.";
	
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
	
	private static String relaxText = "Good Job. Press Ok and take a 10 second break.";
	
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
		
		//sets up break window
		setupLoadingWindow();
		
		//wait for data to stabilize
		System.out.println("Waiting 10 seconds for signals to stabalize...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		
		/*
		 * Elicit pattern A
		 */
		M = elicitPattern(firstTrainingPattern, "The first pattern", "_TrainingData_A", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_A");
		// svm generation test
		SvmMatrix svm1 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm1.generateSVM();
		
		
		/*
		 * Let the user take a break
		 */
		breakTime();
		
		
		/*
		 * Elicit pattern B
		 */
		M = elicitPattern(secondTrainingPattern, "The second pattern", "_TrainingData_B", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_B");
		SvmMatrix svm2 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm2.generateSVM();
	   
		
		/*
		 * Let the user take a break
		 */
		breakTime();
		
		
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
		
		// only if you need to output the matrix
//		svm.svmout(fileName);
		
		// don't need svm anymore
		svm = null;
		
		/*Elicit patterns n times*/
		for(int i = 1; i <= n; i++) {
			
			/*
			 * Elicit pattern A
			 */
			M = elicitPattern(firstTestPattern, "The first pattern", "_TestData_A_1stSecond_"+ i, T2, true);		
			M1 = grabSecondPattern("_TestData_A_2ndSecond_"+ i, T2);
			M.toFile(fileName, "_TestData_A_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_A_2ndSecond_"+ i);
			//obtain and display results
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println(outputresult(model.predict(prepareTest(M1))));
			
//REMOVE?
//			test prepareTest
//			double[] _test = prepareTest(M1);
//			System.out.println("first element: "+_test[0]+" ,last element: "+_test[_test.length-1]);
//REMOVE?

			/*
			 * Let the user take a break
			 */
			breakTime();
			
/*            window.setVisible(true);
            dc.setMatrix(breakTime);
            //dc_separate = new DataCollector("thread_data", fileName + "_Break_10sec_BallRollingLeft_" + i);
            while (dc.writingMatrix.get()) {
            	Thread.yield();
            }
            //dc_separate = null;
            M = dc.getMatrix();
            matrixFilename = M.toFile(fileName, "Break_10sec_BallRollingLeft_" + i);
            MiddleMatrixChunk mmc1 = new MiddleMatrixChunk(matrixFilename,breakTime);
            mmc1.generateChunk();
            mmc1.chunkout();
            window.setVisible(false);*/
			
			
			/*
			 * Elicit pattern B
			 */
			M = elicitPattern(secondTestPattern, "The second pattern", "_TestData_B_1stSecond_"+ i, T2, true);		
			M1 = grabSecondPattern("_TestData_B_2ndSecond_"+ i, T2);
			M.toFile(fileName, "_TestData_B_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_B_2ndSecond_"+ i);
			//obtain and display results
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println(outputresult(model.predict(prepareTest(M1))));
			
			
			/*
			 * Let the user take a break
			 */
			breakTime();
			
			
/*            window.setVisible(true);
            dc.setMatrix(breakTime);
            //dc_separate = new DataCollector("thread_data", fileName + "_Break_10sec_BallRollingRight_" + i);
            while (dc.writingMatrix.get()) {
            	Thread.yield();
            }
            //dc_separate = null;
            M = dc.getMatrix();
            matrixFilename = M.toFile(fileName, "Break_10sec_BallRollingRight_" + i);
            MiddleMatrixChunk mmc2 = new MiddleMatrixChunk(matrixFilename,breakTime);
            mmc2.generateChunk();
            mmc2.chunkout();
            window.setVisible(false);*/
			
			
			/*
			 * Elicit pattern C
			 */
			M = elicitPattern(thirdTestPattern, "The third pattern", "_TestData_C_1stSecond_"+ i, T2, true);		
			M1 = grabSecondPattern("_TestData_C_2ndSecond_"+ i, T2);
			M.toFile(fileName, "_TestData_C_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_C_2ndSecond_"+ i);
			
			//obtain and display results
			System.out.println(outputresult(model.predict(prepareTest(M))));
			System.out.println(outputresult(model.predict(prepareTest(M1))));

			
			/*
			 * Let the user take a break
			 */
			breakTime();
			
			
/*			window.setVisible(true);
            dc.setMatrix(breakTime);
            //dc_separate = new DataCollector("thread_data", fileName + "_Break_10sec_BallFloatingUp_" + i);
            while (dc.writingMatrix.get()) {
            	Thread.yield();
            }
            //dc_separate = null;
            M = dc.getMatrix();
            matrixFilename = M.toFile(fileName, "Break_10sec_BallFloatingUp_" + i);
            MiddleMatrixChunk mmc3 = new MiddleMatrixChunk(matrixFilename,breakTime);
            mmc3.generateChunk();
            mmc3.chunkout();
            window.setVisible(false);*/
			
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
	 * Displays the break time dialog
	 */
	private static void breakTime() {
		JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
	}
	
	/*
	 * Elicits a pattern from the data collector. Returns the matrix data
	 */
	private static Matrix elicitPattern(String message, String title, String fileSuffix, int length, boolean wait) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
		
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
	 * TODO: This is a cutout of elicitPatternString... 
	 * Should probably refactor the above to handle this case.
	 */
	private static Matrix grabSecondPattern(String fileSuffix, int length) {
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
	
	/* TODO
	 * Nobody knows what I do because my creator cant comment
	 */
	private static void setupLoadingWindow() {
		window = new JWindow();
	  	JPanel pan = new JPanel();
	  	pan.setBorder(new LineBorder(Color.BLACK));
	  	pan.setLayout(new GridLayout(1,1));
	  	pan.add(new JLabel("Please rest for 10 seconds..."));
	  	
	  	window.getContentPane().add(pan,"Center");
	  	window.setSize(200,100);
	  	window.setLocationRelativeTo(null);
	}
	
	/* TODO
	 * Nobody knows what I do because my creator cant comment
	 */
	static double[] prepareTest(Matrix input){
		// convert the matrix into a sample
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
	
	/* TODO
	 * Nobody knows what I do because my creator cant comment
	 */
	static String outputresult(double [] results)
	{
		// output the results of SVM's estimation
		return "\n" + results[0] + "chance it was pattern A\n" + results[1] +
				"chance it was pattern B\n" + results[2] + "chance it was pattern C";
		
	}
	
}
