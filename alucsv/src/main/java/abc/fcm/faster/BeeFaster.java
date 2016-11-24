package abc.fcm.faster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cluster.LoadData;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class BeeFaster {

	/** The number of colony size (employed bees+onlooker bees) */
	int NP = 100;
	/** The number of food sources equals the half of the colony size */
	int foodNum = NP / 2;
	/**
	 * A food source which could not be improved through "limit" trials is
	 * abandoned by its employed bee
	 */
	int limit = 4;
	/** The number of cycles for foraging {a stopping criteria} */
	int maxCycle = 20;
	int mCycle = 0;

	/** Problem specific variables */
	/** The number of parameters of the problem to be optimized */
	int dimension = 11*3;
	/** lower bound of the parameters. */
	double lb = 0;
	/**
	 * upper bound of the parameters. lb and ub can be defined as arrays for the
	 * problems of which parameters have different bounds
	 */
	double ub = 1;

	/** Algorithm can be run many times in order to see its robustness */
	int runtime = 20;

	/**
	 * foods is the population of food sources. Each row of foods matrix is a
	 * vector holding dimension parameters to be optimized. The number of rows
	 * of foods matrix equals to the foodNum
	 * */
	double foods[][] = new double[foodNum][dimension];

	/**
	 * f is a vector holding objective function values associated with food
	 * sources
	 */
	double funVal[] = new double[foodNum];
	/**
	 * fitness is a vector holding fitness (quality) values associated with food
	 * sources
	 */
	double fitness[] = new double[foodNum];

	/**
	 * trial is a vector holding trial numbers through which solutions can not
	 * be improved
	 */
	double trial[] = new double[foodNum];

	/**
	 * prob is a vector holding probabilities of food sources (solutions) to be
	 * chosen
	 */
	double prob[] = new double[foodNum];


	/** Optimum solution obtained by ABC algorithm */
	double globalMin = Double.MAX_VALUE;
	/**
	 * Holds the squared errors for all clusters. 平方误差
	 */
	double squaredError=0;

	/** Parameters of the optimum solution */
	double globalParams[] = new double[dimension];
	/** globalMins holds the globalMin of each run in multiple runs */
	double globalMins[] = new double[runtime];

	/**
	 * the mean Euclidean distance between X_{m} and the rest  of solutions
	 */
	double mean = 0;
	
	int centroidNum = 3;
	private int threadNum =4;
	private ExecutorService threadPool = null;
	
	private FCMFaster fcm;
	
	protected Instances data;
	
	public BeeFaster(){}
	
	public BeeFaster(FCMFaster fcm){
		this.fcm = fcm;
		startPool();
	}
	public void setData(Instances data){
		this.data = data;
	}
	public void startPool(){
		if(threadPool != null){
			threadPool.shutdownNow();
		}
		threadPool = Executors.newFixedThreadPool(threadNum);
	}
	/*
	 * Variables are initialized in the range [lb,ub]. If each parameter has
	 * different range, use arrays lb[j], ub[j] instead of lb and ub
	 */
	/* Counters of food sources are also initialized in this function */

	public void init(int index) {
		double[] solution = new double[dimension];
		for (int j = 0; j < dimension; j++) {
			foods[index][j] = Math.random() * (ub - lb) + lb;
			solution[j] = foods[index][j];
		}
		funVal[index] = calculateFunction(solution);
		fitness[index] = calculateFitness(funVal[index]);
		trial[index] = 0;
	}
	private class InitTask implements Callable<Boolean>{
		private int start;
		private int end;
		public InitTask(int start,int end){
			this.start=start;
			this.end = end;
		}
		public Boolean call(){
			double[] solution = new double[dimension];
			for (int i = start; i < end; i++) {
				for (int j = 0; j < dimension; j++) {
					foods[i][j] = Math.random() * (ub - lb) + lb;
					solution[j] = foods[i][j];
				}
				funVal[i] = calculateFunction(solution);
				fitness[i] = calculateFitness(funVal[i]);
				trial[i] = 0;
			}
			return true;
		}
	}
	/* All food sources are initialized */
	public void initial() {
		int numPerTask = foodNum / threadNum;
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		for (int i = 0; i < threadNum; i++) {
			int start = i * numPerTask;
			int end = start + numPerTask;
			if (i == threadNum - 1) {
				end = foodNum;
			}
			results.add(threadPool.submit(new InitTask( start, end)));
		}
		try{
			for(Future<Boolean> task : results){
				task.get();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	/**
	 * mean Euclidean distances between X_{m} and the rest of solutions.
	 * @return
	 */
	public void calculateMean(int index){
		double sum=0;
		for(int i=0; i< foodNum; i++){
			double total = 0;
			if(index!=i){
			for(int j=0; j<dimension; j++){
					total+=Math.pow(foods[index][j] - foods[i][j],2);
				}
			}
			sum+=total;
		}
		mean= sum/(foodNum-1);
	}
	/**
	 * calculate the  neighbor of  X_{m} and itself (N_{m})
	 * @param index
	 * @return
	 */
	public List<double[]> calculateNeighbor(int index){
		List<double[]> neighbors = new ArrayList<>();
		calculateMean(index);
		for(int i=0; i<foodNum; i++){
			double total =0;
			if(index !=i){
				for(int j=0; j<dimension; j++){
					total += Math.pow(foods[index][j] - foods[i][j], 2);
				}
			}
			if(total < mean){
				neighbors.add(foods[i]);
			}
		}
		return neighbors;
	}
	/**
	 * calculate the best solution among the neighbor of  X_{m} and itself (N_{m})
	 * @param index
	 * @return X_{Nm}^best
	 */
	public double[] calculateNeighborBest(int index){
		List<double[]> neighbors = calculateNeighbor(index);
		double maxFit = lb;
		double[] maxNeighbor = null;
		for(double[] neighbor : neighbors){
			double objVal = calculateFunction(neighbor);
			double fitness = calculateFitness(objVal);
			if(maxFit<fitness){
				maxFit = fitness;
				maxNeighbor = neighbor;
			}
		}
		return maxNeighbor;
		
	}
	/** The best food source is memorized */
	public void memorizeBestSource() {
		int k=0;
		for (int i = 0; i < foodNum; i++) {
			if (funVal[i] < globalMin) {
				globalMin = funVal[i];
				k=i;
				for (int j = 0; j < dimension; j++)
					globalParams[j] = foods[i][j];
			}
		}
	}
	public void updateClusterInfo(){
		int k = Utils.minIndex(funVal);
		double[] solution = new double[dimension];
		for(int i=0;i<dimension; i++){
			solution[i]=foods[k][i];
		}
		Instances centroids = arrayToInstances(solution);
		fcm.setClusterCentroids(centroids);
		double[] errors = fcm.buildClusterErrors();
		squaredError = Utils.sum(errors);
	}
private class EmployBeeTask implements Callable<Boolean>{
	private int start;
	private int end;
	public EmployBeeTask(int start,int end){
		this.start=start;
		this.end = end;
	}
	public Boolean call(){
		Random rand = new Random();
		for (int i = start; i < end; i++) {
			/* The parameter to be changed is determined randomly */
			int dj = rand.nextInt(dimension);
			/*
			 * A randomly chosen solution is used in producing a mutant solution
			 * of the solution i
			 */
			int foodi = rand.nextInt(foodNum);
			double[] solution = new double[dimension];
			for (int j = 0; j < dimension; j++) {
				solution[j] = foods[i][j];
			}
			/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
			double r = rand.nextDouble() * 2 - 1;
			solution[dj] = foods[i][dj]+ (foods[i][dj] - foods[foodi][dj])
					* r*(1 + 1/(Math.exp(-maxCycle*1.0/mCycle)+1));

			/*
			 * if generated parameter value is out of boundaries, it is shifted
			 * onto the boundaries
			 */
			if (solution[dj] < lb)
				solution[dj] = lb;
			if (solution[dj] > ub)
				solution[dj] = ub;
			double objValSol = calculateFunction(solution);
			double fitnessSol = calculateFitness(objValSol);

			/*
			 * a greedy selection is applied between the current solution i and
			 * its mutant
			 */
			if (fitnessSol > fitness[i]) {

				/**
				 * If the mutant solution is better than the current solution i,
				 * replace the solution with the mutant and reset the trial
				 * counter of solution i
				 * */
				trial[i] = 0;
				for (int j = 0; j < dimension; j++)
					foods[i][j] = solution[j];
				funVal[i] = objValSol;
				fitness[i] = fitnessSol;
			} else {
				/*
				 * if the solution i can not be improved, increase its trial
				 * counter
				 */
				trial[i] = trial[i] + 1;
			}
		}
		return true;
	}
}
	
	/**
	 * Employed Bee Phase
	 */
	public void sendEmployedBees() {
		int numPerTask = foodNum / threadNum;
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		for (int i = 0; i < threadNum; i++) {
			int start = i * numPerTask;
			int end = start + numPerTask;
			if (i == threadNum - 1) {
				end = foodNum;
			}
			results.add(threadPool.submit(new EmployBeeTask(start, end)));
		}
		try{
			for(Future<Boolean> task : results){
				task.get();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void calculateProbabilities() {

		double sum = 0;

		for (int i = 0; i < foodNum; i++) {
			sum += fitness[i];
		}

		for (int i = 0; i < foodNum; i++) {
			prob[i] = fitness[i] / sum;
		}

	}
	private class OnlookerBeeTask implements Callable<Boolean>{
		private int start;
		private int end;
		private int neighbour;
		private Random rand;
		public OnlookerBeeTask(int start,int end,int neighbour,Random rand){
			this.start=start;
			this.end = end;
			this.neighbour = neighbour;
			this.rand = rand;
		}
		public Boolean call(){
			double[] solution = new double[dimension];
					for (int j = 0; j < dimension; j++){
						solution[j] = foods[start][j];
					}
					double[] bestNeighbor = calculateNeighborBest(start);
					int minFIndex = Utils.minIndex(funVal);
					
					/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
					
					double r = rand.nextDouble()-1;
					solution[end] =  bestNeighbor[end]
							+ (bestNeighbor[end] - foods[neighbour][end])* r+
							rand.nextDouble()*1.5*(foods[minFIndex][end]-bestNeighbor[end]);

					/*
					 * if generated parameter value is out of boundaries, it is
					 * shifted onto the boundaries
					 */
					if (solution[end] < lb)
						solution[end] = lb;
					if (solution[end] > ub)
						solution[end] = ub;
					double objValSol = calculateFunction(solution);
					double fitnessSol = calculateFitness(objValSol);

					/*
					 * a greedy selection is applied between the current solution i
					 * and its mutant
					 */
					if (fitnessSol > fitness[start]) {
						/*
						 * If the mutant solution is better than the current
						 * solution i, replace the solution with the mutant and
						 * reset the trial counter of solution i
						 */
						trial[start] = 0;
						for (int j = 0; j < dimension; j++)
							foods[start][j] = solution[j];
						funVal[start] = objValSol;
						fitness[start] = fitnessSol;
					} else {
						/*
						 * if the solution i can not be improved, increase its trial
						 * counter
						 */
						trial[start] = trial[start] + 1;
					}
					return true;
				}
	}
	/** onlooker Bee Phase */
	public void sendOnlookerBees() {

		int i, j, t;
		i = 0;
		t = 0;
		Random rand = new Random();
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		while (t < foodNum) {
			double r = Math.random();
//			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			/*
			 * choose a food source depending on its probability to be chosen
			 */
			if (r < prob[i]) {
				t++;

				/* The parameter to be changed is determined randomly */
				int dj = rand.nextInt(dimension);

				/*
				 * A randomly chosen solution is used in producing a mutant
				 * solution of the solution i
				 */
				int neighbour = rand.nextInt(foodNum);

				/*
				 * Randomly selected solution must be different from the
				 * solution i
				 */
				while (neighbour == i) {
					// System.out.println(Math.random()*32767+"  "+32767);
					neighbour = rand.nextInt(foodNum);
				}
				results.add(threadPool.submit(new OnlookerBeeTask( i, dj,neighbour,rand)));
			}
			i++;
			if (i == foodNum)
				i = 0;
		}/* while */
		try{
			for(Future<Boolean> task : results){
				task.get();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		/* end of onlooker bee phase */
	}

	/*
	 * determine the food sources whose trial counter exceeds the "limit" value.
	 * In Basic ABC, only one scout is allowed to occur in each cycle
	 */
	void sendScoutBees() {
		int maxtrialindex, i;
		maxtrialindex = 0;
		for (i = 1; i < foodNum; i++) {
			if (trial[i] > trial[maxtrialindex])
				maxtrialindex = i;
		}
		if (trial[maxtrialindex] >= limit) {
			init(maxtrialindex);
		}
	}
	/** Fitness function */
	public double calculateFitness(double fun) {
		double result = 0;
		if (fun >= 0) {
			result = 1 / (fun + 1);
		} else {
			result = 1 + Math.abs(fun);
		}
		return result;
	}
	private class CalFcmTask implements Callable<Boolean>{
		private int start;
		private int end;
		public CalFcmTask(int start,int end){
			this.start=start;
			this.end = end;
		}
		public Boolean call(){
			double[] solution = new double[dimension];
		
			for (int i = start; i < end; i++) {
				for (int u = 0; u < dimension; u++){
					solution[u] = foods[i][u];
				}
				int k =0;
				Instances centroids = arrayToInstances(solution);
				fcm.setClusterCentroids(centroids);
				Instances ins =fcm.buildCentroids();
				for(int ii =0; ii<ins.numInstances();ii++){
					Instance in = ins.get(ii);
					for(int jj = 0;jj<in.numAttributes();jj++){
						solution[k++]=in.value(jj);
					}
				}
	/*
				if (solution[param2change] < lb)
					solution[param2change] = lb;
				if (solution[param2change] > ub)
					solution[param2change] = ub;
		*/
				double objValSol = calculateFunction(solution);
				double fitnessSol = calculateFitness(objValSol);

				/*
				 * a greedy selection is applied between the current solution i and
				 * its mutant
				 */
				if (fitnessSol > fitness[i]) {

					/**
					 * If the mutant solution is better than the current solution i,
					 * replace the solution with the mutant and reset the trial
					 * counter of solution i
					 * */
					trial[i] = 0;
					for (int j = 0; j < dimension; j++)
						foods[i][j] = solution[j];
					funVal[i] = objValSol;
					fitness[i] = fitnessSol;
				} else {
					/*
					 * if the solution i can not be improved, increase its trial
					 * counter
					 */
					trial[i] = trial[i] + 1;
				}
			}
		return true;
		}
	}
	public  void calculateFcm(){
		int numPerTask = foodNum / threadNum;
		List<Future<Boolean>> results = new ArrayList<Future<Boolean>>();
		for (int i = 0; i < threadNum; i++) {
			int start = i * numPerTask;
			int end = start + numPerTask;
			if (i == threadNum - 1) {
				end = foodNum;
			}
			results.add(threadPool.submit(new CalFcmTask( start, end)));
		}
		try{
			for(Future<Boolean> task : results){
				task.get();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * calculate function value
	 * 
	 * @param sol
	 * @return
	 */
	public double calculateFunction(double sol[]) {
		Instances centroids = arrayToInstances(sol);
		fcm.setClusterCentroids(centroids);
		double funVal = fcm.buildModel();
		return funVal;

	}
	public Instances arrayToInstances(double sol[]) {
		int count = 0;
		Instances centroids = new Instances(data,0);
		for(int i=0;i<centroidNum; i++){
			double[] val = new double[dimension/centroidNum];
			for(int j=0; j< dimension/centroidNum; j++ ){
				val[j] = sol[count++];
			}
			Instance ins = new DenseInstance(1.0,val);
			centroids.add(ins);
		}
		return centroids;

	}

	
	public static void main(String[] args) {
		String path = "dataset/88.0-93.0-normalize-combine.arff";
		LoadData ld = new LoadData();
		FCMFaster fcm = new FCMFaster();
		BeeFaster bee = new BeeFaster(fcm);
		Instances instances = ld.loadData(path);
		fcm.init(instances);
		Instances in = new Instances(instances,0);
		in.add(instances.instance(0));
		bee.setData(in);
		
		int iter = 0;
		int run = 0;
		int j = 0;
		double mean = 0;
		for (run = 0; run < bee.runtime; run++) {
			bee.initial();
			bee.memorizeBestSource();
			for (iter = 0; iter < bee.maxCycle; iter++) {
				bee.mCycle = iter+1;
				bee.sendEmployedBees();
				bee.calculateProbabilities();
				bee.sendOnlookerBees();
				bee.calculateFcm();
				bee.memorizeBestSource();
				bee.sendScoutBees();
				System.out.println("iter="+iter+" globalMin="+bee.globalMin);
			}
			bee.updateClusterInfo();
			System.out.println("globalMin = "+bee.globalMin);
			System.out.println(fcm.toString());
			bee.globalMins[run] = bee.globalMin;
			mean = mean + bee.globalMin;
		}

		int maxIndex = Utils.maxIndex(bee.globalMins);
		int minIndex = Utils.minIndex(bee.globalMins);
		double max = bee.globalMins[maxIndex];
		double min = bee.globalMins[minIndex];

		mean = mean / bee.runtime;

		double stdError = 0;

		for (j = 0; j < bee.runtime; j++) {
			stdError += Math.pow(bee.globalMins[j] - mean, 2);
		}
		stdError /= bee.runtime;
		System.out.println("maxFunVal=" + max + " minFunVal=" + min + " mean="
				+ mean + " stdError=" + stdError);
	}
}

 
