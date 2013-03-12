package Raw;

public class Poll {
    private long startTime;

    public Poll() {
    	startTime = 0;
    }

    public void start() {
    	startTime = System.currentTimeMillis();
    }
    
    public long getCurrentTime() {
    	return System.currentTimeMillis() - startTime;
    }
}
