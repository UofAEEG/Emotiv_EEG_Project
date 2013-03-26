package PatternGame;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
	private static int n = 10; //number of test data rounds
	private static int t = 10;
	private static int T2 = 1; //duration of test data in seconds
	private static int T1 = t * T2; //duration of training data in seconds
	private static int breakTime = 10;
	
	private static JWindow window;
	
	private static String firstTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    					  "left side of your head.\nFocus on the ball and follow its movement.\n" +
		    					  "You will continue this thought for 10 seconds.\n" +
		    					  "Click OK when you are ready to begin.";
	
	private static String secondTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    								  "right side of your head.\nFocus on the ball and follow its movement.\n" +
		    								  "You will continue this thought for 10 seconds.\n" +
		    								  "Start thinking about the thought before you click." +
		    								  "Click OK when you are ready to begin.";
	
	private static String thirdTrainingPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
		    								 "top of your head.\nFocus on the ball and follow its movement.\n" +
		    								 "You will continue this thought for 10 seconds.\n" +
		    								 "Click OK when you are ready to begin.";
	
	private static String breakText = "Good Job. Take a short break before the next pattern.\n" +
									  "Click Ok when you are ready to continue.";
	
	private static String firstTestPattern = "Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
		    								 "left side of your head.\nFocus on the ball and follow its movement.\n" +
		    								 "You will continue this thought for 1 second.\n" +
		    								 "Start thinking about the thought before you click." +
		    								 "Click OK when you are ready to begin.";
	
	private static String secondTestPattern = 
					"Imagine a spinning ball inside the middle your head. This ball is rolling to towards the " +
				    "right side of your head.\nFocus on the ball and follow its movement.\n" +
					"You will continue this thought for 1 second.\n" +
					"Start thinking about the thought before you click." +
				    "Click OK when you are ready to begin.";
	
	private static String thirdTestPattern = "Imagine a spinning ball inside the middle your head. This ball is flying up to towards the " +
		    "top of your head.\nFocus on the ball and follow its movement.\n" +
			"You will continue this thought for 10 seconds.\n" +
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
		
		// file name of matrices
		String matrixFilename;
		
		//the filename is the date
		fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());
		
		//start the data collecting thread
		DataCollector dc = new DataCollector("thread1", fileName);
		
		//DataCollector dc_separate = null;
		
		//sets up break window
		setupLoadingWindow();
		
		//wait for data to stabilize
		System.out.println("Waiting 10 seconds for signals to stabalize...");
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		//Elicit pattern A
		JOptionPane.showMessageDialog(null, firstTrainingPattern, "The first pattern", JOptionPane.PLAIN_MESSAGE);
		dc.setMatrix(T1);
		//dc_separate = new DataCollector("thread_data", fileName + "_BallRollingLeft");
		while(dc.writingMatrix.get()) {
			Thread.yield();//wait for the matrix to be written
		}
		//dc_separate = null;
		M = dc.getMatrix();
		matrixFilename = M.toFile(fileName, "BallRollingLeft");
		// svm generation test
		SvmMatrix svm1 = new SvmMatrix(matrixFilename,T1,T2);
		svm1.generateSVM();
		//Let the user take a break
		JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
		
		//Elicit pattern B
		JOptionPane.showMessageDialog(null, secondTrainingPattern,  "The second pattern", JOptionPane.PLAIN_MESSAGE);
		dc.setMatrix(T1);
		//dc_separate = new DataCollector("thread_data", fileName + "_BallRollingRight");
		while(dc.writingMatrix.get()) {
			Thread.yield();//wait for the matrix to be written
		}
		//dc_separate = null;
		M = dc.getMatrix();
		matrixFilename = M.toFile(fileName, "BallRollingRight");
		SvmMatrix svm2 = new SvmMatrix(matrixFilename,T1,T2);
		svm2.generateSVM();
		
	   //Let the user take a break
	  	JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
		
		//elicit pattern C
		JOptionPane.showMessageDialog(null, thirdTrainingPattern, "The third pattern", JOptionPane.PLAIN_MESSAGE);
		dc.setMatrix(T1);
		//dc_separate = new DataCollector("thread_data", fileName + "_BallFloatingUp");
		while(dc.writingMatrix.get()) {
			Thread.yield();//wait for the matrix to be written
		}
		//dc_separate = null;
		M = dc.getMatrix();
		matrixFilename = M.toFile(fileName, "BallFloatingUp");
		SvmMatrix svm3 = new SvmMatrix(matrixFilename,T1,T2);
		svm3.generateSVM();
		
		CombineSvmMatrix svm = new CombineSvmMatrix(svm1,svm2,svm3);
		svm1 = null; svm2 = null; svm3 = null; // don't need these anymore
		
		//start training
		svmModel Model = new svmModel();
		Model.train(svm);
		
		// only if you need to output the matrix
		svm.svmout(fileName);
		
		// don't need svm anymore
		svm = null;
		
		
		
	    //Let the user take a break
		JOptionPane.showMessageDialog(null, breakText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
		
		/*Elicit patterns n times*/
		for(int i = 1; i <= n; i++) {
			//Elicit pattern A
			JOptionPane.showMessageDialog(null, firstTestPattern, "The first pattern", JOptionPane.PLAIN_MESSAGE);
			
			dc.setMatrix(T2);
			//dc_separate = new DataCollector("thread_data", fileName + "_BallRollingLeft_"+i);
			while(dc.writingMatrix.get()) {
				Thread.yield();//wait for the matrix to be written
			}
			//dc_separate = null;                                                                            
			M = dc.getMatrix();
			M.toFile(fileName, "BallRollingLeft_" + i);
		    
			 //Let the user take a break
		  	JOptionPane.showMessageDialog(null, relaxText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
			
            window.setVisible(true);
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
            window.setVisible(false);
			
			//Elicit pattern B
			JOptionPane.showMessageDialog(null, secondTestPattern, "The second pattern", JOptionPane.PLAIN_MESSAGE);
			dc.setMatrix(T2);
			//dc_separate = new DataCollector("thread_data", fileName + "_BallRollingRight_" + i);
			while(dc.writingMatrix.get()) {
				Thread.yield();//wait for the matrix to be written
			}
			//dc_separate = null;
			M = dc.getMatrix();
			M.toFile(fileName, "BallRollingRight_" + i);
			
			//Let the user take a break
		  	JOptionPane.showMessageDialog(null, relaxText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
            window.setVisible(true);
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
            window.setVisible(false);
			
			
			//elicit pattern C
			JOptionPane.showMessageDialog(null, thirdTestPattern, "The third pattern", JOptionPane.PLAIN_MESSAGE);
			dc.setMatrix(T2);
			//dc_separate = new DataCollector("thread_data", fileName + "_BallFloatingUp_" + i);
			while(dc.writingMatrix.get()) {
				Thread.yield();//wait for the matrix to be written
			}
			//dc_separate = null;
			M = dc.getMatrix();
			M.toFile(fileName, "BallFloatingUp_" + i);
			
			JOptionPane.showMessageDialog(null, relaxText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
			window.setVisible(true);
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
            window.setVisible(false);
			
            if (i == n) {
            	JOptionPane.showMessageDialog(null, 
						"Thats it! You're done.\n" +
						"Sorry, you didn't win the prize.\nPlease play again",
						"You're done!", 
					    JOptionPane.PLAIN_MESSAGE);
            }
            
            /*
			//Let the user take a break unless they are done
		    if(i != n) {
		 	    //Let the user take a break
			  	JOptionPane.showMessageDialog(null, relaxText, "It's break time!", JOptionPane.PLAIN_MESSAGE);
		    	
		    } else {
		    	JOptionPane.showMessageDialog(null, 
						"Thats it! You're done.\n" +
						"Sorry, you didn't win the prize.\nPlease play again",
						"You're done!", 
					    JOptionPane.PLAIN_MESSAGE);
		    }*/
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
	 * Takes as input a matrix of size 128*14*T1
	 * Preforms a sliding window operation to form a test data matrix
	 * 
	 * loops through the matrix taking sample sizes of 128*14*t2
	 * there will be (n-1) samples of this size which will
	 * make 3*128*(t-1) rows of the test data matrix
	 * 
	 * Confusing, clear this up. should i take only one matric and output another, or should i take all
	 * 3 matrices and output a big one ?
	 */
	private void slidingWindow(Matrix x) {
	    
	}

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
	
}
