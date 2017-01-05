package abc.integrated;

import java.util.concurrent.Callable;

import weka.core.Instances;
import abc.ann.ABCANN;
import abc.ann.MultilayerPerceptron;

public class ANN implements Callable<Boolean>{
	
	private ABCANN abcAnn;
	private MultilayerPerceptron bp;
	private Instances train;
	
	public ANN(Instances train) {
		this.train = train;
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}
	public ANN(Instances train,MultilayerPerceptron bp) {
		this.train = train;
		abcAnn = new ABCANN();
		this.bp = bp;
	}
	public ANN(Instances train,ABCANN abcAnn,MultilayerPerceptron bp) {
		this.train = train;
		this.abcAnn = abcAnn;
		this.bp = bp;
	}

	@Override
	public Boolean call() throws Exception {
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
		return true;
	}

}
