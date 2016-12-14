package abc.ann;

import java.util.Random;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import cluster.LoadData;

public class ABCBP {

	private ABCANN abcAnn;
	private MultilayerPerceptron bp;

	public ABCBP() {
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}

	public double predict(Instances train, Instances test) throws Exception {
		abcAnn.setData(train);
		abcAnn.setInputNum(train.numAttributes() - 1);
		abcAnn.setHiddenNum(3);
		abcAnn.setOutNum(1);

		abcAnn.initial();
		abcAnn.memorizeBestSource();
		for (int iter = 0; iter < abcAnn.maxCycle; iter++) {
			abcAnn.mCycle = iter + 1;
			abcAnn.sendEmployedBees();
			abcAnn.calculateProbabilities();
			abcAnn.sendOnlookerBees();
			abcAnn.memorizeBestSource();
			abcAnn.sendScoutBees();
			System.out.println("mcycle = " + abcAnn.mCycle);
		}
		System.out.println("人工蜂群的最小值：" + abcAnn.getMinObjFunValue());
		double[] weights = abcAnn.getBestFood();
		bp.buildNetwork(train);
		bp.initWeights(weights);
		bp.buildClassifier(train);
		System.out.println(bp.toString());
		return bp.testError(test);
	}

	public void cep() throws Exception {
		String path = "dataset/Concrete-normalize.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		Random rand = new Random();
		int mIter = 10;
		int dataNum = data.numInstances();
		int trainNum = (int) (0.75 * dataNum);
		int testNum = dataNum - trainNum;
		Instances train = new Instances(data, trainNum);
		Instances test = new Instances(data, testNum);
		double rootMeanSquaredError = 0;
		double meanAbsoluteError = 0;
		double rootMeanSquaredErrorStd = 0;
		double meanAbsoluteErrorStd = 0;
		double[] rootMeanSquaredResults = new double[mIter];
		double[] meanAbsoluteResults = new double[mIter];
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

			Instances trainCopy = new Instances(train);
			System.out.println(trainCopy.numInstances());
			Instances testCopy = new Instances(test);
			
			predict(trainCopy, testCopy);
			System.out.println("iter =" + k);
			Evaluation evaluation = new Evaluation(trainCopy);
			evaluation.evaluateModel(bp, testCopy);
			rootMeanSquaredError += evaluation.rootMeanSquaredError();
			meanAbsoluteError += evaluation.meanAbsoluteError();
//			System.out.println(evaluation.toSummaryString());
		}
		rootMeanSquaredError /= mIter;
		meanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			rootMeanSquaredErrorStd += Math.pow(rootMeanSquaredResults[i] - rootMeanSquaredError, 2);
			meanAbsoluteErrorStd += Math.pow(meanAbsoluteResults[i] - meanAbsoluteError, 2);
		}
		rootMeanSquaredErrorStd = Math.sqrt(rootMeanSquaredErrorStd);
		meanAbsoluteErrorStd = Math.sqrt(meanAbsoluteErrorStd);
		System.out.println("mean of rootMeanSquaredError = " + rootMeanSquaredError + " the std = " + rootMeanSquaredErrorStd);
		System.out.println("mean of meanAbsoluteError = " + meanAbsoluteError + " the std = " + meanAbsoluteErrorStd);
	}
	
	
	public static void main(String[] args) {
		ABCBP abcBp = new ABCBP();
		try {
			abcBp.cep();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("finished");
		}
	}

}
