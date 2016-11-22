package abc.fcm;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.core.Instances;
import weka.core.Utils;
import cluster.LoadData;


public class AluBeeFCM {
	
	private int threadNum = 4;
	private int runCount = 30;
//	private int maxCycle = 1000;
	private Instances instances;
	private ExecutorService threadPool ;
	
	public void setData(Instances ins){
		instances = ins;
		threadPool = Executors.newFixedThreadPool(threadNum);
	}
	public  class InnerBee implements Callable<double[]>{
		private AluBee bee;
		private AluFCM fcm;
		private int start;
		private int end;
		private double[] results;
		private Instances ins;
		
		public InnerBee(int start,int end,Instances instances){
			this.start = start;
			this.end = end;
			results = new double[end - start];
			fcm = new AluFCM();
			bee = new AluBee(fcm);
			this.ins = instances;
		}
		public void setData(){
			fcm.init(ins);
			Instances in = new Instances(ins,0);
			in.add(ins.instance(0));
			bee.setData(in);
		}
		public double[] call(){
			for (int run = start; run < end; run++) {
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
				System.out.println("globalMin = "+bee.globalMin);
				System.out.println(fcm.toString());
				results[run-start] = bee.globalMin;
			}
			return results;
		}
		
	}
	public void buildCluster(){
		int numPerTask = runCount / threadNum;
		ArrayList<Future<double[]>> results = new ArrayList<>();
		double[] array = new double[runCount];
		for (int i = 0; i < threadNum; i++) {
			int start = i * numPerTask;
			int end = start + numPerTask;
			if (i == threadNum - 1) {
				end = runCount;
			}
			Instances ins = new Instances(instances);
			InnerBee ib = new InnerBee( start, end,ins);
			ib.setData();
			results.add(threadPool.submit(ib));
		}
		try{
			int cut =0;
			for(Future<double[]> result : results){
				double[] arr = result.get();
				if(arr != null){
					for(double a : arr){
						array[cut++] = a;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			threadPool.shutdown();
		}
		double mean = 0;
		int maxIndex = Utils.maxIndex(array);
		int minIndex = Utils.minIndex(array);
		double max = array[maxIndex];
		double min = array[minIndex];

		mean = mean / runCount;

		double stdError = 0;

		for (int j = 0; j < runCount; j++) {
			stdError += Math.pow(array[j] - mean, 2);
		}
		stdError /= runCount;
		System.out.println("maxFunVal=" + max + " minFunVal=" + min + " mean="
				+ mean + " stdError=" + stdError);
	}
	
	public static void main(String[] args){
		AluBeeFCM abf = new AluBeeFCM();
		String path = "/home/ucas/software/aluminum-electrolysis/iris-normalize-noClass.arff";
		LoadData ld = new LoadData();
		abf.setData(ld.loadData(path));
		abf.buildCluster();
	}
	
}