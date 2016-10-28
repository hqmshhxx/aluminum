package cluster;

import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;

public class Distance {

	public double dis(double[] arr1, double[] arr2) {
		double sum = 0.0;
		for (int i = 0; i < arr1.length; i++) {
			sum += Math.pow(arr1[i] - arr2[i], 2);
		}
		return Math.sqrt(sum);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		double[] arr1 = new double[] { 1440.0, 2589.0, 3.942, 11.0, 9.0, 8.0,
				955.0, 2.56, 20.0, 250.0, 210.0, 1350.0, 0.185, 0.04 };
		double[] arr2 = new double[] { 1439.6148076226207, 2856.3180913615633,
				3.9539567711908754, 17.45440682896458, 13.32025566217351,
				7.442745416472658, 951.629846959992, 2.5046455305820157,
				16.11810972943862, 259.05781080700024, 191.94504275600278,
				1423.8433806679518, 0.14983171555858746, 0.03459933663678528 };
		Distance dis = new Distance();
		double d = dis.dis(arr1, arr2);
		System.out.println(d);
		LoadData ld = new LoadData();
		Instances instances = ld
				.loadData("/home/ucas/software/aluminium-electrolysis/one-log/101-normalize.arff");
		EuclideanDistance ed = new EuclideanDistance();
		ed.setInstances(instances);
		Instance one = new DenseInstance(1.0, arr1);
		Instance two = new DenseInstance(1.0, arr2);
		double val = ed.distance(one, two);
		System.out.println(val);

	}

}
