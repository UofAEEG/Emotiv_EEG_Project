package Raw;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RawData {
	
	static boolean keyPressed;
	static Pointer eEvent;
	static Pointer eState;
	static BufferedWriter out = null;
	static String fileName = null;
	static String patterns[] = 
			{"Ball inside head rolling left",
			"Ball inside head rolling right",
			"Ball flying out of the top of head"};
	
	public static void main(String[] args) {
		
		eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	eState				= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 		= null;
		IntByReference nSamplesTaken= null;
		IntByReference contactQuality = null;
    	short composerPort			= 1726;
    	int option 					= 1;
    	int state  					= 0;
    	int numChannels             = 14;
    	float secs 					= 60;
    	boolean readytocollect 		= false;
    	keyPressed = false;
    	fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm").format(new Date());
    	int sample = 0;
    	
    	/* Initialize */
    	final int seconds = 10;
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);
		contactQuality = new IntByReference(0);
		Matrix sensorMatrix = new Matrix(seconds);
		
		int startTime = 0;
		int curPattern = 1;
		int maxPattern = 3;
		boolean firstCheck = true;
		boolean trainSVM = false;
		int svmRepeat = 10;
		int svmRepeatIdx = 0;
		
		BufferedWriter svmout = null;
		
//BEGIN PROVIDED EMOTIV CODE
//INGORE
    	switch (option) {
		case 1:
		{
			if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				return;
			}
			break;
		}
		case 2:
		{
			System.out.println("Target IP of EmoComposer: [127.0.0.1] ");

			if (Edk.INSTANCE.EE_EngineRemoteConnect("127.0.0.1", composerPort, "Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Cannot connect to EmoComposer on [127.0.0.1]");
				return;
			}
			System.out.println("Connected to EmoComposer on [127.0.0.1]");
			break;
		}
		default:
			System.out.println("Invalid option...");
			return;
    	}
    	
		Pointer hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs);
		System.out.print("Buffer size in secs: ");
		System.out.println(secs);
    		
    	System.out.println("Start receiving EEG Data!");
