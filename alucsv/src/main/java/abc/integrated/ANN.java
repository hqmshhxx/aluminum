package abc.integrated;

import java.util.Random;
import java.util.concurrent.Callable;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import abc.ann.ABCANN;
import abc.ann.MultilayerPerceptron;

public class ANN implements Callable<Boolean>{
	
	private ABCANN abcAnn;
	private MultilayerPerceptron bp;
	private Instances data;
	
	public ANN(Instances data) {
		this.data = data;
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}
	public ANN(Instances data,MultilayerPerceptron bp) {
		this.data = data;
		abcAnn = new ABCANN();
		this.bp = bp;
	}
	public ANN(Instances data,ABCANN abcAnn,MultilayerPerceptron bp) {
		this.data = data;
		this.abcAnn = abcAnn;
		this.bp = bp;
	}

	@Override
	public Boolean call() throws Exception {
		abcAnn.setData(data);
		abcAnn.setBp(bp);
		abcAnn.setInputNum(data.numAttributes() - 1);
		abcAnn.setHiddenNum(12);
		abcAnn.setOutNum(data.numClasses());
		abcAnn.build();
		System.out.println("人工蜂群的最小值：" + abcAnn.getMinObjFunValue());
		double[] weights = abcAnn.getBestFood();
		bp.initWeights(weights);
		bp.buildClassifier(null);
		regression();
		return true;
	}
	
	public void regression()  throws Exception{
		Random rand = new Random();
		int mIter = 1;
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
			rand.setSeed(k);
			data.randomize(rand);
			for (int q = 0; q < dataNum; q++) {
				if (q < trainNum) {
					train.add(data.instance(q));
				} else {
					test.add(data.instance(q));
				}
			}
			
			Evaluation trainEvaluation = new Evaluation(train);
			trainEvaluation.evaluateModel(bp, train);
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

}
