package cluster;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class ApacheFCM {
	
	private Instances instances;
	private List<DoublePoint> points=new ArrayList<>();
	private List<CentroidCluster<DoublePoint>> clusters =new ArrayList<>() ;
	
	public Instances loadData(String fileName) {
		ArffLoader loader = new ArffLoader();
		try {
			loader.setFile(new File(fileName));
			instances = loader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
			instances = null;
		}
		return instances;
	}
	public void buildCluster(){
		DistanceMeasure measure=new EuclideanDistance();
		RandomGenerator random=new JDKRandomGenerator();
		random.setSeed(10);
		for(int i=0; i<instances.numInstances(); i++){
			double[] dataset=new double[instances.numAttributes()];
			for(int j=0; j<instances.numAttributes(); j++){
				dataset[j]=instances.instance(i).value(j);
			}
			DoublePoint dp=new DoublePoint(dataset);
			points.add(dp);
		}
		
		FKM<DoublePoint> fmc=new FKM<>(3,2,500,measure,1e-3,random);
		clusters=fmc.cluster(points);
		System.out.println("iterator: "+fmc.getIteration());
		for(int i=0; i<clusters.size();i++){
			System.out.println(clusters.get(i).getPoints().size());
			double[] values=clusters.get(i).getCenter().getPoint();
			for(double value : values){
				System.out.print(value+" ");
			}
			System.out.println();
		}
		
	
		
	}
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ApacheFCM bi = new ApacheFCM();
//		bi.loadData("/home/ucas/software/aluminium-electrolysis/one-log/101.arff");
		bi.loadData("/home/ucas/software/aluminium-electrolysis/CSV日报/7820.arff");

		bi.buildCluster();
		
	}

}
