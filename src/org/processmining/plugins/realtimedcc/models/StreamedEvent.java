package org.processmining.plugins.realtimedcc.models;

public class StreamedEvent {
	public String activity;
	public String caseId;
	public long creationTime;
	public long receivedTime;
	public long queuedTime;
	public long startTime;
	public long doneTime;
	public boolean finished;
	public boolean outcome;
	public int model;
	
	public StreamedEvent() {
		
	}
	
	public StreamedEvent(String string, String string2, String string3) {
		this.caseId = string;
		this.activity = string2;
		// TODO seconds might not be granular enough in some cases
		// same activity in same case withing second window could lead to problems
		// largely untested.
		// Solve this later by adding an "event index"
		this.creationTime = Long.parseLong(string3);
		this.finished = false;
	}
	
	public StreamedEvent(StreamedEvent o) {
		StreamedEvent t = new StreamedEvent();
		t.activity = o.activity;
		t.caseId = o.caseId;
		t.creationTime = o.creationTime;
		t.receivedTime = o.receivedTime;
		t.queuedTime = o.queuedTime;
		t.startTime = o.startTime;
		t.doneTime = o.doneTime;
		t.finished = o.finished;
		t.outcome = o.outcome;
		t.model = o.model;
	}

	
}
