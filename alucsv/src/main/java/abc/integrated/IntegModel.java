package abc.integrated;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import abc.ann.MultilayerPerceptron;
import abc.fcm.BeeFCM;


public class IntegModel {
	
	private BeeFCM fcm;
	private MultilayerPerceptron[] subNets;
	
	private double[] subResults;
	
	private LastNet lastNet;
	
	
	private Instances centroids;
	private Instances[] clusters;
	
	
	private int numClusters = 3;
	
	private ExecutorService threadPool;
	
	public IntegModel(){
		fcm = new BeeFCM();
		subNets = new MultilayerPerceptron[numClusters];
		lastNet = new LastNet();
	}
	
	public void startPool(){
		if(threadPool != null){
			threadPool.shutdownNow();
		}
		threadPool = Executors.newFixedThreadPool(numClusters);
	}
	
	public void buildModel(Instances data) throws Exception{
		List<Future<Boolean>> tasks = new ArrayList<>();
		clusters = new Instances[numClusters];
		for(int i=0; i<numClusters; i++){
			clusters[i] = new Instances(data,0);
		}
		subResults = new double[data.numInstances()];
		fcm.setNumClusters(numClusters);
		fcm.buildClusterer(data);
		centroids = fcm.getClusterCentroids();
		int[] assignments = fcm.getAssignments();
		for(int i=0; i<assignments.length; i++){
			clusters[assignments[i]].add(data.instance(i));
		}
		for(int i=0; i<numClusters; i++){
			ANN ann = new ANN(clusters[i],subNets[i]);
			tasks.add(threadPool.submit(ann));
		}
		for(int i=0; i<numClusters; i++){
			tasks.get(i).get();
		}
		Attribute att = new Attribute("ele");
		Attribute cla = data.classAttribute();
		ArrayList<Attribute> attList = new ArrayList<>();
		attList.add(att);
		attList.add(cla);
		Instances lastTrain = new Instances("last train", attList, data.numInstances());
		for(int i=0; i<assignments.length; i++){
			double[] attValue = new double[2];
			double[] predict = subNets[assignments[i]].distributionForInstance(data.instance(i));
			subResults[i] = predict[0];
			attValue[0] = predict[0];
			attValue[1] = data.instance(i).classValue(); 
			Instance  ins = new DenseInstance(1.0,attValue);
			lastTrain.add(ins);
		}
		
		lastNet.build(lastTrain);
		
	}
	

}
