package PatternGame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MiddleMatrixChunk {

	double chunk[][] = null;
	String filename;
	int time;
	
	public MiddleMatrixChunk(String filename, int time) {
		this.chunk = new double[128][14];
		this.filename = filename;
		this.time = time;
	}
	
	public void generateChunk() {
		BufferedReader reader = null;
		String line;
		
		try {
			reader = new BufferedReader(new FileReader("MatrixData/" + filename));
			
			// skip around 40% of the file
			for (int i=0; i<(time*128*0.4); i++) {
				reader.readLine();
			}
			
			for (int i=0; i<128; i++) {
				line = reader.readLine();
				String[] tokens = line.split(" ");
				
				for (int j=0; j<14; j++) {
					chunk[i][j] = Double.parseDouble(tokens[j]);
				}
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
	}
	
	public void chunkout() {
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(new FileWriter("RandomChunk/random_" + filename + ".txt"));
			
			for (int i=0; i<128; i++) {
				for (int j=0; j<14; j++) {
					writer.write(String.format("%.2f",chunk[i][j]) + " ");
				}
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(-1);
		}
	}

}