//END PROVIDED EMOTIV CODE    	
		
    	/*Initialize the text file we are printing to for the visualization data*/
		try {
			out = new BufferedWriter(new FileWriter("data/" + fileName + ".txt"));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		//Initialize the key listener
		Listener listener = new Listener ("EEG Patterns Game");
		
//BEGIN PROVIDED EMOTIV CODE
//IGNORE
		while (true) 
		{	
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) 
			{
				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
						if (userID != null)
						{
							System.out.println("User added");
							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
							readytocollect = true;
						}
				}
				
				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {
					
					Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
					
					
					//get the contact quality
					EmoState.INSTANCE.ES_GetContactQualityFromAllChannels(eState, contactQuality, numChannels);
					
				}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
//END PROVIDED EMOTIV CODE			
			
			
			
			/*This is the main sensor reading loop*/
			if (readytocollect) 
			{
				
				//get the data from device
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null)
				{
					if (nSamplesTaken.getValue() != 0) {
						
						double[] data = new double[nSamplesTaken.getValue()];
						
						try {
							for (int sampleIdx=0 ; sampleIdx < nSamplesTaken.getValue() ; ++sampleIdx) {
								
								//write the millisecond time stamp
								Edk.INSTANCE.EE_DataGet(hData, 19, data, nSamplesTaken.getValue());
								
								int timeEnlapsed = (int) (data[sampleIdx] * 1000);
								
								// only execute code after 10 seconds
								if (timeEnlapsed < 10000) continue;
								else if (firstCheck){
									for (int i = 1; i < 15 ; i++) {
										if (EmoState.INSTANCE.ES_GetContactQuality(eState, i) != 4){
											listener.setLabel("Poor signal.");
											while (!keyPressed);
											System.exit(1);
										}
									}
									firstCheck = false;
								}

								if (trainSVM == false && startTime == 0 && curPattern > maxPattern)
								{
									keyPressed = false;
									//cleanUp();
									trainSVM = true;
									startTime = 0;
									curPattern = 1; // reset pattern
									sample = 0;
									sensorMatrix = null;
									continue;
									//listener.setLabel("Recording is finished. Press space to Exit.");
									//while (!keyPressed);
									//System.exit(0);
								}

								/*
								if (doneRecording) {	
									listener.setLabel("Finished recording.","", "Press Space to record.");
									while (!keyPressed){}
									keyPressed = false;
								}*/
								
								if (trainSVM == false && startTime == 0 && !keyPressed) {
									//listener.setLabel("Please relax all muscles, and clear your mind to visualize the following pattern in your head.",
									//	"Press Space to record the following pattern: "+patterns[currentPattern-1]);
									listener.setLabel("Please relax and visualize the following in your mind for 10 seconds: ", 
											patterns[curPattern-1] + "(1 of 1)", "Press Space to record.");
									SVMPrinting(data, sampleIdx, hData, nSamplesTaken, sensorMatrix, sample, false);
									continue;
								}
								
								if (trainSVM == false && startTime == 0) {
									startTime = timeEnlapsed;
								} else if (trainSVM == false && timeEnlapsed - startTime > 10000) {
									curPattern++;
									startTime = 0;
									keyPressed = false;
									//doneRecording = true;
									continue;
								}
								
								if (trainSVM == false)
									SVMPrinting(data, sampleIdx, hData, nSamplesTaken, sensorMatrix, sample, true);
								else if (trainSVM == true && sensorMatrix != null)
									SVMPrinting(data, sampleIdx, hData, nSamplesTaken, sensorMatrix, sample, true);
								else 
									SVMPrinting(data, sampleIdx, hData, nSamplesTaken, sensorMatrix, sample, false);
								
								
								if (trainSVM == true && curPattern > maxPattern) {
									// exit program
									cleanUp();
									System.exit(0);
								}
								
								if (trainSVM == true && !keyPressed) {
									listener.setLabel("Please relax and visualize the following in your mind for 1 second: ", 
											patterns[curPattern-1] + " ("+(svmRepeatIdx+1)+" of "+svmRepeat+")", "Press Space to record.");
									continue;
								}
								
								if (trainSVM == true && startTime == 0) {
									startTime = timeEnlapsed;

									sensorMatrix = new Matrix();

									continue;
								} else if (trainSVM == true && timeEnlapsed - startTime > 1000) {
									
									startTime = 0;
									keyPressed = false;
									
									svmRepeatIdx++;
									if (svmRepeatIdx >= svmRepeat) {
										svmRepeatIdx = 0;
										curPattern++;
										
										// close svm file
										svmout.flush();
										svmout.close();
										svmout = null;
									}
									
									continue;
								}
								
									if (sensorMatrix != null) sample++;
								
									if (sensorMatrix != null && sample == sensorMatrix.MATRIX_SIZE*sensorMatrix.numSeconds) {
										//push matrix to SVM
										//then recreate the matrix;
										sensorMatrix.toFile();
										sample = 0;
									}
									
							}
						} catch(IOException e) {
							System.out.println(e.getMessage());
							System.exit(-1);
						}
					}
				}
			}
		}
		//close all connections
		cleanUp();
	
	}


	public static void cleanUp() {
		//close all connections
		try {
			out.flush();
			out.close();
		
			Edk.INSTANCE.EE_EngineDisconnect();
			Edk.INSTANCE.EE_EmoStateFree(eState);
			Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
			System.out.println("Disconnected!");
			System.exit(0);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// print to file
	public static void SVMPrinting(double[] data, int sampleIdx, Pointer hData, 
									IntByReference nSamplesTaken, Matrix sensorMatrix, 
									int sample, boolean matrixRecord) throws IOException {

		out.write(Integer.toString((int) (data[sampleIdx] * 1000)) + " ");

		
		for (int i = 0 ; i < 25 ; i++) {
			
			Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
			
			if (matrixRecord && i >= 3 && i <= 16) {
				sensorMatrix.matrix[sample][i-3] = data[sampleIdx];
			}
			
			//Write the column data to the file
			out.write( Double.toString((data[sampleIdx])));
			out.write(" ");
		}
		
		out.write((keyPressed)? "1" : "0");
		
		for (int i = 1; i < 15 ; i++) {
			out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
		}
		out.newLine();
	}
}
