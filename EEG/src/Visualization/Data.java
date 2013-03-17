package Visualization;

import SDK.Edk;
import SDK.EdkErrorCode;
import SDK.EmoState;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.io.BufferedWriter;
import java.io.IOException;

public class Data {
	
	static boolean keyPressed;
	static Pointer eEvent;
	static Pointer eState;
	static boolean collecting;
	
	/*
	 * Uninstantiatable
	 */
	private Data() {
		super();
	}
	
	public static void getData(BufferedWriter out) throws IOException {
		
		/*Initialization*/
		eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	eState				= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 		= null;
		IntByReference nSamplesTaken= null;
    	int state  					= 0;
    	float secs 					= 60;
    	boolean readytocollect 		= false;
    	keyPressed = false;
    	collecting = true;
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);

//BEGIN PROVIDED EMOTIV CODE	
			if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				return;
			}
    	
		Pointer hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs);
//END PROVIDED EMOTIV CODE 
		
    	System.out.println("Started receiving EEG Data!");
    	
		//start the key listener
		new Listener ("EEG Key Listener");

		while (collecting) 
		{	
//BEGIN PROVIDED EMOTIV CODE	
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
					
				}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.err.println("Internal error in Emotiv Engine!");
				break;
			}
//END PROVIDED EMOTIV CODE 
			
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
								
								//Write the data columns to the file
								out.write( Double.toString((data[sampleIdx])));
								out.write(" ");
							}
							
							//write key indicator column
							out.write((keyPressed)? "1" : "0");
							
							//Print the contact quality columns to our file
							//The ordering is consistent with the ordering of the logical input
				    		//channels in EE_InputChannels_enum.
							for (int i = 1; i < 15 ; i++) {
								out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
							}
							
							//next row
							out.newLine();
						}
					}
				}
			}
		} //end while
		cleanUp();
	}


	public static void cleanUp() {
		//close all connections;
		Edk.INSTANCE.EE_EngineDisconnect();
		Edk.INSTANCE.EE_EmoStateFree(eState);
		Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
	}
}
