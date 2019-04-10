package org.processmining.plugins.realtimedcc.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.AcceptingPetriNetArrayImpl;
import org.processmining.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;
import org.processmining.plugins.realtimedcc.models.DecomposedReplayerSettings;
import org.processmining.plugins.realtimedcc.models.EventlogStreamerSettings;
import org.processmining.plugins.realtimedcc.replayer.ReplayController;
import org.processmining.plugins.realtimedcc.streamer.StreamController;
import org.processmining.plugins.seppedccc.models.FakePluginContext;

public class RealtimeDecomposedExperiment {
	
	public static String basePath = "C:\\Users\\n11093\\Dropbox\\"
			+ "Event-based Real-time Decomposed Conformance Analysis\\experiment\\";
	public static String petriPath = basePath+"..\\example\\decomposition\\";
	public static String xesPath = basePath+"10000-all-noise.xes";
	public static String csvPath = basePath+"noiseExperiment-50000.csv";
	public static int sendRate = 100000;
	
	public static void main(String[] args) throws Exception {
		DecomposedReplayerSettings s = new DecomposedReplayerSettings();
		s.classifier = XLogInfoImpl.NAME_CLASSIFIER;
		AcceptingPetriNetArray a = loadPetrinets();
		Set<String> transitions = new HashSet<String>();
		for (int i = 0; i < a.getSize(); i++)
			for (Transition t : a.getNet(i).getNet().getTransitions())
				transitions.add(t.getLabel());
		ReplayController controller = new ReplayController(a, s);
		ExperimentReplayListener listener = new ExperimentReplayListener(controller, a.getSize(), 
				new ArrayList<String>(transitions),
				new File(csvPath));
		//DashboardReplayListener dlistener = new DashboardReplayListener(controller);
		controller.addListener(listener);
		//controller.addListener(dlistener);
		Thread t = new Thread(controller);
		t.start();
		
		EventlogStreamerSettings ss = new EventlogStreamerSettings();
		ss.classifier = XLogInfoImpl.NAME_CLASSIFIER;
		XLog log = loadLog();
		StreamController scontroller = new StreamController(log, ss, controller);
		//StreamListener slistener = new ExperimentStreamerListener(scontroller);
		//DashboardStreamerListener slistener = new DashboardStreamerListener(scontroller);
		scontroller.setSendRate(sendRate);
		//scontroller.addListener(slistener);
		Thread st = new Thread(scontroller);
		st.start();
		
	}
	
	public static XLog loadLog() {
		XLog log = ImportUtils.openXES(new File(xesPath));
		return log;
	}
	
	public static AcceptingPetriNetArray loadPetrinets() throws Exception {
		ImportAcceptingPetriNetPlugin importer = new ImportAcceptingPetriNetPlugin();
		AcceptingPetriNetArray array = new AcceptingPetriNetArrayImpl();
		array.init();
		File dir = new File(petriPath);
		for (File pn : dir.listFiles()) {
			if (!pn.getName().endsWith(".pnml")) continue;
			AcceptingPetriNet net = (AcceptingPetriNet) importer.importFile(new FakePluginContext(), pn);
			for (Transition t : net.getNet().getTransitions()) {
				if (t.getLabel().startsWith("sid-")) {
					t.setInvisible(true);
					System.err.println("Transition set to invisible");
				}
			}
			array.addNet(net);
		}
		
		return array;
	}
	
}
