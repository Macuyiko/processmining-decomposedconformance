package org.processmining.plugins.realtimedcc.replayer;

public abstract class AbstractReplayListener implements ReplayListener {
	private final ReplayController controller;

	public AbstractReplayListener(ReplayController controller) {
		this.controller = controller;
	}
	
	public ReplayController getController() {
		return controller;
	}
}
