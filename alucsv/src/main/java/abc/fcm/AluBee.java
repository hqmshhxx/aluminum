package abc.fcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class AluBee {

	/** The number of colony size (employed bees+onlooker bees) */
	int NP = 200;
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
	int dimension = 9*6;
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

	/**
	 * New solution (neighbour) produced by
	 * v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter
	 * and k is a randomlu chosen solution different from i
	 */
	double solution[] = new double[dimension];

	/** Objective function value of new solution */
	double objValSol;
	/** Fitness value of new solution */
	double fitnessSol;
	/**
	 * param2change corrresponds to j, neighbour corresponds to k in equation
	 * v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})
	 */
	int neighbour, param2change;

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
	/** a random number in the range [0,1) */
	double r;
	/**
	 * the mean Euclidean distance between X_{m} and the rest  of solutions
	 */
	double mean = 0;
	
	int centroidNum = 6;
	
	private AluFCM fcm;
	
	protected Instances data;
	
	public AluBee(){}
	
	public AluBee(AluFCM fcm){
		this.fcm = fcm;
	}
	public void setData(Instances data){
		this.data = data;
	}
	/*
	 * Variables are initialized in the range [lb,ub]. If each parameter has
	 * different range, use arrays lb[j], ub[j] instead of lb and ub
	 */
	/* Counters of food sources are also initialized in this function */

	public void init(int index) {
		for (int j = 0; j < dimension; j++) {
			foods[index][j] = Math.random() * (ub - lb) + lb;
			solution[j] = foods[index][j];
		}
		funVal[index] = calculateFunction(solution);
		fitness[index] = calculateFitness(funVal[index]);
		trial[index] = 0;
	}

	/* All food sources are initialized */
	public void initial() {
		int i;
		for (i = 0; i < foodNum; i++) {
			init(i);
		}
		globalMin = funVal[0];
		for (i = 0; i < dimension; i++)
			globalParams[i] = foods[0][i];
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
		for(int i=0;i<dimension; i++){
			solution[i]=foods[k][i];
		}
		Instances centroids = arrayToInstances(solution);
		fcm.setClusterCentroids(centroids);
		double[] errors = fcm.buildClusterErrors();
		squaredError = Utils.sum(errors);
	}

	
	/**
	 * Employed Bee Phase
	 */
	public void sendEmployedBees() {
		int i, j;
		Random rand = new Random();
		for (i = 0; i < foodNum; i++) {
			/* The parameter to be changed is determined randomly */
			param2change = rand.nextInt(dimension);

			/*
			 * A randomly chosen solution is used in producing a mutant solution
			 * of the solution i
			 */
			neighbour = rand.nextInt(foodNum);

			for (j = 0; j < dimension; j++) {
				solution[j] = foods[i][j];
			}
			/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
			r = rand.nextDouble() * 2 - 1;
			solution[param2change] = foods[i][param2change]
					+ (foods[i][param2change] - foods[neighbour][param2change])
					* r*(1 + 1/(Math.exp(-maxCycle*1.0/mCycle)+1));

			/*
			 * if generated parameter value is out of boundaries, it is shifted
			 * onto the boundaries
			 */
			if (solution[param2change] < lb)
				solution[param2change] = lb;
			if (solution[param2change] > ub)
				solution[param2change] = ub;
			objValSol = calculateFunction(solution);
			fitnessSol = calculateFitness(objValSol);

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
				for (j = 0; j < dimension; j++)
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

		/* end of employed bee phase */

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

	/** onlooker Bee Phase */
	public void sendOnlookerBees() {

		int i, j, t;
		i = 0;
		t = 0;
		Random rand = new Random();
		while (t < foodNum) {
			r = Math.random();
//			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			/*
			 * choose a food source depending on its probability to be chosen
			 */
			if (r < prob[i]) {
				t++;

				/* The parameter to be changed is determined randomly */
				param2change = rand.nextInt(dimension);

				/*
				 * A randomly chosen solution is used in producing a mutant
				 * solution of the solution i
				 */
				neighbour = rand.nextInt(foodNum);

				/*
				 * Randomly selected solution must be different from the
				 * solution i
				 */
				while (neighbour == i) {
					// System.out.println(Math.random()*32767+"  "+32767);
					neighbour = rand.nextInt(foodNum);
				}
				for (j = 0; j < dimension; j++)
					solution[j] = foods[i][j];
				double[] bestNeighbor = calculateNeighborBest(i);
				int minFIndex = Utils.minIndex(funVal);
				
				/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				
				r = rand.nextDouble()-1;
				solution[param2change] =  bestNeighbor[param2change]
						+ (bestNeighbor[param2change] - foods[neighbour][param2change])* r+
						rand.nextDouble()*1.5*(foods[minFIndex][param2change]-bestNeighbor[param2change]);

				/*
				 * if generated parameter value is out of boundaries, it is
				 * shifted onto the boundaries
				 */
				if (solution[param2change] < lb)
					solution[param2change] = lb;
				if (solution[param2change] > ub)
					solution[param2change] = ub;
				objValSol = calculateFunction(solution);
				fitnessSol = calculateFitness(objValSol);

				/*
				 * a greedy selection is applied between the current solution i
				 * and its mutant
				 */
				if (fitnessSol > fitness[i]) {
					/*
					 * If the mutant solution is better than the current
					 * solution i, replace the solution with the mutant and
					 * reset the trial counter of solution i
					 */
					trial[i] = 0;
					for (j = 0; j < dimension; j++)
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
			i++;
			if (i == foodNum)
				i = 0;
		}/* while */

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
	public  void calculateFcm(){
		for(int i=0; i<foodNum; i++){
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
			objValSol = calculateFunction(solution);
			fitnessSol = calculateFitness(objValSol);

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
		AluBee bee = new AluBee();
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
				bee.memorizeBestSource();
				bee.sendScoutBees();
			}
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
