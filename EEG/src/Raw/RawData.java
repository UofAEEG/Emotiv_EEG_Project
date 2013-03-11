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
	
	// I assume Gabor wants us to push this matrix to SVM every time its full
	// double [sample] [sensor]
	//{[a,b,c,...],[a',b',c',...],[a",b",c",..]}
	static BufferedWriter matrixout = null;
	final static int MATRIX_SIZE = 128;
	static double[][] sensorMatrix = new double[MATRIX_SIZE][14];
	static int sample = 0;
	
	public static void main(String[] args) throws IOException  {
		
		eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	eState				= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 		= null;
		IntByReference nSamplesTaken= null;
		IntByReference contactQuality= null;
    	short composerPort			= 1726;
    	int option 					= 1;
    	int state  					= 0;
    	int numChannels             = 14;
    	int rval = 0;
    	float secs 					= 60;
    	boolean readytocollect 		= false;
    	keyPressed = false;
    	
    	String fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm'.txt'").format(new Date());
    	
    	
    	
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);
		contactQuality  = new IntByReference();

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
    	
		
    	//Setup the file for printing data to.
		try {
			out = new BufferedWriter(new FileWriter("data/" + fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	/////////////////////////////////////////////////////////////////
		// temporary
		/////////////////////////////////////////////////////////////////
		try {
			matrixout = new BufferedWriter(new FileWriter("data/Matrix_"+fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//////////////////////////////////////////////////////////////////
		
		//start the key listener
		new Listener ("EEG Key Listener");
		
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
					rval = EmoState.INSTANCE.ES_GetContactQualityFromAllChannels(eState, contactQuality, numChannels);
					
				}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
			
			
			
			if (readytocollect) 
			{
				
				//get the data from device
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null)
				{
					if (nSamplesTaken.getValue() != 0) {
						
						double[] data = new double[nSamplesTaken.getValue()];
						
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
									sensorMatrix[sample][i-3] = data[sampleIdx];
								}
								
								//Write the column data to the file
								out.write( Double.toString((data[sampleIdx])));
								out.write(" ");
							}
							
							sample++;
							// if matrix is full push to SVM
							if (sample == MATRIX_SIZE-1) {
								//push matrix to SVM
								//then recreate the matrix;
								
								for (int i=0; i<MATRIX_SIZE; i++) {
									for (int j=0; j<14; j++){
										matrixout.write(Double.toString(sensorMatrix[i][j])+" ");
									}
									matrixout.write("\n");
								}
								matrixout.write("\n\n\n\n");
								sample = 0;
							}
							
							//the contact quality columns
							//The ordering of the array is consistent with the ordering of the logical input
				    		//channels in EE_InputChannels_enum.
							
							
							/*
							 * Maybe a bug, but the API call wants in intbyreference, yet
							 * I think we should be giving it an array. this makes no sense
							for(int i = 0; i < numChannels; i++) {
								out.write(contactQuality[0]); ?????????
							}*/
							
							if (keyPressed) {
								out.write("1");
							} else {
								out.write("0");
							}
							
							for (int i = 1; i < 15 ; i++) {
							
								out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
							
							}
							
							out.newLine();
						}
					}
				}
			}
		}
		//close all connections
		cleanUp();
	
	}


	public static void cleanUp() throws IOException {
		//close all connections
		out.close();
		Edk.INSTANCE.EE_EngineDisconnect();
		Edk.INSTANCE.EE_EmoStateFree(eState);
		Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
		System.out.println("Disconnected!");
	}
}
