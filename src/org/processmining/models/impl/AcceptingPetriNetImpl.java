package org.processmining.models.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.AcceptingPetriNet;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.opennet.OpenNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.Pnml;
import org.processmining.plugins.pnml.Pnml.PnmlType;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;

public class AcceptingPetriNetImpl implements AcceptingPetriNet {

	private Petrinet net;
	private Marking initialMarking;
	private Set<Marking> finalMarkings;

	protected AcceptingPetriNetImpl() {
		
	}
	
	public void init(Petrinet net) {
		this.net = net;
		initialMarking = new Marking();
		finalMarkings = new HashSet<Marking>();
		for (Place place : net.getPlaces()) {
			if (net.getInEdges(place).isEmpty()) {
				initialMarking.add(place);
			}
			if (net.getOutEdges(place).isEmpty()) {
				Marking finalMarking = new Marking();
				finalMarking.add(place);
				finalMarkings.add(finalMarking);
			}
		}
		if (finalMarkings.isEmpty()) {
			finalMarkings.add(new Marking());
		}
	}

	public void init(PluginContext context, Petrinet net) {
		this.net = net;
		try {
			initialMarking = context.tryToFindOrConstructFirstObject(Marking.class, InitialMarkingConnection.class,
					InitialMarkingConnection.MARKING, net);
		} catch (ConnectionCannotBeObtained e) {
			initialMarking = new Marking();
			for (Place place : net.getPlaces()) {
				if (net.getInEdges(place).isEmpty()) {
					initialMarking.add(place);
				}
			}
		}
		try {
			Collection<Marking> finalMarkingCollection = context.tryToFindOrConstructAllObjects(Marking.class,
					FinalMarkingConnection.class, FinalMarkingConnection.MARKING, net);
			finalMarkings = new HashSet<Marking>(finalMarkingCollection);
		} catch (ConnectionCannotBeObtained e) {
			finalMarkings = new HashSet<Marking>();
			for (Place place : net.getPlaces()) {
				if (net.getOutEdges(place).isEmpty()) {
					Marking finalMarking = new Marking();
					finalMarking.add(place);
					finalMarkings.add(finalMarking);
				}
			}
			if (finalMarkings.isEmpty()) {
				finalMarkings.add(new Marking());
			}
		}
	}

	public void setInitialMarking(Marking initialMarking) {
		this.initialMarking = new Marking(initialMarking);
	}

	public void setFinalMarkings(Set<Marking> finalMarkings) {
		this.finalMarkings = new HashSet<Marking>();
		for (Marking finalMarking : finalMarkings) {
			this.finalMarkings.add(new Marking(finalMarking));
		}
	}

	public Petrinet getNet() {
		return net;
	}

	public Marking getInitialMarking() {
		return initialMarking;
	}

	public Set<Marking> getFinalMarkings() {
		return finalMarkings;
	}

