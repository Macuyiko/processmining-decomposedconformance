package org.processmining.plugins.realtimedcc.experiments;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.processmining.plugins.realtimedcc.streamer.AbstractStreamListener;
import org.processmining.plugins.realtimedcc.streamer.StreamController;

public class ExperimentStreamerListener extends AbstractStreamListener {
	private final int UPDATE_INTERVAL = 1000;
	private Timer timer;
	
	private long eventsSent = 0;
	
	public ExperimentStreamerListener(StreamController controller) {
		super(controller);
		
		timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
        });
		
		timer.start();
	}
	
	public void update() {
		String repr = "================================================"+"\r\n";
		repr += "STREAMER TIME: "+(System.currentTimeMillis()/1000L)+"\r\n";
		repr += "STREAMER SENT: "+eventsSent+"\r\n";
		System.out.println(repr);
	}

	@Override
	public void notifyEventSent(String eventIdentifier) {
		eventsSent++;
	}

}


