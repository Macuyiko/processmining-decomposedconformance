package org.processmining.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.connections.DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.dialogs.DecomposeAcceptingPetriNetUsingActivityClusterArrayDialog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.ActivityClusterArray;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters;

@Plugin(name = "Split Accepting Petri Net", parameterLabels = { "Accepting Petri Net", "Activity Cluster Array",
		"Parameters" }, returnLabels = { "Accepting Petri Net Array" }, returnTypes = { AcceptingPetriNetArray.class })
public class DecomposeAcceptingPetriNetUsingActivityClusterArrayPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Split Accepting Petri Net, UI", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNetArray decomposeUI(UIPluginContext context, AcceptingPetriNet acceptingNet,
			ActivityClusterArray clusters) {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			activities.addAll(cluster);
		}
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				acceptingNet, activities, null);
		DecomposeAcceptingPetriNetUsingActivityClusterArrayDialog dialog = new DecomposeAcceptingPetriNetUsingActivityClusterArrayDialog(
				acceptingNet, activities, parameters);
		InteractionResult result = context.showWizard("Configure split (transition-activity map)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return decomposePrivateConnection(context, acceptingNet, clusters, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Split Accepting Petri Net, Default", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNetArray decomposeDefault(PluginContext context, AcceptingPetriNet acceptingNet,
			ActivityClusterArray clusters) {
		Set<XEventClass> activities = new HashSet<XEventClass>();
		for (Set<XEventClass> cluster : clusters.getClusters()) {
			activities.addAll(cluster);
		}
		DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters = new DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters(
				acceptingNet, activities, null);
		return decomposePrivateConnection(context, acceptingNet, clusters, parameters);
	}

	@PluginVariant(variantLabel = "Split Accepting Petri Net, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public AcceptingPetriNetArray decomposeParameters(PluginContext context, AcceptingPetriNet acceptingNet,
			ActivityClusterArray clusters, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {
		return decomposePrivateConnection(context, acceptingNet, clusters, parameters);
	}

	private AcceptingPetriNetArray decomposePrivateConnection(PluginContext context, AcceptingPetriNet acceptingNet,
			ActivityClusterArray clusters, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {
		Collection<DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection.class, context, acceptingNet);
			for (DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection connection : connections) {
				if (connection.getObjectWithRole(DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection.NET)
						.equals(acceptingNet) && connection.getParameters().equals(parameters)) {
					return connection
							.getObjectWithRole(DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection.NETS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNetArray acceptingNets = decomposePrivate(context, acceptingNet, clusters, parameters);
		context.getConnectionManager().addConnection(
				new DecomposeAcceptingPetriNetUsingActivityClusterArrayConnection(acceptingNet, acceptingNets,
						clusters, parameters));
		return acceptingNets;
	}

	private AcceptingPetriNetArray decomposePrivate(PluginContext context, AcceptingPetriNet acceptingNet,
			ActivityClusterArray clusters, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {
		AcceptingPetriNetArray acceptingNets = DivideAndConquerFactory.createAcceptingPetriNetArray();
		acceptingNets.init();
		for (Set<XEventClass> activities : clusters.getClusters()) {
			AcceptingPetriNet acceptingSubNet = decomposePrivate(context, acceptingNet, activities, parameters);
			acceptingNets.addNet(acceptingSubNet);
		}
		return acceptingNets;
	}

	private AcceptingPetriNet decomposePrivate(PluginContext context, AcceptingPetriNet acceptingNet,
			Set<XEventClass> activities, DecomposeAcceptingPetriNetUsingActivityClusterArrayParameters parameters) {
		Petrinet net = acceptingNet.getNet();
		Petrinet subNet = PetrinetFactory.newPetrinet(net.getLabel());
		Set<Transition> forbiddenTransitions = new HashSet<Transition>();
		for (Transition transition : net.getTransitions()) {
			XEventClass activity = parameters.getMapping().get(transition);
			if (!activity.equals(parameters.getInvisibleActivity()) && !activities.contains(activity)) {
				forbiddenTransitions.add(transition);
			}
		}
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> inEdges = new HashSet<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>();
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> outEdges = new HashSet<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>();
		for (Transition transition : net.getTransitions()) {
			XEventClass activity = parameters.getMapping().get(transition);
			if (!activity.equals(parameters.getInvisibleActivity()) && activities.contains(activity)) {
				addPredecessorEdges(net, net.getInEdges(transition), inEdges, forbiddenTransitions);
				addSuccessorEdges(net, net.getOutEdges(transition), outEdges, forbiddenTransitions);
			}
		}
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = new HashSet<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>>();
		edges.addAll(inEdges);
		edges.retainAll(outEdges);
		Set<Transition> transitions = new HashSet<Transition>();
		Set<Place> places = new HashSet<Place>();
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
			if (edge.getSource() instanceof Transition) {
				transitions.add((Transition) edge.getSource());
				places.add((Place) edge.getTarget());
			} else {
				transitions.add((Transition) edge.getTarget());
				places.add((Place) edge.getSource());
			}
		}
		Map<Transition, Transition> transitionMap = new HashMap<Transition, Transition>();
		Map<Place, Place> placeMap = new HashMap<Place, Place>();
		for (Transition transition : transitions) {
			Transition subTransition = subNet.addTransition(transition.getLabel());
			if (parameters.getMapping().get(transition).equals(parameters.getInvisibleActivity())) {
				subTransition.setInvisible(true);
			}
			transitionMap.put(transition, subTransition);
		}
		for (Place place : places) {
			Place subPlace = subNet.addPlace(place.getLabel());
			placeMap.put(place, subPlace);
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
			if (places.contains(edge.getSource())) {
				subNet.addArc(placeMap.get(edge.getSource()), transitionMap.get(edge.getTarget()));
			} else {
				subNet.addArc(transitionMap.get(edge.getSource()), placeMap.get(edge.getTarget()));
			}
		}
		Marking subInitialMarking = new Marking();
		for (Place place : acceptingNet.getInitialMarking().baseSet()) {
			if (places.contains(place)) {
				subInitialMarking.add(placeMap.get(place), acceptingNet.getInitialMarking().occurrences(place));
			}
		}
		Set<Marking> subFinalMarkings = new HashSet<Marking>();
		for (Marking finalMarking : acceptingNet.getFinalMarkings()) {
			Marking subFinalMarking = new Marking();
			for (Place place : finalMarking.baseSet()) {
				if (places.contains(place)) {
					subFinalMarking.add(placeMap.get(place), finalMarking.occurrences(place));
				}
			}
			subFinalMarkings.add(subFinalMarking);

		}
		AcceptingPetriNet subAcceptingPetriNet = DivideAndConquerFactory.createAcceptingPetriNet();
		subAcceptingPetriNet.init(subNet);
		subAcceptingPetriNet.setInitialMarking(subInitialMarking);
		subAcceptingPetriNet.setFinalMarkings(subFinalMarkings);
		return subAcceptingPetriNet;
	}

	private void addPredecessorEdges(Petrinet net,
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> toDo,
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges,
			Set<Transition> forbiddenTransitions) {
		while (!toDo.isEmpty()) {
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = toDo.iterator().next();
			toDo.remove(edge);
			if (edge.getSource() instanceof Place || !forbiddenTransitions.contains(edge.getSource())) {
				if (!edges.contains(edge)) {
					edges.add(edge);
					toDo.addAll(net.getInEdges(edge.getSource()));
				}
			}
		}
	}

	private void addSuccessorEdges(Petrinet net,
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> toDo,
			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges,
			Set<Transition> forbiddenTransitions) {
		while (!toDo.isEmpty()) {
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = toDo.iterator().next();
			toDo.remove(edge);
			if (edge.getTarget() instanceof Place || !forbiddenTransitions.contains(edge.getTarget())) {
				if (!edges.contains(edge)) {
					edges.add(edge);
					toDo.addAll(net.getOutEdges(edge.getTarget()));
				}
			}
		}
	}
}
