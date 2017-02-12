package dc;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import data.LoadData;
import weka.core.Instances;


public class LinePlot {
	
	public void buildLine(){
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("黑体", Font.BOLD, 20));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		double[] plain = createData();
		for(int i=0; i<plain.length; i++){
			dataset.addValue(plain[i], "first", i+"");
		}
	
		JFreeChart line = ChartFactory.createLineChart("预测对比图", "个数", "电流效率", null);
		CategoryPlot mPlot = (CategoryPlot)line.getPlot();
		mPlot.setDataset(dataset);
		NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
		numberAxis.setAutoRangeMinimumSize(1);
		numberAxis.setRange(87, 97);
		
		LineAndShapeRenderer renderer = new LineAndShapeRenderer();
//		mPlot.setRenderer(0, renderer);
		mPlot.setBackgroundPaint(Color.LIGHT_GRAY);
		mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
		mPlot.setOutlinePaint(Color.RED);//边界线
		
		ChartFrame mChartFrame = new ChartFrame("折线图", line);
		mChartFrame.pack();
		mChartFrame.setVisible(true);
	}
	
	public double[] createData(){
		String path = "dataset/705-plain.arff";
		LoadData ld = new LoadData();
		Instances data = ld.loadData(path);
		int count = data.numInstances();
		int num = data.numAttributes();
		double[] plain = new double[count];
		for(int i=0; i<count; i++){
			plain[i] = data.instance(i).value(num-1);
		}
		return plain;
	}
    public static void main( String[] args ){
        LinePlot lp = new LinePlot();
        lp.buildLine();
    }
}
