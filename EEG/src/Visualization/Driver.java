package Visualization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Driver {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//the filename the data will be written to
		String fileName = new SimpleDateFormat("yyyy-MM-dd-hh-mm'.txt'").format(new Date());

		System.out.println("Initializing data collection.");
		
		//Record the data to the file 
		try {
			//open the file for writing
			BufferedWriter out = new BufferedWriter(new FileWriter("VisualizationData/" + fileName));
			//get the data
			Data.getData(out);	
			//close the file
			out.close();
		} catch (IOException e) {
			System.err.println("IO Exception thrown by RawData.java");
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		System.out.println("Data successfully collected. See file: " + fileName);
		System.exit(0);
	}

}
