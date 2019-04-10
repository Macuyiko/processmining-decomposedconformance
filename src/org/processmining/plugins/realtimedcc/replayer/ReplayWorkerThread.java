package org.processmining.plugins.realtimedcc.replayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Timer;

import org.processmining.framework.util.Pair;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.ProcessReplayModel.ReplayMove;
import org.processmining.plugins.realtimedcc.models.PetrinetReplayUtils;
import org.processmining.plugins.realtimedcc.models.RealtimePetrinetReplayModel;
import org.processmining.plugins.realtimedcc.models.StreamedEvent;

public class ReplayWorkerThread extends Thread {
	
	private List<Pair<Integer, StreamedEvent>> eventsToHandle;
	
	private Map<Integer, Set<StreamedEvent>> eventsHandled;
	private long previousHandledSize = 0;
	private double throughput = 0;
	private Map<Integer, AcceptingPetriNet> models;
	private Map<Pair<Integer, String>, RealtimePetrinetReplayModel> replayers;

	private boolean running;

	private ReplayController controller;
	
	public ReplayWorkerThread(ReplayController controller) {
		this.controller = controller;
		this.eventsToHandle = new ArrayList<Pair<Integer, StreamedEvent>>();
		this.eventsHandled = new HashMap<Integer, Set<StreamedEvent>>();
		this.models = new HashMap<Integer, AcceptingPetriNet>();
		this.replayers = new HashMap<Pair<Integer, String>, RealtimePetrinetReplayModel>();
		this.running = true;
		
		Timer timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				long nowSize = 0;
				for (Set<StreamedEvent> s : eventsHandled.values())
					nowSize += s.size();
				throughput = nowSize - previousHandledSize;
				previousHandledSize = nowSize;
			}
        });
		timer.start();
	}

	public synchronized void assign(int i, AcceptingPetriNet net) {
		models.put(i, net);
		eventsHandled.put(i, new HashSet<StreamedEvent>());
	}

	public synchronized void queue(StreamedEvent sEvent, int modelIndex) {
		eventsToHandle.add(new Pair<Integer, StreamedEvent>(modelIndex, sEvent));
		sEvent.queuedTime = System.currentTimeMillis() / 1000L;
	//	System.err.println("Thread has queued event");
	}
	
	public synchronized Pair<Integer, StreamedEvent> dequeue() {
		if (eventsToHandle.size() == 0)
			return null;
		Pair<Integer, StreamedEvent> modelEvent = eventsToHandle.remove(0);
		return modelEvent;
	}
	
	public void reset() {
		for (RealtimePetrinetReplayModel replayer : replayers.values())
			replayer.reset();
	}

	public void run() {
		while (running) {
			Pair<Integer, StreamedEvent> modelEvent = dequeue();
			if (modelEvent == null){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					running = false;
					break;
				}
				continue;
			}
			String activity = modelEvent.getSecond().activity;
			String caseId = modelEvent.getSecond().caseId;
			int model = modelEvent.getFirst();
			Pair<Integer, String> key = new Pair<Integer, String>(model, caseId);
			
		//	System.err.println("Thread is handling "+caseId+" "+activity+" on model "+model);
			
			if (!replayers.containsKey(key)) {
				replayers.put(key, new RealtimePetrinetReplayModel(models.get(model).getNet(), 
					PetrinetReplayUtils.getVisibleTransitionLabels(models.get(model).getNet()),
					PetrinetUtils.getInitialMarking(models.get(model).getNet())));
				replayers.get(key).reset();
			}
			
			// TODO: keep previous steps fixed
			modelEvent.getSecond().startTime = System.currentTimeMillis() / 1000L;
			replayers.get(key).replay(activity);
			modelEvent.getSecond().doneTime = System.currentTimeMillis() / 1000L;
			ReplayMove move = replayers.get(key).getReplayMove(replayers.get(key).size()-1);
			
			switch (move) {
			case BOTH_FORCED:
				modelEvent.getSecond().outcome = false;
				break;
			case BOTH_SYNCHRONOUS:
				modelEvent.getSecond().outcome = true;
				break;
			case LOGONLY_INSERTED:
				modelEvent.getSecond().outcome = false;
				break;
			case MODELONLY_SKIPPED:
				modelEvent.getSecond().outcome = true;
				break;
			case MODELONLY_UNOBSERVABLE:
				modelEvent.getSecond().outcome = false;
				break;
			default:
				break;
			}
			
		//	System.err.println("Thread reports: "+caseId+" activity "+activity+" on model "+model+": "+move);
			modelEvent.getSecond().finished = true;
			eventsHandled.get(model).add(modelEvent.getSecond());
			
			controller.notifyEventReplayed(modelEvent.getSecond());
		}
	}

	public double getThroughput() {
		return throughput;
	}

	public List<Pair<Integer, StreamedEvent>> getEventsToHandle() {
		return eventsToHandle;
	}

	public Map<Integer, Set<StreamedEvent>> getEventsHandled() {
		return eventsHandled;
	}

	public Map<Integer, AcceptingPetriNet> getModels() {
		return models;
	}

	public Map<Pair<Integer, String>, RealtimePetrinetReplayModel> getReplayers() {
		return replayers;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

}
