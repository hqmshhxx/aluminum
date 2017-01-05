package abc.integrated;

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
		
	}
}
