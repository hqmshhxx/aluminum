package abc;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

import weka.core.Utils;

public class Alubee {

	/** The number of colony size (employed bees+onlooker bees) */
	int NP = 20;
	/** The number of food sources equals the half of the colony size */
	int foodNum = NP / 2;
	/**
	 * A food source which could not be improved through "limit" trials is
	 * abandoned by its employed bee
	 */
	int limit = 100;
	/** The number of cycles for foraging {a stopping criteria} */
	int maxCycle = 2500;

	/** Problem specific variables */
	/** The number of parameters of the problem to be optimized */
	int numAttr = 100;
	/** lower bound of the parameters. */
	double lb = -5.12;
	/**
	 * upper bound of the parameters. lb and ub can be defined as arrays for the
	 * problems of which parameters have different bounds
	 */
	double ub = 5.12;

	/** Algorithm can be run many times in order to see its robustness */
	int runtime = 30;

//	int dizi1[] = new int[10];
	/**
	 * foods is the population of food sources. Each row of foods matrix is a
	 * vector holding numAttr parameters to be optimized. The number of rows of foods
	 * matrix equals to the foodNum
	 * */
	double foods[][] = new double[foodNum][numAttr];

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
	 * trial is a vector holding trial numbers through which solutions can not be improved
	 */
	double trial[] = new double[foodNum];

	/**
	 * prob is a vector holding probabilities of food sources (solutions) to be chosen
	 */
	double prob[] = new double[foodNum];

	/**
	 * New solution (neighbour) produced by
	 * v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter
	 * and k is a randomlu chosen solution different from i
	 */
	double solution[] = new double[numAttr];

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
	double globalParams[] = new double[numAttr];
	/** globalMins holds the globalMin of each run in multiple runs */
	double globalMins[] = new double[runtime];
	/** a random number in the range [0,1) */
	double r;
	/**
	 * the mean Euclidean distance between X_{m} and the rest  of solutions
	 */
	double mean;

	/*
	 * a function pointer returning double and taking a numAttr-dimensional array as
	 * argument
	 */
	/*
	 * If your function takes additional arguments then change function pointer
	 * definition and lines calling "...=function(solution);" in the code
	 */

	// typedef double (*FunctionCallback)(double sol[numAttr]);

	/* benchmark functions */

	// double sphere(double sol[numAttr]);
	// double Rosenbrock(double sol[numAttr]);
	// double Griewank(double sol[numAttr]);
	// double Rastrigin(double sol[numAttr]);

