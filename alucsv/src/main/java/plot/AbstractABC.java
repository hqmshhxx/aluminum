package plot;

import java.util.Random;

import weka.core.Utils;

public abstract class AbstractABC implements ABC{
	
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

	/** Problem specific variables */
	/** The number of parameters of the problem to be optimized */
	int dimension = 50;
	/** lower bound of the parameters. */
	double lb = -10.0;
	/**
	 * upper bound of the parameters. lb and ub can be defined as arrays for the
	 * problems of which parameters have different bounds
	 */
	double ub = 10.0;

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
	
	double[] meanFunctionValues ;

	@Override
	public void init(int index) {
		// TODO Auto-generated method stub
		int j;
		for (j = 0; j < dimension; j++) {
			foods[index][j] = Math.random() * (ub - lb) + lb;
			solution[j] = foods[index][j];
		}
		funVal[index] = calculateFunction(solution);
		fitness[index] = calculateFitness(funVal[index]);
		trial[index] = 0;
	}

	@Override
	public void initial() {
		// TODO Auto-generated method stub
		int i;
		for (i = 0; i < foodNum; i++) {
			init(i);
		}
		globalMin = funVal[0];
		for (i = 0; i < dimension; i++)
			globalParams[i] = foods[0][i];
	}

	

	@Override
	public void calculateProbabilities() {
		// TODO Auto-generated method stub
		double sum = 0;

		for (int i = 0; i < foodNum; i++) {
			sum += fitness[i];
		}

		for (int i = 0; i < foodNum; i++) {
			prob[i] = fitness[i] / sum;
		}
	}

	@Override
	public double calculateFitness(double fun) {
		// TODO Auto-generated method stub
		double result = 0;
		if (fun >= 0) {
			result = 1 / (fun + 1);
		} else {
			result = 1 + Math.abs(fun);
		}
		return result;
	}
	@Override
	public void sendEmployedBees() {
		// TODO Auto-generated method stub
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
					* r;

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

	@Override
	public void sendScoutBees() {
		// TODO Auto-generated method stub
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
	
	@Override
	public void memorizeBestSource() {
		// TODO Auto-generated method stub
		int i, j;
		for (i = 0; i < foodNum; i++) {
			if (funVal[i] < globalMin) {
				globalMin = funVal[i];
				for (j = 0; j < dimension; j++)
					globalParams[j] = foods[i][j];
			}
		}
	}

	@Override
	public void runABC() {
		// TODO Auto-generated method stub
		meanFunctionValues = new double[maxCycle];
		System.out.println("ub = "+this.ub+" lb = "+this.lb+" maxCycle = "+this.maxCycle);
		double mean = 0;
		for (int run = 0; run < runtime; run++) {
			initial();
			memorizeBestSource();
			for (int iter = 0; iter < maxCycle; iter++) {
				sendEmployedBees();
				calculateProbabilities();
				sendOnlookerBees();
				memorizeBestSource();
				sendScoutBees();
				meanFunctionValues[iter] += globalMin;
			}
			globalMins[run] = globalMin;
			mean = mean + globalMin;
		}
		
		for(int i=0; i<maxCycle; i++){
			meanFunctionValues[i] /= runtime;
		}

		int maxIndex = Utils.maxIndex(globalMins);
		int minIndex = Utils.minIndex(globalMins);
		double max = globalMins[maxIndex];
		double min = globalMins[minIndex];

		mean = mean / runtime;

		double stdError = 0;

		for (int j = 0; j < runtime; j++) {
			stdError += Math.pow(globalMins[j] - mean, 2);
		}
		stdError /= runtime;
/*
		System.out.println("maxFunVal=" + max + " minFunVal=" + min + " mean="
				+ mean + " stdError=" + stdError);
*/				
	}
	
	public double[] getMeanFunctionValues(){
		return meanFunctionValues;
	}
	
	

}
