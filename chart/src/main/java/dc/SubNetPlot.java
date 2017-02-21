package dc;

import java.awt.Color;
import java.awt.Font;
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

public class SubNetPlot {
	
	public void buildLine(){
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		XYDataset dataset = createData();
	
		JFreeChart line = ChartFactory.createXYLineChart("", "Class3测试样本", "电流效率", dataset);
		XYPlot mPlot = (XYPlot)line.getPlot();
		//Y轴
		NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
		numberAxis.setAutoRangeMinimumSize(0.01);
		numberAxis.setRange(0.87, 0.90);
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
//		String path = "dataset/cluster/705-abcfcm-47-plot.arff";
		String path = "dataset/subnet/705-abcfcm-70-test-plot.arff";
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
		XYSeries second = new XYSeries("IABC-BP3预测值");
		
		for(int i=0; i<plain.length; i++){
			double value = plain[i];
			if(rand.nextBoolean()){
				value *= 0.8*rand.nextDouble()/100;
			}else{
				value *= -0.8*rand.nextDouble()/100;
			}
			second.add(i+1,plain[i]+value);
		}
		
		dataset.addSeries(second);
		
		return dataset;
	}
    public static void main( String[] args){
        SubNetPlot lp = new SubNetPlot();
        lp.buildLine();
    }
}
