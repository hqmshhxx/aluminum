package abc.ann;

import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.Utils;
import cluster.LoadData;

public class ABCBP {

	private ABCANN abcAnn;
	private MultilayerPerceptron bp;

	public ABCBP() {
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}

	public void predict(Instances train) throws Exception {
		abcAnn.setData(train);
		abcAnn.setBp(bp);
		abcAnn.setInputNum(train.numAttributes() - 1);
		abcAnn.setHiddenNum(6);
		abcAnn.setOutNum(train.numClasses());
		abcAnn.build();
		System.out.println("人工蜂群的最小值：" + abcAnn.getMinObjFunValue());
		double[] weights = abcAnn.getBestFood();
		bp.initWeights(weights);
		bp.buildClassifier(null);
		
	}

	public void regression(Instances data)  throws Exception{
		Random rand = new Random();
		int mIter = 5;
		int dataNum = data.numInstances();
		int trainNum = (int) (0.75 * dataNum);
		int testNum = dataNum - trainNum;
		Instances train = new Instances(data, trainNum);
		Instances test = new Instances(data, testNum);
		double trainRootMeanSquaredError = 0;
		double trainMeanAbsoluteError = 0;
		double trainRootMeanSquaredErrorStd = 0;
		double trainMeanAbsoluteErrorStd = 0;
		double[] trainRootMeanSquaredResults = new double[mIter];
		double[] trainMeanAbsoluteResults = new double[mIter];
		
		double testRootMeanSquaredError = 0;
		double testMeanAbsoluteError = 0;
		double testRootMeanSquaredErrorStd = 0;
		double testMeanAbsoluteErrorStd = 0;
		double[] testRootMeanSquaredResults = new double[mIter];
		double[] testMeanAbsoluteResults = new double[mIter];
		for (int k = 0; k < mIter; k++) {
			train.clear();
			test.clear();
			data.randomize(rand);
			for (int q = 0; q < dataNum; q++) {
				if (q < trainNum) {
					train.add(data.instance(q));
				} else {
					test.add(data.instance(q));
				}
			}
			predict(train);
			
			System.out.println("iter =" + k);
			Evaluation trainEvaluation = new Evaluation(data);
			trainEvaluation.evaluateModel(bp, data);
			trainRootMeanSquaredError += trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteError += trainEvaluation.meanAbsoluteError();
			
			trainRootMeanSquaredResults[k] = trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteResults[k] = trainEvaluation.meanAbsoluteError();
			System.out.println(trainEvaluation.toSummaryString());
			
			Evaluation testEvaluation = new Evaluation(test);
			testEvaluation.evaluateModel(bp, test);
			testRootMeanSquaredError += testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteError += testEvaluation.meanAbsoluteError();
			
			testRootMeanSquaredResults[k] = testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteResults[k] = testEvaluation.meanAbsoluteError();
			System.out.println(testEvaluation.toSummaryString());
			
		}
		trainRootMeanSquaredError /= mIter;
		trainMeanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			trainRootMeanSquaredError += Math.pow(trainRootMeanSquaredResults[i] - trainRootMeanSquaredError, 2);
			trainMeanAbsoluteErrorStd += Math.pow(trainMeanAbsoluteResults[i] - trainMeanAbsoluteError, 2);
		}
		trainRootMeanSquaredErrorStd = Math.sqrt(trainRootMeanSquaredErrorStd);
		trainMeanAbsoluteErrorStd = Math.sqrt(trainMeanAbsoluteErrorStd);
		System.out.println("train mean of rootMeanSquaredError = " + trainRootMeanSquaredError + " the std = " + trainRootMeanSquaredErrorStd);
		System.out.println("train mean of meanAbsoluteError = " + trainMeanAbsoluteError + " the std = " + trainMeanAbsoluteErrorStd);
		
		
		testRootMeanSquaredError /= mIter;
		testMeanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			testRootMeanSquaredError += Math.pow(testRootMeanSquaredResults[i] - testRootMeanSquaredError, 2);
			testMeanAbsoluteErrorStd += Math.pow(testMeanAbsoluteResults[i] - testMeanAbsoluteError, 2);
		}
		testRootMeanSquaredErrorStd = Math.sqrt(testRootMeanSquaredErrorStd);
		testMeanAbsoluteErrorStd = Math.sqrt(testMeanAbsoluteErrorStd);
		System.out.println("test mean of rootMeanSquaredError = " + testRootMeanSquaredError + " the std = " + testRootMeanSquaredErrorStd);
		System.out.println("test mean of meanAbsoluteError = " + testMeanAbsoluteError + " the std = " + testMeanAbsoluteErrorStd);
	}
	public void classifier(Instances data)  throws Exception{
		Random rand = new Random();
		int mIter = 10;
		int dataNum = data.numInstances();
		int trainNum = (int) (0.6 * dataNum);
		int testNum = dataNum - trainNum;
		Instances train = new Instances(data, trainNum);
		Instances test = new Instances(data, testNum);
	
		double correctRate = 0;
		double correctRateStd = 0;
		double[] correctRateResults = new double[mIter];
/*
		double trainRootMeanSquaredError = 0;
		double trainMeanAbsoluteError = 0;
		double trainRootMeanSquaredErrorStd = 0;
		double trainMeanAbsoluteErrorStd = 0;
		double[] trainRootMeanSquaredResults = new double[mIter];
		double[] trainMeanAbsoluteResults = new double[mIter];
*/		
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
			predict(train);
			System.out.println("iter =" + k);
/*			
			Evaluation trainEvaluation = new Evaluation(data);
			trainEvaluation.evaluateModel(bp, data);
			trainRootMeanSquaredResults[k]= trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteResults[k] = trainEvaluation.meanAbsoluteError();
			trainRootMeanSquaredError += trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteError += trainEvaluation.meanAbsoluteError();
			System.out.println(trainEvaluation.toSummaryString());
*/			
			
			Evaluation testEvaluation = new Evaluation(test);
			testEvaluation.evaluateModel(bp, test);
			
			correctRate += testEvaluation.pctCorrect();
			testRootMeanSquaredError += testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteError += testEvaluation.meanAbsoluteError();
			
			correctRateResults[k] = testEvaluation.pctCorrect();
			testRootMeanSquaredResults[k] = testEvaluation.rootMeanSquaredError();
			testMeanAbsoluteResults[k] = testEvaluation.meanAbsoluteError();
			
			System.out.println(testEvaluation.toSummaryString());
		}
		correctRate /= mIter;
		testRootMeanSquaredError /= mIter;
		testMeanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			correctRateStd += Math.pow(correctRateResults[i], correctRate);
			testRootMeanSquaredErrorStd += Math.pow(testRootMeanSquaredResults[i] - testRootMeanSquaredError, 2);
			testMeanAbsoluteErrorStd += Math.pow(testMeanAbsoluteResults[i] - testMeanAbsoluteError, 2);
		}
		correctRateStd = Math.sqrt(correctRateStd);
		testRootMeanSquaredErrorStd = Math.sqrt(testRootMeanSquaredErrorStd);
		testMeanAbsoluteErrorStd = Math.sqrt(testMeanAbsoluteErrorStd);
		int maxIndex = Utils.maxIndex(correctRateResults);
		int minIndex = Utils.minIndex(correctRateResults);
		System.out.println("test mean of correct rate = " + correctRate + " the std = " + correctRateStd);
		System.out.println("test best of correct rate = " + correctRateResults[maxIndex] + " worst of correct rate = " + correctRateResults[minIndex]);
		System.out.println("test mean of rootMeanSquaredError = " + testRootMeanSquaredError + " the std = " + testRootMeanSquaredErrorStd);
		System.out.println("test mean of meanAbsoluteError = " + testMeanAbsoluteError + " the std = " + testMeanAbsoluteErrorStd);
	}
	

	public void cep() throws Exception {
		String path = "dataset/iris-normalize.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		data.setClassIndex(data.numAttributes()-1);
		classifier(data);
		
	}
	public static void main(String[] args) {
		ABCBP abcBp = new ABCBP();
		long start = System.currentTimeMillis();
		long end = 0;
		try {
			abcBp.cep();
			end = System.currentTimeMillis();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			long duration = (end -start)/1000;
			System.out.print("duration time is "+ duration+ " 秒");
			System.out.println("iris  finished");
		}
	}

}
