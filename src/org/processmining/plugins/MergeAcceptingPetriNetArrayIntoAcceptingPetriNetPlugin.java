package org.processmining.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.connections.MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.AcceptingPetriNetArray;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.impl.DivideAndConquerFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters;

@Plugin(name = "Merge Accepting Petri Nets", parameterLabels = { "Accepting Petri Net Array", "Parameters" }, returnLabels = { "Accepting Petri Net" }, returnTypes = { AcceptingPetriNet.class })
public class MergeAcceptingPetriNetArrayIntoAcceptingPetriNetPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Merge Accepting Petri Nets, Default", requiredParameterLabels = { 0 })
	public AcceptingPetriNet mergeDefault(PluginContext context, AcceptingPetriNetArray nets) {
		MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters = new MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters();
		return mergePrivateConnection(context, nets, parameters);
	}

	@PluginVariant(variantLabel = "Merge Accepting Petri Nets, Parameters", requiredParameterLabels = { 0, 1 })
	public AcceptingPetriNet mergeParameters(PluginContext context, AcceptingPetriNetArray nets,
			MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters) {
		return mergePrivateConnection(context, nets, parameters);
	}

	private AcceptingPetriNet mergePrivateConnection(PluginContext context, AcceptingPetriNetArray nets,
			MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters) {
		Collection<MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection> connections;
		try {
			connections = context.getConnectionManager().getConnections(
					MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection.class, context, nets);
			for (MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection connection : connections) {
				if (connection.getObjectWithRole(MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection.NETS)
						.equals(nets) && connection.getParameters().equals(parameters)) {
					return connection.getObjectWithRole(MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection.NET);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		AcceptingPetriNet markedNet = mergePrivate(context, nets, parameters);
		context.getProvidedObjectManager().createProvidedObject("Initial marking for " + markedNet.getNet().getLabel(),
				markedNet.getInitialMarking(), Marking.class, context);
		context.addConnection(new InitialMarkingConnection(markedNet.getNet(), markedNet.getInitialMarking()));
		context.getConnectionManager().addConnection(
				new MergeAcceptingPetriNetArrayIntoAcceptingPetriNetConnection(nets, markedNet, parameters));
		return markedNet;
	}

	private AcceptingPetriNet mergePrivate(PluginContext context, AcceptingPetriNetArray nets,
			MergeAcceptingPetriNetArrayIntoAcceptingPetriNetParameters parameters) {
		AcceptingPetriNet mergedNet = DivideAndConquerFactory.createAcceptingPetriNet();
		mergedNet.init(PetrinetFactory.newPetrinet(nets.getSize() > 0 ? nets.getNet(0).getNet().getLabel()
				: "Empty net"));
		Marking initialMarking = mergedNet.getInitialMarking();
		mergedNet.getFinalMarkings().add(new Marking());
		Map<Place, Place> placeMap = new HashMap<Place, Place>();
		Map<String, Transition> transitionMap = new HashMap<String, Transition>();
		for (int index = 0; index < nets.getSize(); index++) {
			Petrinet net = nets.getNet(index).getNet();
			for (Transition transition : net.getTransitions()) {
				String label = transition.getLabel();
				if (!transitionMap.containsKey(label)) {
					Transition mergedTransition = mergedNet.getNet().addTransition(label);
					mergedTransition.setInvisible(transition.isInvisible());
					transitionMap.put(label, mergedTransition);
				}
			}
			for (Place place : net.getPlaces()) {
				Place mergedPlace = mergedNet.getNet().addPlace(place.getLabel());
				placeMap.put(place, mergedPlace);
				if (nets.getNet(index).getInitialMarking().contains(place)) {
					initialMarking.add(mergedPlace, nets.getNet(index).getInitialMarking().occurrences(place));
				}
			}
//			if (!nets.getNet(index).getFinalMarkings().isEmpty()) {
				Set<Marking> extendedFinalMarkings = new HashSet<Marking>();
//				System.out.println("New Final markings");
				for (Marking currentFinalMarking : mergedNet.getFinalMarkings()) {
					for (Marking finalMarking : nets.getNet(index).getFinalMarkings()) {
						Marking marking = new Marking(currentFinalMarking);
						for (Place p : finalMarking.baseSet()) {
							marking.add(placeMap.get(p), finalMarking.occurrences(p));
						}
						extendedFinalMarkings.add(marking);
//						System.out.println(marking);
					}
					Marking marking = new Marking(currentFinalMarking);
					Marking finalMarking = nets.getNet(index).getInitialMarking();
					for (Place p : finalMarking.baseSet()) {
						marking.add(placeMap.get(p), finalMarking.occurrences(p));
					}
					extendedFinalMarkings.add(marking);
//					System.out.println(marking);
				}
//				if (extendedFinalMarkings.isEmpty()) {
//					extendedFinalMarkings.add(new Marking());
//				}
				mergedNet.setFinalMarkings(extendedFinalMarkings);
//			}
			for (PetrinetEdge<?, ?> edge : net.getEdges()) {
				if (edge instanceof Arc) {
					Arc arc = (Arc) edge;
					if (arc.getSource() instanceof Place) {
						mergedNet.getNet().addArc(placeMap.get(arc.getSource()),
								transitionMap.get(arc.getTarget().getLabel()));
					} else {
						mergedNet.getNet().addArc(transitionMap.get(arc.getSource().getLabel()),
								placeMap.get(arc.getTarget()));
					}
				}
			}
		}
		if (mergedNet.getFinalMarkings().size() > 1) {
			mergedNet.getFinalMarkings().remove(new Marking());
		}
		return mergedNet;
	}

}
