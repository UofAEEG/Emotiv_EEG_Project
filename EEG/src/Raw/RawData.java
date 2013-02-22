package Raw;


import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class RawData {
	      
	public static void main(String[] args) {
		System.out.println("Hello Emotiv World");
		
		Pointer eEvent				= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	Pointer eState				= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 		= null;
		IntByReference nSamplesTaken= null;
    	short composerPort			= 1726;
    	int option 					= 1;
    	int state  					= 0;
    	float secs 					= 1;
    	boolean readytocollect 		= false;
    	String fileName = (new Timestamp((new java.util.Date()).getTime())).toString();
    	PrintWriter out = null;
    	
    	
    	userID 			= new IntByReference(0);
		nSamplesTaken	= new IntByReference(0);
    	
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
    	
    	IntByReference samplingRateOut = null;
    	samplingRateOut	= new IntByReference(0);
		Edk.INSTANCE.EE_DataGetSamplingRate(userID.getValue(), samplingRateOut);
		System.out.println("SamplingRateOut: " + samplingRateOut.getValue());
    	
		
    	try {
		    out = new PrintWriter(new BufferedWriter(new FileWriter(fileName + ".txt", true)));
		} catch (IOException e) {
			System.out.println("Error writing to file");
		}
    	
    	Poll timer = new Poll();
    	timer.start();
    	
		while (true) 
		{	
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) 
			{
				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) 
				if (userID != null)
					{
						System.out.println("User added");
						Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
						readytocollect = true;
					}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
			
			if (readytocollect) 
			{
				Edk.INSTANCE.EE_DataUpdateHandle(0, hData);

				Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

				if (nSamplesTaken != null)
				{
					if (nSamplesTaken.getValue() != 0) {
						
						System.out.print("Updated: ");
						System.out.println(nSamplesTaken.getValue());
						
						out.print("0: " + timer.getCurrentTime() + " ms ,");
						
						double[] data = new double[nSamplesTaken.getValue()];
						
						for (int sampleIdx=0 ; sampleIdx<nSamplesTaken.getValue() ; ++sampleIdx) {
							System.out.print(timer.getCurrentTime()+" ");
							for (int i = 1 ; i <= 36 ; i++) {

								Edk.INSTANCE.EE_DataGet(hData, i-1, data, nSamplesTaken.getValue());
								out.print(i + ": ");
								out.print(data[sampleIdx]);
								out.print(", ");
							}	
							out.println();
						}
					}
				}
			}
		}
		out.close();
    	Edk.INSTANCE.EE_EngineDisconnect();
    	Edk.INSTANCE.EE_EmoStateFree(eState);
    	Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
    	System.out.println("Disconnected!");
	}
	
}
