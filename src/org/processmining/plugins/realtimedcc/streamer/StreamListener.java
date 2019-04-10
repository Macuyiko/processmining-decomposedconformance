package org.processmining.plugins.realtimedcc.streamer;

public interface StreamListener {
	public StreamController getController();
	public void notifyEventSent(String eventIdentifier);
}
