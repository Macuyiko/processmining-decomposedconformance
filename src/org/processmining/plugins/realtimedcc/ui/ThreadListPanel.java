package org.processmining.plugins.realtimedcc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.Scrollable;

public class ThreadListPanel extends JPanel implements Scrollable {

	private static final long serialVersionUID = 6683195060214341707L;
	private List<ThreadPanel> threadPanels;
	public static final int W = 300;
	public static final int H = 180;
    private Dimension dimSize;

	public ThreadListPanel(DashboardReplayListener parent) {
		this.setLayout(new FlowLayout());
		this.setBackground(Color.WHITE);
	}
	
	public void initialize(int size) {
		this.removeAll();
		threadPanels = new ArrayList<ThreadPanel>();
		dimSize = new Dimension(W + 2 * 2, (size + 1) * H + (size + 1) * 2);
		this.setPreferredSize(dimSize);
		for (int i = 0; i < size; i++) {
			threadPanels.add(new ThreadPanel());
			this.add(threadPanels.get(threadPanels.size()-1));
		}
		
	}
	
	public List<ThreadPanel> getThreadPanels() {
		return threadPanels;
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

