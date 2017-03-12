package plot;

public interface ABC {
	
	public void init(int index);
	public void initial();
	
	public void sendEmployedBees();
	public void sendOnlookerBees();
	public void sendScoutBees();
	public void calculateProbabilities();
	
	public double calculateFitness(double fun);
	public double calculateFunction(double sol[]);
	
	public void memorizeBestSource();
	
	public void runABC();
	
	
}
