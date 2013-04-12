package PatternGame;

import java.io.BufferedWriter;
import java.io.IOException;

import SDK.Edk;
import SDK.EdkErrorCode;
import SDK.EmoState;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Initializes the connection to the Emotiv device in
 * preparation for raw data collection. Some of this code
 * was provided by Emotiv and modified for our needs.
 * 
 * @author Mark Galloway
 */
public class DataCollector extends Thread {
	
	private Pointer eState = null;
	private Pointer eEvent = null;
	private BufferedWriter out = null;
	public boolean collecting;
	public boolean writingMatrix;
	private Matrix matrix;
	private int sampleNo;
	private Sample sample = null;
	
	/*
	 * Initializes and starts the thread of execution
	 */
	public DataCollector(String threadName, Sample sample) {
		super(threadName);
		this.sample = sample;
		start();
	}
	
	/*
	 * The threads main flow of execution. Initializes the Emotiv device and
	 * then reads data from the sensors. This data is constantly written to the
	 * file specified by fileName. Also, writes sensor data to the Matrix object
	 * matrix when set to do so. 
	 * 
	 */
	public void run() {
		/*Initialization*/
		eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	eState				= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 		= null;
		IntByReference nSamplesTaken= null;
    	int state  					= 0;
    	float secs 					= 60;
    	boolean readytocollect 		= false;
    	collecting = true;
    	writingMatrix = false;
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);
	
		if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
			System.err.println("Emotiv Engine start up failed.");
			return;
		}
    	
		Pointer hData = Edk.INSTANCE.EE_DataCreate();
		Edk.INSTANCE.EE_DataSetBufferSizeInSec(secs); 
		
    	System.out.println("Started receiving EEG Data!");
    	
    	
		while (collecting) {	
			
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
				break;
			}
			
			if (readytocollect) {
				//get the data from device
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);
				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null) {
					if (nSamplesTaken.getValue() != 0) {
						
						double[] data = new double[nSamplesTaken.getValue()];
						breakpoint:
						if(writingMatrix) {
							
							for (int sampleIdx=0 ; sampleIdx < nSamplesTaken.getValue(); ++sampleIdx) {
								try {
									//write the millisecond time stamp
									Edk.INSTANCE.EE_DataGet(hData, 19, data, nSamplesTaken.getValue());
									//The millisecond column
									out.write(Integer.toString((int) (data[sampleIdx] * 1000)) + " ");
									
									//loop through the the data columns
									for (int i = 0 ; i < 25 ; i++) {
										//get the data
										Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
										//write only the columns we are interested in, the sensors
										if ( i >= 3 && i <= 16) {
											try {
												matrix.matrix[sampleNo][i-3] = data[sampleIdx];
											} catch (ArrayIndexOutOfBoundsException e) {
											    //matrix is full, we are done
												writingMatrix = false;
												sample.setSample(matrix);
												break breakpoint;
											}
										}
										
										//Write the column data to the file
										out.write( Double.toString((data[sampleIdx])));
										out.write(" ");
									}
									
									sampleNo++;
									
									//Print the contact quality columns to our file
									//The ordering is consistent with the ordering of the logical input
						    		//channels in EE_InputChannels_enum.
									for (int i = 1; i < 15 ; i++)
										out.write(" " + EmoState.INSTANCE.ES_GetContactQuality(eState, i) + " ");
									
									//next row
									out.newLine();
								} catch (IOException e) {
									System.err.println(e.getMessage());
									System.exit(-1);
								} 
							}//END for()
						}//END IF writing matrix
					}
				}
			} //END  if(ready to collect)
			
			if(Thread.interrupted()) {
				//We have been interrupted, time to make a baby (a sample)
				startWritingMatrix();
			}
		} //END while
		cleanUp();
	}

	/*
	 * Sets the paramaters from sample.
	 */
	private void startWritingMatrix() {
		this.sampleNo = 0;
		this.out = sample.getFileHandle();
		this.matrix = sample.getMatrix();
		this.writingMatrix = true;
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
