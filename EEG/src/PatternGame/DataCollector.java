package PatternGame;

import java.io.BufferedWriter;
import java.io.IOException;

import SDK.Edk;
import SDK.EdkErrorCode;
import SDK.EmoState;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/*
 * Initializes the connection to the Emotiv device in
 * preparation for raw data collection. The Majority of this code
 * was provided by Emotiv and modified for our needs.
 */
public class DataCollector {
	
	private Pointer eEvent;
	private Pointer eState;
	IntByReference userID = null;
	private int state;
	private float secs;
	boolean readytocollect;
	Pointer hData;
	
	/*
	 * Initializes the connection to the Emotiv device in
	 * preparation for raw data collection.
	 */
	public DataCollector() {
		super();
		this.eEvent = Edk.INSTANCE.EE_EmoEngineEventCreate();
		this.eState = Edk.INSTANCE.EE_EmoStateCreate();
		userID = new IntByReference(0);
    	state = 0;
    	secs = 60;
    	readytocollect = false;

    	if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
			System.err.println("Emotiv Engine start up failed.");
			System.exit(-1); //TODO: throw exception here?
		}
    	
    	hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs);
    	
		state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

		// New event needs to be handled
		if (state == EdkErrorCode.EDK_OK.ToInt()) {
			int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
			Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);
			// Log the EmoState if it has been updated
			if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
					if (userID != null) {
						System.out.println("User added");
						Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
						readytocollect = true;
					}
			}
			if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {
				Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
			}
			
		}
		else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
			System.err.println("Internal error in Emotiv Engine!");
			System.exit(-1); //TODO: throw exception here?
		}
	}
	
	/*
	 * Fills the argument matrix with data from the
	 * 14 sensors until full.
	 * 
	 */
	public Matrix collectData(Matrix matrix, BufferedWriter out) {
		/*Initialization*/
		int sample = 0;
		IntByReference nSamplesTaken = new IntByReference(0);

		while (sample < matrix.matrixSize) {
			
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) {
				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
						if (userID != null) {
							System.out.println("User added");
							Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
							readytocollect = true;
						}
				}
				if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {
					Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
				}
				
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.err.println("Internal error in Emotiv Engine!");
				System.exit(-1); //TODO: throw exception here?
			}
			
			if (readytocollect) {
				//get the data from device
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null)
				{
					if (nSamplesTaken.getValue() != 0) {
						double[] data = new double[nSamplesTaken.getValue()];
						
						for (int sampleIdx=0 ; sampleIdx < nSamplesTaken.getValue() && sample < matrix.matrixSize; ++sampleIdx) {
							try {
								//write the millisecond time stamp
								Edk.INSTANCE.EE_DataGet(hData, 19, data, nSamplesTaken.getValue());
								//The millisecond column
								out.write(Integer.toString((int) (data[sampleIdx] * 1000)) + " ");
								
								//loop through the the data columns
								for (int i = 0 ; i < 25 ; i++) {
									
									Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
									
									if (i >= 3 && i <= 16) {
										matrix.matrix[sample][i-3] = data[sampleIdx];
									}
									
									//Write the column data to the file
									out.write( Double.toString((data[sampleIdx])));
									out.write(" ");
								}
								//increment the sample
								sample++;
								
								//write key indicator column
								out.write("0");
								
								//Print the contact quality columns to our file
								//The ordering is consistent with the ordering of the logical input
					    		//channels in EE_InputChannels_enum.
								for (int i = 1; i < 15 ; i++) {
									out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
								}
								
								//next row
								out.newLine();
							} catch (IOException e) {
								System.err.println(e.getMessage());
								System.exit(-1);
							}
						}//END for()
					}
				}
			} //END  if(ready to collect)
		} //END while
		return matrix;
	}

	/*
	 * Shuts down the Emotiv connection
	 * Frees the eState and eEvent memory
	 */
	public void cleanUp() {
		//close all connections;
		Edk.INSTANCE.EE_EngineDisconnect();
		Edk.INSTANCE.EE_EmoStateFree(eState);
		Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
	}
}
