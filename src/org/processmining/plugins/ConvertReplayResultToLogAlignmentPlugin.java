package org.processmining.plugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.connections.ConvertReplayResultToLogAlignmentConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ConvertReplayResultToLogAlignmentDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.LogAlignment;
import org.processmining.models.LogAlignmentArray;
import org.processmining.models.ReplayResultArray;
import org.processmining.models.TraceAlignment;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ConvertReplayResultToLogAlignmentParameters;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

@Plugin(name = "Convert Replay Results", parameterLabels = { "Event Log Array", "Accepting Petri net Array",
		"Replay Result Array", "Parameters" }, returnLabels = { "Log Alignment Array" }, returnTypes = { LogAlignmentArray.class })
public class ConvertReplayResultToLogAlignmentPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, UI", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignmentArray convertDialog(UIPluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		ConvertReplayResultToLogAlignmentParameters parameters = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		ConvertReplayResultToLogAlignmentDialog dialog = new ConvertReplayResultToLogAlignmentDialog(replayResults, logs,
				nets, parameters);
		int n = 0;
		String[] title = { "Configure conversion (classifier)", "Configure conversion (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
				dialog.finish();
			} else {
				return null;
			}
		}
		return convertPrivateConnection(context, logs, nets, replayResults, convert(logs, parameters), parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Convert Replay Results, Default", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignmentArray convertDefault(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults) {
		ConvertReplayResultToLogAlignmentParameters parameters = new ConvertReplayResultToLogAlignmentParameters(logs,
				nets);
		return convertPrivateConnection(context, logs, nets, replayResults, convert(logs, parameters), parameters);
	}

	@PluginVariant(variantLabel = "Convert Replay Results, Parameters", requiredParameterLabels = { 0, 1, 2, 3 })
	public LogAlignmentArray convertParameters(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets,
			ReplayResultArray replayResults,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		return convertPrivateConnection(context, logs, nets, replayResults, convert(logs, parameters), parameters);
	}

	private LogAlignmentArray convertPrivateConnection(PluginContext context, EventLogArray logs, AcceptingPetriNetArray nets, ReplayResultArray replayResults,
			ActivityClusterArray clusters, ConvertReplayResultToLogAlignmentParameters parameters) {
		Collection<ConvertReplayResultToLogAlignmentConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ConvertReplayResultToLogAlignmentConnection.class, context, replayResults);
			for (ConvertReplayResultToLogAlignmentConnection connection : connections) {
				if (connection.getObjectWithRole(ConvertReplayResultToLogAlignmentConnection.RESULTS).equals(
						replayResults)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(ConvertReplayResultToLogAlignmentConnection.ALIGNMENTS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		LogAlignmentArray alignments = convertPrivate(replayResults, clusters, parameters);
		context.getConnectionManager().addConnection(
				new ConvertReplayResultToLogAlignmentConnection(logs, nets, replayResults, alignments, parameters));
		return alignments;
	}

	private LogAlignmentArray convertPrivate(ReplayResultArray replayResults, ActivityClusterArray clusters,
			ConvertReplayResultToLogAlignmentParameters parameters) {
		LogAlignmentArray logAlignments = DivideAndConquerFactory.createLogAlignmentArray();
		logAlignments.init();
		for (int index = 0; index < replayResults.getSize(); index++) {
			logAlignments.addAlignment(convertPrivate(replayResults.getReplay(index), parameters.getMapping(index),
					clusters.getCluster(index)));
		}
		return logAlignments;
	}

	private LogAlignment convertPrivate(PNRepResult replayResults, TransEvClassMapping tecMap, Set<XEventClass> cluster) {
		LogAlignment logAlignment = DivideAndConquerFactory.createLogAlignment();
		logAlignment.init();
		for (SyncReplayResult replayResult : replayResults) {
			TraceAlignment traceAlignment = convertPrivate(replayResult, tecMap);
			logAlignment.putAlignment(traceAlignment.getLogMoves(), traceAlignment);
			logAlignment.setCluster(cluster);
		}
		return logAlignment;
	}

	private TraceAlignment convertPrivate(SyncReplayResult replayResult, TransEvClassMapping tecMap) {
		TraceAlignment traceAlignment = DivideAndConquerFactory.createTraceAlignment();
		traceAlignment.init();
		/* Seppe: no idea what this thing was doing here but size is not used anywhere so...
		int size = 0;
		for (int i = 0; i < replayResult.getStepTypes().size(); i++) {
			StepTypes stepType = replayResult.getStepTypes().get(i);
			if (isMove(stepType)) {
				size++;
			}
		}
		*/
		for (int i = 0; i < replayResult.getStepTypes().size(); i++) {
			StepTypes stepType = replayResult.getStepTypes().get(i);
			Object nodeInstance = replayResult.getNodeInstance().get(i);
			if (isMove(stepType)) {
				XEventClass eventClass = null;
				if (nodeInstance instanceof XEventClass) {
					/*
					 * nodeInstance is an XEventClass
					 */
					eventClass = (XEventClass) nodeInstance;
				} else if (nodeInstance instanceof Transition) {
					/*
					 * nodeInstance is a Transition
					 */
					eventClass = tecMap.get(nodeInstance);
				} else {
					System.err.println("Unknown node instance: " + nodeInstance);
				}
				traceAlignment.addLegalMove(stepType, eventClass);
			}
		}
		return traceAlignment;
	}

	//	private boolean isLogMove(StepTypes stepType) {
	//		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L;
	//	}

	//	private boolean isModelMove(StepTypes stepType) {
	//		return stepType == StepTypes.LMGOOD || stepType == StepTypes.MREAL;
	//	}

	private boolean isMove(StepTypes stepType) {
		return stepType == StepTypes.LMGOOD || stepType == StepTypes.L || stepType == StepTypes.MREAL;
	}
	
	/*
	 * Create clusters from an event log array.
	 */
	private ActivityClusterArray convert(EventLogArray logs, ConvertReplayResultToLogAlignmentParameters parameters) {
		Set<XEventClass> activities = new HashSet<XEventClass>(logs.getSize());
		for (int index = 0; index < logs.getSize(); index++) {
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier(index));
			activities.addAll(info.getEventClasses().getClasses());
		}
		ActivityClusterArray clusters = DivideAndConquerFactory.createActivityClusterArray();
		clusters.init("", activities);
		for (int index = 0; index < logs.getSize(); index++) {
			XLogInfo info = XLogInfoFactory.createLogInfo(logs.getLog(index), parameters.getClassifier(index));
			clusters.addCluster(new HashSet<XEventClass>(info.getEventClasses().getClasses()));
		}
		return clusters;
	}
}
