package cluster;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;


public class LoadData {
	
	
	public Instances loadData(String fileName) {
		Instances instances=null;
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
	public Instances changeTarget(Instances instances,double base,int Multiple){
		
		Instances newIns = new Instances(instances);
		Random rand = new Random();
		rand.setSeed(0);
		int numIns = instances.numInstances();
		int numAttr = instances.numAttributes();
		for(int i=0; i<numIns;i++){
			newIns.instance(i).setValue(numAttr-1, base + Multiple*rand.nextDouble());
		}
		return newIns;
	}
	
	public void saveData(Instances instances,String fileName){
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		File outputFile = new File(fileName);
		try {
			saver.setFile(outputFile);
			saver.writeBatch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
