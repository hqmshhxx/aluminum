package cluster;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class BiFCM {

	/** 属性的最少个数 */
	private int minAttributes = 2;
	/** instance的最少个数 */
	private int minInstances = 2;
	/** 目标函数值改变量范围 */
	private double endValue = 1e-2;

	private double mValue = 0;
	private double alpha = 2;

	private Instances instances;
	private int rows = 0;
	private int cols = 0;
	private double means=0.0;
	
	private Instances[] array;
	private int plainRows=0;
	private int plainCols=0;
	private Instances plain;

	private int mIterations = 0;
	private int maxIterations = 500;
	protected ReplaceMissingValues m_ReplaceMissingFilter;
	protected NominalToBinary m_NominalToBinary;
	protected boolean m_dontReplaceMissing = false;

	public void loadData(String fileName) {
		ArffLoader loader = new ArffLoader();
		try {
			loader.setFile(new File(fileName));
			instances = loader.getDataSet();
		} catch (IOException e) {
			e.printStackTrace();
			instances = null;
		}
	}

	public void multipleNodeDeletion() {

		m_ReplaceMissingFilter = new ReplaceMissingValues();
		instances.setClassIndex(-1);
		if (!m_dontReplaceMissing) {
			try {
				m_ReplaceMissingFilter.setInputFormat(instances);
				instances = Filter.useFilter(instances, m_ReplaceMissingFilter);
				  // convert nominal attributes
//			    m_NominalToBinary = new NominalToBinary();
//			    m_NominalToBinary.setInputFormat(instances);
//			    instances = Filter.useFilter(instances, m_NominalToBinary);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int count;
		array=new Instances[3];
		array[0]=new Instances(instances,0);
		array[1]=new Instances(instances,0);
	
		plain=new Instances(instances);
		plainRows=instances.numInstances();
		plainCols=instances.numAttributes();
		
		while (true) {
			mIterations++;
			System.out.println("mIterations = "+mIterations);
			count=0;
			rows = instances.numInstances();
			cols = instances.numAttributes();
		
			/*compute H(I,J)*/
			means=calculateMeans();
			mValue = calculateH();
			
			System.out.println("single deletion\nmValue = "+ mValue+" rows = "+rows+" cols = "+cols);
			
			if (mValue <= endValue) {
				System.out.println("找到了最小值");
				break;
			}
			if(rows<=minInstances||cols<=minAttributes){
				break;
			}
			
			double[] mScores = new double[rows];
		
			for (int i = 0; i <  instances.numInstances(); i++) {
				double iJ=calculate_iJ(i);
				mScores[i] = calculateInstances(i,iJ);
			}
			
			for (int j = 0; j <  instances.numInstances(); j++) {
				if (mScores[j] > alpha * mValue) {
					if(plainCols==instances.numAttributes()){
						array[0].add(plain.instance(j));
					}else{
						array[1].add(plain.instance(j));
					}
					plain.delete(j-count);
					instances.delete(j - count);
					++count;
				}
			}
			mScores=null;
			count = 0;
		
			rows = instances.numInstances();
			cols = instances.numAttributes();
			/*compute H(I,J)*/
			means = calculateMeans();
			mValue = calculateH();
		
			System.out.println("mutiple deletion rows\nmValue = "+ mValue+" rows = "+rows+" cols = "+cols);
			
			if(rows<minInstances||cols<minAttributes){
				break;
			}
			mScores = new double[cols];
			for (int j = 0; j < instances.numAttributes(); j++) {
				double Ij=calculate_Ij(j);
				mScores[j] = calculateAttributes(j,Ij);
			}
			
			for (int j = 0; j < cols; j++) {
				if (mScores[j] > alpha * mValue) {
					instances.deleteAttributeAt(j - count);
					++count;
				}
			}
			rows = instances.numInstances();
			cols = instances.numAttributes();
			/*compute H(I,J)*/
			means = calculateMeans();
			mValue = calculateH();
			System.out.println("mutiple deletion cols\nmValue = "+ mValue+" rows = "+rows+" cols = "+cols);

			if (mValue <= endValue) {
				System.out.println("找到了最小值");
				break;
			}

			if (rows == instances.numInstances() && cols == instances.numAttributes()) {
				singleNodeDeletion();
			}

			if (mIterations == maxIterations) {
				System.out.println("到达最大循环数");
				break;
			}
			System.out.println("==============================");
		}
		array[2]=new Instances(plain);
	}

	public void singleNodeDeletion() {

		double[] mScores = new double[instances.numInstances()];
		int maxRowIndex = 0;
		int maxColIndex = 0;
		double[] maxH = new double[2];

		for (int i = 0; i <  instances.numInstances(); i++) {
			double iJ=calculate_iJ(i);
			mScores[i] = calculateInstances(i,iJ);
		}
		maxRowIndex = Utils.maxIndex(mScores);
		maxH[0] = mScores[maxRowIndex];

		mScores = new double[instances.numAttributes()];
		for (int j = 0; j <  instances.numAttributes(); j++) {
			double Ij=calculate_Ij(j);
			mScores[j] = calculateAttributes(j,Ij);
		}
		maxColIndex = Utils.maxIndex(mScores);
		maxH[1] = mScores[maxColIndex];

		if (maxH[0] > maxH[1]) {
			if(plainCols==instances.numAttributes()){
				array[0].add(plain.instance(maxRowIndex));
			}else{
				array[1].add(plain.instance(maxRowIndex));
			}
			plain.delete(maxRowIndex);
			instances.delete(maxRowIndex);
		} else {
			instances.deleteAttributeAt(maxColIndex);
		}
	}

	public double calculateInstances(int row,double iJ) {
		double score = 0;
			for (int j = 0; j < instances.numAttributes(); j++) {
				double Ij= calculate_Ij(j);
				score += Math.pow(instances.instance(row).value(j) - iJ - Ij+ means, 2);
			}
		score /= instances.numAttributes();
		return score;
	}

	public double calculateAttributes(int col,double Ij) {
		double score = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			double iJ = calculate_iJ(i);
			score += Math.pow(instances.instance(i).value(col) - iJ - Ij + means, 2);
		}
		score /= instances.numInstances();
		return score;
	}
	
	public double calculate_iJ(int row){
		double iJ=0.0;
		for (int j = 0; j < instances.numAttributes(); j++) {
			iJ += instances.instance(row).value(j);
		}
		iJ /= instances.numAttributes();
		return iJ;
	}

	public double calculate_Ij(int col){
		double Ij=0.0;
		for (int i = 0; i < instances.numInstances(); i++) {
			Ij += instances.instance(i).value(col);
		}
		Ij /= instances.numInstances();
		return Ij;
	}
	public double calculateH() {
		double score = 0;
		for (int i = 0; i < instances.numInstances(); i++) {
			for (int j = 0; j < instances.numAttributes(); j++) {
				double[] values = calculateIJ( i, j);
				score += Math.pow(instances.instance(i).value(j) - values[0] - values[1] + means, 2);
			}
		}
		score /= (instances.numAttributes() * instances.numInstances());
		return score;
	}

	public double calculateMeans() {
		double mean = 0.0;
		for (int i = 0; i < instances.numInstances(); i++) {
			for (int j = 0; j < instances.numAttributes(); j++) {
				mean += instances.instance(i).value(j);
			}
		}
		mean /= instances.numAttributes() * instances.numInstances();
		return mean;
	}
	public double[] calculateIJ(int row, int col) {
		double scoreI = 0;
		double scoreJ = 0;
		double[] values = new double[2];
		int rowNum = instances.numInstances();
		int colNum = instances.numAttributes();
		
		for (int i = 0; i < instances.numInstances(); i++) {
			scoreI += instances.instance(i).value(col);
		}
		
		for (int j = 0; j < instances.numAttributes(); j++) {
			scoreJ += instances.instance(row).value(j);
		}
	
		values[0] += scoreI * 1.0 / rowNum;
		values[1] += scoreJ * 1.0 / colNum;
		return values;

	}
	
	public int getMinAttributes() {
		return minAttributes;
	}

	public void setMinAttributes(int minAttributes) {
		this.minAttributes = minAttributes;
	}

	public int getMinInstances() {
		return minInstances;
	}

	public void setMinInstances(int minInstances) {
		this.minInstances = minInstances;
	}

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public double getEndValue() {
		return endValue;
	}

	public void setEndValue(double endValue) {
		this.endValue = endValue;
	}

	public String toString() {
		// TODO Auto-generated method stub
		StringBuilder temp = new StringBuilder();
		temp.append("\nBiCluster\n======\n");
		temp.append("Number of iterations: " + mIterations);
		temp.append("\nthe value is: " + mValue);
		
		temp.append("\narray[0] length: "+array[0].numInstances());
		temp.append("\narray[1] length: "+array[1].numInstances());
		temp.append("\narray[2] length: "+array[2].numInstances());
		
		/*
		temp.append("\ninstances");
		temp.append("\n"+instances.toString());
		temp.append("\n\n");
*/
		System.out.println(temp.toString());
		return temp.toString();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BiFCM bi = new BiFCM();
		bi.loadData("/home/ucas/software/aluminium-electrolysis/one-log/101-normalize.arff");
		bi.multipleNodeDeletion();
		bi.toString();
	}
}
