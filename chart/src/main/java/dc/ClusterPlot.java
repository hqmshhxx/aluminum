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

public class ClusterPlot {

	public void buildLine(){
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("黑体", Font.BOLD, 20));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		XYDataset dataset = createData();
		JFreeChart line = ChartFactory.createXYLineChart("FCM聚类结果", "训练样本", "电流效率", dataset);
		XYPlot mPlot = (XYPlot)line.getPlot();
		//Y轴
		NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
		numberAxis.setAutoRangeMinimumSize(0.01);
		numberAxis.setRange(0.85, 0.95);
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
		String pathOne = "dataset/cluster/705-fcm-70-plot.arff";
		String pathTwo = "dataset/cluster/705-fcm-47-plot.arff";
		String pathThree = "dataset/cluster/705-fcm-63-plot.arff";
		LoadData ld = new LoadData();
		Instances dataOne = ld.loadData(pathOne);
		Instances dataTwo = ld.loadData(pathTwo);
		Instances dataThree = ld.loadData(pathThree);
		
		double[] plainOne = new double[dataOne.numInstances()];
		for(int i=0; i<dataOne.numInstances(); i++){
			plainOne[i] = dataOne.instance(i).value(dataOne.numAttributes()-1);
		}
		double[] plainTwo = new double[dataTwo.numInstances()];
		for(int i=0; i<dataTwo.numInstances(); i++){
			plainTwo[i] = dataTwo.instance(i).value(dataTwo.numAttributes()-1);
		}
		double[] plainThree = new double[dataThree.numInstances()];
		for(int i=0; i<dataThree.numInstances(); i++){
			plainThree[i] = dataThree.instance(i).value(dataThree.numAttributes()-1);
		}
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		//第一类子样本
		XYSeries first = new XYSeries("第一类子样本");
		for(int i=0; i<plainOne.length; i++){
			first.add(i+1,plainOne[i]);
		}
		dataset.addSeries(first);
		//第二类子样本
		XYSeries second = new XYSeries("第二类子样本");
		for(int i=0; i<plainTwo.length; i++){
			second.add(i+1,plainTwo[i]);
		}
		dataset.addSeries(second);
		//第三类子样本
		XYSeries  three = new XYSeries("第三类子样本");
		for(int i=0; i<plainThree.length; i++){
			three.add(i+1,plainThree[i]);
		}
		dataset.addSeries(three);
		return dataset;
	}
    public static void main( String[] args){
        ClusterPlot cp = new ClusterPlot();
        cp.buildLine();
    }
}
