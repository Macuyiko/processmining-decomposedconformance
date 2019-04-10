package org.processmining.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.connections.DecomposeEventLogUsingActivityClusterArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.DecomposeEventLogUsingActivityClusterArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.EventLogArray;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;

@Plugin(name = "Split Event Log", parameterLabels = { "Event Log", "Activity Cluster Array", "Parameters" }, returnLabels = { "Event Log Array" }, returnTypes = { EventLogArray.class })
public class DecomposeEventLogUsingActivityClusterArrayPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Split Event Log, UI", requiredParameterLabels = { 0, 1 })
	public EventLogArray decomposeUI(UIPluginContext context, XLog eventLog, ActivityClusterArray clusters) {
		DecomposeEventLogUsingActivityClusterArrayParameters parameters = new DecomposeEventLogUsingActivityClusterArrayParameters(
				eventLog);
		DecomposeEventLogUsingActivityClusterArrayDialog dialog = new DecomposeEventLogUsingActivityClusterArrayDialog(
				eventLog, parameters);
		InteractionResult result = context.showWizard("Configure split (classifier, empty traces)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return decomposePrivateConnection(context, eventLog, clusters, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Split Event Log, Default", requiredParameterLabels = { 0, 1 })
	public EventLogArray decomposeDefault(PluginContext context, XLog eventLog, ActivityClusterArray clusters) {
		DecomposeEventLogUsingActivityClusterArrayParameters parameters = new DecomposeEventLogUsingActivityClusterArrayParameters(
				eventLog);
		return decomposePrivateConnection(context, eventLog, clusters, parameters);
	}

	@PluginVariant(variantLabel = "Split Event Log, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public EventLogArray decomposeParameters(
			PluginContext context,
			XLog eventLog,
			ActivityClusterArray clusters,
			DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		return decomposePrivateConnection(context, eventLog, clusters, parameters);
	}

	private EventLogArray decomposePrivateConnection(PluginContext context, XLog eventLog,
			ActivityClusterArray clusters, DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		Collection<DecomposeEventLogUsingActivityClusterArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					DecomposeEventLogUsingActivityClusterArrayConnection.class, context, eventLog);
			for (DecomposeEventLogUsingActivityClusterArrayConnection connection : connections) {
				if (connection.getObjectWithRole(DecomposeEventLogUsingActivityClusterArrayConnection.LOG).equals(
						eventLog)
						&& connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(DecomposeEventLogUsingActivityClusterArrayConnection.LOGS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		EventLogArray eventLogs = decomposePrivate(context, eventLog, clusters, parameters);
		
		context.getConnectionManager().addConnection(
				new DecomposeEventLogUsingActivityClusterArrayConnection(eventLog, clusters, eventLogs, parameters));
		return eventLogs;
	}
	
	private EventLogArray decomposePrivate(PluginContext context, XLog eventLog, ActivityClusterArray clusters,
			DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		
		//Create the subLogs
		EventLogArray eventLogs = DivideAndConquerFactory.createEventLogArray();
		eventLogs.init();
		
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		//Compute the labels of each cluster and create an empty log for each cluster
		Map<Integer, Set<String>> labelsMap = new HashMap<Integer, Set<String>>(); 
		for (Set<XEventClass> activities : clusters.getClusters()) {
			Set<String> labels = new HashSet<String>();
			for (XEventClass activity : activities) {
				labels.add(activity.getId());
			}
			XLog subLog = factory.createLog((XAttributeMap)eventLog.getAttributes().clone());
			int idx = eventLogs.addLog(subLog);
			labelsMap.put(idx, labels);
		}
		
		//Walk thought each trace of the original log
		Map<Integer, XTrace> tracesMap = new HashMap<Integer, XTrace>(); 
		Iterator<XTrace> itTrace = eventLog.iterator();	
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			
			//Create a empty trace for each cluster
			for(Integer i: labelsMap.keySet()){
				XTrace subTrace = factory.createTrace((XAttributeMap) trace.getAttributes().clone());
				tracesMap.put(i, subTrace);
			}
			
			//Walk thought each event of the original trace
			Iterator<XEvent> itEvent = trace.iterator();
			while (itEvent.hasNext()) {
				XEvent event = itEvent.next();
				String id = parameters.getClassifier().getClassIdentity(event);
				
				for(Entry<Integer, Set<String>> labelsEntry: labelsMap.entrySet()){
					if(labelsEntry.getValue().contains(id)){
						XEvent subEvent = factory.createEvent((XAttributeMap) event.getAttributes().clone());
						tracesMap.get(labelsEntry.getKey()).add(subEvent);
					}
				}
			}
			
			//At the traces to each sublogs of each cluster
			for(Integer i: tracesMap.keySet()){
				XTrace subTrace = tracesMap.get(i);
				if(!(parameters.getRemoveEmptyTraces() && subTrace.isEmpty())) {
					eventLogs.getLog(i).add(subTrace);
				}
				
			}

		}
		
		return eventLogs;
	}

	@SuppressWarnings("unused")
	private EventLogArray decomposePrivateOriginalEric(PluginContext context, XLog eventLog, ActivityClusterArray clusters,
			DecomposeEventLogUsingActivityClusterArrayParameters parameters) {
		EventLogArray eventLogs = DivideAndConquerFactory.createEventLogArray();
		eventLogs.init();
		for (Set<XEventClass> activities : clusters.getClusters()) {
			Set<String> labels = new HashSet<String>();
			for (XEventClass activity : activities) {
				labels.add(activity.getId());
			}
			System.out.println("Start Processing cluster");
			XLog subLog = filterLog(eventLog, parameters.getClassifier(), labels, parameters.getRemoveEmptyTraces());
			int idx = eventLogs.addLog(subLog);
			System.out.println("Finished Processing cluster "+idx);
		}
		return eventLogs;
	}

	private XLog filterLog(XLog eventLog, XEventClassifier classifier, Collection<String> labels,
			boolean removeEmptyTraces) {
		XLog filteredLog = (XLog) eventLog.clone();
		Iterator<XTrace> itTrace = filteredLog.iterator();
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			Iterator<XEvent> itEvent = trace.iterator();
			while (itEvent.hasNext()) {
				XEvent event = itEvent.next();
				String id = classifier.getClassIdentity(event);
				if (!labels.contains(id)) {
					itEvent.remove();
				}
			}
			if (removeEmptyTraces && trace.isEmpty()) {
				itTrace.remove();
			}
		}
		return filteredLog;
	}
}
