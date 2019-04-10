package org.processmining.plugins.seppedccc;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.kutoolbox.logmappers.PetrinetLogMapper;
import org.processmining.plugins.neconformance.models.ProcessReplayModel;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.trees.LogTree;
import org.processmining.plugins.neconformance.trees.ukkonen.UkkonenLogTree;
import org.processmining.plugins.neconformance.ui.EvaluationVisualizator;
import org.processmining.plugins.seppedccc.models.ReplayModelArray;

@Plugin(name = "Visualize Replay Model Array", 
	returnLabels = { "Visualized Replay Result Array" }, 
	returnTypes = { JComponent.class }, 
	parameterLabels = { "Replay Model Array" }, 
	userAccessible = false)
@Visualizer

public class VisualizeReplayModelArrayPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(UIPluginContext context, ReplayModelArray replayModelArray) {
		EventLogArray logs = replayModelArray.getEventLogArray();
		AcceptingPetriNetArray nets = replayModelArray.getAcceptingPetrinetArray();
		PetrinetLogMapper[] mappings = replayModelArray.getMapperArray();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		for (int index = 0; index < replayModelArray.size(); index++) {
			System.err.println(index + " / " + replayModelArray.size());
			String label = "Replay " + (index + 1);
			if (logs.getLog(index).size() != replayModelArray.get(index).size())
				tabbedPane.add(label, new JLabel("Sorry, visualization for grouped replay not available yet"));
			else
				tabbedPane.add(label, makeVizPane(context, 
					replayModelArray.get(index), 
					logs.getLog(index), 
					nets.getNet(index).getNet(), 
					mappings[index]));
		}
		return tabbedPane;

	}
	
	public static JComponent makeVizPane(UIPluginContext context, 
			List<ProcessReplayModel<Transition, XEventClass, Marking>> logReplayModels,
			XLog log, Petrinet onet, 
			PetrinetLogMapper mapper) {
		mapper.applyMappingOnTransitions();
		
		LogTree logTree = new UkkonenLogTree(log);
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(XLogInfoImpl.STANDARD_CLASSIFIER, log);
		LogTreeWeightedNegativeEventInducer inducer = new LogTreeWeightedNegativeEventInducer(
				eventClasses,
				AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
				logTree);
		inducer.setReturnZeroEvents(false);
		inducer.setUseBothWindowRatios(false);
		inducer.setUseWeighted(true);
				
		return new EvaluationVisualizator(logReplayModels, inducer, log, onet, mapper, true);
		
	}
}
