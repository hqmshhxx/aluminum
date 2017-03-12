package plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class IABC extends AbstractABC implements ABC{
	
	/**
	 * the mean Euclidean distance between X_{m} and the rest  of solutions
	 */
	double mean;


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
	public List<Integer> calculateNeighbor(int index){
		List<Integer> neighbors = new ArrayList<>();
		calculateMean(index);
		for(int i=0; i<foodNum; i++){
			double total =0;
			if(index !=i){
				for(int j=0; j<dimension; j++){
					total += Math.pow(foods[index][j] - foods[i][j], 2);
				}
			}
			if(total < mean){
				neighbors.add(i);
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
		List<Integer> neighbors = calculateNeighbor(index);
		int bestIndex = neighbors.get(0);
		for(Integer neighbor : neighbors){
			if(fitness[neighbor]>fitness[bestIndex]){
				bestIndex = neighbor;
			}
		}
		return foods[bestIndex];
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
	}

	
}