	/* Write your own objective function name instead of sphere */
	// FunctionCallback function = &sphere;

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
	 * mean Euclidean distances between X_{m} and the rest of solutions.
	 * @return
	 */
	public void calculateMean(int index){
		double sum=0;
		for(int i=0; i< foodNum; i++){
			double total = 0;
			if(index!=i){
			for(int j=0; j<numAttr; j++){
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
		for(int i=0; i<foodNum; i++){
			double total =0;
			if(index !=i){
				for(int j=0; j<numAttr; j++){
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
				for (j = 0; j < numAttr; j++)
					globalParams[j] = foods[i][j];
			}
		}
	}

	/*
	 * Variables are initialized in the range [lb,ub]. If each parameter has
	 * different range, use arrays lb[j], ub[j] instead of lb and ub
	 */
	/* Counters of food sources are also initialized in this function */

	public void init(int index) {
		int j;
		for (j = 0; j < numAttr; j++) {
			r = ((double) Math.random() * 32767 / ((double) 32767 + (double) (1)));
			foods[index][j] = r * (ub - lb) + lb;
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
		for (i = 0; i < numAttr; i++)
			globalParams[i] = foods[0][i];
	}

	/**
	 * Employed Bee Phase
	 */
	public void sendEmployedBees() {
		int i, j;
		for (i = 0; i < foodNum; i++) {
			/* The parameter to be changed is determined randomly */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			param2change = (int) (r * numAttr);

			/*
			 * A randomly chosen solution is used in producing a mutant solution
			 * of the solution i
			 */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			neighbour = (int) (r * foodNum);

			/* Randomly selected solution must be different from the solution i */
			// while(neighbour==i)
			// {
			// r = ( (double)Math.random()*32767 / ((double)(32767)+(double)(1))
			// );
			// neighbour=(int)(r*foodNum);
			// }
			for (j = 0; j < numAttr; j++) {
				solution[j] = foods[i][j];
			}
			
			/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			solution[param2change] = foods[i][param2change]
					+ (foods[i][param2change] - foods[neighbour][param2change])
					* (r - 0.5) * 2;

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
				for (j = 0; j < numAttr; j++)
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

	/*
	 * A food source is chosen with the probability which is proportioal to its
	 * quality
	 */
	/* Different schemes can be used to calculate the probability values */
	/* For example prob(i)=fitness(i)/sum(fitness) */
	/* or in a way used in the metot below prob(i)=a*fitness(i)/max(fitness)+b */
	/*
	 * probability values are calculated by using fitness values and normalized
	 * by dividing maximum fitness value
	 */
	public void calculateProbabilities() {
		int i;
		double maxfit;
		maxfit = fitness[0];
		for (i = 1; i < foodNum; i++) {
			if (fitness[i] > maxfit)
				maxfit = fitness[i];
		}

		for (i = 0; i < foodNum; i++) {
			prob[i] = (0.9 * (fitness[i] / maxfit)) + 0.1;
		}

	}

	/** onlooker Bee Phase */
	public void sendOnlookerBees() {

		int i, j, t;
		i = 0;
		t = 0;

		while (t < foodNum) {

			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			/*
			 * choose a food source depending on its probability to be chosen
			 */
			if (r < prob[i]) {
				t++;

				/* The parameter to be changed is determined randomly */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				param2change = (int) (r * numAttr);

				/*
				 * A randomly chosen solution is used in producing a mutant
				 * solution of the solution i
				 */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				neighbour = (int) (r * foodNum);

				/*
				 * Randomly selected solution must be different from the
				 * solution i
				 */
				while (neighbour == i) {
					// System.out.println(Math.random()*32767+"  "+32767);
					r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
					neighbour = (int) (r * foodNum);
				}
				for (j = 0; j < numAttr; j++){
					solution[j] = foods[i][j];
				}
				double[] bestNeighbor = calculateNeighborBest(i);
				int maxFitIndex = Utils.maxIndex(fitness);
				/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				solution[param2change] = bestNeighbor[param2change]
						+ (bestNeighbor[param2change] - foods[neighbour][param2change])
						* (r - 0.5) * 2+Math.random()*(fitness[maxFitIndex]);

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
					for (j = 0; j < numAttr; j++)
						foods[i][j] = solution[j];
					funVal[i] = objValSol;
					fitness[i] = fitnessSol;
				} else {
					/*
					 * if the solution i can not be improved, increase its trial counter
					 */
					trial[i] = trial[i] + 1;
				}
			} 
			i++;
			if (i == foodNum){
				i = 0;
			}
				
		}/* while */

		/* end of onlooker bee phase */
	}

	/*
	 * determine the food sources whose trial counter exceeds the "limit" value.
	 * In Basic ABC, only one scout is allowed to occur in each cycle
	 */
	public void sendScoutBees() {
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

	/**
	 * calculate function value
	 * 
	 * @param sol
	 * @return
	 */
	public double calculateFunction(double sol[]) {
		return Rastrigin(sol);
	}

	public double sphere(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < numAttr; j++) {
			top = top + sol[j] * sol[j];
		}
		return top;
	}

	public double Rosenbrock(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < numAttr - 1; j++) {
			top = top
					+ 100
					* Math.pow((sol[j + 1] - Math.pow((sol[j]), (double) 2)),
							(double) 2) + Math.pow((sol[j] - 1), (double) 2);
		}
		return top;
	}

	public double Griewank(double sol[]) {
		int j;
		double top1, top2, top;
		top = 0;
		top1 = 0;
		top2 = 1;
		for (j = 0; j < numAttr; j++) {
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

		for (j = 0; j < numAttr; j++) {
			top = top
					+ (Math.pow(sol[j], (double) 2) - 10* Math.cos(2 * Math.PI * sol[j]) + 10);
		}
		return top;
	}

	public static void main(String[] args) {
		Alubee bee = new Alubee();
		int iter = 0;
		int run = 0;
		int j = 0;
		double mean = 0;
		// srand(time(NULL));
		for (run = 0; run < bee.runtime; run++) {
			bee.initial();
			bee.memorizeBestSource();
			for (iter = 0; iter < bee.maxCycle; iter++) {
				bee.sendEmployedBees();
				bee.calculateProbabilities();
				bee.sendOnlookerBees();
				bee.memorizeBestSource();
				bee.sendScoutBees();
			}
			for (j = 0; j < bee.numAttr; j++) {
				// System.out.println("GlobalParam[%d]: %f\n",j+1,globalParams[j]);
				System.out.println("GlobalParam[" + (j + 1) + "]:"
						+ bee.globalParams[j]);
			}
			// System.out.println("%d. run: %e \n",run+1,globalMin);
			System.out.println((run + 1) + ".run:" + bee.globalMin);
			bee.globalMins[run] = bee.globalMin;
			mean = mean + bee.globalMin;
		}
		mean = mean / bee.runtime;
		// System.out.println("Means of %d runs: %e\n",runtime,mean);
		System.out.println("Means  of " + bee.runtime + "runs: " + mean);
	}
}
