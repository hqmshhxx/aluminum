package abc.fcm;

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
		
		int classIndex = train.attribute("class").index();
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
	public static void main(String[] args) {
		Evaluation evl = new Evaluation();
		
		String path = "dataset/iris-normalize.arff";
		LoadData ld = new LoadData();
		try {
			evl.cep(ld.loadData(path));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
