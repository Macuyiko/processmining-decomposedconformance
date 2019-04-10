package org.processmining.plugins.realtimedcc.experiments;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Timer;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.realtimedcc.models.StreamedEvent;
import org.processmining.plugins.realtimedcc.replayer.AbstractReplayListener;
import org.processmining.plugins.realtimedcc.replayer.ReplayController;
import org.processmining.plugins.realtimedcc.replayer.ReplayWorkerThread;

import au.com.bytecode.opencsv.CSVWriter;

public class ExperimentReplayListener extends AbstractReplayListener {
	private Timer timer;
	public final int UPDATE_INTERVAL = 1000;
	private Map<Integer, Integer> modelToFaults;
	private Map<Integer, String> modelToLastViolatedActivity;
	private Map<String, Pair<Boolean, Integer>> transitionToFaultsOrCorrect;
	private CSVWriter writer;
	private int linesize = 0;
	private int modelSize;
	private List<String> transitions;
	
	private Set<String> globalTP;
	private long previousGlobalTP = 0;
	
	public ExperimentReplayListener(ReplayController controller, int modelSize, List<String> transitions, File csvFile) throws IOException {
		super(controller);
		
		this.modelSize = modelSize;
		this.transitions = transitions;
		this.modelToFaults = new HashMap<Integer, Integer>();
		this.transitionToFaultsOrCorrect = new HashMap<String, Pair<Boolean, Integer>>();
		this.modelToLastViolatedActivity = new HashMap<Integer, String>();
		this.globalTP = new HashSet<String>();
		
		for (int m = 0; m < modelSize; m++){
			modelToFaults.put(m, 0);
			modelToLastViolatedActivity.put(m, "");
		}
		for (String a : transitions)
			transitionToFaultsOrCorrect.put(a, new Pair<Boolean, Integer>(true, 0));
		
		
		
		timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					update();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        });
		
		writer = new CSVWriter(new FileWriter(csvFile));
		List<String> header = new ArrayList<String>();
		header.add("Time");
		for (int i = 0; i < getController().getWorkerThreads().size(); i++) {
			ReplayWorkerThread wt = getController().getWorkerThreads().get(i);
			header.add(i+"_throughput");
			header.add(i+"_eventsqueued");
			header.add(i+"_modelshandled");
			header.add(i+"_replayersactive");
			for (Entry<Integer, Set<StreamedEvent>> e : wt.getEventsHandled().entrySet())
				header.add(i+"_handledreplaysmodel_"+e.getKey());
		}
		for (int m = 0; m < modelSize; m++) {
			header.add(m+"_lastviolatingact");
			header.add(m+"_faults");
		}
		
		for (String tr : transitions) {
			header.add(tr+"_faultsorcorrect");
		}
		
		header.add("globalTP");
		
		linesize = header.size();
		String[] headera = header.toArray(new String[]{});
		writer.writeNext(headera);
		writer.flush();
		
		timer.start();
	}

	public synchronized void update() throws IOException {
		String[] line = new String[linesize];
		String repr = "================================================"+"\r\n";
		repr += "TIME: "+(System.currentTimeMillis()/1000L)+"\r\n";
		int l = 0;
		line[0] = ""+(System.currentTimeMillis()/1000L); l++;
		for (int i = 0; i < getController().getWorkerThreads().size(); i++) {
			ReplayWorkerThread wt = getController().getWorkerThreads().get(i);
			repr += ""+i+"  "+wt.getThroughput()+" , "+wt.getEventsToHandle().size()+
					"     ,"+wt.getModels().size()+","+wt.getReplayers().size()+"\r\n";
			line[l] = ""+wt.getThroughput(); l++;
			line[l] = ""+wt.getEventsToHandle().size(); l++;
			line[l] = ""+wt.getModels().size(); l++;
			line[l] = ""+wt.getReplayers().size(); l++;
			for (Entry<Integer, Set<StreamedEvent>> e : wt.getEventsHandled().entrySet()) {
				line[l] = ""+e.getValue().size();
				l++;
			}
		}
		for (int m = 0; m < modelSize; m++) {
			line[l] = ""+modelToLastViolatedActivity.get(m); l++;
			line[l] = ""+modelToFaults.get(m); l++;
			modelToFaults.put(m, 0);
		}
		
		for (String tr : transitions) {
			line[l] = ""+transitionToFaultsOrCorrect.get(tr).getSecond() *
					(transitionToFaultsOrCorrect.get(tr).getFirst() ? 1 : -1); l++;
		}
		
		long pt = globalTP.size() - previousGlobalTP;
		previousGlobalTP = globalTP.size();
		line[l] = ""+pt; l++;
		
		writer.writeNext(line);
		writer.flush();
		
		System.out.println(repr);
	}

	@Override
	public synchronized void notifyEventReplayed(StreamedEvent event) {
		globalTP.add(event.activity+event.caseId+event.creationTime);
		
		if (event.outcome == false) {
			modelToFaults.put(event.model, modelToFaults.get(event.model) + 1);
			modelToLastViolatedActivity.put(event.model, event.activity);
			if (transitionToFaultsOrCorrect.get(event.activity).getFirst() == true)
				transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(false, 0));
			transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(false, 
					transitionToFaultsOrCorrect.get(event.activity).getSecond()+1));
		} else {
			if (transitionToFaultsOrCorrect.get(event.activity).getFirst() == false)
				transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(true, 0));
			transitionToFaultsOrCorrect.put(event.activity, new Pair<Boolean, Integer>(true, 
					transitionToFaultsOrCorrect.get(event.activity).getSecond()+1));
		}
	}
	

	@Override
	public void notifyWaitingConnection() {
		
	}

	@Override
	public void notifyGotConnection() {
		
	}

}

