package unnormalize;

public class MmPair {
	private double min;
	private double max;
	private String name;
	
	public MmPair(){
	}
	public MmPair(double min, double max){
		this.min=min;
		this.max=max;
		
	}
	public MmPair(double min, double max, String name){
		this(min, max);
		this.name=name;
	}
	
	public  double getMin(){
		return min;
	}
	public double getMax(){
		return max;
	}
	
}
