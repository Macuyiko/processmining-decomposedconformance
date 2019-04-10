package org.processmining.plugins.realtimedcc.streamer;

public abstract class AbstractStreamListener implements StreamListener {
	private final StreamController controller;

	public AbstractStreamListener(StreamController controller) {
		this.controller = controller;
	}
	
	public StreamController getController() {
		return controller;
	}
}
