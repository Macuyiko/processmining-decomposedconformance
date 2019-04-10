package org.processmining.plugins.seppedccc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JTabbedPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapperPanel;
import org.processmining.plugins.kutoolbox.utils.LogUtils;
import org.processmining.plugins.kutoolbox.utils.PetrinetUtils;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.models.impl.PetrinetReplayModel;
import org.processmining.plugins.neconformance.models.impl.TraceReplayRunnable;
import org.processmining.plugins.seppedccc.models.ReplayModelArray;

@Plugin(name = "Replay Event Log Array and Petri net Array with Heuristic Replayer", 
	parameterLabels = { "Event Log Array", "Accepting Petri net Array" }, 
	returnLabels = { "Replay Model Array" }, 
	returnTypes = { ReplayModelArray.class })

public class ReplayEventLogArrayOnAcceptingPetriNetArrayWithSeppePlugin {

	@UITopiaVariant(uiLabel = "Replay Event Log Array and Petri net Array with Heuristic Replayer",
			affiliation = "KU Leuven",
			author = "Seppe K.L.M. vanden Broucke",
			email = "seppe.vandenbroucke@kuleuven.be",
			website = "http://www.processmining.be")
	@PluginVariant(variantLabel = "Replay Event Logs, UI", requiredParameterLabels = { 0, 1 })
	
	public ReplayModelArray replayUIDefault(UIPluginContext context, 
			EventLogArray logs, 
			AcceptingPetriNetArray nets) {
		
		PetrinetLogMapper[] mappings = makeMappings(context, nets, logs);
		
		return replay(nets, logs, mappings, true, false);
	}
	
	@PluginVariant(variantLabel = "Replay Event Logs", requiredParameterLabels = { 0, 1 })
	public ReplayModelArray replayDefault(PluginContext context, 
			EventLogArray logs, 
			AcceptingPetriNetArray nets) {
		
		PetrinetLogMapper[] mappings = makeMappings(nets, logs);
		
		return replay(nets, logs, mappings, true, false);
	}
	
	public static ReplayModelArray replay(
			AcceptingPetriNetArray nets, 
			EventLogArray logs, 
			PetrinetLogMapper[] mappings,
			boolean useGrouping,
			boolean useMt) {
		
		ReplayModelArray replayModelArray = new ReplayModelArray(nets, logs, mappings);
		BlockingQueue<Runnable> worksQueue = null;
		ThreadPoolExecutor executor = null;
		
		long rStartTime = System.currentTimeMillis();
		
		int size = (nets.getSize() < logs.getSize() ? nets.getSize() : logs.getSize());
		for (int index = 0; index < size; index++) {
			if (useMt) {
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
			
			List<ProcessReplayModel<Transition, XEventClass, Marking>> replayedLog = 
					new ArrayList<ProcessReplayModel<Transition, XEventClass, Marking>>();
			XEventClasses eventClasses = XEventClasses.deriveEventClasses(
					mappings[index].getEventClassifier(),
					logs.getLog(index));
			
			XLog logused = (XLog) logs.getLog(index).clone();
			if (useGrouping) {
				GroupedXLog groupedLog = new GroupedXLog(logused);
				logused = groupedLog.getGroupedLog();
			}
			int logsize = logused.size();
			
			for (int t = 0; t < logsize; t++) {
				XTrace trace = logused.get(t);
				ProcessReplayModel<Transition, XEventClass, Marking> replayModel = new PetrinetReplayModel(
						nets.getNet(index).getNet(),
						PetrinetUtils.getInitialMarking(nets.getNet(index).getNet()),
						mappings[index]);
				
				List<XEventClass> classSequence = LogUtils.getTraceEventClassSequence(trace, eventClasses);
				if (useMt) {
					taskList.add(new TraceReplayRunnable<Transition, XEventClass, Marking>(replayModel, classSequence));
				} else {
					replayModel.reset();
					replayModel.replay(classSequence);
					replayedLog.add(replayModel);
				}
			}
			
			if (useMt) {
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
				
				for (TraceReplayRunnable<Transition, XEventClass, Marking> task : taskList) {
					replayedLog.add(task.getReplayModel());
				}
			}
			
			replayModelArray.add(replayedLog);	
		}
		
		long rEndTime = System.currentTimeMillis();
		double difference = (rEndTime - rStartTime) / 1000D;
		System.out.println("Total replay took: " + difference);
		
		return replayModelArray;
	}
	
	
	public static PetrinetLogMapper[] makeMappings(UIPluginContext context, AcceptingPetriNetArray nets, EventLogArray logs) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		PetrinetLogMapperPanel[] mapperPanel = new PetrinetLogMapperPanel[nets.getSize()];
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int i = 0; i < nets.getSize(); i++) {
			String label = "Log + Net Mapping " + (i + 1);
			mapperPanel[i] = new PetrinetLogMapperPanel(logs.getLog(i), nets.getNet(i).getNet());
			tabbedPane.add(label, mapperPanel[i]);
		}
		InteractionResult ir = context.showWizard("Mapping", true, true, tabbedPane);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = mapperPanel[i].getMap();
		}

		return mapper;
	}
	
	public static PetrinetLogMapper[] makeMappings(AcceptingPetriNetArray nets, EventLogArray logs) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = PetrinetLogMapper.getStandardMap(logs.getLog(i), nets.getNet(i).getNet());
		}
		return mapper;
	}
	
	public static PetrinetLogMapper[] makeMappings(UIPluginContext context, AcceptingPetriNetArray nets, XLog log) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		PetrinetLogMapperPanel[] mapperPanel = new PetrinetLogMapperPanel[nets.getSize()];
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int i = 0; i < nets.getSize(); i++) {
			String label = "Log + Net Mapping " + (i + 1);
			mapperPanel[i] = new PetrinetLogMapperPanel(log, nets.getNet(i).getNet());
			tabbedPane.add(label, mapperPanel[i]);
		}
		InteractionResult ir = context.showWizard("Mapping", true, true, tabbedPane);
		if (!ir.equals(InteractionResult.FINISHED)) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = mapperPanel[i].getMap();
		}

		return mapper;
	}
	
	public static PetrinetLogMapper[] makeMappings(AcceptingPetriNetArray nets, XLog log) {
		PetrinetLogMapper[] mapper = new PetrinetLogMapper[nets.getSize()];
		for (int i = 0; i < nets.getSize(); i++) {
			mapper[i] = PetrinetLogMapper.getStandardMap(log, nets.getNet(i).getNet());
		}
		return mapper;
	}
}
