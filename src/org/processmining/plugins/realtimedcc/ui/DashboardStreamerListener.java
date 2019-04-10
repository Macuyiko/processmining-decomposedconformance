package org.processmining.plugins.realtimedcc.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Timer;
import javax.swing.JFrame;

import org.processmining.plugins.realtimedcc.streamer.AbstractStreamListener;
import org.processmining.plugins.realtimedcc.streamer.StreamController;

public class DashboardStreamerListener extends AbstractStreamListener {
	private JFrame frame;
	private StreamerDashboardPanel panel;
	
	private final int UPDATE_INTERVAL = 1000;
	private Timer timer;
	
	public DashboardStreamerListener(StreamController controller) {
		super(controller);
		
		frame = new JFrame("Streamer Dashboard");
		
		panel = new StreamerDashboardPanel(this);
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowActivated(WindowEvent arg0) {}

			@Override
			public void windowClosed(WindowEvent arg0) {
				getController().tearDown();
			}

			@Override
			public void windowClosing(WindowEvent arg0) {}

			@Override
			public void windowDeactivated(WindowEvent arg0) {}

			@Override
			public void windowDeiconified(WindowEvent arg0) {}

			@Override
			public void windowIconified(WindowEvent arg0) {}

			@Override
			public void windowOpened(WindowEvent arg0) {}
		});
		
		timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				panel.update();
			}
        });
		
		timer.start();
	}

	@Override
	public void notifyEventSent(String eventIdentifier) {
		panel.setCurrentNrEventsSent(panel.getCurrentNrEventsSent()+1);
	}

	public void notifySendRate(int value) {
		getController().setSendRate(value);
	}
}


