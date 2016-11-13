package abc;

import java.lang.Math;

public class BeeColony {

	/** The number of colony size (employed bees+onlooker bees) */
	int NP = 20;
	/** The number of food sources equals the half of the colony size */
	int FoodNumber = NP / 2;
	/**
	 * A food source which could not be improved through "limit" trials is
	 * abandoned by its employed bee
	 */
	int limit = 100;
	/** The number of cycles for foraging {a stopping criteria} */
	int maxCycle = 2500;

	/** Problem specific variables */
	/** The number of parameters of the problem to be optimized */
	int D = 100;
	/** lower bound of the parameters. */
	double lb = -5.12;
	/**
	 * upper bound of the parameters. lb and ub can be defined as arrays for the
	 * problems of which parameters have different bounds
	 */
	double ub = 5.12;

	/** Algorithm can be run many times in order to see its robustness */
	int runtime = 30;

	int dizi1[] = new int[10];
	/**
	 * Foods is the population of food sources. Each row of Foods matrix is a
	 * vector holding D parameters to be optimized. The number of rows of Foods
	 * matrix equals to the FoodNumber
	 * */
	double Foods[][] = new double[FoodNumber][D];

	/**
	 * f is a vector holding objective function values associated with food
	 * sources
	 */
	double f[] = new double[FoodNumber];
	/**
	 * fitness is a vector holding fitness (quality) values associated with food
	 * sources
	 */
	double fitness[] = new double[FoodNumber];

	/**
	 * trial is a vector holding trial numbers through which solutions can not
	 * be improved
	 */
	double trial[] = new double[FoodNumber];

	/**
	 * prob is a vector holding probabilities of food sources (solutions) to be
	 * chosen
	 */
	double prob[] = new double[FoodNumber];

	/**
	 * New solution (neighbour) produced by
	 * v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) j is a randomly chosen parameter
	 * and k is a randomlu chosen solution different from i
	 */
	double solution[] = new double[D];

	/** Objective function value of new solution */
	double ObjValSol;
	/** Fitness value of new solution */
	double FitnessSol;
	/**
	 * param2change corrresponds to j, neighbour corresponds to k in equation
	 * v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij})
	 */
	int neighbour, param2change;

	/** Optimum solution obtained by ABC algorithm */
	double GlobalMin;
	/** Parameters of the optimum solution */
	double GlobalParams[] = new double[D];
	/** GlobalMins holds the GlobalMin of each run in multiple runs */
	double GlobalMins[] = new double[runtime];
	/** a random number in the range [0,1) */
	double r;

	/*
	 * a function pointer returning double and taking a D-dimensional array as
	 * argument
	 */
	/*
	 * If your function takes additional arguments then change function pointer
	 * definition and lines calling "...=function(solution);" in the code
	 */

	// typedef double (*FunctionCallback)(double sol[D]);

	/* benchmark functions */

	// double sphere(double sol[D]);
	// double Rosenbrock(double sol[D]);
	// double Griewank(double sol[D]);
	// double Rastrigin(double sol[D]);

	/* Write your own objective function name instead of sphere */
	// FunctionCallback function = &sphere;

	/** Fitness function */
	public double CalculateFitness(double fun) {
		double result = 0;
		if (fun >= 0) {
			result = 1 / (fun + 1);
		} else {
			result = 1 + Math.abs(fun);
		}
		return result;
	}

