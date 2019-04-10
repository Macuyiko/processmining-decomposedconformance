package org.processmining.plugins.seppedccc;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.tue.astar.AStarException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.nikefs2.NikeFS2VirtualFileSystem;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.kutoolbox.utils.ImportUtils;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.models.impl.TraceReplayRunnable;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.seppedccc.models.DecomposedReplayerSettings;
import org.processmining.plugins.seppedccc.models.FakePluginContext;

import au.com.bytecode.opencsv.CSVWriter;

public class HeadlessExperimentManager {
	public static void main(String[] args) throws Exception {
		NikeFS2VirtualFileSystem.instance().setSwapFileSize(800000000);
		Boot.boot(HeadlessExperimentManager.class, PluginContext.class);
		
		String basePath = "D:/Seppe vanden Broucke/BPM2013benchmarks/";
		//String basePath = "C:/Users/n11093/Desktop/BPM2013benchmarks/";
		String experimentResultPath = basePath + "results.csv";
		File baseFile = new File(basePath);
		CSVWriter writer = new CSVWriter(new FileWriter(new File(experimentResultPath)));
		Map<String, Integer> columns = new HashMap<String, Integer>();
		columns.put("net", 0);
		columns.put("log", 1);
		columns.put("replayer", 2);
		columns.put("decomposed", 3);
		columns.put("k", 4);
		columns.put("grouped", 5);
		columns.put("mt", 6);
		
		List<String[]> configs = new ArrayList<String[]>();
		for (File petriFile : baseFile.listFiles()) {
			if (!petriFile.isDirectory() && petriFile.getName().endsWith(".tpn")) {
				File logFile = new File(petriFile.getAbsolutePath().replace(".tpn", ".mxml.gz"));
				for (String replayer : new String[]{"arya", "seppe"})
				for (String decomposed : new String[]{"yes"})
				for (String k : new String[]{"1", "5", "10", "15", "20", "25", "30", "50", "75", "100"}) {
					if (decomposed.equals("no") && !k.equals("1")) continue;
					// grouping and no mt for now...
						configs.add(new String[]{
							petriFile.getAbsolutePath(),
							logFile.getAbsolutePath(),
							replayer,
							decomposed,
							k,
							"yes",
							"no"});
				}
			}
		}
		
		for (String[] config : configs) {
			System.out.println("New experiment");
			for (String cc : config) System.out.println(cc);
			
			File petriFile = new File(config[0]);
			File logFile = new File(config[1]);
			
			DecomposedConformanceCheckerPlugin.clac();
			
			Petrinet net = ImportUtils.openTPN(petriFile);
			XLog log = ImportUtils.openMXMLGZ(logFile);
				
			DecomposedReplayerSettings settings = DecomposedReplayerSettings.getDefaultSettings(net, log);
			
			settings.setUseArya(config[2].equals("arya"));
			settings.setMaximumSize(Integer.parseInt(config[4]));
			settings.setUseGroupedLogs(config[5].equals("yes"));
			settings.setUseMultiThreaded(config[6].equals("yes"));
			settings.setUsePureArya(true);
			
			Map<String, Double> results = DecomposedConformanceCheckerPlugin.LAST_RUN_TIMINGS;
			if (config[3].equals("yes"))
				results = runDecomposedExperiment(net, log, settings);
			else
				if (config[2].equals("arya"))
					results = runNonDecomposedArya(net, log, settings);
				else
					results = runNonDecomposedSeppe(net, log, settings);
				
			for (Entry<String, Double> entry : results.entrySet())
				if (!columns.containsKey(entry.getKey()))
					columns.put(entry.getKey(), columns.size());
				
			String line[] = new String[columns.size()];
			
			for (int k = 0; k < config.length; k++)
				line[k] = config[k];
			
			for (Entry<String, Double> entry : results.entrySet())
				line[columns.get(entry.getKey())] = entry.getValue()+"";
			
			writer.writeNext(line);
			writer.flush();
		}
		
		String line[] = new String[columns.size()];
		for (Entry<String, Integer> entry : columns.entrySet())
			line[entry.getValue()] = entry.getKey();
		writer.writeNext(line);
		writer.flush();
		writer.close();
	}
	
	@Bootable
	public void boot(CommandLineArgumentList commandlineArguments) throws Exception {
		
	}
	
