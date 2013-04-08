package PatternGame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class UserEvaluation extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String fileName = null;
	private static BufferedWriter fileHandle = null;
	private static Sample sample = null;
	private static DataCollector dc = null;
	private static int t = 10;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	
	
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
		M = elicitPattern("Think of a distinct training pattern for " + t + " seconds \n" +
					      "This pattern will referenced later as Pattern A", "Pattern A", "_TrainingData_A", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_A");
		// svm generation test
		SvmMatrix svm1 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm1.generateSVM();
		
		
		/*
		 * Elicit pattern B
		 */
		M = elicitPattern("Think of a distinct training pattern for " + t + " seconds \n" +
			      "This pattern will referenced later as Pattern B", "Pattern B", "_TrainingData_B", T1, false);
		matrixFilename = M.toFile(fileName, "_TrainingData_B");
		SvmMatrix svm2 = new SvmMatrix("MatrixData/" + matrixFilename,T1,T2);
		svm2.generateSVM();
		
		
		/*
		 * elicit pattern C
		 */
	  	M = elicitPattern("Think of a distinct training pattern for " + t + " seconds \n" +
			      "This pattern will referenced later as Pattern C", "Pattern C", "_TrainingData_C", T1,false);
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
		
		int i = 1;
		while(true) {
			
			/*
			 * Elicit test pattern
			 */
			//TODO custom button text: begin, exit
			int val = JOptionPane.showConfirmDialog(null, "Think of one of your test patterns for " + T2 + "second(s) ", null, JOptionPane.YES_NO_OPTION);
			
			if(val == 1) {
			    //exit condition
				//TODO: print statistics before exit
				cleanUp();
			}
			
			//grab the patterns
			M = elicitPattern(null, null, "_TestData_1stSecond_"+ i, T2, true);		
			M1 = elicitPattern(null, null, "_TestData_2ndSecond_"+ i, T2, false);
			M.toFile(fileName, "_TestData_1stSecond_"+ i);
			M1.toFile(fileName, "_TestData_2ndSecond_"+ i);
			
			
			//obtain and display results for the first second
			double[] results1 = model.predict(PatternDriver.prepareTest(M));
			predictPattern(results1);
			System.out.println("_TestData_1stSecond_"+ i);
			System.out.println(PatternDriver.outputresult(results1));
			
			
			//obtain and display results for the second second
			double[] results2 = model.predict(PatternDriver.prepareTest(M1));
			predictPattern(results2);
			System.out.println("_TestData_2ndSecond_"+ i);
			System.out.println(PatternDriver.outputresult(results2));
			
			i++;
		}

	}
	
	/*
	 * 
	 */
	static void predictPattern(double[] results) {
		Double max = null;
		int index = -1;
		for(int i = 0; i < results.length; i++) {
			if(results[i] > 0.5) {
				max = results[i];
				index = i;
			}
		}
		
		if(index == -1) {
			//could not determine a distinct pattern
			
			
		} else {
			
			//svm found a strong match. 
		}
		
		
		//TODO: ask user which pattern they were thinking.
		
		//record into statistics correct/incorrect and what pattern
		
	}
	
	
	/*
	 * The program shutdown process, closes down the
	 * data collector and waits for the thread to rejoin
	 */
	static void cleanUp() {
		dc.collecting = false;
		try {
			dc.join();
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		System.out.println("Output files are prefixed with the date " + fileName);
		System.out.println("Exiting");
		System.exit(0);
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

}