	/** The best food source is memorized */
	public void MemorizeBestSource() {
		int i, j;
		for (i = 0; i < FoodNumber; i++) {
			if (f[i] < GlobalMin) {
				GlobalMin = f[i];
				for (j = 0; j < D; j++)
					GlobalParams[j] = Foods[i][j];
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
		for (j = 0; j < D; j++) {
			r = ((double) Math.random() * 32767 / ((double) 32767 + (double) (1)));
			Foods[index][j] = r * (ub - lb) + lb;
			solution[j] = Foods[index][j];
		}
		f[index] = calculateFunction(solution);
		fitness[index] = CalculateFitness(f[index]);
		trial[index] = 0;
	}

	/* All food sources are initialized */
	public void initial() {
		int i;
		for (i = 0; i < FoodNumber; i++) {
			init(i);
		}
		GlobalMin = f[0];
		for (i = 0; i < D; i++)
			GlobalParams[i] = Foods[0][i];
	}

	/**
	 * Employed Bee Phase
	 */
	public void SendEmployedBees() {
		int i, j;
		for (i = 0; i < FoodNumber; i++) {
			/* The parameter to be changed is determined randomly */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			param2change = (int) (r * D);

			/*
			 * A randomly chosen solution is used in producing a mutant solution
			 * of the solution i
			 */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			neighbour = (int) (r * FoodNumber);

			/* Randomly selected solution must be different from the solution i */
			// while(neighbour==i)
			// {
			// r = ( (double)Math.random()*32767 / ((double)(32767)+(double)(1))
			// );
			// neighbour=(int)(r*FoodNumber);
			// }
			for (j = 0; j < D; j++) {
				solution[j] = Foods[i][j];
			}
			/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			solution[param2change] = Foods[i][param2change]
					+ (Foods[i][param2change] - Foods[neighbour][param2change])
					* (r - 0.5) * 2;

			/*
			 * if generated parameter value is out of boundaries, it is shifted
			 * onto the boundaries
			 */
			if (solution[param2change] < lb)
				solution[param2change] = lb;
			if (solution[param2change] > ub)
				solution[param2change] = ub;
			ObjValSol = calculateFunction(solution);
			FitnessSol = CalculateFitness(ObjValSol);

			/*
			 * a greedy selection is applied between the current solution i and
			 * its mutant
			 */
			if (FitnessSol > fitness[i]) {

				/**
				 * If the mutant solution is better than the current solution i,
				 * replace the solution with the mutant and reset the trial
				 * counter of solution i
				 * */
				trial[i] = 0;
				for (j = 0; j < D; j++)
					Foods[i][j] = solution[j];
				f[i] = ObjValSol;
				fitness[i] = FitnessSol;
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
	public void CalculateProbabilities() {
		int i;
		double maxfit;
		maxfit = fitness[0];
		for (i = 1; i < FoodNumber; i++) {
			if (fitness[i] > maxfit)
				maxfit = fitness[i];
		}

		for (i = 0; i < FoodNumber; i++) {
			prob[i] = (0.9 * (fitness[i] / maxfit)) + 0.1;
		}

	}

	/** onlooker Bee Phase */
	public void SendOnlookerBees() {

		int i, j, t;
		i = 0;
		t = 0;

		while (t < FoodNumber) {

			r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
			/*
			 * choose a food source depending on its probability to be chosen
			 */
			if (r < prob[i]) {
				t++;

				/* The parameter to be changed is determined randomly */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				param2change = (int) (r * D);

				/*
				 * A randomly chosen solution is used in producing a mutant
				 * solution of the solution i
				 */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				neighbour = (int) (r * FoodNumber);

				/*
				 * Randomly selected solution must be different from the
				 * solution i
				 */
				while (neighbour == i) {
					// System.out.println(Math.random()*32767+"  "+32767);
					r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
					neighbour = (int) (r * FoodNumber);
				}
				for (j = 0; j < D; j++)
					solution[j] = Foods[i][j];

				/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				r = ((double) Math.random() * 32767 / ((double) (32767) + (double) (1)));
				solution[param2change] = Foods[i][param2change]
						+ (Foods[i][param2change] - Foods[neighbour][param2change])
						* (r - 0.5) * 2;

				/*
				 * if generated parameter value is out of boundaries, it is
				 * shifted onto the boundaries
				 */
				if (solution[param2change] < lb)
					solution[param2change] = lb;
				if (solution[param2change] > ub)
					solution[param2change] = ub;
				ObjValSol = calculateFunction(solution);
				FitnessSol = CalculateFitness(ObjValSol);

				/*
				 * a greedy selection is applied between the current solution i
				 * and its mutant
				 */
				if (FitnessSol > fitness[i]) {
					/*
					 * If the mutant solution is better than the current
					 * solution i, replace the solution with the mutant and
					 * reset the trial counter of solution i
					 */
					trial[i] = 0;
					for (j = 0; j < D; j++)
						Foods[i][j] = solution[j];
					f[i] = ObjValSol;
					fitness[i] = FitnessSol;
				} else {
					/*
					 * if the solution i can not be improved, increase its trial counter
					 */
					trial[i] = trial[i] + 1;
				}
			} 
			i++;
			if (i == FoodNumber)
				i = 0;
		}/* while */

		/* end of onlooker bee phase */
	}

	/*
	 * determine the food sources whose trial counter exceeds the "limit" value.
	 * In Basic ABC, only one scout is allowed to occur in each cycle
	 */
	void SendScoutBees() {
		int maxtrialindex, i;
		maxtrialindex = 0;
		for (i = 1; i < FoodNumber; i++) {
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

	double sphere(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < D; j++) {
			top = top + sol[j] * sol[j];
		}
		return top;
	}

	double Rosenbrock(double sol[]) {
		int j;
		double top = 0;
		for (j = 0; j < D - 1; j++) {
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
		for (j = 0; j < D; j++) {
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

		for (j = 0; j < D; j++) {
			top = top
					+ (Math.pow(sol[j], (double) 2) - 10* Math.cos(2 * Math.PI * sol[j]) + 10);
		}
		return top;
	}

	public static void main(String[] args) {
		BeeColony bee = new BeeColony();
		int iter = 0;
		int run = 0;
		int j = 0;
		double mean = 0;
		// srand(time(NULL));
		for (run = 0; run < bee.runtime; run++) {
			bee.initial();
			bee.MemorizeBestSource();
			for (iter = 0; iter < bee.maxCycle; iter++) {
				bee.SendEmployedBees();
				bee.CalculateProbabilities();
				bee.SendOnlookerBees();
				bee.MemorizeBestSource();
				bee.SendScoutBees();
			}
			for (j = 0; j < bee.D; j++) {
				// System.out.println("GlobalParam[%d]: %f\n",j+1,GlobalParams[j]);
				System.out.println("GlobalParam[" + (j + 1) + "]:"
						+ bee.GlobalParams[j]);
			}
			// System.out.println("%d. run: %e \n",run+1,GlobalMin);
			System.out.println((run + 1) + ".run:" + bee.GlobalMin);
			bee.GlobalMins[run] = bee.GlobalMin;
			mean = mean + bee.GlobalMin;
		}
		mean = mean / bee.runtime;
		// System.out.println("Means of %d runs: %e\n",runtime,mean);
		System.out.println("Means  of " + bee.runtime + "runs: " + mean);
	}
}
