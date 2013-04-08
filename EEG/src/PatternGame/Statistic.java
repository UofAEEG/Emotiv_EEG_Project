package PatternGame;

/*
 * A class for holding the information of one prediction,
 * used in calculating statistics for user evaluation.
 */
public class Statistic {
	
	Integer predictedPattern = null; // A = 0, B = 1, C = 2,   -1 for undetermined
	Double predictionAccuracy = null; //null for undetermined
	Integer userIdentifiedPattern = null; // A = 0, B = 1, C = 2	
	
	public Statistic(Integer predictedPattern, Double predictionAccuracy , Integer userIdentifiedPattern) {
		this.predictedPattern = predictedPattern;
		this.userIdentifiedPattern = userIdentifiedPattern;
		this.predictionAccuracy = predictionAccuracy;
		
	}
	
	/*
	 * returns true if the predicted pattern is the same as the 
	 * userIdentifiedPattern
	 */
	public boolean isCorrectGuess() {
		return (this.predictedPattern == this.userIdentifiedPattern)? true : false;
	}
}