	public static Map<String, Double> runDecomposedExperiment(Petrinet net, XLog log, DecomposedReplayerSettings settings) {
		DecomposedConformanceCheckerPlugin.executePluginGiven(null, net, log, settings);
		return DecomposedConformanceCheckerPlugin.LAST_RUN_TIMINGS;
	}
	
	public static Map<String, Double> runNonDecomposedArya(Petrinet net, XLog log, DecomposedReplayerSettings settings) {
		PluginContext context = new FakePluginContext();
		
		DecomposedConformanceCheckerPlugin.tick("setup_time");
		PetrinetReplayerWithILP replayer = new PetrinetReplayerWithILP(); // same as used in decomp.
		
		XEventClass dummy = new XEventClass("$\\tau$", settings.getMapper().getEventClasses().size());
		TransEvClassMapping tecMap = DecomposedConformanceCheckerPlugin.makeAryaMapping(
				net, log, dummy, settings.getMapper());
		IPNReplayParameter parameters = new CostBasedCompleteParam(
				settings.getMapper().getEventClasses(), 
				dummy,
				net.getTransitions());
		parameters.setGUIMode(false);
		parameters.setInitialMarking(PetrinetUtils.getInitialMarking(net));
		Set<Marking> finalMarkings = new HashSet<Marking>();
		finalMarkings.add(PetrinetUtils.getFinalMarking(net));
		parameters.setFinalMarkings(finalMarkings.toArray(new Marking[]{}));
		DecomposedConformanceCheckerPlugin.tock("setup_time");
		
		DecomposedConformanceCheckerPlugin.tick("replay");
		try {
			replayer.replayLog(context, net, log, tecMap, parameters);
		} catch (AStarException e) {
			e.printStackTrace();
		}
		DecomposedConformanceCheckerPlugin.tock("replay");
		return DecomposedConformanceCheckerPlugin.LAST_RUN_TIMINGS;
	}
	
	public static Map<String, Double> runNonDecomposedSeppe(Petrinet net, XLog log, DecomposedReplayerSettings settings) {
		DecomposedConformanceCheckerPlugin.tick("setup");
		BlockingQueue<Runnable> worksQueue = null;
		ThreadPoolExecutor executor = null;

		if (settings.isUseMultiThreaded()) {
			worksQueue = new ArrayBlockingQueue<Runnable>(10);
			executor = new ThreadPoolExecutor(10, 	// core size
					20, 	// max size
					1, 		// keep alive time
					TimeUnit.MINUTES, 	// keep alive time units
					worksQueue 			// the queue to use
			);
		}
				
		List<TraceReplayRunnable<Transition, XEventClass, Marking>> taskList = 
				new ArrayList<TraceReplayRunnable<Transition, XEventClass, Marking>>();
		
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(settings.getMapper().getEventClassifier(), log);
		DecomposedConformanceCheckerPlugin.tock("setup");
		
		DecomposedConformanceCheckerPlugin.tick("grouping");
		XLog logused = log;
		if (settings.isUseGroupedLogs()) {
			GroupedXLog groupedLog = new GroupedXLog(logused);
			logused = groupedLog.getGroupedLog();
		}
		int logsize = logused.size();
		DecomposedConformanceCheckerPlugin.tock("grouping");
		
		DecomposedConformanceCheckerPlugin.tick("replay");
		for (int t = 0; t < logsize; t++) {
			XTrace trace = logused.get(t);
			ProcessReplayModel<Transition, XEventClass, Marking> replayModel = null;
			
			replayModel = new PetrinetReplayModel(
					net,
					PetrinetUtils.getInitialMarking(net),
					settings.getMapper());
			
			List<XEventClass> classSequence = LogUtils.getTraceEventClassSequence(trace, eventClasses);
			if (settings.isUseMultiThreaded()) {
				taskList.add(new TraceReplayRunnable<Transition, XEventClass, Marking>(replayModel, classSequence));
			} else {
				replayModel.reset();
				replayModel.replay(classSequence);
			}
		}
		
		if (settings.isUseMultiThreaded()) {
			for (int i = 0; i < taskList.size(); i++) {
				TraceReplayRunnable<Transition, XEventClass, Marking> task = taskList.get(i);
				while (executor.getQueue().remainingCapacity() == 0);
				executor.execute(task);
			}
			
			executor.shutdown();
			try {
				while (!executor.awaitTermination(10, TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		DecomposedConformanceCheckerPlugin.tock("replay");
		
		return DecomposedConformanceCheckerPlugin.LAST_RUN_TIMINGS;
	}
}
