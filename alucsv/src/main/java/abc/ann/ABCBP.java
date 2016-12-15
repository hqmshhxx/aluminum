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

	public void predict(Instances train) throws Exception {
		abcAnn.setData(train);
		abcAnn.setBp(bp);
		abcAnn.setInputNum(train.numAttributes() - 1);
		abcAnn.setHiddenNum(3);
		abcAnn.setOutNum(train.numClasses());
		abcAnn.build();
		System.out.println("人工蜂群的最小值：" + abcAnn.getMinObjFunValue());
		double[] weights = abcAnn.getBestFood();
		bp.initWeights(weights);
		bp.buildClassifier(null);
		
	}

	public void classifier(Instances data)  throws Exception{
		int mIter = 2;
		
		double rootMeanSquaredError = 0;
		double meanAbsoluteError = 0;
		double rootMeanSquaredErrorStd = 0;
		double meanAbsoluteErrorStd = 0;
		double[] rootMeanSquaredResults = new double[mIter];
		double[] meanAbsoluteResults = new double[mIter];
		for (int k = 0; k < mIter; k++) {
			
			predict(data);
			
			System.out.println("iter =" + k);
			Evaluation evaluation = new Evaluation(data);
			evaluation.evaluateModel(bp, data);
			rootMeanSquaredError += evaluation.rootMeanSquaredError();
			meanAbsoluteError += evaluation.meanAbsoluteError();
			System.out.println(evaluation.toSummaryString());
		}
		rootMeanSquaredError /= mIter;
		meanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			rootMeanSquaredErrorStd += Math.pow(rootMeanSquaredResults[i] - rootMeanSquaredError, 2);
			meanAbsoluteErrorStd += Math.pow(meanAbsoluteResults[i] - meanAbsoluteError, 2);
		}
		rootMeanSquaredErrorStd = Math.sqrt(rootMeanSquaredErrorStd);
		meanAbsoluteErrorStd = Math.sqrt(meanAbsoluteErrorStd);
		System.out.println("train mean of rootMeanSquaredError = " + rootMeanSquaredError + " the std = " + rootMeanSquaredErrorStd);
		System.out.println("train mean of meanAbsoluteError = " + meanAbsoluteError + " the std = " + meanAbsoluteErrorStd);
	}
	public void regression(Instances data) throws Exception{
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
			
			predict(trainCopy);
			
			System.out.println("iter =" + k);
			Evaluation evaluation = new Evaluation(trainCopy);
			evaluation.evaluateModel(bp, testCopy);
			rootMeanSquaredError += evaluation.rootMeanSquaredError();
			meanAbsoluteError += evaluation.meanAbsoluteError();
			System.out.println(evaluation.toSummaryString());
		}
		rootMeanSquaredError /= mIter;
		meanAbsoluteError /= mIter;
		for (int i = 0; i < mIter; i++) {
			rootMeanSquaredErrorStd += Math.pow(rootMeanSquaredResults[i] - rootMeanSquaredError, 2);
			meanAbsoluteErrorStd += Math.pow(meanAbsoluteResults[i] - meanAbsoluteError, 2);
		}
		rootMeanSquaredErrorStd = Math.sqrt(rootMeanSquaredErrorStd);
		meanAbsoluteErrorStd = Math.sqrt(meanAbsoluteErrorStd);
		System.out.println("test mean of rootMeanSquaredError = " + rootMeanSquaredError + " the std = " + rootMeanSquaredErrorStd);
		System.out.println("test mean of meanAbsoluteError = " + meanAbsoluteError + " the std = " + meanAbsoluteErrorStd);
	}

	public void cep() throws Exception {
		String path = "dataset/heart-disease-normalize.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		data.setClassIndex(data.numAttributes()-1);
		if(data.numClasses()>1){
			classifier(data);
		}else{
			regression(data);
		}
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
