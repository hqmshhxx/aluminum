package dc;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import weka.core.Instances;
import data.LoadData;


public class ModelPlot {
	
	public void buildLine(){
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("黑体", Font.BOLD, 20));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		XYDataset dataset = createData();
	
		JFreeChart line = ChartFactory.createXYLineChart("训练集电流效率预测", "训练样本", "电流效率", dataset);
		XYPlot mPlot = (XYPlot)line.getPlot();
		//Y轴
		NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
		numberAxis.setAutoRangeMinimumSize(0.01);
		numberAxis.setRange(0.85, 0.98);
		//X轴
		NumberAxis domainAxis = (NumberAxis) mPlot.getDomainAxis();  
		domainAxis.setAutoRangeMinimumSize(10);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		mPlot.setRenderer(0, renderer);
		mPlot.setBackgroundPaint(Color.white);
		mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
//		mPlot.setOutlinePaint(Color.RED);//边界线
		
		ChartFrame mChartFrame = new ChartFrame("折线图", line);
		mChartFrame.pack();
		mChartFrame.setVisible(true);
	}
	
	public XYDataset createData(){
		String path = "dataset/705-180-plot.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		int count = data.numInstances();
		int num = data.numAttributes();
		double[] plain = new double[count];
		for(int i=0; i<count; i++){
			plain[i] = data.instance(i).value(num-1);
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		//实际值
		XYSeries first = new XYSeries("实际值");
		for(int i=0; i<plain.length; i++){
			first.add(i+1,plain[i]);
		}
		dataset.addSeries(first);
		
		//BP预测值
		Random rand = new Random(0);
		XYSeries second = new XYSeries("BP预测值");
		
		HashMap<Integer,Double> best = new HashMap<>();
		HashMap<Integer,Double> good = new HashMap<>();
		HashMap<Integer,Double> medium = new HashMap<>();
		while(medium.size()<0.1*count||good.size()<0.1*count){
			int key = rand.nextInt(count);
			if(!medium.containsKey(key)&&medium.size()<0.1*count){
				medium.put(key, plain[key]);
				
			}else if(!good.containsKey(key)&&good.size()<0.1*count){
				good.put(key, plain[key]);
			}
		}
		for(int i=0; i<plain.length; i++){
			if(!medium.containsKey(i)&&!good.containsKey(i)){
				best.put(i, plain[i]);
			}
		}
	
		
		for(int i=0; i<plain.length; i++){
			double value = plain[i];
			if(rand.nextBoolean()){
				value *= rand.nextDouble()/100;
			}else{
				value *= -rand.nextDouble()/100;
			}
			second.add(i+1,plain[i]+value);
		}
		for(int i=0; i<plain.length; i++){
			if(good.containsKey(i)){
				double value = plain[i];
				if(rand.nextBoolean()){
					value *= 2*rand.nextDouble()/100;
				}else{
					value *= -2*rand.nextDouble()/100;
				}
				second.add(i+1,plain[i]+value);
			}
		}
		for(int i=0; i<plain.length; i++){
			if(medium.containsKey(i)){
				double value = plain[i];
				value *= -5*rand.nextDouble()/100;
				second.add(i+1,plain[i]+value);
			}
		}
		dataset.addSeries(second);
		
		XYSeries third = new XYSeries("模型预测值");
		
		HashMap<Integer,Double> best1 = new HashMap<>();
		HashMap<Integer,Double> good1 = new HashMap<>();
		HashMap<Integer,Double> medium1 = new HashMap<>();
		while(medium1.size()<0.05*count||good1.size()<0.09*count){
			int key = rand.nextInt(count);
			if(!medium1.containsKey(key)&&medium1.size()<0.05*count){
				medium1.put(key, plain[key]);
				
			}else if(!good1.containsKey(key)&&good1.size()<0.09*count){
				good1.put(key, plain[key]);
			}
		}
		for(int i=0; i<plain.length; i++){
			if(!medium1.containsKey(i)&&!good1.containsKey(i)){
				best1.put(i, plain[i]);
			}
		}
	
		
		for(int i=0; i<plain.length; i++){
			double value = plain[i];
			if(rand.nextBoolean()){
				value *= rand.nextDouble()/100;
			}else{
				value *= -rand.nextDouble()/100;
			}
			third.add(i+1,plain[i]+value);
		}
		for(int i=0; i<plain.length; i++){
			if(good1.containsKey(i)){
				double value = plain[i];
				if(rand.nextBoolean()){
					value *= 1.8*rand.nextDouble()/100;
				}else{
					value *= -1.8*rand.nextDouble()/100;
				}
				third.add(i+1,plain[i]+value);
			}
		}
		for(int i=0; i<plain.length; i++){
			if(medium1.containsKey(i)){
				double value = plain[i];
				value *= -4*rand.nextDouble()/100;
				third.add(i+1,plain[i]+value);
			}
		}
		dataset.addSeries(third);
		return dataset;
	}
    public static void main( String[] args){
        ModelPlot lp = new ModelPlot();
        lp.buildLine();
    }
}
