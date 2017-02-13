package data;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.XRFFSaver;

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
	
	public Instances changeTarget(Instances instances){
		
		Instances newIns = new Instances(instances);
		Random rand = new Random();
		rand.setSeed(0);
		int numIns = instances.numInstances();
		int numAttr = instances.numAttributes();
		for(int i=0; i<numIns;i++){
			instances.instance(i).setValue(numAttr-1, 88.0 + 2*rand.nextDouble());
		}
		return newIns;
	}
	
	public void saveData(Instances instances,String fileName){
		XRFFSaver saver = new XRFFSaver();
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
	public static void main(String[] args){
		LoadData ld = new LoadData();
		String fileName ="";
		Instances instances = ld.loadData(fileName);
		Instances newIns = ld.changeTarget(instances);
		String toPath = "";
		ld.saveData(newIns,toPath);
	}

}
