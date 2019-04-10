package org.processmining.plugins.realtimedcc.experiments;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import javax.swing.Timer;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.AcceptingPetriNetArrayImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;
import org.processmining.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.seppedccc.ActivityClusterArrayFromMappingPlugin;
import org.processmining.plugins.seppedccc.ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin;
import org.processmining.plugins.seppedccc.models.FakePluginContext;
import au.com.bytecode.opencsv.CSVWriter;

public class SeppeDecomposedExperiment {
	
	public static String basePath = "C:\\Users\\n11093\\Dropbox\\"
			+ "Event-based Real-time Decomposed Conformance Analysis\\experiment\\";
	public static String petriPath = basePath+"..\\example\\decomposition\\";
	public static String xesPath = basePath+"10000-all-nonoise.xes";
	public static String csvPath = basePath+"seppeDecomposed-nonoise.csv";
	private final static int UPDATE_INTERVAL = 1000;
	
	private static long previousHandledEvents = 0;
	private static long handledEvents = 0;
	private static CSVWriter writer;
	private static Timer timer;
	
	public static void main(String[] args) throws Exception {
		AcceptingPetriNetArray a = loadPetrinets();
		
		timer = new Timer(UPDATE_INTERVAL, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				update();
			}
        });
		
		writer = new CSVWriter(new FileWriter(new File(csvPath)));
		writer.writeNext(new String[]{"Time", "Throughput"});	
		runExperiment(a, loadLog());
		writer.close();
	}
	
	public static void update() {
		long throughput = handledEvents - previousHandledEvents;
		previousHandledEvents = handledEvents;
		System.out.println("UPDATE: "+throughput);
		writer.writeNext(new String[]{""+(System.currentTimeMillis()/1000L), ""+throughput});
		
	}

	private static void runExperiment(AcceptingPetriNetArray a, XLog log) {
		CLIContext uiContext = new CLIContext();
		PluginContext context = new CLIPluginContext(uiContext, "Dummy Context");
		XEventClassifier classifier = XLogInfoImpl.STANDARD_CLASSIFIER;
		
		PetrinetLogMapper[] mapper = ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin.makeMappings(a, log);
		ActivityClusterArray activityClusterArray = 
			ActivityClusterArrayFromMappingPlugin.executePluginGiven(
				context, 
				a, 
				log, 
				mapper);
		
		DecomposeEventLogUsingActivityClusterArrayPlugin decomposePlugin = 
				new DecomposeEventLogUsingActivityClusterArrayPlugin();
		DecomposeEventLogUsingActivityClusterArrayParameters decomposeParams = 
				new DecomposeEventLogUsingActivityClusterArrayParameters(log);
		decomposeParams.setClassifier(classifier);
		EventLogArray eventLogArray = decomposePlugin.decomposeParameters(
				context, log, activityClusterArray, decomposeParams);
		
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(classifier, log);
		
		timer.start();
		
		int size = (a.getSize() < eventLogArray.getSize() ? a.getSize() : eventLogArray.getSize());
		for (int index = 0; index < size; index++) {
			for (int t = 0; t < eventLogArray.getLog(index).size(); t++) {
				XTrace trace = eventLogArray.getLog(index).get(t);
				ProcessReplayModel<Transition, XEventClass, Marking> replayModel = null;
				
				replayModel = new PetrinetReplayModel(
						a.getNet(index).getNet(),
						PetrinetUtils.getInitialMarking(a.getNet(index).getNet()),
						mapper[index]);
				
				List<XEventClass> classSequence = LogUtils.getTraceEventClassSequence(trace, eventClasses);
				replayModel.reset();
				replayModel.replay(classSequence);
				handledEvents += classSequence.size();
			}
		}
		

		
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
