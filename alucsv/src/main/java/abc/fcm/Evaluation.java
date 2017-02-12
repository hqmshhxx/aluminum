package abc.fcm;

import java.io.File;
import java.util.Random;

import cluster.LoadData;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.RandomizableClusterer;
import weka.core.Instances;

public class Evaluation {
	
	private RandomizableClusterer cluster;
	
	
	public Evaluation(){
//		cluster = new ABCSimpleFCM();
		cluster = new BeeFCM();
//		cluster = new FuzzyCMeans();
	}
	public void setCluster(RandomizableClusterer cluster){
		this.cluster = cluster;
	}
	
	public double predict(Instances train,Instances test,int seed) throws Exception{
		
		int classIndex = train.numAttributes()-1;
		test.setClassIndex(classIndex);
		train.setClassIndex(classIndex);
		cluster.setSeed(seed);
		cluster.buildClusterer(train);
		
		
		ClusterEvaluation evaluation = new ClusterEvaluation();
		evaluation.setClusterer(cluster);
		evaluation.evaluateClusterer(train);
		System.out.println(evaluation.clusterResultsToString());
		return 1;
	}
	public void cep(Instances data) throws Exception{
		int mIter = 10;
		Random rand = new Random();
		int dataNum = data.numInstances();
		int trainNum = (int)(1* dataNum);
		int testNum = dataNum-trainNum;
		Instances train = new Instances(data,trainNum);
		Instances test = new Instances(data,testNum);
		double mean=0;
		double std =0;
		double[] results = new double[mIter];
		for(int k=0; k<mIter; k++){
			train.clear();
			test.clear();
			data.randomize(rand);
			for(int q=0;q<dataNum;q++){
				if(q<trainNum){
					train.add(data.instance(q));
				}else{
					test.add(data.instance(q));
				}
			}
			Instances trainCopy = new Instances(train);
			Instances testCopy = new Instances(test);
			double result = predict(trainCopy,testCopy,k);
			mean += result;
			results[k]=result;
			System.out.println("iter ="+k);
		}
		mean /= mIter;
		for(int i=0;i<mIter; i++){
			std += Math.pow(results[i]-mean, 2);
		}
		std = Math.sqrt(std);
		System.out.println("the mean = " + mean+" the std = "+ std);
	}
	
	
	public void saveClusters(){
		String path = "dataset/705_cell_3.arff";
		String newPath = "dataset/705_cell_3-regression.arff";
		LoadData ld = new LoadData();
		Instances instances = ld.loadData(path);
		Instances newInstances = ld.loadData(newPath);
		int classIndex = instances.numAttributes()-1;
		instances.setClassIndex(classIndex);
		FuzzyCMeans fcm = new FuzzyCMeans();
		fcm.setSeed(10);
		ClusterEvaluation evaluation = new ClusterEvaluation();
		evaluation.setClusterer(fcm);
		try {
			fcm.buildClusterer(instances);
			evaluation.evaluateClusterer(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(evaluation.clusterResultsToString());
		int[] assignments = null;
		try {
			assignments = fcm.getAssignments();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Instances[] mClusters = new Instances[3];
		for(int i=0; i<3; i++){
			mClusters[i]=new Instances(newInstances,0);
		}
		for(int i=0; i<assignments.length; i++){
			int index = assignments[i];
			mClusters[index].add(newInstances.get(i));
		}
		newInstances.clear();
		for(int i=0; i<mClusters.length; i++){
			int num = mClusters[i].numInstances();
			Instances changed = null;
			if(num==114){
				changed = ld.changeTarget(mClusters[i],88,2);
			}else if(num==117){
				changed = ld.changeTarget(mClusters[i],90,3);
			}else{
				changed = ld.changeTarget(mClusters[i],93,3);
			}
			newInstances.addAll(changed);
			ld.saveData(changed, "/home/ucas/software/aluminum-electrolysis/CSV日报/cluster-result/"+num+".arff");
		}
		Random rand = new Random();
		rand.setSeed(0);
		newInstances.randomize(rand);
		ld.saveData(newInstances, "/home/ucas/software/aluminum-electrolysis/CSV日报/cluster-result/705-plain.arff");
	}
	public static void main(String[] args) {
		Evaluation evl = new Evaluation();
		evl.saveClusters();
		
		
	/*	
		String path = "dataset/705_cell-normalize-Regression.arff";
		LoadData ld = new LoadData();
		try {
			evl.cep(ld.loadData(path));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	*/	
	}
}
