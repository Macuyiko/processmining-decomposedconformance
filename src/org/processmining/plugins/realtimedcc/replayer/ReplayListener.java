package org.processmining.plugins.realtimedcc.replayer;

import org.processmining.plugins.realtimedcc.models.StreamedEvent;

public interface ReplayListener {
	public void notifyEventReplayed(StreamedEvent event);
	public ReplayController getController();
	public void notifyWaitingConnection();
	public void notifyGotConnection();
}
