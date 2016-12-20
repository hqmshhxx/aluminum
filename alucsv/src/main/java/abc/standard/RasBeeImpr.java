package abc.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.core.Utils;

public class RasBeeImpr {

	/** The number of colony size (employed bees+onlooker bees) */
	int NP = 200;
	/** The number of food sources equals the half of the colony size */
	int foodNum = NP / 2;
	/**
	 * A food source which could not be improved through "limit" trials is
	 * abandoned by its employed bee
	 */
	int limit = 100;
	/** The number of cycles for foraging {a stopping criteria} */
	int maxCycle = 1000;
	int mCycle = 0;

	/** Problem specific variables */
	/** The number of parameters of the problem to be optimized */
	int dimension = 20;
	/** lower bound of the parameters. */
	double lb = -5.12;
	/**
	 * upper bound of the parameters. lb and ub can be defined as arrays for the
	 * problems of which parameters have different bounds
	 */
	double ub = 5.12;

	/** Algorithm can be run many times in order to see its robustness */
	int runtime = 30;

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
	double globalMin;

	/** Parameters of the optimum solution */
	double globalParams[] = new double[dimension];
	/** globalMins holds the globalMin of each run in multiple runs */
	double globalMins[] = new double[runtime];
	/** a random number in the range [0,1) */
	double r;
	/**
	 * the mean Euclidean distance between X_{m} and the rest  of solutions
	 */
	double mean;

	/*
	 * a function pointer returning double and taking a dimension-dimensional
	 * array as argument
	 */
	/*
	 * If your function takes additional arguments then change function pointer
	 * definition and lines calling "...=function(solution);" in the code
	 */

	// typedef double (*FunctionCallback)(double sol[dimension]);

	/* benchmark functions */

	// double sphere(double sol[dimension]);
	// double Rosenbrock(double sol[dimension]);
	// double Griewank(double sol[dimension]);
	// double Rastrigin(double sol[dimension]);

	/* Write your own objective function name instead of sphere */
	// FunctionCallback function = &sphere;
	
	
	/*
	 * Variables are initialized in the range [lb,ub]. If each parameter has
	 * different range, use arrays lb[j], ub[j] instead of lb and ub
	 */
	/* Counters of food sources are also initialized in this function */

	public void init(int index) {
		int j;
		for (j = 0; j < dimension; j++) {
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
		int i, j;
		for (i = 0; i < foodNum; i++) {
			if (funVal[i] < globalMin) {
				globalMin = funVal[i];
				for (j = 0; j < dimension; j++)
					globalParams[j] = foods[i][j];
			}
		}
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

			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
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
				
				/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				
				r = rand.nextDouble() * 2 - 1;
				solution[param2change] =  bestNeighbor[param2change]
						+ (bestNeighbor[param2change] - foods[neighbour][param2change])* r+
						rand.nextDouble()*1.5*(globalParams[param2change]-bestNeighbor[param2change]);

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

	
	/**
	 * calculate function value
	 * 
	 * @param sol
	 * @return
	 */
	public double calculateFunction(double sol[]) {
		return Rastrigin(sol);

	}

	double sphere(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < dimension; j++) {
			top = top + sol[j] * sol[j];
		}
		return top;
	}

	double Rosenbrock(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < dimension - 1; j++) {
			top = top
					+ 100
					* Math.pow((sol[j + 1] - Math.pow((sol[j]), (double) 2)),
							(double) 2) + Math.pow((sol[j] - 1), (double) 2);
		}
		return top;
	}

	double Griewank(double sol[]) {
		int j;
		double top1, top2, top;
		top = 0;
		top1 = 0;
		top2 = 1;
		for (j = 0; j < dimension; j++) {
			top1 = top1 + Math.pow((sol[j]), (double) 2);
			top2 = top2
					* Math.cos((((sol[j]) / Math.sqrt((double) (j + 1))) * Math.PI) / 180);

		}
		top = (1 / (double) 4000) * top1 - top2 + 1;
		return top;
	}

	public double Rastrigin(double sol[]) {
		int j;
		double top = 0;

		for (j = 0; j < dimension; j++) {
			top = top
					+ (Math.pow(sol[j], (double) 2) - 10
							* Math.cos(2 * Math.PI * sol[j]) + 10);
		}
		return top;
	}

	public static void main(String[] args) {
		RasBeeImpr bee = new RasBeeImpr();
		int iter = 0;
		int run = 0;
		int j = 0;
		double mean = 0;
		for (run = 0; run < bee.runtime; run++) {
			bee.initial();
			
			for (iter = 0; iter < bee.maxCycle; iter++) {
				bee.mCycle = iter+1;
				bee.sendEmployedBees();
				bee.memorizeBestSource();
				bee.calculateProbabilities();
				bee.sendOnlookerBees();
				bee.sendScoutBees();
			}
			bee.memorizeBestSource();
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
