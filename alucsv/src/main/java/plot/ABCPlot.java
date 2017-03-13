package plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

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

public class ABCPlot {
	private SABCImpl abc;
	private GABCImpl gabc;
	private IABCImpl iabc;

	public ABCPlot() {
		abc = new SABCImpl(-100, 100, 200);
		gabc = new GABCImpl(-100, 100, 200);
		iabc = new IABCImpl(-100, 100, 200);

	}

	public XYDataset createData() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries first = new XYSeries("abc");
		XYSeries second = new XYSeries("gabc");
		XYSeries third = new XYSeries("iabc");

		abc.runABC();
		gabc.runABC();
		iabc.runABC();

		double[] fd = abc.getMeanFunctionValues();
		for (int i = 0; i < fd.length; i++) {
			first.add(i + 1, fd[i]);
		}
		double[] sd = gabc.getMeanFunctionValues();
		for (int i = 0; i < fd.length; i++) {
			second.add(i + 1, sd[i]);
		}
		double[] td = iabc.getMeanFunctionValues();
		for (int i = 0; i < fd.length; i++) {
			third.add(i + 1, td[i]);
		}
		dataset.addSeries(first);
		dataset.addSeries(second);
		dataset.addSeries(third);
		return dataset;
	}

	public void buildLine() {
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
		mChartTheme.setLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
		mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
		ChartFactory.setChartTheme(mChartTheme);
		XYDataset dataset = createData();

		JFreeChart line = ChartFactory.createXYLineChart("Sphere Function with D=50", "cycles",
				"mean of best function value", dataset);
		XYPlot mPlot = (XYPlot) line.getPlot();
		// Y轴
		NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
		numberAxis.setAutoRangeMinimumSize(1.0);
		numberAxis.setRange(0, 120000);
		// X轴
		NumberAxis domainAxis = (NumberAxis) mPlot.getDomainAxis();
		domainAxis.setAutoRangeMinimumSize(10);
		// domainAxis.setRange(0, 200);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		
		Stroke solid1 = new BasicStroke(5.0f);
		Stroke solid2 = new BasicStroke(3.0f);
		Stroke solid3 = new BasicStroke(1.0f);

		renderer.setSeriesStroke(0, solid1);
		renderer.setSeriesStroke(1, solid2);
		renderer.setSeriesStroke(2, solid3);

		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesShapesVisible(1, false);
		renderer.setSeriesShapesVisible(2, false);
		renderer.setSeriesPaint(2, Color.BLACK);
		
		mPlot.setRenderer(0, renderer);
		mPlot.setBackgroundPaint(Color.WHITE);
		mPlot.setRangeGridlinePaint(Color.BLUE);// 背景底部横虚线
		// mPlot.setOutlinePaint(Color.RED);//边界线

		ChartFrame mChartFrame = new ChartFrame("折线图", line);
		mChartFrame.pack();
		mChartFrame.setVisible(true);
	}

	public static void main(String[] args) {
		ABCPlot lp = new ABCPlot();
		lp.buildLine();
	}
}
