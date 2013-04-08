package PatternGame;

/*
 * A class for holding the information of one prediction,
 * used in calculating statistics for user evaluation.
 */
public class Statistic {
	
	Integer predictedPattern = null; // A = 0, B = 1, C = 2
	Double predictionAccuracy = null;
	Integer userIdentifiedPattern = null; // A = 0, B = 1, C = 2	
	
	public Statistic(int predictedPattern, double predictionAccuracy ,int userIdentifiedPattern) {
		this.predictedPattern = new Integer(predictedPattern);
		this.userIdentifiedPattern = new Integer(userIdentifiedPattern);
		this.predictionAccuracy = new Double(predictionAccuracy);
		
	}
	
	/*
	 * returns true if the predicted pattern is the same as the 
	 * userIdentifiedPattern
	 */
	public boolean isCorrectGuess() {
		return (this.predictedPattern == this.userIdentifiedPattern)? true : false;
	}
}
