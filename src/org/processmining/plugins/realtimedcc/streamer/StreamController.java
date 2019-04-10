package org.processmining.plugins.realtimedcc.streamer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.realtimedcc.models.EventlogStreamerSettings;
import org.processmining.plugins.realtimedcc.replayer.ReplayController;

public class StreamController implements Runnable {

	private EventlogStreamerSettings settings;
	private Socket clientSocket;
	private PrintWriter outputWriter;
	private Map<Long, Set<Pair<XTrace, Integer>>> sortedEvents;
	private double sendRate;
	
	private boolean running;
	
	private List<StreamListener> listeners = new ArrayList<StreamListener>();
	private ReplayController receivingController;
	
	
	public StreamController(XLog log, EventlogStreamerSettings settings) throws IOException {
		this.settings = settings;
		this.sortedEvents = new TreeMap<Long, Set<Pair<XTrace, Integer>>>();
		this.sendRate = settings.eventsPerSecond;
		this.running = true;
		
		System.err.println("Sorting events...");
		long eventcounter = 0;
		long previousTime = 0;
		for (XTrace trace : log) {
			for (int i = 0; i < trace.size(); i++) {
				Date time = XTimeExtension.instance().extractTimestamp(trace.get(i));
				long t = (time == null || time.getTime() == previousTime) 
						? eventcounter 
						: time.getTime();
				if (!sortedEvents.containsKey(t))
					sortedEvents.put(t, new HashSet<Pair<XTrace, Integer>>());
				sortedEvents.get(t).add(new Pair<XTrace, Integer>(trace, i));
				previousTime = t;
				eventcounter++;
			}
		}
	}

	
	public StreamController(XLog log, EventlogStreamerSettings settings, ReplayController controller) throws IOException {
		this(log, settings);
		// Setting a receiver will not go over the network
		// This is used for experiments
		this.receivingController = controller;
	}


	@Override
	public void run() {
		if (receivingController == null) {
			System.err.println("Connecting to server...");
			try {
				clientSocket = new Socket("127.0.0.1", settings.port);
				clientSocket.setTcpNoDelay(true);
				outputWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		}
		
		for (Entry<Long, Set<Pair<XTrace, Integer>>> eventEntry : sortedEvents.entrySet()) {
			if (!isRunning()) break;
			for (Pair<XTrace, Integer> pair : eventEntry.getValue()) {
				if (!isRunning()) break;
				
				long millisBetweenEvent = (long) (1000D / sendRate);
				
				String traceId = XConceptExtension.instance().extractName(pair.getFirst());
				String eventId = settings.classifier.getClassIdentity(pair.getFirst().get(pair.getSecond()));
				long time = System.currentTimeMillis() / 1000L;
				String event = traceId + settings.delimiter + eventId + settings.delimiter + time;
			//	System.err.println("Event sent: "+event);
				if (receivingController == null) {
					outputWriter.println(event);
				} else {
					receivingController.handleEvent(event);
				}
				this.notifyEventSent(event);
				try {
					Thread.sleep(millisBetweenEvent);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.err.println("Done!");

		if (receivingController == null) {
			try {
				clientSocket.close();
			} catch (IOException e) {}
		}
	}


	private void notifyEventSent(String event) {
		for (StreamListener listener : listeners) {
			listener.notifyEventSent(event);
		}
	}


	public void addListener(StreamListener dashboard) {
		listeners.add(dashboard);
	}
	
	public List<StreamListener> getListeners() {
		return listeners;
	}

	public StreamListener getListener(int index) {
		return listeners.get(index);
	}
	
	public void removeListener(int index) {
		listeners.remove(index);
	}


	public double getSendRate() {
		return sendRate;
	}


	public void setSendRate(double sendRate) {
		this.sendRate = sendRate;
	}


	public synchronized boolean isRunning() {
		return running;
	}


	public synchronized void setRunning(boolean running) {
		this.running = running;
	}


	public void tearDown() {
		setRunning(false);
	}

}
