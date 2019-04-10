package org.processmining.plugins;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.connections.ConvertCausalActivityGraphToActivityClusterArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.ConvertCausalActivityGraphToActivityClusterArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.CausalActivityGraph;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.parameters.ConvertCausalActivityGraphToActivityClusterArrayParameters;

@Plugin(name = "Create Clusters", parameterLabels = { "Causal Activity Graph", "Parameters" }, returnLabels = { "Activity Cluster Array" }, returnTypes = { ActivityClusterArray.class })
public class ConvertCausalActivityGraphToActivityClusterArrayPlugin {

//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Clusters, UI", requiredParameterLabels = { 0 })
	public ActivityClusterArray convertUI(UIPluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayParameters parameters = new ConvertCausalActivityGraphToActivityClusterArrayParameters();
		ConvertCausalActivityGraphToActivityClusterArrayDialog dialog = new ConvertCausalActivityGraphToActivityClusterArrayDialog(
				context, parameters);
		InteractionResult result = context.showWizard("Configure creation", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return convertPrivateConnection(context, graph, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Create Clusters, Default", requiredParameterLabels = { 0 })
	public ActivityClusterArray convertDefault(PluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayParameters parameters = new ConvertCausalActivityGraphToActivityClusterArrayParameters();
		return convertPrivateConnection(context, graph, parameters);
	}

	@PluginVariant(variantLabel = "Create Clusters, Parameters", requiredParameterLabels = { 0, 1 })
	public ActivityClusterArray convertParameters(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		return convertPrivateConnection(context, graph, parameters);
	}

	private ActivityClusterArray convertPrivateConnection(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		Collection<ConvertCausalActivityGraphToActivityClusterArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					ConvertCausalActivityGraphToActivityClusterArrayConnection.class, context, graph);
			for (ConvertCausalActivityGraphToActivityClusterArrayConnection connection : connections) {
				if (connection.getObjectWithRole(ConvertCausalActivityGraphToActivityClusterArrayConnection.GRAPH)
						.equals(graph) && connection.getParameters().equals(parameters)) {
					return connection
							.getObjectWithRole(ConvertCausalActivityGraphToActivityClusterArrayConnection.CLUSTERS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		ActivityClusterArray clusters = convertPrivate(context, graph, parameters);
		context.getConnectionManager().addConnection(
				new ConvertCausalActivityGraphToActivityClusterArrayConnection(graph, clusters, parameters));
		return clusters;
	}

	public ActivityClusterArray convert(PluginContext context, CausalActivityGraph graph) {
		ConvertCausalActivityGraphToActivityClusterArrayParameters parameters = new ConvertCausalActivityGraphToActivityClusterArrayParameters();
		return convertPrivate(context, graph, parameters);
	}
	private ActivityClusterArray convertPrivate(PluginContext context, CausalActivityGraph graph,
			ConvertCausalActivityGraphToActivityClusterArrayParameters parameters) {
		ActivityClusterArray clusters = DivideAndConquerFactory.createActivityClusterArray();
		List<XEventClass> activities = graph.getActivities();
		clusters.init(graph.getLabel(), new HashSet<XEventClass>(activities));
		Set<Pair<XEventClass, XEventClass>> causalities = new HashSet<Pair<XEventClass, XEventClass>>(
				graph.getSetCausalities());
		while (!causalities.isEmpty()) {
			Pair<XEventClass, XEventClass> selectedCausality = causalities.iterator().next();
			causalities.remove(selectedCausality);
			Set<XEventClass> sources = new HashSet<XEventClass>();
			Set<XEventClass> targets = new HashSet<XEventClass>();
			sources.add(selectedCausality.getFirst());
			targets.add(selectedCausality.getSecond());
			boolean notReady = true;
			while (notReady) {
				notReady = false;
				Set<Pair<XEventClass, XEventClass>> newCausalities = new HashSet<Pair<XEventClass, XEventClass>>();
				for (Pair<XEventClass, XEventClass> causality : causalities) {
					if (sources.contains(causality.getFirst()) || targets.contains(causality.getSecond())) {
						notReady = sources.add(causality.getFirst()) || notReady;
						notReady = targets.add(causality.getSecond()) || notReady;
					} else {
						newCausalities.add(causality);
					}
				}
				causalities = newCausalities;
			}
			Set<XEventClass> cluster = new HashSet<XEventClass>();
			cluster.addAll(sources);
			cluster.addAll(targets);
//			System.out.println(cluster);
			clusters.addCluster(cluster);
		}
		return clusters;
	}
}