	public void importFromStream(PluginContext context, InputStream input) throws Exception {
		PnmlImportUtils utils = new PnmlImportUtils();
		Pnml pnml = utils.importPnmlFromStream(context, input, "", 0);
		if (pnml == null) {
			return;
		}
		OpenNet openNet = new OpenNet(pnml.getLabel());
		Marking openInitialMarking = new Marking();
		GraphLayoutConnection openLayout = new GraphLayoutConnection(openNet);
		pnml.convertToNet(openNet, openInitialMarking, openLayout);
		Petrinet net = PetrinetFactory.newPetrinet(pnml.getLabel());
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		layout.setLabel(openLayout.getLabel());
		layout.setLayedOut(true);
		Map<Transition, Transition> transitionMap = new HashMap<Transition, Transition>();
		Map<Place, Place> placeMap = new HashMap<Place, Place>();
		for (Transition openTransition : openNet.getTransitions()) {
			Transition transition = net.addTransition(openTransition.getLabel());
			transitionMap.put(openTransition, transition);
			layout.setPosition(transition, openLayout.getPosition(openTransition));
			layout.setSize(transition, openLayout.getSize(openTransition));
		}
		for (Place openPlace : openNet.getPlaces()) {
			Place place = net.addPlace(openPlace.getLabel());
			placeMap.put(openPlace, place);
			layout.setPosition(place, openLayout.getPosition(openPlace));
			layout.setSize(place, openLayout.getSize(openPlace));
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> openEdge : openNet.getEdges()) {
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge = null; 
			if (placeMap.containsKey(openEdge.getSource())) {
				edge = net.addArc(placeMap.get(openEdge.getSource()), transitionMap.get(openEdge.getTarget()));
			} else {
				edge = net.addArc(transitionMap.get(openEdge.getSource()), placeMap.get(openEdge.getTarget()));
			}
			layout.setEdgePoints(edge, openLayout.getEdgePoints(openEdge));
		}
		context.addConnection(layout);
		init(net);
		Marking initialMarking = new Marking();
		for (Place place : openInitialMarking.baseSet()) {
			initialMarking.add(placeMap.get(place), openInitialMarking.occurrences(place));
		}
		context.addConnection(new InitialMarkingConnection(net, initialMarking));
		finalMarkings.clear();
		for (Marking openFinalMarking : openNet.getFinalMarkings()) {
			Marking finalMarking = new Marking();
			for (Place place : openFinalMarking.baseSet()) {
				finalMarking.add(placeMap.get(place), openFinalMarking.occurrences(place));
			}
			finalMarkings.add(finalMarking);
		}
	}

	public void exportToFile(PluginContext context, File file) throws IOException {
		OpenNet openNet = new OpenNet(net.getLabel());
		GraphLayoutConnection layout = new GraphLayoutConnection(net);
		GraphLayoutConnection openLayout = null;
		try {
			layout = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, net);
			openLayout = new GraphLayoutConnection(openNet);
			openLayout.setLabel(layout.getLabel());
			openLayout.setLayedOut(true);
		} catch (ConnectionCannotBeObtained e) {
		}
		Map<Transition, Transition> transitionMap = new HashMap<Transition, Transition>();
		Map<Place, Place> placeMap = new HashMap<Place, Place>();
		for (Transition transition : net.getTransitions()) {
			Transition openTransition = openNet.addTransition(transition.getLabel());
			transitionMap.put(transition, openTransition);
			if (openLayout != null) {
				openLayout.setPosition(openTransition, layout.getPosition(transition));
				openLayout.setSize(openTransition, layout.getSize(transition));
			}		
		}
		for (Place place : net.getPlaces()) {
			Place openPlace = openNet.addPlace(place.getLabel());
			placeMap.put(place, openPlace);
			if (openLayout != null) {
				openLayout.setPosition(openPlace, layout.getPosition(place));
				openLayout.setSize(openPlace, layout.getSize(place));
			}
		}
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : net.getEdges()) {
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> openEdge;
			if (placeMap.containsKey(edge.getSource())) {
				openEdge = openNet.addArc(placeMap.get(edge.getSource()), transitionMap.get(edge.getTarget()));
			} else {
				openEdge = openNet.addArc(transitionMap.get(edge.getSource()), placeMap.get(edge.getTarget()));
			}
			if (openLayout != null) {
				openLayout.setEdgePoints(openEdge, layout.getEdgePoints(edge));
			}
		}
		if (openLayout != null) {
			context.addConnection(openLayout);
		}
		Marking openInitialMarking = new Marking();
		for (Place place : initialMarking.baseSet()) {
			openInitialMarking.add(placeMap.get(place), initialMarking.occurrences(place));
		}
		context.addConnection(new InitialMarkingConnection(openNet, openInitialMarking));
		for (Marking finalMarking : finalMarkings) {
			Marking openFinalMarking = new Marking();
			for (Place place : finalMarking.baseSet()) {
				openFinalMarking.add(placeMap.get(place), finalMarking.occurrences(place));
			}
			openNet.addFinalMarking(openFinalMarking);
		}
		Pnml pnml = new Pnml().convertFromNet(openNet, openInitialMarking, openLayout);
		pnml.setType(PnmlType.LOLA);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);

		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		bw.write(text);
		bw.close();
	}

}
