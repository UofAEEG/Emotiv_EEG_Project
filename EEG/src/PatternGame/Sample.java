package PatternGame;

import java.io.BufferedWriter;

/*
 * This class is acts as a synchronization handler for interactions
 * between DataCollector and PatternDriver. Basically, manages a 
 * producer consumer interaction.
 */
public class Sample {

	private int size;
	private BufferedWriter fileHandle;
	private boolean collecting;
	private Matrix matrix;
	
	//initialize to null
	public Sample() {
		this.size = 0;
		this.fileHandle = null;
		this.collecting = false;
		this.matrix = null;
	}
	
	/*
	 * This function sets the parameters
	 */
	synchronized void requestSample(BufferedWriter fileHandle, int seconds, DataCollector dc) {
		
		//this block is redundant and shouldnt ever be entered in our implementation
		if(isCollecting()) {
			try {
				wait();
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		
		this.fileHandle = fileHandle;
		this.matrix = new Matrix(seconds);
		this.size = matrix.matrixSize;
		this.collecting = true;
		//notify dc we need him
		dc.interrupt();
	}

	synchronized Matrix getSample() {
		if(isCollecting()) {
			try {
				wait();
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		return this.matrix;
	}
	
	/*
	 * Sets the matrix object and notifies any 
	 * 
	 */
	synchronized void setSample(Matrix m) {
		this.matrix = m;
		this.collecting = false;
		this.notifyAll();
	}
	
	public Matrix getMatrix() {
		return this.matrix;
	}

	public int getSize() {
		return size;
	}


	public BufferedWriter getFileHandle() {
		return fileHandle;
	}

	public boolean isCollecting() {
		return collecting;
	}

}
