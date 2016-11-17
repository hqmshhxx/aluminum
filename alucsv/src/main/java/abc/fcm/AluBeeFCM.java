package abc.fcm;

import weka.core.Instances;
import weka.core.Utils;
import cluster.LoadData;


public class AluBeeFCM {
	
	private AluBee bee;
	private AluFCM fcm;
	public AluBeeFCM (){
		fcm = new AluFCM();
		bee = new AluBee(fcm);
	}
	public void setData(Instances ins){
		fcm.init(ins);
		Instances in = new Instances(ins,0);
		in.add(ins.instance(0));
		bee.setData(in);
	}
	
	public void buildCluster(){
		double mean = 0;
		double squaredError = 0;
		for (int run = 0; run < bee.runtime; run++) {
			bee.initial();
			bee.memorizeBestSource();
			for (int iter = 0; iter < bee.maxCycle; iter++) {
				bee.mCycle = iter+1;
				bee.sendEmployedBees();
				bee.calculateProbabilities();
				bee.sendOnlookerBees();
				bee.calculateFcm();
				bee.memorizeBestSource();
				bee.sendScoutBees();
				System.out.println("iter="+iter);
			}
			System.out.println("run="+run);
			bee.globalMins[run] = bee.globalMin;
			mean += bee.globalMin;
			squaredError += bee.squaredError;
		}
		int maxIndex = Utils.maxIndex(bee.globalMins);
		int minIndex = Utils.minIndex(bee.globalMins);
		double max = bee.globalMins[maxIndex];
		double min = bee.globalMins[minIndex];

		mean = mean / bee.runtime;
		squaredError /= bee.runtime;

		double stdError = 0;

		for (int j = 0; j < bee.runtime; j++) {
			stdError += Math.pow(bee.globalMins[j] - mean, 2);
		}
		stdError /= bee.runtime;
		System.out.println("maxFunVal=" + max + " minFunVal=" + min + " mean="
				+ mean + " stdError=" + stdError);
		System.out.println("cluster squared error =" + squaredError);
	}
	
	public static void main(String[] args){
		AluBeeFCM abf = new AluBeeFCM();
		String path = "/home/ucas/software/aluminum-electrolysis/iris-normalize-noClass.arff";
		LoadData ld = new LoadData();
		abf.setData(ld.loadData(path));
		abf.buildCluster();
	}
	
}