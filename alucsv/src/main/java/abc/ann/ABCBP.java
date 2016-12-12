package abc.ann;

import weka.core.Instances;
import cluster.LoadData;

public class ABCBP {
	
	private ABCANN abcAnn;
	private MultilayerPerceptron bp;
	
	
	public ABCBP(){
		abcAnn = new ABCANN();
		bp = new MultilayerPerceptron();
	}
	
	public void buildNet() throws Exception{
		String path = "dataset/XOR.arff";
		LoadData ld = new LoadData();
		Instances instances = ld.loadData(path);
		abcAnn.setData(instances);
		abcAnn.setInputNum(2);
		abcAnn.setHiddenNum(3);
		abcAnn.setOutNum(1);
	
		abcAnn.initial();
		abcAnn.memorizeBestSource();
		for (int iter = 0; iter < abcAnn.maxCycle; iter++) {
			abcAnn.mCycle = iter+1;
			abcAnn.sendEmployedBees();
			abcAnn.calculateProbabilities();
			abcAnn.sendOnlookerBees();
			abcAnn.memorizeBestSource();
			abcAnn.sendScoutBees();
		}
		System.out.println("人工蜂群的最小值："+abcAnn.getMinObjFunValue());
		double[] weights = abcAnn.getBestFood();
		bp.buildNetwork(instances);
		bp.initWeights(weights);
		bp.buildClassifier(instances);
		System.out.println(bp.toString());
	}
	
	
	public static void main(String[] args){
		ABCBP abcBp = new ABCBP();
		try {
			abcBp.buildNet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			System.out.println("finished");
		}
	}

}
