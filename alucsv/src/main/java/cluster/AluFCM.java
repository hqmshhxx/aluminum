package cluster;

import java.util.Random;

import weka.core.Instances;
import weka.core.Utils;

public class AluFCM {

	private FastFCM fcm;
	private int maxCycle = 20;
	private double[] results = new double[maxCycle];
	private Instances instances;
	
	public AluFCM (){
		fcm = new FastFCM();
		
	}
	public void setData(Instances ins){
		instances = ins;
	}
	
	public void buildCluster() throws Exception{
		fcm.setMaxIterations(200);
		fcm.setNumClusters(3);
		Random rand = new Random();
		for(int i =0; i< maxCycle; i++){
			fcm.setSeed(rand.nextInt(maxCycle));
			fcm.buildClusterer(instances);
			System.out.println(fcm.toString());
			results[i]=fcm.getFunObjValue();
		}
		double mean = 0;
		double squaredError = 0;
		int maxIndex = Utils.maxIndex(results);
		int minIndex = Utils.minIndex(results);
		double max = results[maxIndex];
		double min = results[minIndex];

		mean = Utils.sum(results)/maxCycle;

		double stdError = 0;

		for (int j = 0; j < maxCycle; j++) {
			stdError += Math.pow(results[j] - mean, 2);
		}
		stdError /= maxCycle;
		System.out.println("maxFunVal=" + max + " minFunVal=" + min + " mean="
				+ mean + " stdError=" + stdError);
		System.out.println("cluster squared error =" + squaredError);
	}
	public static void main(String[] args){
		AluFCM af = new AluFCM();
		String path = "dataset/winequality-white-normalize-noClass.arff";
		LoadData ld = new LoadData();
		af.setData(ld.loadData(path));
		try {
			af.buildCluster();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			System.out.println("finished");
		}
	}
}
