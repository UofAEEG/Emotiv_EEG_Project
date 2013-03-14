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
    	fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm'.txt'").format(new Date());
    	int sample = 0;
    	
    	/* Initialize */
    	final int seconds = 10;
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);
		contactQuality = new IntByReference(0);
		Matrix sensorMatrix = new Matrix(seconds);

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
			out = new BufferedWriter(new FileWriter("data/" + fileName));
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		//Initialize the key listener
		new Listener ("EEG Key Listener");
		
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
								//The millisecond column
								out.write(Integer.toString((int) (data[sampleIdx] * 1000)) + " ");
								
								//the rest of the data columns
								for (int i = 0 ; i < 25 ; i++) {
	
									Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
									
									// fill in matrix
									if (i >= 3 && i <= 16) {
										sensorMatrix.matrix[sample][i-3] = data[sampleIdx];
									}
									
									//Write the column data to the file
									out.write( Double.toString((data[sampleIdx])));
									out.write(" ");
								}
								
								sample++;
								// if matrix is full push to SVM
								if (sample == (sensorMatrix.MATRIX_SIZE*sensorMatrix.numSeconds) - 1) {
									//push matrix to SVM
									//then recreate the matrix;
									sensorMatrix.toFile();
									sample = 0;
								}
								
								//print key pressed indicator
								out.write((keyPressed)? "1" : "0");
	//							if (keyPressed) {
	//								out.write("1");
	//							} else {
	//								out.write("0");
	//							}
								 
								// print the contact quality columns
	                            //The ordering of the array is consistent with the ordering of the logical input
	                            //channels in EE_InputChannels_enum.
								for (int i = 1; i < 15 ; i++) {
								
									out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
								
								}
								//next line of the data file
								out.newLine();
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
}
