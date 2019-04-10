package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;

public class StreamerDashboardPanel extends JPanel {
	private DashboardStreamerListener parent;
	
	private static final long serialVersionUID = 1919791473676715146L;
	
	private static final int COUNT = 2 * 60;
    private final DynamicTimeSeriesCollection dataset;
    private final JFreeChart chart;
    private final JSlider slider;
    private long currentNrEventsSent = 0;
    
	public StreamerDashboardPanel(DashboardStreamerListener streamerDashboard) {
		this.parent = streamerDashboard;
		
		this.setLayout(new BorderLayout());
		
		dataset = new DynamicTimeSeriesCollection(1, COUNT, new Second());
		dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2013));
		dataset.addSeries(new float[]{}, 0, "Nr. of Events Sent (100s)");
		chart = createChart(dataset);
		this.add(new ChartPanel(chart), BorderLayout.CENTER);
		JPanel sliderPanel = new JPanel();
		final JLabel sliderLbl = new JLabel();
		slider  = new JSlider(1, 500, 1);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				sliderLbl.setText(slider.getValue()+"");
				parent.notifySendRate(slider.getValue());
			}
		});
		sliderPanel.add(slider);
		sliderPanel.add(sliderLbl);
		this.add(sliderPanel, BorderLayout.SOUTH);
	}
	
	public void update() {
		float[] newData = new float[]{currentNrEventsSent/100F};
		ValueAxis range = chart.getXYPlot().getRangeAxis();
		if (newData[0] >= range.getRange().getUpperBound())
			range.setRange(0, newData[0]);
		
        dataset.advanceTime();
        dataset.appendData(newData);
        
        parent.notifySendRate(slider.getValue());
	}
	
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("", "hh:mm:ss", "events", dataset, true, true, false);
		final XYPlot plot = chart.getXYPlot();
		ValueAxis domain = plot.getDomainAxis();
		domain.setAutoRange(true);
		ValueAxis range = plot.getRangeAxis();
		range.setRange(0, 10);
		return chart;
	}

	public long getCurrentNrEventsSent() {
		return currentNrEventsSent;
	}

	public void setCurrentNrEventsSent(long currentNrEventsSent) {
		this.currentNrEventsSent = currentNrEventsSent;
	}
}