package unnormalize;

import java.util.HashMap;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;

public class Unnormalize {
	private Map<Integer,MmPair> map=new HashMap<>();
	private Instances instances;
	public void init(){
		MmPair pairOne=new MmPair(340,3542);
		MmPair pairTwo=new MmPair(92,976);
		MmPair pairThree=new MmPair(3.896,4.018);
		MmPair pairFour=new MmPair(940,961);
		MmPair pairFive=new MmPair(2.18,2.66);
		MmPair pairSix=new MmPair(220,270);
		MmPair pairSeven=new MmPair(130,230);
		MmPair pairEight=new MmPair(1350,1500);
		MmPair pairNine=new MmPair(194,196.3);
		map.put(0, pairOne);
		map.put(1, pairTwo);
		map.put(2, pairThree);
		map.put(3, pairFour);
		map.put(4, pairFive);
		map.put(5, pairSix);
		map.put(6, pairSeven);
		map.put(7, pairEight);
		map.put(8, pairNine);
		
	}
	
	
	public void unnor(double[] record){
		double[] plainRecord=new double[record.length];
		for(int i=0; i<record.length-1; i++){
			MmPair pair=map.get(i);
			plainRecord[i]=record[i]*(pair.getMax()-pair.getMin())+pair.getMin();
		}
		for(double value : plainRecord){
			System.out.print(value+",");
		}
		System.out.println();
	}
	public void batchUnnor(){
		LoadData loadData= new LoadData();
		instances=loadData.loadData("/home/ucas/software/aluminium-electrolysis/CSV日报/CSV一厂房日报/101-102-r90-r88-less-attr.arff");
		System.out.println("the result :");
		for(int i=0; i<instances.numInstances(); i++){
			unnor(instances.instance(i).toDoubleArray());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Unnormalize um= new Unnormalize();
		um.init();
		um.batchUnnor();

	}

}