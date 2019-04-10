package org.processmining.plugins.realtimedcc.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

public class ModelTickerPanel extends JPanel {
	private final  ModelTickerListPanel parent;
	
	private static final long serialVersionUID = -3330443333646409070L;
	
	private static final int COUNT = 2 * 60;
	private DynamicTimeSeriesCollection dataset;
	private JFreeChart chart;
	private JLabel label;
	private final int index;
    
	public ModelTickerPanel(ModelTickerListPanel p, int i) {
		this.parent = p;
		this.index = i;
		this.setLayout(new BorderLayout());
		this.setBackground(Color.WHITE);
		
		dataset = new DynamicTimeSeriesCollection(1, COUNT, new Second());
		dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2013));
		dataset.addSeries(new float[]{}, 0, "");
		chart = createChart(dataset);
		label = new JLabel("Waiting for data...");
		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		JButton button = new JButton("View model");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.notifyShowModel(index);
			}
		});
		this.add(button, BorderLayout.SOUTH);
		this.add(label, BorderLayout.NORTH);
		
		this.add(new ChartPanel(chart), BorderLayout.CENTER);
	}
	
	public void updateTitle(String text) {
		label.setText(text);
	}
	
	public void updateData(float[] newData) {
		ValueAxis range = chart.getXYPlot().getRangeAxis();
		if (newData[0] >= range.getRange().getUpperBound())
			range.setRange(0, newData[0]);
		
        dataset.advanceTime();
        dataset.appendData(newData);
	}
	
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("", "", "", dataset, false, false, false);
		final XYPlot plot = chart.getXYPlot();
		ValueAxis domain = plot.getDomainAxis();
		domain.setAutoRange(true);
		domain.setVisible(false);
		ValueAxis range = plot.getRangeAxis();
		range.setRange(0, 3);
		
		StandardChartTheme theme = (StandardChartTheme) org.jfree.chart.StandardChartTheme.createJFreeTheme();
		theme.setRangeGridlinePaint(Color.decode("#C0C0C0"));
		theme.setPlotBackgroundPaint(Color.white);
		theme.setChartBackgroundPaint(Color.white);
		theme.setGridBandPaint(Color.red);
		theme.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
		theme.setBarPainter(new StandardBarPainter());
		theme.setAxisLabelPaint(Color.decode("#666666"));
		theme.apply(chart);
		chart.getXYPlot().setOutlineVisible(false);
		chart.getXYPlot().getRangeAxis().setAxisLineVisible(false);
		chart.getXYPlot().getRangeAxis().setTickMarksVisible(false);
		chart.getXYPlot().setRangeGridlineStroke(new BasicStroke());
		chart.getXYPlot().getRangeAxis().setTickLabelPaint(Color.decode("#666666"));
		chart.getXYPlot().getDomainAxis().setAxisLineVisible(false);
		chart.getXYPlot().getDomainAxis().setTickMarksVisible(false);
		chart.getXYPlot().getDomainAxis().setTickLabelPaint(Color.decode("#666666"));
		chart.setTextAntiAlias(true);
		chart.setAntiAlias(true);
		chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.decode("#4572a7"));
		chart.getXYPlot().getRenderer().setSeriesStroke(0, new BasicStroke(3));
		return chart;
	}
	
	 @Override
     public Dimension getPreferredSize() {
         return new Dimension(ModelTickerListPanel.W, ModelTickerListPanel.H);
     }
}