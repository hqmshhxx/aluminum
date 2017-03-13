package plot;

import java.util.Random;

public abstract class GABC extends AbstractABC implements ABC{
	
	public GABC(double lb, double ub, int maxCycle){
		super.lb = lb;
		super.ub = ub;
		super.maxCycle = maxCycle;
		
		this.lb = lb;
		this.ub = ub;
		this.maxCycle = maxCycle;
	}


	@Override
	public void sendOnlookerBees() {
		// TODO Auto-generated method stub
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

				/* v_{ij}=x_{ij}+\phi_{ij}*(x_{kj}-x_{ij}) */
				// r = ((double) Math.random() * 32767 / ((double) (32767) +
				// (double) (1)));
				r = rand.nextDouble() * 2 - 1;
				

				solution[param2change] = foods[i][param2change]
						+ (foods[i][param2change] - foods[neighbour][param2change])
						* r+rand.nextDouble()*1.5*(globalParams[param2change] - foods[i][param2change]);

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

	}

}
