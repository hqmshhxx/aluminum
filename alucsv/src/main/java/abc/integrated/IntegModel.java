package abc.integrated;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cluster.LoadData;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import abc.ann.ABCBP;
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
		for(int i=0; i<numClusters; i++){
			subNets[i] = new MultilayerPerceptron();
		}
		lastNet = new LastNet();
	}
	
	public void startPool(){
		if(threadPool != null){
			threadPool.shutdownNow();
		}
		threadPool = Executors.newFixedThreadPool(numClusters);
	}
	
	public void buildModel(Instances data) throws Exception{
		startPool();
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
			System.out.println("第"+i+"个 task 完成");
		}
		threadPool.shutdown();
		System.out.println("最后的总神经网络开始：");
		Instances lastTrain = buildLastData(data,assignments);
		System.out.println("last train num is "+ lastTrain.numInstances());
		lastNet.build(lastTrain);
	}
	public Instances buildLastData(Instances data, int[] assignments)throws Exception{
		Attribute att = new Attribute("ele");
		Attribute cla = data.classAttribute();
		ArrayList<Attribute> attList = new ArrayList<>();
		attList.add(att);
		attList.add(cla);
		Instances lastData = new Instances("last data", attList, data.numInstances());
		for(int i=0; i<assignments.length; i++){
			double[] attValue = new double[2];
			double predictValue = subNets[assignments[i]].classifyInstance(data.instance(i));
			subResults[i] = predictValue;
			attValue[0] = predictValue;
			attValue[1] = data.instance(i).classValue(); 
			Instance  ins = new DenseInstance(1.0,attValue);
			lastData.add(ins);
		}
		lastData.setClassIndex(lastData.numAttributes()-1);
		return lastData;
	}
	
	public int[] testAssignments(Instances test) throws Exception{
		int num = test.numInstances();
		Instances copyTest = new Instances(test);
		copyTest.setClassIndex(-1);
		copyTest.deleteAttributeAt(copyTest.numAttributes()-1);
		int[] assignments = new int[num];
		for(int i=0; i<num; i++){
			assignments[i] = fcm.clusterInstance(copyTest.instance(i));
		}
		return assignments;
	}
	
	public void regression(Instances data)  throws Exception{
		Random rand = new Random();
		int mIter = 1;
		int dataNum = data.numInstances();
		int trainNum = (int) (0.85 * dataNum);
		int testNum = dataNum - trainNum;
		Instances train = new Instances(data, trainNum);
		Instances test = new Instances(data, testNum);
		
		double testRootMeanSquaredError = 0;
		double testMeanAbsoluteError = 0;
		double testRootMeanSquaredErrorStd = 0;
		double testMeanAbsoluteErrorStd = 0;
		double[] testRootMeanSquaredResults = new double[mIter];
		double[] testMeanAbsoluteResults = new double[mIter];
		for (int k = 0; k < mIter; k++) {
			train.clear();
			test.clear();
			rand.setSeed(k);
			data.randomize(rand);
			for (int q = 0; q < dataNum; q++) {
				if (q < trainNum) {
					train.add(data.instance(q));
				} else {
					test.add(data.instance(q));
				}
			}
			buildModel(train);
			System.out.println("iter =" + k);
			
			int[] testAssignments = testAssignments(test);
			Instances lastTest = buildLastData(test,testAssignments);
			Evaluation testEvaluation = new Evaluation(lastTest);
			testEvaluation.evaluateModel(lastNet.getBP(), lastTest);
			testRootMeanSquaredError += testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteError += testEvaluation.meanAbsoluteError();
			
			testRootMeanSquaredResults[k] = testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteResults[k] = testEvaluation.meanAbsoluteError();
			System.out.println(testEvaluation.toSummaryString());
			
		}
		testRootMeanSquaredError /= mIter;
		testMeanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			testRootMeanSquaredError += Math.pow(testRootMeanSquaredResults[i] - testRootMeanSquaredError, 2);
			testMeanAbsoluteErrorStd += Math.pow(testMeanAbsoluteResults[i] - testMeanAbsoluteError, 2);
		}
		testRootMeanSquaredErrorStd = Math.sqrt(testRootMeanSquaredErrorStd);
		testMeanAbsoluteErrorStd = Math.sqrt(testMeanAbsoluteErrorStd);
		System.out.println("last test mean of rootMeanSquaredError = " + testRootMeanSquaredError + " the std = " + testRootMeanSquaredErrorStd);
		System.out.println("last test mean of meanAbsoluteError = " + testMeanAbsoluteError + " the std = " + testMeanAbsoluteErrorStd);
	}
	
	public void cep() throws Exception {
		String path = "dataset/705_cell-normalize-Regression.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		data.setClassIndex(data.numAttributes()-1);
		long start = System.currentTimeMillis();
		regression(data);
		long end = System.currentTimeMillis();
		System.out.println((end-start)*1.0/1000+"s");
		
	}
	public static void main(String[] args) {
		IntegModel im = new IntegModel();
		try {
			im.cep();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println(" finished");
		}
	}

}
