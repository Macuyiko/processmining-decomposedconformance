package org.processmining.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connections.MergeLogAlignmentArrayIntoLogAlignmentConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.MergeLogAlignmentArrayIntoLogAlignmentDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.LogAlignment;
import org.processmining.models.LogAlignmentArray;
import org.processmining.models.TraceAlignment;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.MergeLogAlignmentArrayIntoLogAlignmentParameters;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

@Plugin(name = "Merge Log Alignments", parameterLabels = { "Event Log", "Log Alignment Array", "Parameters" }, returnLabels = { "Log Alignment" }, returnTypes = { LogAlignment.class })
public class MergeLogAlignmentArrayIntoLogAlignmentPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Merge Log Alignments, UI", requiredParameterLabels = { 0, 1 })
	public LogAlignment convertDialog(UIPluginContext context, XLog log, LogAlignmentArray alignments) {
		MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		MergeLogAlignmentArrayIntoLogAlignmentDialog dialog = new MergeLogAlignmentArrayIntoLogAlignmentDialog(log,
				parameters);
		int n = 0;
		String[] title = { "Configure merge (classifier)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 0, dialog.getPanel(n));
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
		return mergePrivateConnection(context, log, alignments, parameters);
	}

	@PluginVariant(variantLabel = "Merge Log Alignments, Default", requiredParameterLabels = { 0, 1 })
	public LogAlignment convertParameters(PluginContext context, XLog log, LogAlignmentArray alignments) {
		MergeLogAlignmentArrayIntoLogAlignmentParameters parameters = new MergeLogAlignmentArrayIntoLogAlignmentParameters(
				log, alignments);
		return mergePrivateConnection(context, log, alignments, parameters);
	}

	@PluginVariant(variantLabel = "Merge Log Alignments, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public LogAlignment convertParameters(PluginContext context, XLog log, LogAlignmentArray alignments,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		return mergePrivateConnection(context, log, alignments, parameters);
	}

	private LogAlignment mergePrivateConnection(PluginContext context, XLog log, LogAlignmentArray alignments,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		Collection<MergeLogAlignmentArrayIntoLogAlignmentConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					MergeLogAlignmentArrayIntoLogAlignmentConnection.class, context, log, alignments);
			for (MergeLogAlignmentArrayIntoLogAlignmentConnection connection : connections) {
				if (connection.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.LOG).equals(log)
						&& connection.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.ALIGNMENTS)
								.equals(alignments) && connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(MergeLogAlignmentArrayIntoLogAlignmentConnection.ALIGNMENT);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		LogAlignment alignment = mergePrivate(log, alignments, parameters);
		context.getConnectionManager().addConnection(
				new MergeLogAlignmentArrayIntoLogAlignmentConnection(log, alignments, alignment, parameters));
		return alignment;
	}

	private LogAlignment mergePrivate(XLog log, LogAlignmentArray alignments,
			MergeLogAlignmentArrayIntoLogAlignmentParameters parameters) {
		LogAlignment logAlignment = DivideAndConquerFactory.createLogAlignment();
		logAlignment.init();
		XEventClassifier classifier = parameters.getClassifier();
		ActivityClusterArray clusters = convert(alignments);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		for (XTrace trace : log) {
			List<XEventClass> activities = new ArrayList<XEventClass>();
			for (XEvent event : trace) {
				activities.add(info.getEventClasses().getClassOf(event));
			}
//			System.out.println("Merging trace: " + activities);
			List<TraceAlignment> traceAlignments = new ArrayList<TraceAlignment>();
			for (int i = 0; i < clusters.getClusters().size(); i++) {
				logAlignment.addToCluster(clusters.getCluster(i));
//				System.out.println("Projected trace: " + project(activities, clusters.getCluster(i)));
				TraceAlignment traceAlignment = alignments.getAlignment(i).getAlignment(project(activities, clusters.getCluster(i)));
//				if (traceAlignment == null) {
//					System.out.println("Unknown partial trace: ");
//					System.out.println("    Trace: " + activities);
//					System.out.println("  Cluster: " + clusters.getCluster(i));
//					System.out.println("   PTrace: " + project(activities, clusters.getCluster(i)));
//				}
//				System.out.println("Alignment: " + traceAlignment.getLegalMoves());
				traceAlignments.add(traceAlignment);
			}
			logAlignment.putAlignment(activities, mergeTracesPrivate(activities, clusters, traceAlignments));
		}
		return logAlignment;
	}

	public TraceAlignment mergeTracesPrivate(List<XEventClass> trace, ActivityClusterArray clusters,
			List<TraceAlignment> alignments) {
		TraceAlignment alignment = DivideAndConquerFactory.createTraceAlignment();
		alignment.init();
		alignment.setLegalMoves(new ArrayList<Pair<StepTypes, XEventClass>>(alignments.get(0).getLegalMoves()));
		Set<XEventClass> cluster = new HashSet<XEventClass>(clusters.getCluster(0));
//		System.out.println("Start cluster: " + cluster);
		for (int i = 1; i < clusters.getClusters().size(); i++) {
			TraceAlignment otherAlignment = alignments.get(i);
			Set<XEventClass> otherCluster = clusters.getCluster(i);
//			System.out.println("  Add cluster: " + otherCluster);
			alignment.setLegalMoves(mergeLegalMovesPrivate(alignment.getLegalMoves(), otherAlignment.getLegalMoves(),
					trace, cluster, otherCluster));
//			System.out.println("Alignment: " + alignment.getLegalMoves());			
			cluster.addAll(otherCluster);
		}
		return alignment;
	}

	private List<XEventClass> project(List<XEventClass> trace, Set<XEventClass> cluster) {
		List<XEventClass> projectedTrace = new ArrayList<XEventClass>();
		for (XEventClass activity : trace) {
			if (cluster.contains(activity)) {
				projectedTrace.add(activity);
			}
		}
		return projectedTrace;
	}

	private List<Pair<StepTypes, XEventClass>> mergeLegalMovesPrivate(List<Pair<StepTypes, XEventClass>> lm1,
			List<Pair<StepTypes, XEventClass>> lm2, List<XEventClass> trace, Set<XEventClass> alfa1,
			Set<XEventClass> alfa2) {

		int ctr1 = 0;
		int size1 = lm1.size();
		int ctr2 = 0;
		int size2 = lm2.size();
		int traceCtr = 0;
		int traceSize = trace.size();
		List<Pair<StepTypes, XEventClass>> lm = new ArrayList<Pair<StepTypes, XEventClass>>();

		while (ctr1 < size1 || ctr2 < size2 || traceCtr < traceSize) {
			if (ctr1 < size1 && ctr2 < size2 && traceCtr < traceSize
					&& (lm1.get(ctr1).getFirst() == StepTypes.LMGOOD || lm1.get(ctr1).getFirst() == StepTypes.L)
					&& lm1.get(ctr1).getSecond().equals(trace.get(traceCtr))
					&& lm2.get(ctr2).getFirst() == StepTypes.LMGOOD
					&& lm2.get(ctr2).getSecond().equals(trace.get(traceCtr))) {
				lm.add(lm2.get(ctr2));
				ctr1++;
				ctr2++;
				traceCtr++;
			} else if (ctr1 < size1 && ctr2 < size2 && traceCtr < traceSize
					&& (lm1.get(ctr1).getFirst() == StepTypes.LMGOOD || lm1.get(ctr1).getFirst() == StepTypes.L)
					&& lm1.get(ctr1).getSecond().equals(trace.get(traceCtr)) && lm2.get(ctr2).getFirst() == StepTypes.L
					&& lm2.get(ctr2).getSecond().equals(trace.get(traceCtr))) {
				lm.add(lm1.get(ctr1));
				ctr1++;
				ctr2++;
				traceCtr++;
			} else if (ctr1 < size1 && traceCtr < traceSize
					&& (lm1.get(ctr1).getFirst() == StepTypes.LMGOOD || lm1.get(ctr1).getFirst() == StepTypes.L)
					&& lm1.get(ctr1).getSecond().equals(trace.get(traceCtr)) && !alfa2.contains(trace.get(traceCtr))) {
				lm.add(lm1.get(ctr1));
				ctr1++;
				traceCtr++;
			} else if (ctr2 < size2 && traceCtr < traceSize && !alfa1.contains(trace.get(traceCtr))
					&& (lm2.get(ctr2).getFirst() == StepTypes.LMGOOD || lm2.get(ctr2).getFirst() == StepTypes.L)
					&& lm2.get(ctr2).getSecond().equals(trace.get(traceCtr))) {
				lm.add(lm2.get(ctr2));
				ctr2++;
				traceCtr++;
			} else if (ctr1 < size1 && lm1.get(ctr1).getFirst() == StepTypes.MREAL) {
				lm.add(lm1.get(ctr1));
				ctr1++;
			} else if (ctr2 < size2 && lm2.get(ctr2).getFirst() == StepTypes.MREAL) {
				lm.add(lm2.get(ctr2));
				ctr2++;
			} else if (traceCtr < traceSize && !alfa1.contains(trace.get(traceCtr)) && !alfa2.contains(trace.get(traceCtr))) {
				traceCtr++;
			} else {
				/*
				 * No rule applies, merge undefined.
				 */
				return null;
			}
		}
		if (ctr1 != size1 || ctr2 != size2 || traceCtr != traceSize) {
			/*
			 * Something's wrong, bail out.
			 */
			return null;
		}
		return lm;
	}

	private ActivityClusterArray convert(LogAlignmentArray logAlignments) {
		Set<XEventClass> activities = new HashSet<XEventClass>(logAlignments.getSize());
		for (int index = 0; index < logAlignments.getSize(); index++) {
			activities.addAll(logAlignments.getAlignment(index).getCluster());
		}
		ActivityClusterArray clusters = DivideAndConquerFactory.createActivityClusterArray();
		clusters.init("", activities);
		for (int index = 0; index < logAlignments.getSize(); index++) {
			clusters.addCluster(logAlignments.getAlignment(index).getCluster());
		}
		return clusters;
	}

}
