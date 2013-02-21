package Raw;

public class RawData {
	      
	
	public static void main(String[] args) {
		System.out.println("1");
		
		String path = System.getProperty("java.library.path");
	    System.out.println(path);
	    
	    System.load("C://edk.dll");
	}
	
}
