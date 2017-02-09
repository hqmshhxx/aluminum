package abc.integrated;

import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import abc.ann.ABCANN;
import abc.ann.MultilayerPerceptron;

public class LastNet {
	private ABCANN abcAnn;
	private MultilayerPerceptron bp;

	public LastNet() {
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}
	
	public void build(Instances train) throws Exception {
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
		regression(train);
	}
	
	public  MultilayerPerceptron getBP(){
		return bp;
	}
	
	public void regression(Instances train)  throws Exception{
		Random rand = new Random();
		int mIter = 1;
		double trainRootMeanSquaredError = 0;
		double trainMeanAbsoluteError = 0;
		double trainRootMeanSquaredErrorStd = 0;
		double trainMeanAbsoluteErrorStd = 0;
		double[] trainRootMeanSquaredResults = new double[mIter];
		double[] trainMeanAbsoluteResults = new double[mIter];
	
		for (int k = 0; k < mIter; k++) {
			rand.setSeed(k);
			
			Evaluation trainEvaluation = new Evaluation(train);
			trainEvaluation.evaluateModel(bp, train);
			trainRootMeanSquaredError += trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteError += trainEvaluation.meanAbsoluteError();
			
			trainRootMeanSquaredResults[k] = trainEvaluation.rootMeanSquaredError();
			trainMeanAbsoluteResults[k] = trainEvaluation.meanAbsoluteError();
			System.out.println(trainEvaluation.toSummaryString());
			
		}
		trainRootMeanSquaredError /= mIter;
		trainMeanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			trainRootMeanSquaredError += Math.pow(trainRootMeanSquaredResults[i] - trainRootMeanSquaredError, 2);
			trainMeanAbsoluteErrorStd += Math.pow(trainMeanAbsoluteResults[i] - trainMeanAbsoluteError, 2);
		}
		trainRootMeanSquaredErrorStd = Math.sqrt(trainRootMeanSquaredErrorStd);
		trainMeanAbsoluteErrorStd = Math.sqrt(trainMeanAbsoluteErrorStd);
		System.out.println("last train mean of rootMeanSquaredError = " + trainRootMeanSquaredError + " the std = " + trainRootMeanSquaredErrorStd);
		System.out.println("last train mean of meanAbsoluteError = " + trainMeanAbsoluteError + " the std = " + trainMeanAbsoluteErrorStd);
		
	}
}
