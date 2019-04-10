package org.processmining.plugins.seppedccc.models;

import java.util.Timer;
import java.util.TimerTask;

public class FinalizationStopper extends TimerTask {
    private static final Timer TIMER = new Timer("Finalization stopper", true);
    private final Thread toStop;
    volatile boolean interrupted;

    public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	public FinalizationStopper(final long timeout) {
        this.toStop = Thread.currentThread();
        TIMER.schedule(this, timeout, timeout);
    }

    public void run() {
    	if (!interrupted)
    		toStop.interrupt();
        interrupted = true;
    }
}