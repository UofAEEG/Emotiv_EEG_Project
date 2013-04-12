package game.helper;

/* 
 * @author tfung
 */
public class Pair {
	private float x;
	private float y;
	
	public Pair(float inx, float iny) {
		x = inx;
		y = iny;
	}
	
	public Pair(int inx, int iny) {
		x = inx;
		y = iny;
	}
	
	public int getX() {
		return (int) x;
	}
	
	public int getY() {
		return (int) y;
	}
}