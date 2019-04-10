package org.processmining.plugins.realtimedcc.replayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.realtimedcc.models.DecomposedReplayerSettings;
import org.processmining.plugins.realtimedcc.models.StreamedEvent;

public class ReplayController implements Runnable {

	private DecomposedReplayerSettings settings;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private BufferedReader inputReader;
	private List<ReplayWorkerThread> workerThreads;
	private Map<Integer, Integer> modelToThread;
	private Map<String, Set<Integer>> transitionToModels;
	private AcceptingPetriNetArray acceptingPetriNetArray;
	
	private List<ReplayListener> listeners = new ArrayList<ReplayListener>();
	
	private boolean running;
	
	public ReplayController(AcceptingPetriNetArray net, DecomposedReplayerSettings settings) throws IOException {
		this.acceptingPetriNetArray = net;
		this.settings = settings;
		this.serverSocket = new ServerSocket(settings.port);
		this.modelToThread = new HashMap<Integer, Integer>();
		this.transitionToModels = new HashMap<String, Set<Integer>>();
		this.workerThreads = new ArrayList<ReplayWorkerThread>();
		this.running = true;
		
		// Build indexes... note that there might be a better way to distribute models over threads depending
		// on the model's size. Since we're rushing against a deadline, ignore this for now. Doesn't really matter
		// anyway.
		int modelsPerThread = (int) Math.ceil((double)net.getSize() / (double)settings.numberOfThreads);
		System.err.println("Using "+settings.numberOfThreads+" threads with "+modelsPerThread+" models per thread...");
		System.err.println("Going to distribute "+net.getSize()+" models...");
		
		for (int t  = 0; t < settings.numberOfThreads; t++) {
			ReplayWorkerThread worker = new ReplayWorkerThread(this);
			this.workerThreads.add(worker);
		}
		
		int modelIndex = 0;
		for (int t = 0; t < settings.numberOfThreads; t++) {
			for (int i = modelIndex; i < modelIndex + modelsPerThread && i < net.getSize(); i++) {
				modelToThread.put(i, t);
				System.err.println("Thread "+t+" is now handling model "+i);
				workerThreads.get(t).assign(i, net.getNet(i));
			}
			modelIndex += modelsPerThread;
		}
		
		for (int i = 0; i < net.getSize(); i++) {
			for (Transition t : net.getNet(i).getNet().getTransitions()) {
				String label = t.getLabel();
				if (!transitionToModels.containsKey(label))
					transitionToModels.put(label, new HashSet<Integer>());
				transitionToModels.get(label).add(i);
				System.err.println(label+" was mapped to model "+i);
			}
		}
		
		System.err.println("Starting threads...");		
		for (ReplayWorkerThread thread : workerThreads) {
			thread.start();
		}
	}

	
	@Override
	public void run() {
		while (isRunning()) {
			System.err.println("Waiting for client to connect...");
			for (ReplayListener listener : listeners) {
				listener.notifyWaitingConnection();
			}
			try {
				clientSocket = serverSocket.accept();
				clientSocket.setTcpNoDelay(true);
				inputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			System.err.println("Connected...");
			for (ReplayListener listener : listeners) {
				listener.notifyGotConnection();
			}
			System.err.println("Resetting threads...");		
			for (ReplayWorkerThread thread : workerThreads) {
				thread.reset();
			}
			while (isRunning()) {
				try {
					String event = inputReader.readLine();
					if (event == null) continue;
					handleEvent(event);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

	}


	public void handleEvent(String event) {
		String[] splitEvent = event.split(settings.delimiter);
		for (int modelIndex : transitionToModels.get(splitEvent[1])) {
			StreamedEvent sEvent = new StreamedEvent(splitEvent[0], splitEvent[1], splitEvent[2]);
			sEvent.receivedTime = System.currentTimeMillis() / 1000L;
			sEvent.model = modelIndex;
			workerThreads.get(modelToThread.get(modelIndex)).queue(sEvent, modelIndex);
		}
	}


	public void addListener(ReplayListener dashboard) {
		listeners.add(dashboard);
	}
	
	public List<ReplayListener> getListeners() {
		return listeners;
	}

	public ReplayListener getListener(int index) {
		return listeners.get(index);
	}
	
	public void removeListener(int index) {
		listeners.remove(index);
	}


	public AcceptingPetriNetArray getAcceptingPetriNetArray() {
		return acceptingPetriNetArray;
	}


	public List<ReplayWorkerThread> getWorkerThreads() {
		return workerThreads;
	}


	public synchronized boolean isRunning() {
		return running;
	}


	public synchronized void setRunning(boolean running) {
		this.running = running;
	}


	public void notifyEventReplayed(StreamedEvent event) {
		for (ReplayListener listener : listeners) {
			listener.notifyEventReplayed(event);
		}
	}


	public void tearDown() {
		for (ReplayWorkerThread t : workerThreads)
			t.setRunning(false);
		this.setRunning(false);
	}

}
