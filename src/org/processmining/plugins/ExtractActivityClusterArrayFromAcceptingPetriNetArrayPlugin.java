package org.processmining.plugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.connections.ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ExtractActivityClusterArrayFromAcceptingPetriNetArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters;

@Plugin(name = "Create Activity Clusters", parameterLabels = { "Accepting Petri Net Array", "Parameters" }, returnLabels = { "Activity Cluster Array" }, returnTypes = { ActivityClusterArray.class })
public class ExtractActivityClusterArrayFromAcceptingPetriNetArrayPlugin {

	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", email = "jmunoz"
			+ (char) 0x40 + "lsi.upc.edu")
	@PluginVariant(variantLabel = "Create Activity Clusters, Default", requiredParameterLabels = { 0 })
	public ActivityClusterArray extractUI(UIPluginContext context, AcceptingPetriNetArray nets) {
		Set<XEventClass> activities = getActivities(nets);
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(
				nets, activities);
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayDialog dialog = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayDialog(
				activities, parameters);
		InteractionResult result = context.showWizard("Configure creation (transition-activity map)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return extractPrivateConnection(context, nets, parameters);
	}

	@UITopiaVariant(affiliation = "Universitat Politecnica de Catalunya", author = "J.Munoz-Gama", email = "jmunoz"
			+ (char) 0x40 + "lsi.upc.edu")
	@PluginVariant(variantLabel = "Create Activity Clusters, Default", requiredParameterLabels = { 0 })
	public ActivityClusterArray extractDefault(PluginContext context, AcceptingPetriNetArray nets) {
		ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters = new ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters(
				nets, getActivities(nets));
		return extractPrivateConnection(context, nets, parameters);
	}

	@PluginVariant(variantLabel = "Create Activity Clusters, Parameters", requiredParameterLabels = { 0, 1 })
	public ActivityClusterArray extractParameters(PluginContext context, AcceptingPetriNetArray nets,
			ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters) {
		return extractPrivateConnection(context, nets, parameters);
	}

	private ActivityClusterArray extractPrivateConnection(PluginContext context, AcceptingPetriNetArray nets,
			ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters) {
		Collection<ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection.class, context, nets);
			for (ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection connection : connections) {
				if (connection.getObjectWithRole(ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection.NETS)
						.equals(nets) && connection.getParameters().equals(parameters)) {
					return connection
							.getObjectWithRole(ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection.CLUSTERS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		ActivityClusterArray clusters = extractPrivate(context, nets, parameters);
		context.getConnectionManager().addConnection(
				new ExtractActivityClusterArrayFromAcceptingPetriNetArrayConnection(nets, clusters, parameters));
		return clusters;
	}

	private ActivityClusterArray extractPrivate(PluginContext context, AcceptingPetriNetArray nets,
			ExtractActivityClusterArrayFromAcceptingPetriNetArrayParameters parameters) {
		Set<XEventClass> allEC = new HashSet<XEventClass>();
		List<Set<XEventClass>> clusterList = new LinkedList<Set<XEventClass>>();

		for (int i = 0; i < nets.getSize(); i++) {
			Set<XEventClass> cluster = new HashSet<XEventClass>();
			for (Transition t : nets.getNet(i).getNet().getTransitions()) {
				if (!parameters.getMapping().get(t).equals(parameters.getInvisibleActivity())) {
					cluster.add(parameters.getMapping().get(t));
					allEC.add(parameters.getMapping().get(t));
				}
			}
			clusterList.add(i, cluster);
		}

		ActivityClusterArray clusters = DivideAndConquerFactory.createActivityClusterArray();
		clusters.init("Extracted from Accepting Petri Net Array", allEC);
		for (int i = 0; i < clusterList.size(); i++) {
			clusters.addCluster(i, clusterList.get(i));
		}
		return clusters;
	}

	private Set<XEventClass> getActivities(AcceptingPetriNetArray nets) {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (int i = 0; i < nets.getSize(); i++) {
			AcceptingPetriNet net = nets.getNet(i);
			for (Transition transition : net.getNet().getTransitions()) {
				if (!transition.isInvisible()) {
					XEventClass activity = new XEventClass(transition.getLabel(), activities.size());
					if (!activities.contains(activity)) {
						activities.add(activity);
					}
				}
			}
		}
		return activities;
	}
}
