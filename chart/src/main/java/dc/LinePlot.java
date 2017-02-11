package dc;

import java.awt.Color;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

public class LinePlot {
	
	public void buildLine(){
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("黑体", Font.BOLD, 20));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(10,"first ", "1");
		dataset.addValue(8,"first ", "2");
		dataset.addValue(9,"first ", "3");
		dataset.addValue(11,"first ", "4");
		dataset.addValue(10,"first ", "5");
		JFreeChart line = ChartFactory.createLineChart("预测对比图", "个数", "电流效率", dataset);
		CategoryPlot mPlot = (CategoryPlot)line.getPlot();
		mPlot.setBackgroundPaint(Color.LIGHT_GRAY);
		mPlot.setRangeGridlinePaint(Color.BLUE);//背景底部横虚线
		mPlot.setOutlinePaint(Color.RED);//边界线
		
		ChartFrame mChartFrame = new ChartFrame("折线图", line);
		mChartFrame.pack();
		mChartFrame.setVisible(true);
	}
	
    public static void main( String[] args ){
        LinePlot lp = new LinePlot();
        lp.buildLine();
    }
}
