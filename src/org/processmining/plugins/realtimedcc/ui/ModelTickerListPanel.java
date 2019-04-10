package org.processmining.plugins.realtimedcc.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.Scrollable;

public class ModelTickerListPanel extends JPanel implements Scrollable {
	private static final long serialVersionUID = -1581584937367038412L;
	
	private DashboardReplayListener parent;
	
	public static int W = 700;
	public static int H = 140;
    private Dimension dimSize;
    
	private List<ModelTickerPanel> tickerPanels;
	
	public ModelTickerListPanel(DashboardReplayListener parent) {
		this.parent = parent;
		this.setLayout(new FlowLayout());
	}
	
	public void initialize(int size) {
		this.removeAll();
		tickerPanels = new ArrayList<ModelTickerPanel>();
		dimSize = new Dimension(W + 2 * 2, (size + 1) * H + (size + 1) * 2);
		this.setPreferredSize(dimSize);
		for (int i = 0; i < size; i++) {
			tickerPanels.add(new ModelTickerPanel(this, i));
			tickerPanels.get(tickerPanels.size()-1).updateTitle("Model "+i+" (waiting for data)");
			this.add(tickerPanels.get(tickerPanels.size()-1));
		}
	}
	
	public void notifyShowModel(int index) {
		parent.notifyShowModel(index);
	}

	public List<ModelTickerPanel> getTickerPanels() {
		return tickerPanels;
	}

	@Override
    public Dimension getPreferredScrollableViewportSize() {
        return dimSize;
    }

    @Override
    public int getScrollableUnitIncrement(
        Rectangle visibleRect, int orientation, int direction) {
        return getIncrement(orientation);
    }

    @Override
    public int getScrollableBlockIncrement(
        Rectangle visibleRect, int orientation, int direction) {
        return getIncrement(orientation);
    }

    private int getIncrement(int orientation) {
        if (orientation == JScrollBar.HORIZONTAL) {
            return W + 2;
        } else {
            return H + 2;
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

}

