package PatternGame;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
/*
 * @author mgallowa
 */
public class UserEvaluation extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String fileName = null;
	private static BufferedWriter fileHandle = null;
	private static BufferedWriter statsOut = null;
	private static Sample sample = null;
	private static DataCollector dc = null;
	private static int t = 10;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	
	static String folder = "MatrixData/";
	static String trainingA = "_TrainingData_A.txt";
	static String trainingB = "_TrainingData_B.txt";
	static String trainingC = "_TrainingData_C.txt";
	
	private static ArrayList<Statistic> stats = null;
	
	public static void main(String[] args) {
		
		
		
		stats = new ArrayList<Statistic>();
		Matrix M = null; //handle for the matrices
		Matrix M1 =  null; // need another matrix for the second sample during testing
		sample = new Sample(); //handle for the sample object
		
		// file name of matrices
		String matrixFilename;
		
		//the filename is the date
		fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());
		
		trainingA = folder + fileName + trainingA;
		trainingB = folder + fileName + trainingB;
		trainingC = folder + fileName + trainingC;
		
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
		
		
		//START TRAINING
		svmModel model = new svmModel();
		model.train(svm);
		
		
		
		
		
		try {
			statsOut = new BufferedWriter(new FileWriter("Statistics/" + fileName + ".txt"));
		
			statsOut.write("Pattern A training Data:");
			statsOut.newLine();
			double[] res = model.predict(PatternDriver.prepareTest(OfflineSVMInput.readMatrix(trainingA, T1)));
			writeResults(res,statsOut);
			
			System.out.println();
			statsOut.write("Pattern b training Data:");
			res =model.predict(PatternDriver.prepareTest(OfflineSVMInput.readMatrix(trainingB, T1)));
			writeResults(res,statsOut);
			
			System.out.println();
			statsOut.write("Pattern C training Data:");
			
			
			res = model.predict(PatternDriver.prepareTest(OfflineSVMInput.readMatrix(trainingC, T1)));
			writeResults(res,statsOut);
			
			svm1 = null; svm2 = null; svm3 = null; // don't need these anymore
			// don't need svm anymore
			svm = null;
			
			int attempt = 1;
			while(true) {
				
				/*
				 * Elicit test pattern
				 */
				
				Object[] ops = {"Continue","I'm bored, exit"};
		        int val = JOptionPane.showOptionDialog(null,
		        									"Think of one of your test patterns for " + T2 + "second(s) ",
		        									null,
		        									JOptionPane.YES_NO_CANCEL_OPTION,
		        									JOptionPane.QUESTION_MESSAGE,
		        									null,
		        									ops,
		        									null);
				
				if(val == 1) {
					//exit conition
					break;
					
				}
				
				//grab the patterns
				M = elicitPattern(null, null, "_TestData_1stSecond_"+ attempt, T2, true);		
				M.toFile(fileName, "_TestData_1stSecond_"+ attempt);
				
				
				//obtain and display results for the first second
				double[] results1 = model.predict(PatternDriver.prepareTest(M));
				
				Object[] options = {" A "," B "," C "};
		        int userPattern = JOptionPane.showOptionDialog(null,
		        									"Which pattern were you thinking of?",
		        									null,
		        									JOptionPane.YES_NO_CANCEL_OPTION,
		        									JOptionPane.QUESTION_MESSAGE,
		        									null,
		        									options,
		        									null);		
				
				predictPattern(results1, userPattern);
				statsOut.newLine();
				statsOut.write("Pattern "+ attempt);
				
				writeResults(results1,statsOut);
				
				attempt++;
			}
		
		
			statsOut.newLine();
			statsOut.newLine();
			
			statsOut.write("Total number of patterns predicted: " + (attempt - 1));
			
			statsOut.newLine();
		
			statsOut.write("Overall Prediction Accuracy (correct predictions/total predictions): " + overallPredictionAccuracy());
			statsOut.newLine();
			
			statsOut.write("Average Prediction Accuracy (sum of prediction accuracy /total prediction): " + averagePredictionAccuracy());
			statsOut.newLine();
			
			statsOut.write("Average Prediction Accuracy of Pattern A (correct predictions/total predictions): " + individualPatternAccuracy(0));
			statsOut.newLine();
			
			statsOut.write("Average Prediction Accuracy of Pattern B (correct predictions/total predictions): " + individualPatternAccuracy(1));
			statsOut.newLine();
			
			statsOut.write("Average Prediction Accuracy of Pattern C (correct predictions/total predictions): " + individualPatternAccuracy(2));
			
			
			statsOut.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		
		cleanUp();
	}
	
	/*
	 * 
	 */
	static void writeResults(double[] results, BufferedWriter statsOut) {
		try {
			statsOut.newLine();
			
			statsOut.write(results[0] + " chance it was pattern A");
			statsOut.newLine();
			statsOut.write(results[1] + " chance it was pattern B");
			statsOut.newLine();
			statsOut.write(results[2] + " chance it was pattern C");
		
			statsOut.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	
	/*
	 * 
	 */
	static double individualPatternAccuracy(int pattern) {
		int count = 0;
		int correct = 0;
		
		for(Statistic s: stats) {
			if(s.predictedPattern == pattern) {
					if(s.isCorrectGuess()) {
						correct++;
					}
				count++;
			}
		}
		
		if(count == 0) {
			return 0.0;
		}
		
		return (double)correct / count;
	}
	
	
	/*
	 * 
	 */
	static double averagePredictionAccuracy() {
		
		int count = 0;
		double sum = 0.0;
		
		for(Statistic s: stats) {
			if(s.isCorrectGuess()) {
				sum += s.predictionAccuracy;
				count++;
			}
		}
		
		return sum/count;
	}
	
	/*
	 * 
	 */
	static double overallPredictionAccuracy() {
		
		int correct = 0;
		int count = 0;
		
		for(Statistic s: stats) {
			if(s.isCorrectGuess()) {
				correct++;
			}
			count++;
		}
		
		return (double)correct / count;
	}
	
	
	/*
	 * Takes the results array returned from model.predict, asks the user which pattern
	 * they were thinking of, and stores the results as statistics
	 */
	static void predictPattern(double[] results, int userPattern) {
		Double max = null;
		int index = -1;
		for(int i = 0; i < results.length; i++) {
			if(results[i] > 0.5) {
				max = results[i];
				index = i;
			}
		}

        Statistic stat;
		if(index == -1) {
			//could not determine a distinct pattern
			stat = new Statistic(-1, 0.0, userPattern);
		} else {
			stat = new Statistic(index, max, userPattern); 
		}
		stats.add(stat);
		
		Object[] labels = {"A","B","C"};
		
		if(max != null && stat.isCorrectGuess()) {
			JOptionPane.showMessageDialog(null, "I correctly predicted your pattern with " + (int)(max.doubleValue() * 100) + " accuracy", "Results", JOptionPane.PLAIN_MESSAGE);
		} else if (index >= 0) {
			JOptionPane.showMessageDialog(null, "Oops, I mispredicted your pattern to be " + labels[index] + "\n" +
												"With " + (int)(max.doubleValue() * 100) + "% accuracy", 
												"Results", JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "I was unable to identify your pattern", "Results", JOptionPane.PLAIN_MESSAGE);
		}
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
