package cluster;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffLoader;

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

}
